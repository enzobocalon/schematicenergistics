package com.schematicenergistics.logic;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.schematicenergistics.blockentity.CannonInterfaceEntity;
import com.schematicenergistics.core.Registration;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import com.schematicenergistics.part.CannonInterfacePart;

public interface ICannonInterfaceHost {
    CannonInterfaceLogic getLogic();
    @Nullable CannonInterfaceEntity getEntity();
    @Nullable CannonInterfacePart getPart();

    default void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(Registration.CANNON_INTERFACE_MENU.get(), player, locator);
    }
}
