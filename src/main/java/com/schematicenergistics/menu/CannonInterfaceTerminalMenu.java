package com.schematicenergistics.menu;

import appeng.menu.AEBaseMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import com.schematicenergistics.network.PacketHandler;
import com.schematicenergistics.network.payloads.TerminalListClientPacket;
import org.jetbrains.annotations.Nullable;
import com.schematicenergistics.part.CannonInterfaceTerminal;

public class CannonInterfaceTerminalMenu extends AEBaseMenu {
    private @Nullable CannonInterfaceTerminal terminal = null;

    public CannonInterfaceTerminalMenu(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);

        if (host instanceof CannonInterfaceTerminal terminal) {
            this.terminal = terminal;
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.terminal == null) return;
        if (getPlayer() instanceof ServerPlayer player) {
            PacketHandler.sendToClient(
                    new TerminalListClientPacket(this.terminal.getCannonInterfaces(), terminal.getBlockEntity().getBlockPos()),
                    player
            );
        }
    }
}
