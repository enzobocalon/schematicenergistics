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
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.MachineSource;
import com.google.common.collect.ImmutableSet;
import core.Registration;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import lib.CraftingHelper;
import lib.CraftingRequest;
import logic.CannonInterfaceLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CannonInterfaceEntity extends AENetworkedBlockEntity {
    private @Nullable CannonInterfaceLogic cannonLogic = null;

    public CannonInterfaceEntity(BlockPos pos, BlockState state) {
        this(Registration.CANNON_INTERFACE_ENTITY.get(), pos, state);
    }

    public boolean request(AEItemKey what, long amount, boolean simulate) {
        return this.getLogic().request(what, amount, simulate);
    }

    public int refill(int amountToRefill) {
        return this.getLogic().refill(amountToRefill);
    }

    public CannonInterfaceEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
    }

    public void onReady() {
        this.getMainNode().setExposedOnSides(this.getExposedSides());
        if (this.cannonLogic == null && this.getLevel() != null) {
            this.cannonLogic = new CannonInterfaceLogic(this.getLevel(), this.getMainNode(), this.getExposedSides());
        }
        super.onReady();
    }

    private EnumSet<Direction> getExposedSides() {
        return EnumSet.allOf(Direction.class);
    }

    public CannonInterfaceLogic getLogic() {
        if (this.cannonLogic == null) {
            throw new IllegalStateException("CannonInterfaceLogic is not initialized yet.");
        }
        return this.cannonLogic;
    }

    public @Nullable IGridNode getGridNode() {
        return super.getGridNode();
    }
}
