package com.schematicenergistics.screen;

import com.schematicenergistics.widgets.SEIcon;
import com.schematicenergistics.widgets.SESimpleIconButton;
import com.schematicenergistics.widgets.SEToggleButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

final class CannonInterfaceButtons {
    private static final String CANNON_INTERFACE_PREFIX = "gui.schematicenergistics.cannon_interface.";

    private CannonInterfaceButtons() {}

    static SEToggleButton toolbarToggle(SEIcon onIcon, SEIcon offIcon, String onKey,
            String offKey, SEToggleButton.Listener listener, boolean initialState,
            Consumer<SEToggleButton> register) {
        SEToggleButton button = toggle(onIcon, offIcon, onKey, offKey, listener, initialState);
        register.accept(button);
        return button;
    }

    static SEToggleButton positionedToggle(SEIcon onIcon, SEIcon offIcon, String onKey,
            String offKey, SEToggleButton.Listener listener, boolean initialState,
            int x, int y, Consumer<SEToggleButton> register) {
        SEToggleButton button = toggle(onIcon, offIcon, onKey, offKey, listener, initialState);
        button.setPosition(x, y);
        register.accept(button);
        return button;
    }

    static SESimpleIconButton iconButton(SEIcon icon, Component title, Component hint,
            Button.OnPress onPress, int x, int y, Consumer<SESimpleIconButton> register) {
        SESimpleIconButton button = new SESimpleIconButton(icon, title, hint, onPress);
        button.setPosition(x, y);
        register.accept(button);
        return button;
    }

    static Component cannonInterfaceText(String key) {
        return Component.translatable(CANNON_INTERFACE_PREFIX + key);
    }

    private static SEToggleButton toggle(SEIcon onIcon, SEIcon offIcon, String onKey,
            String offKey, SEToggleButton.Listener listener, boolean initialState) {
        return new SEToggleButton(
                onIcon, offIcon,
                cannonInterfaceText(onKey),
                cannonInterfaceText(onKey + "_hint"),
                cannonInterfaceText(offKey),
                cannonInterfaceText(offKey + "_hint"),
                listener, initialState);
    }
}
