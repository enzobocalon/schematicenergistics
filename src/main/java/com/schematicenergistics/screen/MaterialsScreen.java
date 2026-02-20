package com.schematicenergistics.screen;

import appeng.api.stacks.AEItemKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import com.schematicenergistics.lib.ColorHelper;
import com.schematicenergistics.lib.MaterialListEntry;
import com.schematicenergistics.menu.MaterialsMenu;
import com.schematicenergistics.network.PacketHandler;
import com.schematicenergistics.network.payloads.ReturnToCannonInterfacePacket;
import com.schematicenergistics.widgets.SEIcon;
import com.schematicenergistics.widgets.SESimpleIconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MaterialsScreen extends AEBaseScreen<MaterialsMenu> {

    private static final int START_X = 9;
    private static final int START_Y = 19;
    private static final int ITEM_WIDTH = 152;
    private static final int ITEM_HEIGHT = 22;
    private static final int ITEM_SPACING = 1;
    private static final int VISIBLE_ITEMS = 6;

    private final Blitter rowBg;
    private final Blitter rowHover;
    private Scrollbar scrollbar;

    private final List<MaterialRow> materials = new ArrayList<>();
    private ItemStack hoveredStack = ItemStack.EMPTY;

    public MaterialsScreen(MaterialsMenu menu, Inventory playerInventory, Component title,
                           ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth = 176;
        this.imageHeight = 164;

        this.scrollbar = widgets.addScrollBar("scrollbar", Scrollbar.DEFAULT);
        this.rowBg = style.getImage("materialRow");
        this.rowHover = style.getImage("materialRowHover");
    }

    @Override
    protected void init() {
        super.init();

        SESimpleIconButton backButton = new SESimpleIconButton(
                SEIcon.BACK,
                Component.translatable("gui.schematicenergistics.materials.return_interface"),
                Component.empty(),
                btn -> onReturn()
        );
        backButton.setPosition(leftPos + imageWidth - 28, this.topPos - 10);
        this.addRenderableWidget(backButton);

        updateScrollbar();
    }

    private void updateScrollbar() {
        int maxScroll = Math.max(0, materials.size() - VISIBLE_ITEMS);
        scrollbar.setRange(0, maxScroll, 1);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);

        hoveredStack = ItemStack.EMPTY;
        renderMaterialRows(gfx, mouseX, mouseY);

        if (!hoveredStack.isEmpty()) {
            gfx.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }
    }

    public void onReturn() {
        var pos = menu.getHostPos();
        if (pos != null) {
            PacketHandler.sendToServer(new ReturnToCannonInterfacePacket(pos));
        }
    }

    private void renderMaterialRows(GuiGraphics gfx, int mouseX, int mouseY) {
        int scrollOffset = scrollbar.getCurrentScroll();
        int absListX = this.leftPos + START_X;
        int absListY = this.topPos  + START_Y;

        for (int i = 0; i < VISIBLE_ITEMS; i++) {
            int index = scrollOffset + i;
            int buttonY = absListY + i * (ITEM_HEIGHT + ITEM_SPACING);

            boolean hovered = mouseX >= absListX && mouseX < absListX + ITEM_WIDTH
                    && mouseY >= buttonY && mouseY < buttonY + ITEM_HEIGHT;

            if (hovered) {
                rowHover.dest(absListX, buttonY, ITEM_WIDTH, ITEM_HEIGHT).blit(gfx);
            }

            if (index >= materials.size()) continue;

            MaterialRow entry = materials.get(index);
            if (hovered && !entry.stack().isEmpty()) {
                hoveredStack = entry.stack();
            }

            int centerOffset = (ITEM_HEIGHT - 16) / 2;

            if (!entry.stack().isEmpty()) {
                gfx.renderItem(entry.stack(), absListX + 2, buttonY + centerOffset);
            }

            int nameMaxW = ITEM_WIDTH - 20 - 4 - 50;
            String dispName = truncateByWidth(entry.name(), nameMaxW);
            gfx.drawString(this.font, dispName,
                    absListX + 20, buttonY + centerOffset + 4,
                    0xFF202020, false);

            String qty  = formatQty(entry.available()) + "/" + formatQty(entry.required());
            int    qtyW = this.font.width(qty);
            gfx.drawString(this.font, qty,
                    absListX + ITEM_WIDTH - 2 - qtyW, buttonY + centerOffset + 4,
                    getQtyColor(entry), false);
        }
    }

    public void receiveMaterialsData(int page, int totalPages, List<MaterialListEntry> entries) {
        this.materials.clear();
        for (var entry : entries) {
            var key = AEItemKey.fromTag(entry.item());
            ItemStack stack = key != null ? key.toStack() : ItemStack.EMPTY;
            String name = stack.isEmpty() ? "" : limitNameLength(stack.getHoverName().getString(), 50);
            this.materials.add(new MaterialRow(key, stack, name,
                    entry.available(), entry.required(), entry.gathered(), entry.craftable()));
        }
        updateScrollbar();
    }

    public ItemStack getHoveredStackForJei() {
        return hoveredStack;
    }

    public List<Rect2i> getExclusionAreasForJei() {
        return List.of(new Rect2i(leftPos, topPos, imageWidth, imageHeight));
    }

    private int getQtyColor(MaterialRow row) {
        if (row.available() >= row.required()) return ColorHelper.COMPLETE;
        if (row.available() > 0) return ColorHelper.PARTIAL;
        return row.craftable() ? ColorHelper.CRAFTABLE : ColorHelper.MISSING;
    }

    private String truncateByWidth(String text, int maxWidth) {
        if (text == null || text.isEmpty()) return "";
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis  = "...";
        int    available = Math.max(0, maxWidth - this.font.width(ellipsis));
        return this.font.plainSubstrByWidth(text, available) + ellipsis;
    }

    private String limitNameLength(String text, int maxChars) {
        if (text == null) return "";
        return text.length() <= maxChars ? text : text.substring(0, maxChars);
    }

    private String formatQty(long qty) {
        return qty >= 10000 ? "9999+" : String.valueOf(qty);
    }

    private record MaterialRow(AEItemKey key, ItemStack stack, String name,
                               long available, long required, int gathered, boolean craftable) {}
}