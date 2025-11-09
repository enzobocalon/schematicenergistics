package com.schematicenergistics.widgets;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class SEToggleButton extends SEIconButton implements ITooltip {

    private final Listener listener;

    private final SEIcon iconOn;
    private final SEIcon iconOff;

    private List<Component> tooltipOn = Collections.emptyList();
    private List<Component> tooltipOff = Collections.emptyList();

    private boolean state;

    public SEToggleButton(SEIcon on, SEIcon off, Component displayName, Component displayHint, Listener listener, boolean state) {
        this(on, off, listener);
        setTooltipOn(List.of(displayName, displayHint));
        setTooltipOff(List.of(displayName, displayHint));
        this.state = state;
    }

    public SEToggleButton(SEIcon on, SEIcon off, Component onName, Component onHint, Component offName, Component offHint, Listener listener, boolean state) {
        this(on, off, listener);
        setTooltipOn(List.of(onName, onHint));
        setTooltipOff(List.of(offName, offHint));
        this.state = state;
    }

    public SEToggleButton(SEIcon on, SEIcon off, Listener listener) {
        super(null);
        this.iconOn = on;
        this.iconOff = off;
        this.listener = listener;
    }

    public void setTooltipOn(List<Component> lines) {
        this.tooltipOn = lines;
    }

    public void setTooltipOff(List<Component> lines) {
        this.tooltipOff = lines;
    }

    @Override
    public void onPress() {
        this.listener.onChange(!state);
    }

    public void setState(boolean isOn) {
        this.state = isOn;
    }

    protected SEIcon getIcon() {
        return this.state ? this.iconOn : this.iconOff;
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return state ? tooltipOn : tooltipOff;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return super.isTooltipAreaVisible() && !getTooltipMessage().isEmpty();
    }

    @FunctionalInterface
    public interface Listener {
        void onChange(boolean state);
    }
}