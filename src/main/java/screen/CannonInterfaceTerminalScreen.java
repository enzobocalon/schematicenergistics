package screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import lib.TerminalListData;
import logic.ICannonInterfaceHost;
import menu.CannonInterfaceTerminalMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import network.payloads.OpenCannonInterfacePacket;
import widgets.SETerminalButton;

import java.util.ArrayList;
import java.util.List;

public class CannonInterfaceTerminalScreen extends AEBaseScreen<CannonInterfaceTerminalMenu> {
    private final Blitter buttonBg;
    private final Blitter buttonBgSelected;

    private Scrollbar scrollbar;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    private static final int VISIBLE_ITEMS = 6;
    private static final int ITEM_HEIGHT = 22;
    private static final int ITEM_WIDTH = 104;
    private static final int ITEM_SPACING = 1;
    private static final int START_X = 8;
    private static final int START_Y = 19;

    private final List<SETerminalButton> cannonButtons = new ArrayList<>();
    private List<TerminalListData> data = new ArrayList<>();
    private int selectedIndex = -1;

    private BlockPos terminalPos = BlockPos.ZERO;

    public CannonInterfaceTerminalScreen(CannonInterfaceTerminalMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.imageWidth = 128;
        this.imageHeight = 164;

        this.scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.buttonBg = style.getImage("cannonListButton");
        this.buttonBgSelected = style.getImage("cannonListButtonSelected");
    }

    @Override
    protected void init() {
        super.init();

        cannonButtons.forEach(this::removeWidget);
        cannonButtons.clear();

        createCannonButtons();

        updateScrollbar();
        updateButtonsContent();
    }

    private void createCannonButtons() {
        for (int i = 0; i < VISIBLE_ITEMS; i++) {
            int buttonX = leftPos + START_X;
            int buttonY = topPos + START_Y + i * (ITEM_HEIGHT + ITEM_SPACING);

            SETerminalButton button = new SETerminalButton(
                    buttonX, buttonY, ITEM_WIDTH, ITEM_HEIGHT,
                    buttonBg, buttonBgSelected,
                    this::onCannonButtonClicked
            );

            cannonButtons.add(button);
            addRenderableWidget(button);
        }
    }

    private void updateScrollbar() {
        if (data == null || data.isEmpty()) {
            scrollbar.setRange(0, 0, 1);
            return;
        }

        int maxScrollValue = Math.max(0, data.size() - VISIBLE_ITEMS);
        scrollbar.setRange(0, maxScrollValue, VISIBLE_ITEMS / 3);
    }

    private void updateButtonsContent() {
        if (data == null) {
            for (SETerminalButton button : cannonButtons) {
                button.clearData();
                button.setSelected(false);
            }
            return;
        }

        int maxScroll = Math.max(0, data.size() - VISIBLE_ITEMS);
        int scrollOffset = Math.min(scrollbar.getCurrentScroll(), maxScroll);

        for (int i = 0; i < VISIBLE_ITEMS; i++) {
            SETerminalButton button = cannonButtons.get(i);
            int itemIndex = i + scrollOffset;

            if (itemIndex < data.size()) {
                TerminalListData terminalData = data.get(itemIndex);
                button.setButtonData(terminalData, itemIndex);
                button.setSelected(itemIndex == selectedIndex);
            } else {
                button.clearData();
            }
        }
    }

    private void onCannonButtonClicked(int itemIndex) {
        if (data == null || itemIndex < 0 || itemIndex >= data.size()) {
            return;
        }

        selectedIndex = itemIndex;
        TerminalListData selectedCannon = data.get(itemIndex);

        updateButtonsContent();

        PacketDistributor.sendToServer(
                new OpenCannonInterfacePacket(selectedCannon.cannonPos(), terminalPos)
        );
    }

    @Override
    protected Component getGuiDisplayName(Component in) {
        return super.getGuiDisplayName(in);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        updateButtonsContent();
    }

    public void receiveData(List<TerminalListData> newData, BlockPos terminalPos) {
        this.data = newData != null ? new ArrayList<>(newData) : new ArrayList<>();

        if (selectedIndex >= this.data.size()) {
            selectedIndex = -1;
        }

        this.terminalPos = terminalPos;
        updateScrollbar();
        updateButtonsContent();
    }
}