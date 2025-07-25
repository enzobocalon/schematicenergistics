package menu;

import appeng.menu.AEBaseMenu;
import core.Registration;
import logic.ICannonInterfaceHost;
import net.minecraft.world.entity.player.Inventory;

public class CannonInterfaceMenu extends AEBaseMenu {

    public CannonInterfaceMenu(int id, Inventory playerInventory, ICannonInterfaceHost host) {
        super(Registration.CANNON_INTERFACE_MENU.get(), id, playerInventory, host);

        this.createPlayerInventorySlots(playerInventory);
    }

}
