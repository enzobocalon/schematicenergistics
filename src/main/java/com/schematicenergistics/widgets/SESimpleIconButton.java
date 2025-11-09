package com.schematicenergistics.widgets;

import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class SESimpleIconButton extends SEIconButton {

    private final SEIcon icon;
    private List<Component> tooltip = Collections.emptyList();

    public SESimpleIconButton(SEIcon icon, Component title, Component hint, OnPress onPress) {
        super(onPress);
        this.icon = icon;
        setTooltip(title, hint);
    }

    public void setTooltip(Component title, Component hint) {
        this.tooltip = List.of(title, hint);
    }

    @Override
    protected SEIcon getIcon() {
        return icon;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return tooltip;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return super.isTooltipAreaVisible() && !tooltip.isEmpty();
    }
}
