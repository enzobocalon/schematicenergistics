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
        if (this.getMainNode() == null) return false;
        if (this.getMainNode().getGrid() == null) return false;
        MEStorage inventory = (Objects.requireNonNull(this.getMainNode().getGrid())).getStorageService().getInventory();
        ICraftingService craftingService = this.getMainNode().getGrid().getCraftingService();
        long itemAmount = inventory.getAvailableStacks().get(what);
        if (itemAmount >= amount) {
            if (!simulate) {
                inventory.extract(what, amount, Actionable.MODULATE, this.actionSource);
            }

            return true;
        } else {
            if (craftingService.isCraftable(what)) {
                CraftingRequest pendingCraft = this.craftingHelper.getPendingCraft();
                if (pendingCraft == null) {
                    this.craftingHelper.startCraft(what, amount);
                    return false;
                }

                if (pendingCraft.getFuture().isDone()) {
                    try {
                        ICraftingPlan plan = pendingCraft.getFuture().get();
                        if (plan.missingItems().isEmpty()) {
                            ICraftingSubmitResult result = craftingService.submitJob(plan, this, null, false, this.actionSource);
                            if (result.successful()) {
                                this.craftingHelper.clearPendingCraft();
                                return true;
                            }

                            this.craftingHelper.clearPendingCraft();
                            return false;
                        }

                        this.craftingHelper.clearPendingCraft();
                        return false;
                    } catch (ExecutionException | InterruptedException ignored) {}
                }
            }

            return false;
        }
    }

    public int refill(int currentAmount) {
        if (this.getMainNode() == null) return 0;
        if (this.getMainNode().getGrid() == null) return 0;
        MEStorage inventory = (Objects.requireNonNull(this.getMainNode().getGrid())).getStorageService().getInventory();
        AEItemKey gunpowderKey = AEItemKey.of(Items.GUNPOWDER);
        long available = inventory.getAvailableStacks().get(gunpowderKey);
        if (available <= 0L) {
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
        return new TickingRequest(1, 20, false);
    }

    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return TickRateModulation.IDLE;
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(new HashSet<>());
    }

    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        if (what instanceof AEItemKey) return 1L;
         else return 0L;
    }

    public void jobStateChange(ICraftingLink link) {}

    public IActionSource getActionSource() {
        return this.actionSource;
    }

    public @Nullable IGridNode getGridNode() {
        return super.getGridNode();
    }
}
