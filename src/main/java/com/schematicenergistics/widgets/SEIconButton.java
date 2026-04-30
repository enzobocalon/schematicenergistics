package com.schematicenergistics.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public abstract class SEIconButton extends Button implements ITooltip {

    public SEIconButton(OnPress onPress) {
        super(0, 0, 16, 16, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    public void setVisibility(boolean vis) {
        this.visible = vis;
        this.active = vis;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            var icon = this.getIcon();
            var yOffset = isHovered() ? 1 : 0;

            Icon bgIcon = isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                    : isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND;

            bgIcon.getBlitter()
                    .dest(getX() - 1, getY() + yOffset, 18, 20)
                    .zOffset(2)
                    .blit(guiGraphics);

            if (icon != null) {
                Blitter blitter = icon.getBlitter();
                if (!this.active) {
                    blitter.opacity(0.5f);
                }
                blitter.dest(getX(), getY() + 1 + yOffset).zOffset(3).blit(guiGraphics);
            }
        }
    }

    protected abstract SEIcon getIcon();

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(getMessage());
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(
                getX(),
                getY(),
                16,
                16);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }
}
