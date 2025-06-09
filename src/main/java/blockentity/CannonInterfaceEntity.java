package blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.MachineSource;
import com.google.common.collect.ImmutableSet;
import core.Registration;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import lib.CraftingHelper;
import lib.CraftingRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CannonInterfaceEntity extends AENetworkedBlockEntity implements IGridTickable, ICraftingRequester {
    private final CraftingHelper craftingHelper;
    public final IActionSource actionSource;

    public CannonInterfaceEntity(BlockPos pos, BlockState state) {
        this(Registration.CANNON_INTERFACE_ENTITY.get(), pos, state);
    }

    public CannonInterfaceEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.craftingHelper = new CraftingHelper(this);
        this.actionSource = new MachineSource(this);
        this.getMainNode().setExposedOnSides(this.getExposedSides()).addService(IGridTickable.class, this).addService(ICraftingRequester.class, this).setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
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
            this.craftingHelper.startCraft(what, amount);
        }

        return false;
    }


    public int refill(int currentAmount) {
        var node = this.getMainNode();
        if (node == null) return 0;
        var grid = node.getGrid();
        if (grid == null) return 0;
        MEStorage inventory = grid.getStorageService().getInventory();
        AEItemKey gunpowderKey = AEItemKey.of(Items.GUNPOWDER);
        long available = inventory.getAvailableStacks().get(gunpowderKey);
        if (available <= 0) {
            return 0;
        } else {
            int amountToFill = 64 - currentAmount;
            int amountToExtract = (int)Math.min(amountToFill, available);
            long extracted = inventory.extract(gunpowderKey, amountToExtract, Actionable.MODULATE, this.actionSource);
            return (int)extracted;
        }
    }

    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
    }

    public void onReady() {
        this.getMainNode().setExposedOnSides(this.getExposedSides());
        super.onReady();
    }

    private EnumSet<Direction> getExposedSides() {
        return EnumSet.allOf(Direction.class);
    }

    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 5, false);
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
