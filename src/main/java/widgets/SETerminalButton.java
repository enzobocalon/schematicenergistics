package widgets;

import appeng.client.gui.style.Blitter;
import core.Registration;
import lib.SEUtils;
import lib.TerminalListData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
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
    }

    public void renderText(String text, int x, int y) {}

    public void renderItemIcon(GuiGraphics guiGraphics) {
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


    @Override
    public boolean isActive() {
        return hasData();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

}