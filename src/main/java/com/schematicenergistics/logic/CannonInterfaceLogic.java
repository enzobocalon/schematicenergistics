package com.schematicenergistics.logic;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import com.google.common.collect.ImmutableSet;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.schematicenergistics.lib.CraftingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.schematicenergistics.util.ISchematicAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CannonInterfaceLogic {
    private static final Logger log = LoggerFactory.getLogger(CannonInterfaceLogic.class);
    private static final long PRECRAFT_SUBMIT_TIMEOUT_TICKS = 20 * 60;
    private final Level level;
    private final IManagedGridNode node;

    private final CraftingHelper craftingHelper;
    private final IActionSource actionSource;
    private final CraftingHelper gunpowderCraftingHelper;

    private ICraftingRequester requester;

    private AEItemKey item = AEItemKey.of(ItemStack.EMPTY);
    private String schematicName = "";
    private String statusMsg = "";
    private String state = "";

    private boolean gunpowderCraftingState = true;
    private boolean craftingState = true;
    private boolean gunpowderState = true;

    private boolean isPreCrafting = false;
    private boolean hasPreCrafted = false;
    private final Map<AEItemKey, CraftingHelper> pendingCraftingJobs = new HashMap<>();

    private BlockPos terminalPos = null;

    private SchematicannonBlockEntity cannonEntity;

    public CannonInterfaceLogic(Level level, IManagedGridNode node, IActionSource actionSource,
            ICraftingRequester requester) {
        this.level = level;
        this.node = node;
        this.actionSource = actionSource;
        this.craftingHelper = new CraftingHelper(this);
        this.gunpowderCraftingHelper = new CraftingHelper(this);
        this.requester = requester;
    }

    public Level getLevel() {
        return this.level;
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        ImmutableSet.Builder<ICraftingLink> builder = ImmutableSet.builder();
        if (this.craftingHelper.getLink() != null)
            builder.add(this.craftingHelper.getLink());
        if (this.gunpowderCraftingHelper.getLink() != null)
            builder.add(this.gunpowderCraftingHelper.getLink());
        return builder.build();
    }

    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        if (!(what instanceof AEItemKey))
            return 0;

        boolean isKnownLink = link.equals(this.craftingHelper.getLink()) ||
                link.equals(this.gunpowderCraftingHelper.getLink());

        if (!isKnownLink) {
            for (CraftingHelper helper : pendingCraftingJobs.values()) {
                if (link.equals(helper.getLink())) {
                    isKnownLink = true;
                    break;
                }
            }
        }

        if (!isKnownLink) {
            return 0;
        }

        if (node == null)
            return 0;
        var grid = node.getGrid();
        if (grid == null)
            return 0;
        MEStorage inventory = grid.getStorageService().getInventory();
        if (inventory == null)
            return 0;

        return inventory.insert(what, amount, mode, this.actionSource);
    }

    public void jobStateChange(ICraftingLink link) {
        if (link.equals(this.craftingHelper.getLink())) {
            this.craftingHelper.setLink(null);
            this.craftingHelper.clearReadyPlan();
        } else if (link.equals(this.gunpowderCraftingHelper.getLink())) {
            this.gunpowderCraftingHelper.setLink(null);
            this.gunpowderCraftingHelper.clearReadyPlan();
        } else {
            Iterator<Map.Entry<AEItemKey, CraftingHelper>> it = pendingCraftingJobs.entrySet().iterator();
            while (it.hasNext()) {
                CraftingHelper helper = it.next().getValue();
                if (link.equals(helper.getLink())) {
                    helper.setLink(null);
                    helper.clearReadyPlan();
                    it.remove();
                    break;
                }
            }
        }
    }

    public boolean request(AEItemKey what, long amount, boolean simulate) {
        if (node == null)
            return false;

        var grid = node.getGrid();
        if (grid == null)
            return false;

        var inventory = grid.getStorageService().getInventory();
        var craftingService = grid.getCraftingService();
        if (inventory == null || craftingService == null)
            return false;

        long available = inventory.getAvailableStacks().get(what);
        if (available >= amount) {
            if (!simulate) {
                inventory.extract(what, amount, Actionable.MODULATE, this.actionSource);
            }
            return true;
        }

        if (!this.craftingState)
            return false;

        if (this.craftingHelper.getLink() != null) {
            return false;
        }

        if (!craftingService.isCraftable(what)) {
            return false;
        }
        if (this.craftingHelper.getPendingCraft() == null) {
            this.craftingHelper.startCraft(what, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
        }

        return false;
    }

    public void setTerminalPos(BlockPos pos) {
        this.terminalPos = pos;
    }

    public BlockPos getTerminalPos() {
        return this.terminalPos;
    }

    public int refill(int amount) {
        if (!this.gunpowderState)
            return 0;
        if (node == null)
            return 0;
        var grid = node.getGrid();
        if (grid == null)
            return 0;
        MEStorage inventory = grid.getStorageService().getInventory();
        AEItemKey gunpowderKey = AEItemKey.of(Items.GUNPOWDER);
        long available = inventory.getAvailableStacks().get(gunpowderKey);
        if (available <= 0) {
            if (!this.gunpowderCraftingState)
                return 0;
            var canCraft = grid.getCraftingService().isCraftable(gunpowderKey);
            if (!canCraft) {
                return 0;
            }
            if (this.gunpowderCraftingHelper.getLink() != null
                    || this.gunpowderCraftingHelper.getPendingCraft() != null) {
                return 0; // Already crafting
            }

            this.gunpowderCraftingHelper.startCraft(gunpowderKey, amount, CalculationStrategy.CRAFT_LESS);
            return 0;
            // It will always return 0 when it requests a craft.
            // The exact amount will be extracted in a next tick when the craft is done.
        } else {
            long extracted = inventory.extract(gunpowderKey, amount, Actionable.MODULATE, this.actionSource);
            return (int) extracted;
        }
    }

    public @Nullable IGridNode getActionableNode() {
        return this.node.getNode();
    }

    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 5, false);
    }

    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {

        if (node == null || !node.isActive()) {
            return TickRateModulation.IDLE;
        }

        if (craftingHelper.getPendingCraft() != null) {
            processingPending(craftingHelper);
        }

        if (gunpowderCraftingHelper.getPendingCraft() != null) {
            processingPending(gunpowderCraftingHelper);
        }

        processPreCrafting();

        return TickRateModulation.FASTER;
    }

    private void processPreCrafting() {
        if (!isPreCrafting)
            return;

        Iterator<Map.Entry<AEItemKey, CraftingHelper>> it = pendingCraftingJobs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<AEItemKey, CraftingHelper> entry = it.next();
            CraftingHelper helper = entry.getValue();

            if (helper.getPendingCraft() != null) {
                processingPending(helper);
            } else if (helper.getLink() == null && helper.getReadyPlan() != null) {
                var level = this.getLevel();
                if (level != null) {
                    long since = helper.getReadyPlanSinceTick();
                    long now = level.getGameTime();
                    if (since >= 0 && now - since > PRECRAFT_SUBMIT_TIMEOUT_TICKS) {
                        helper.clearReadyPlan();
                        it.remove();
                        log.warn("Pre-crafting submit timed out for {}", entry.getKey());
                        continue;
                    }
                }
                var result = node.getGrid().getCraftingService().submitJob(
                        helper.getReadyPlan(),
                        this.requester,
                        null,
                        false,
                        this.actionSource);
                if (result.successful()) {
                    helper.setLink(result.link());
                    helper.clearReadyPlan();
                }
            }

            if (helper.getLink() == null && helper.getPendingCraft() == null && helper.getReadyPlan() == null) {
                it.remove();
            }
        }

        if (pendingCraftingJobs.isEmpty()) {
            isPreCrafting = false;
            hasPreCrafted = true;
            sendSchematicannonState("RUNNING");
        }
    }

    private void processingPending(CraftingHelper helper) {
        var pending = helper.getPendingCraft();
        if (pending != null && pending.getFuture().isDone()) {
            try {
                ICraftingPlan plan = pending.getFuture().get();

                if (plan.missingItems().isEmpty()) {
                    var result = node.getGrid().getCraftingService().submitJob(
                            plan,
                            this.requester,
                            null,
                            false,
                            this.actionSource);

                    if (result.successful()) {
                        helper.setLink(result.link());
                        helper.clearReadyPlan();
                    } else {
                        helper.setReadyPlan(plan);
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                helper.clearPendingCraft();
            }
        }
    }

    public void sendSchematicannonState(String state) {
        BlockEntity be = this.getLinkedCannon();
        if (be instanceof SchematicannonBlockEntity cannonEntity) {
            try {
                cannonEntity.state = SchematicannonBlockEntity.State.valueOf(state.toUpperCase());
                cannonEntity.setChanged();
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public void setSchematicName(String name) {
        if (this.schematicName == null) {
            this.schematicName = "";
        }

        if (name == null) {
            name = "";
        }

        if (!this.schematicName.equals(name)) {
            this.schematicName = name;
            this.hasPreCrafted = false;
        }
    }

    public String getSchematicName() {
        return this.schematicName;
    }

    public AEItemKey getItem() {
        return this.item;
    }

    public void setLinkedCannon(SchematicannonBlockEntity entity) {
        this.cannonEntity = entity;
    }

    public SchematicannonBlockEntity getLinkedCannon() {
        return this.cannonEntity;
    }

    public void setItem(AEItemKey item) {
        this.item = item;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getStatusMsg() {
        return this.statusMsg;
    }

    public void setState(String state) {
        this.state = state;
        if ("STOPPED".equals(state)) {
            this.hasPreCrafted = false;
            this.isPreCrafting = false;
            this.pendingCraftingJobs.clear();
        }

        if ("RUNNING".equals(state) && !hasPreCrafted && !isPreCrafting) {
            if (startPreCrafting()) {
                sendSchematicannonState("PAUSED");
                this.state = "PAUSED";
            } else {
                hasPreCrafted = true;
            }
        }
    }

    private boolean startPreCrafting() {
        if (!(this.cannonEntity instanceof ISchematicAccessor accessor)) {
            return false;
        }

        Object rawChecklist = accessor.schematicenergistics$getChecklist();
        if (rawChecklist == null)
            return false;

        List<?> checklist = null;

        if (rawChecklist instanceof List) {
            checklist = (List<?>) rawChecklist;
        } else {
            try {
                if (rawChecklist instanceof Iterable) {
                    java.util.ArrayList<Object> collected = new java.util.ArrayList<>();
                    for (Object obj : (Iterable<?>) rawChecklist) {
                        collected.add(obj);
                    }
                    if (!collected.isEmpty()) {
                        checklist = collected;
                    }
                }

                if (checklist == null) {
                    for (java.lang.reflect.Field f : rawChecklist.getClass().getDeclaredFields()) {
                        f.setAccessible(true);
                        Object val = f.get(rawChecklist);

                        if (val instanceof List) {
                            List<?> list = (List<?>) val;
                            if (!list.isEmpty()) {
                                checklist = list;
                                break;
                            }
                        } else if (f.getName().equals("required") && val instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) val;
                            if (!map.isEmpty()) {
                                checklist = new java.util.ArrayList<>(map.entrySet());
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (checklist == null || checklist.isEmpty())
            return false;

        var grid = this.node.getGrid();
        if (grid == null)
            return false;
        var storage = grid.getStorageService().getInventory();
        var craftingService = grid.getCraftingService();
        if (storage == null || craftingService == null)
            return false;

        Map<AEItemKey, Long> totalRequired = new HashMap<>();

        for (Object obj : checklist) {
            if (obj == null)
                continue;

            Object reqObj = obj;
            int quantityOverride = -1;

            if (obj instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
                reqObj = entry.getKey();
                Object val = entry.getValue();
                if (val instanceof Number) {
                    quantityOverride = ((Number) val).intValue();
                }
            }

            if (reqObj == null)
                continue;

            ItemStack stack = extractItemStack(reqObj);

            if (stack != null && !stack.isEmpty()) {
                AEItemKey key = AEItemKey.of(stack);
                if (key != null) {
                    long amount = quantityOverride > 0 ? quantityOverride : stack.getCount();
                    totalRequired.merge(key, amount, Long::sum);
                }
            }
        }

        boolean startedAny = false;

        for (Map.Entry<AEItemKey, Long> entry : totalRequired.entrySet()) {
            AEItemKey key = entry.getKey();
            long required = entry.getValue();
            long available = storage.getAvailableStacks().get(key);

            if (available < required) {
                if (craftingService.isCraftable(key)) {
                    long toCraft = required - available;
                    CraftingHelper helper = new CraftingHelper(this);
                    helper.startCraft(key, toCraft, CalculationStrategy.REPORT_MISSING_ITEMS);
                    if (helper.getPendingCraft() != null) {
                        pendingCraftingJobs.put(key, helper);
                        startedAny = true;
                    }
                }
            }
        }

        if (startedAny) {
            this.isPreCrafting = true;
        }

        return startedAny;
    }

    private ItemStack extractItemStack(Object reqObj) {
        if (reqObj instanceof ItemStack) {
            return (ItemStack) reqObj;
        }
        if (reqObj instanceof net.minecraft.world.item.Item) {
            return new ItemStack((net.minecraft.world.item.Item) reqObj);
        }
        if (reqObj instanceof net.minecraft.world.level.block.Block) {
            return new ItemStack((net.minecraft.world.level.block.Block) reqObj);
        }

        try {
            for (java.lang.reflect.Method m : reqObj.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getReturnType() == ItemStack.class) {
                    Object val = m.invoke(reqObj);
                    if (val instanceof ItemStack) {
                        return (ItemStack) val;
                    }
                }
            }

            for (java.lang.reflect.Method m : reqObj.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 0 && m.getReturnType() == ItemStack.class) {
                    m.setAccessible(true);
                    Object val = m.invoke(reqObj);
                    if (val instanceof ItemStack) {
                        return (ItemStack) val;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            java.lang.reflect.Field stackField = null;
            try {
                stackField = reqObj.getClass().getDeclaredField("stack");
            } catch (NoSuchFieldException e) {
                Class<?> sc = reqObj.getClass().getSuperclass();
                if (sc != null && sc != Object.class) {
                    try {
                        stackField = sc.getDeclaredField("stack");
                    } catch (NoSuchFieldException ignored) {
                    }
                }
            }

            if (stackField != null) {
                stackField.setAccessible(true);
                Object val = stackField.get(reqObj);
                if (val instanceof ItemStack) {
                    return (ItemStack) val;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            for (java.lang.reflect.Field f : reqObj.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == ItemStack.class) {
                    Object val = f.get(reqObj);
                    if (val instanceof ItemStack) {
                        return (ItemStack) val;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public String getState() {
        return this.state;
    }

    public IActionSource getActionSource() {
        return this.actionSource;
    }

    public CraftingHelper getCraftingHelper() {
        return this.craftingHelper;
    }

    public IManagedGridNode getGridNode() {
        return this.node;
    }

    public void setGunpowderState(boolean state) {
        this.gunpowderState = state;
    }

    public boolean getGunpowderState() {
        return this.gunpowderState;
    }

    public void setCraftingState(boolean state) {
        this.craftingState = state;
    }

    public boolean getCraftingState() {
        return this.craftingState;
    }

    public void setGunpowderCraftingState(boolean state) {
        this.gunpowderCraftingState = state;
    }

    public boolean getGunpowderCraftingState() {
        return this.gunpowderCraftingState;
    }
}
