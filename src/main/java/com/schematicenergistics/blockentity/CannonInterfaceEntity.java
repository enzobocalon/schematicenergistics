package com.schematicenergistics.blockentity;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.me.helpers.MachineSource;
import com.google.common.collect.ImmutableSet;
import com.schematicenergistics.core.Registration;
import java.util.EnumSet;
import com.schematicenergistics.logic.CannonInterfaceLogic;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import com.schematicenergistics.part.CannonInterfacePart;

public class CannonInterfaceEntity extends AENetworkedBlockEntity implements IGridTickable, ICraftingRequester, ICannonInterfaceHost {
    private @Nullable CannonInterfaceLogic cannonLogic = null;
    private final IActionSource actionSource;

    private boolean gunpowderCraftingState = true; // default
    private boolean craftingState = true;
    private boolean gunpowderState = true;

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
        tag.putBoolean("gunpowderState", this.gunpowderState);
        tag.putBoolean("craftingState", this.craftingState);
        tag.putBoolean("gunpowderCraftingState", this.gunpowderCraftingState);
    }

    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        this.gunpowderState = tag.getBoolean("gunpowderState");
        this.craftingState = tag.getBoolean("craftingState");
        this.gunpowderCraftingState = tag.getBoolean("gunpowderCraftingState");
    }

    public void onReady() {
        this.getMainNode().setExposedOnSides(this.getExposedSides());
        super.onReady();
    }

    private EnumSet<Direction> getExposedSides() {
        return EnumSet.allOf(Direction.class);
    }


    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (this.cannonLogic == null && this.getLevel() != null) {
            this.cannonLogic = new CannonInterfaceLogic(
                    this.getLevel(),
                    this.getMainNode(),
                    this.actionSource,
                    this
                    );

            this.cannonLogic.setGunpowderState(this.gunpowderState);
            this.cannonLogic.setCraftingState(this.craftingState);
            this.cannonLogic.setGunpowderCraftingState(this.gunpowderCraftingState);
        }
    }

    public CannonInterfaceLogic getLogic() {
        return this.cannonLogic;
    }

    @Override
    public @Nullable CannonInterfaceEntity getEntity() {
        return this;
    }

    @Override
    public @Nullable CannonInterfacePart getPart() {
        return null;
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.schematicenergistics.cannon_interface.title");
    }

    public void setConfigState(String type, boolean state) {
        switch (type) {
            case "gunpowderState":
                this.gunpowderState = state;
                if (this.cannonLogic != null) {
                    this.cannonLogic.setGunpowderState(state);
                }
                break;
            case "craftingState":
                this.craftingState = state;
                if (this.cannonLogic != null) {
                    this.cannonLogic.setCraftingState(state);
                }
                break;
            case "gunpowderCraftingState":
                this.gunpowderCraftingState = state;
                if (this.cannonLogic != null) {
                    this.cannonLogic.setGunpowderCraftingState(state);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown config type: " + type);
        }
        this.setChanged();
    }

    public boolean getConfigState(String type) {
        return switch (type) {
            case "gunpowderState" -> this.gunpowderState;
            case "craftingState" -> this.craftingState;
            case "gunpowderCraftingState" -> this.gunpowderCraftingState;
            default -> throw new IllegalArgumentException("Unknown config type: " + type);
        };
    }
}
