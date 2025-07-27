package menu;

import appeng.api.stacks.AEItemKey;
import appeng.menu.AEBaseMenu;
import core.Registration;
import logic.CannonInterfaceLogic;
import logic.ICannonInterfaceHost;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import network.payloads.CannonInterfaceConfigClientPacket;
import network.payloads.CannonInterfaceSyncPacket;

public class CannonInterfaceMenu extends AEBaseMenu {
    private AEItemKey clientItem;
    private ICannonInterfaceHost host;

    public CannonInterfaceMenu(int id, Inventory playerInventory, ICannonInterfaceHost host) {
        super(Registration.CANNON_INTERFACE_MENU.get(), id, playerInventory, host);

        this.createPlayerInventorySlots(playerInventory);
        this.host = host;
        sendState();
    }

    public CannonInterfaceLogic getLogic() {
        return host.getLogic();
    }

    public ICannonInterfaceHost getHost() {
        return this.host;
    };

    @Override
    public BlockEntity getBlockEntity() {
        return super.getBlockEntity();
    }

    public void sendState() {
        if (getPlayer() instanceof ServerPlayer player) {
            boolean currentGunpowderState = getGunpowderState();
            boolean currentCraftingState = getCraftingState();
            PacketDistributor.sendToPlayer(player,
                    new CannonInterfaceConfigClientPacket(
                            currentGunpowderState,
                            currentCraftingState
                    )
            );

        }
    }

    public void receiveStates(String type, boolean value) {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null && part == null) {
            entity.setConfigState(type, value);
            entity.setChanged();
            sendState();
        } else if (part != null && entity == null) {
            // save the state in the part (nbt/tag)
        } else {
            throw new IllegalStateException("Both entity and part are null or not null in CannonInterfaceMenu");
        }
    }

    public boolean getGunpowderState() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getConfigState("gunpowderState");
        } else if (part != null) {
//            return part.getConfigState("gunpowderState");
        }
        return false;
    }

    public boolean getCraftingState() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getConfigState("craftingState");
        } else if (part != null) {
//            return part.getConfigState("craftingState");
        }
        return false;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (getPlayer() instanceof ServerPlayer player) {
            if (this.getLogic() == null || this.getLogic().getItem() == null) return;
           this.clientItem = this.getLogic().getItem();

           PacketDistributor.sendToPlayer(player,
                   new CannonInterfaceSyncPacket(clientItem.toTag(getLogic().getLevel().registryAccess()),
                   this.getLogic().getSchematicName()
           ));
        }
    }
}
