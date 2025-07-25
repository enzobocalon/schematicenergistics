package logic;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import core.Registration;
import net.minecraft.world.entity.player.Player;

public interface ICannonInterfaceHost {
    CannonInterfaceLogic getLogic();

    default void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(Registration.CANNON_INTERFACE_MENU.get(), player, locator);
    }
}
