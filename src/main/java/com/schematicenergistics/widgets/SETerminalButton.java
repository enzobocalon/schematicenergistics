package com.schematicenergistics.widgets;

import appeng.client.gui.style.Blitter;
import com.schematicenergistics.core.Registration;
import com.schematicenergistics.lib.SEUtils;
import com.schematicenergistics.lib.TerminalListData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class SETerminalButton extends AbstractWidget {
    private final Blitter buttonBg;
    private final Blitter buttonBgSelected;
    private final Consumer<Integer> onClickCallback;

    private TerminalListData data = null;
    private int itemIndex = -1;
    private boolean isSelected = false;

    private static final int TEXT_COLOR_NORMAL = 0x403e53;

    public SETerminalButton(int x, int y, int width, int height,
                            Blitter buttonBg, Blitter buttonBgSelected,
                            Consumer<Integer> onClickCallback) {
        super(x, y, width, height, Component.empty());
        this.buttonBg = buttonBg;
        this.buttonBgSelected = buttonBgSelected;
        this.onClickCallback = onClickCallback;
    }


    public void setButtonData(TerminalListData data, int index) {
        this.data = data;
        this.itemIndex = index;

        if (data != null) {
            this.visible = true;
            this.active = true;
            updateTooltip();
        } else {
            this.setMessage(Component.empty());
            this.visible = false;
            this.active = false;
        }
    }


    public void clearData() {
        this.data = null;
        this.itemIndex = -1;
        this.isSelected = false;
        this.setMessage(Component.empty());
        this.visible = false;
        this.active = false;
    }


    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }


    public boolean hasData() {
        return data != null && itemIndex >= 0;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (hasData() && onClickCallback != null) {
            onClickCallback.accept(itemIndex);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!hasData()) {
            return;
        }

        boolean shouldHighlight = isHovered || isSelected;
        Blitter blitter = shouldHighlight ? buttonBgSelected : buttonBg;

        blitter.dest(getX(), getY()).blit(guiGraphics);

        renderItemIcon(guiGraphics);

        int squareEnd = getX() + 2 + 8;
        int iconStart = getX() + getWidth() - 12;
        int titleMaxWidth = iconStart - squareEnd - 2;

        renderStatusIcon(getX() + 3, getY() + 3, parseStateColor(data.state()), guiGraphics);

        // Title
        renderText(
                handleSchematicName(data.schematicName()),
                squareEnd,
                getY() + 2,
                false,
                0.8f,
                null,
                titleMaxWidth,
                guiGraphics
        );

        String pos = SEUtils.capitalizeFirstLetter(data.dimension()) + " @ " + data.cannonPos().toShortString();
        renderText(
                pos,
                getX() + 2,
                getY() + 12,
                false,
                0.8f,
                null,
                0,
                guiGraphics
        );

    }

    private void renderStatusIcon(int x, int y, int color, GuiGraphics guiGraphics) {
        int size = 3;
        guiGraphics.fill(x, y, x + size, y + size, color);
    }

    private void renderText(String text, int x, int y, boolean dropShadow, float scale, Integer color, int maxWidth, GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        var textColor = color != null ? color : TEXT_COLOR_NORMAL;

        int scaledX = (int)(x / scale);
        int scaledY = (int)(y / scale);
        int realMaxWidth = maxWidth > 0 ? maxWidth : getWidth() - 8;
        int scaledMaxWidth = (int)(realMaxWidth / scale);

        renderText(guiGraphics, font, text, scaledX, scaledY, scaledMaxWidth, textColor, dropShadow);

        guiGraphics.pose().popPose();
    }

    private void renderText(GuiGraphics guiGraphics, Font font, String text, int x, int y, int maxWidth, int color, boolean dropShadow) {
        if (text == null || text.isEmpty()) return;

        String displayText = text;
        if (font.width(text) > maxWidth) {
            displayText = font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...";
        }

        guiGraphics.drawString(font, displayText, x, y, color, dropShadow);
    }

    private void renderItemIcon(GuiGraphics guiGraphics) {
        if (data == null) return;

        ItemStack item = data.type() == SEUtils.InterfaceType.PART ?
                Registration.CANNON_INTERFACE_PART_ITEM.get().getDefaultInstance() :
                Registration.CANNON_INTERFACE.get().asItem().getDefaultInstance();

        if (item.isEmpty()) return;

        int iconX = 0;
        int iconY = 0;

        float scale = 0.75f; // 75% size
        if (data.type() == SEUtils.InterfaceType.BLOCK) {
            iconX = getX() + getWidth() - 14;
            iconY = getY() + 1;
        } else {
            iconX = getX() + getWidth() - 14;
            iconY = getY();
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(iconX, iconY, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.renderItem(item, 0, 0);

        guiGraphics.pose().popPose();
    }

    private void updateTooltip() {
        String tooltipText;

        if (data.status() == null || data.status().isEmpty()) {
            tooltipText = "Status: " + Component.translatable("gui.schematicenergistics.cannon_interface.missing_cannon").getString();
        } else if (data.schematicName() == null || data.schematicName().isEmpty()) {
            tooltipText = "Status: " + Component.translatable("gui.schematicenergistics.cannon_interface.schematic_name").getString();
        } else {
            tooltipText = "Status: " + SEUtils.formatCannonStatus(data.status()).getString();
        }

        setTooltip(Tooltip.create(Component.literal(tooltipText)));
    }


    @Override
    public boolean isActive() {
        return hasData();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    private String handleSchematicName(String name) {
        if (name == null || name.isEmpty()) {
            return Component.translatable("gui.schematicenergistics.cannon_interface.schematic_name").getString();
        }
        return name;
    }

    private int parseStateColor(String state) {
        return switch (state) {
            case "PAUSED" -> 0xFFe8c64b; // Yellow
            case "RUNNING" -> 0xFFa1ff9c; // Green
            case "STOPPED" -> 0xFFd73e42; // Red
            default -> 0xFFd73e42; // return red by default
        };
    }
}