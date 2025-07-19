package logic;


import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import com.google.common.collect.ImmutableSet;
import lib.CraftingHelper;
import lib.CraftingRequest;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.concurrent.ExecutionException;

public class CannonInterfaceLogic implements IGridTickable, ICraftingRequester {
    private final Level level;
    private final IManagedGridNode node;
    private final CraftingHelper craftingHelper;
    private final IActionSource actionSource;
    private final CraftingHelper gunpowderCraftingHelper;

    public CannonInterfaceLogic(Level level, IManagedGridNode node, @Nullable EnumSet<Direction> exposedSides) {
        this.level = level;
        this.node = node;
        this.actionSource = new MachineSource(this);
        this.craftingHelper = new CraftingHelper(this);
        this.gunpowderCraftingHelper = new CraftingHelper(this);
        if (exposedSides != null) {
            node.setExposedOnSides(exposedSides).addService(IGridTickable.class, this)
                    .addService(ICraftingRequester.class, this)
                    .setFlags(GridFlags.REQUIRE_CHANNEL);
        }
    }

    public Level getLevel() {
        return this.level;
    }
    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        ImmutableSet.Builder<ICraftingLink> builder = ImmutableSet.builder();
        if (this.craftingHelper.getLink() != null)
            builder.add(this.craftingHelper.getLink());
        if (this.gunpowderCraftingHelper.getLink() != null)
            builder.add(this.gunpowderCraftingHelper.getLink());
        return builder.build();
    }


    @Override
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


    @Override
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
        if (node == null) return 0;
        var grid = node.getGrid();
        if (grid == null) return 0;
        MEStorage inventory = grid.getStorageService().getInventory();
        AEItemKey gunpowderKey = AEItemKey.of(Items.GUNPOWDER);
        long available = inventory.getAvailableStacks().get(gunpowderKey);
        if (available <= 0) {
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
            // The exact amount will be extracted in the next tick when the craft is done.
        } else {
            long extracted = inventory.extract(gunpowderKey, amount, Actionable.MODULATE, this.actionSource);
            return (int)extracted;
        }
    }

    @Override
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
                            this,
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

    public IActionSource getActionSource() {
        return this.actionSource;
    }

    public CraftingHelper getCraftingHelper() {
        return this.craftingHelper;
    }

    public IManagedGridNode getGridNode() {
        return this.node;
    }
}
