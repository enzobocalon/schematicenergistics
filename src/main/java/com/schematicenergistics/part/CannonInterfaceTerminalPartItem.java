package com.schematicenergistics.part;

import appeng.api.parts.IPartItem;
import appeng.items.parts.PartItem;

import java.util.function.Function;

public class CannonInterfaceTerminalPartItem extends PartItem<CannonInterfaceTerminal> {
    public CannonInterfaceTerminalPartItem(Properties properties, Class<CannonInterfaceTerminal> partClass, Function<IPartItem<CannonInterfaceTerminal>, CannonInterfaceTerminal> factory) {
        super(properties, partClass, factory);
    }
}
