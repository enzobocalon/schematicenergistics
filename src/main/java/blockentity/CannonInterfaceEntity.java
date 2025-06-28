package blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import com.google.common.collect.ImmutableSet;
import core.Registration;
import lib.CraftingHelper;
import lib.CraftingRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CannonInterfaceEntity extends AENetworkBlockEntity implements IGridTickable, ICraftingRequester {
    private final CraftingHelper craftingHelper;
    private final IActionSource actionSource;

    public CannonInterfaceEntity(BlockPos pos, BlockState state) {
        super(Registration.CANNON_INTERFACE_ENTITY.get(), pos, state);
        this.craftingHelper = new CraftingHelper(this);
        this.actionSource = new MachineSource(this);
        this.getMainNode()
                .setExposedOnSides(this.getExposedSides())
                .addService(IGridTickable.class, this)
                .addService(ICraftingRequester.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public boolean request(AEItemKey what, long amount, boolean simulate) {
        var node = this.getMainNode();
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
        var node = this.getMainNode();
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
            if (this.craftingHelper.getLink() != null || this.craftingHelper.getPendingCraft() != null) {
                return 0;
            }

            this.craftingHelper.startCraft(gunpowderKey, amount, CalculationStrategy.CRAFT_LESS);
            return 0;
        } else {
            long extracted = inventory.extract(gunpowderKey, amount, Actionable.MODULATE, this.actionSource);
            return (int)extracted;
        }
    }

    public void onReady() {
        this.getMainNode().setExposedOnSides(this.getExposedSides());
        super.onReady();
    }

    private EnumSet<Direction> getExposedSides() {
        return EnumSet.allOf(Direction.class);
    }

    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.IOPort, false, false);
    }

    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        CraftingRequest pending = this.craftingHelper.getPendingCraft();

        if (node == null || !node.isActive()) {
            return TickRateModulation.IDLE;
        }

        if (pending != null && pending.getFuture().isDone()) {
            try {
                ICraftingPlan plan = pending.getFuture().get();

                if (plan.missingItems().isEmpty()) {
                    var result = node.getGrid().getCraftingService().submitJob(
                            plan,
                            this,
                            null,
                            false,
                            this.actionSource);

                    if (result.successful()) {
                        this.craftingHelper.setLink(result.link());
                    }
                }

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.craftingHelper.clearPendingCraft();
            }
        }

        return TickRateModulation.FASTER;
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        ICraftingLink link = this.craftingHelper.getLink();
        if (link != null) return ImmutableSet.of(link);
        return ImmutableSet.of();
    }

    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        if (!(what instanceof AEItemKey)) return 0;
        if (!link.equals(this.craftingHelper.getLink())) return 0;

        var node = this.getMainNode();
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
        }
    }

    public IActionSource getActionSource() {
        return this.actionSource;
    }

    public @Nullable IGridNode getGridNode() {
        return super.getGridNode();
    }
}
