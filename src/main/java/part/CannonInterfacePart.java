package part;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import blockentity.CannonInterfaceEntity;
import com.google.common.collect.ImmutableSet;
import com.schematicenergistics.SchematicEnergistics;
import logic.CannonInterfaceLogic;
import logic.ICannonInterfaceHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


public class CannonInterfacePart extends AEBasePart implements IGridTickable, ICraftingRequester, ICannonInterfaceHost {
    private @Nullable CannonInterfaceLogic cannonLogic = null;
    private final IActionSource actionSource;

    private boolean gunpowderCraftingState = true; // default
    private boolean craftingState = true;
    private boolean gunpowderState = true;

    public CannonInterfacePart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().addService(IGridTickable.class, this)
                .addService(ICraftingRequester.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
        this.actionSource = new MachineSource(this);
    }

    @PartModels
    private static final IPartModel MODEL_BASE = new PartModel(AppEng.makeId("part/interface_base"));

    @PartModels
    private static final ResourceLocation MODEL_INTERFACE = SchematicEnergistics.makeId("part/cannon_interface");

    public @Nullable CannonInterfaceEntity getEntity() {
        return null;
    }

    @Override
    public @Nullable CannonInterfacePart getPart() {
        return this;
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putBoolean("gunpowderState", this.gunpowderState);
        data.putBoolean("craftingState", this.craftingState);
        data.putBoolean("gunpowderCraftingState", this.gunpowderCraftingState);
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.gunpowderState = data.getBoolean("gunpowderState");
        this.craftingState = data.getBoolean("craftingState");
        this.gunpowderCraftingState = data.getBoolean("gunpowderCraftingState");
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0);
    }

    public CannonInterfaceLogic getLogic() {
        // Lazy initialization
        if (this.cannonLogic == null && this.getHost() != null && this.getHost().getBlockEntity().getLevel() != null) {
            this.cannonLogic = new CannonInterfaceLogic(
                    this.getHost().getBlockEntity().getLevel(),
                    this.getMainNode(),
                    this.actionSource,
                    this
            );

            this.cannonLogic.setCraftingState(this.craftingState);
            this.cannonLogic.setGunpowderState(this.gunpowderState);
            this.cannonLogic.setGunpowderCraftingState(this.gunpowderCraftingState);
        }
        return this.cannonLogic;
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
        this.getHost().markForSave();
    }

    public boolean getConfigState(String type) {
        return switch (type) {
            case "gunpowderState" -> this.gunpowderState;
            case "craftingState" -> this.craftingState;
            case "gunpowderCraftingState" -> this.gunpowderCraftingState;
            default -> throw new IllegalArgumentException("Unknown config type: " + type);
        };
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!player.getCommandSenderWorld().isClientSide()) {
            getLogic().setTerminalPos(null);
            this.openMenu(player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public IPartModel getStaticModels() {
        return new PartModel(MODEL_BASE.requireCableConnection(), MODEL_INTERFACE);
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
}
