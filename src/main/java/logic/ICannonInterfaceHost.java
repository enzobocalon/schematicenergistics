package logic;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import blockentity.CannonInterfaceEntity;
import core.Registration;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import part.CannonInterfacePart;

public interface ICannonInterfaceHost {
    CannonInterfaceLogic getLogic();
    @Nullable CannonInterfaceEntity getEntity();
    @Nullable CannonInterfacePart getPart();

    default void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(Registration.CANNON_INTERFACE_MENU.get(), player, locator);
    }
}
