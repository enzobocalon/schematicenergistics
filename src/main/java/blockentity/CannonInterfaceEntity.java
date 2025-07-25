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
import menu.CannonInterfaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CannonInterfaceEntity extends AENetworkedBlockEntity implements IGridTickable, ICraftingRequester, MenuProvider {
    private @Nullable CannonInterfaceLogic cannonLogic = null;
    private final IActionSource actionSource;

    public CannonInterfaceEntity(BlockPos pos, BlockState state) {
        this(Registration.CANNON_INTERFACE_ENTITY.get(), pos, state);
    }

    public CannonInterfaceEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.actionSource = new MachineSource(this);
        this.getMainNode().setExposedOnSides(this.getExposedSides()).addService(IGridTickable.class, this)
                .addService(ICraftingRequester.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
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
            this.cannonLogic = new CannonInterfaceLogic(
                    this.getLevel(),
                    this.getMainNode(),
                    this.actionSource,
                this);
        }
        super.onReady();
    }

    private EnumSet<Direction> getExposedSides() {
        return EnumSet.allOf(Direction.class);
    }

    public CannonInterfaceLogic getLogic() {
        return this.cannonLogic;
    }

    public @Nullable IGridNode getGridNode() {
        return super.getGridNode();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.getLogic().getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        return this.getLogic().insertCraftedItems(link, what, amount, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.getLogic().jobStateChange(link);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return this.getLogic().getTickingRequest(node);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return this.getLogic().tickingRequest(node, ticksSinceLastCall);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CannonInterfaceMenu(
                Registration.CANNON_INTERFACE_MENU.get(),
                i,
                inventory,
                this
        );
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Cannon Interface");
    }
}
