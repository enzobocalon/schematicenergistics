package logic;

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
import lib.CraftingHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;


public class CannonInterfaceLogic {
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
        if (!(what instanceof AEItemKey)) return 0;
        if (!link.equals(this.craftingHelper.getLink()) &&
                !link.equals(this.gunpowderCraftingHelper.getLink())) {
            return 0;
        }

        if (node == null) return 0;
        var grid = node.getGrid();
        if (grid == null) return 0;
        MEStorage inventory = grid.getStorageService().getInventory();
        if (inventory == null) return 0;

        return inventory.insert(what, amount, mode, this.actionSource);
    }


    public void jobStateChange(ICraftingLink link) {
        if (link.equals(this.craftingHelper.getLink())) {
            this.craftingHelper.setLink(null);
        } else if (link.equals(this.gunpowderCraftingHelper.getLink())) {
            this.gunpowderCraftingHelper.setLink(null);
        }
    }

    public boolean request(AEItemKey what, long amount, boolean simulate) {
        if (node == null) return false;

        var grid = node.getGrid();
        if (grid == null) return false;

        var inventory = grid.getStorageService().getInventory();
        var craftingService = grid.getCraftingService();
        if (inventory == null || craftingService == null) return false;

        long available = inventory.getAvailableStacks().get(what);
        if (available >= amount) {
            if (!simulate) {
                inventory.extract(what, amount, Actionable.MODULATE, this.actionSource);
            }
            return true;
        }

        if (!this.craftingState) return false;

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

    public int refill(int amount) {
        if (!this.gunpowderState) return 0;
        if (node == null) return 0;
        var grid = node.getGrid();
        if (grid == null) return 0;
        MEStorage inventory = grid.getStorageService().getInventory();
        AEItemKey gunpowderKey = AEItemKey.of(Items.GUNPOWDER);
        long available = inventory.getAvailableStacks().get(gunpowderKey);
        if (available <= 0) {
            if (!this.gunpowderCraftingState) return 0;
            var canCraft = grid.getCraftingService().isCraftable(gunpowderKey);
            if (!canCraft) {
                return 0;
            }
            if (this.gunpowderCraftingHelper.getLink() != null || this.gunpowderCraftingHelper.getPendingCraft() != null) {
                return 0; // Already crafting
            }

            this.gunpowderCraftingHelper.startCraft(gunpowderKey, amount, CalculationStrategy.CRAFT_LESS);
            return 0;
            // It will always return 0 when it requests a craft.
            // The exact amount will be extracted in a next tick when the craft is done.
        } else {
            long extracted = inventory.extract(gunpowderKey, amount, Actionable.MODULATE, this.actionSource);
            return (int)extracted;
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

        return TickRateModulation.FASTER;
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
                            this.actionSource
                    );

                    if (result.successful()) {
                        helper.setLink(result.link());
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
            cannonEntity.state = SchematicannonBlockEntity.State.valueOf(state.toUpperCase());
            cannonEntity.setChanged();
        }
    }

    public void setSchematicName(String name) {
        this.schematicName = name;
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
