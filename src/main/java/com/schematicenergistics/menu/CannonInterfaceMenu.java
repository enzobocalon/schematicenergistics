package com.schematicenergistics.menu;

import appeng.api.stacks.AEItemKey;
import appeng.menu.AEBaseMenu;
import com.schematicenergistics.core.Registration;
import com.schematicenergistics.logic.CannonInterfaceLogic;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.schematicenergistics.network.PacketHandler;
import com.schematicenergistics.network.payloads.CannonInterfaceConfigClientPacket;
import com.schematicenergistics.network.payloads.CannonInterfaceSyncPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CannonInterfaceMenu extends AEBaseMenu {
    private static final Logger log = LoggerFactory.getLogger(CannonInterfaceMenu.class);
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

    public BlockPos getHostPos() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getBlockPos();
        } else if (part != null && part.getHost() != null && part.getHost().getBlockEntity() != null) {
            return part.getHost().getBlockEntity().getBlockPos();
        }
        return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return super.getBlockEntity();
    }

    public void sendState() {
        if (getPlayer() instanceof ServerPlayer player) {
            boolean currentGunpowderState = getGunpowderState();
            boolean currentCraftingState = getCraftingState();
            boolean currentGunpowderCraftingState = getGunpowderCraftingState();
            boolean currentBulkCraftState = getBulkCraftState();
            PacketHandler.sendToClient(
                    new CannonInterfaceConfigClientPacket(
                            currentGunpowderState,
                            currentCraftingState,
                            currentGunpowderCraftingState,
                            currentBulkCraftState
                    ),
                    player
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
            part.setConfigState(type, value);
            part.getHost().markForSave();
            sendState();
        } else {
            throw new IllegalStateException("Both entity and part are null or not null in CannonInterfaceMenu");
        }
    }

    public boolean getBulkCraftState() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getConfigState("bulkCraftState");
        } else if (part != null) {
            return part.getConfigState("bulkCraftState");
        }
        return false;
    }

    public boolean getGunpowderState() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getConfigState("gunpowderState");
        } else if (part != null) {
            return part.getConfigState("gunpowderState");
        }
        return false;
    }

    public boolean getCraftingState() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getConfigState("craftingState");
        } else if (part != null) {
            return part.getConfigState("craftingState");
        }
        return false;
    }

    public boolean getGunpowderCraftingState() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getConfigState("gunpowderCraftingState");
        } else if (part != null) {
            return part.getConfigState("gunpowderCraftingState");
        }
        return false;
    }

    public void updateStateFromInterface(String state) {
        var logic = getLogic();
        if (logic != null) {
            logic.sendSchematicannonState(state);

        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (getPlayer() instanceof ServerPlayer player && getLogic() != null) {
            this.clientItem = getLogic().getItem();

            var item = clientItem != null && !clientItem.toStack().isEmpty()
                    ? clientItem.toTag()
                    : new CompoundTag();
            var name = getLogic().getSchematicName() != null
                    ? getLogic().getSchematicName()
                    : "";
            var status = getLogic().getStatusMsg() != null
                    ? getLogic().getStatusMsg()
                    : "";
            var state = getLogic().getState() != null ? getLogic().getState() : "";
            var terminal = getLogic().getTerminalPos();

            if (terminal != null) {
                PacketHandler.sendToClient(
                        new CannonInterfaceSyncPacket(
                                item,
                                name,
                                status,
                                state,
                                terminal
                        ),
                        player
                );
            } else {
                PacketHandler.sendToClient(
                        new CannonInterfaceSyncPacket(
                                item,
                                name,
                                status,
                                state
                        ),
                        player
                );
            }
        }
    }

}
