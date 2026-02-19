package com.schematicenergistics.screen;

import appeng.api.stacks.AEItemKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.schematicenergistics.lib.CannonInterfaceClientState;
import com.schematicenergistics.lib.MaterialListEntry;
import com.schematicenergistics.lib.SEUtils;
import com.schematicenergistics.menu.CannonInterfaceMenu;
import com.schematicenergistics.network.payloads.MaterialListSubscribePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import com.schematicenergistics.network.payloads.CannonInterfaceConfigPacket;
import com.schematicenergistics.network.payloads.CannonStatePacket;
import com.schematicenergistics.network.payloads.ReturnToTerminalPacket;
import com.schematicenergistics.widgets.SEIcon;
import com.schematicenergistics.widgets.SESimpleIconButton;
import com.schematicenergistics.widgets.SEToggleButton;

import java.util.ArrayList;
import java.util.List;

public class CannonInterfaceScreen extends AEBaseScreen<CannonInterfaceMenu> {

    // -----------------------------------------------------------------------
    // 主界面字段
    // -----------------------------------------------------------------------
    private final SEToggleButton toggleGunpowderCrafting;
    private final SEToggleButton toggleCrafting;
    private final SEToggleButton toggleGunpowder;
    private final SEToggleButton toggleBulkCraft;
    private SEToggleButton playPause;
    private SESimpleIconButton backButton = null;
    private SESimpleIconButton materialsButton = null;
    private SESimpleIconButton materialsCloseButton = null;

    private boolean craftingState;
    private boolean gunpowderState;
    private boolean gunpowderCraftingState;
    private boolean bulkCraftState;

    private static final int MAX_TEXT_WIDTH = 164;

    private BlockPos terminal = null;

    // -----------------------------------------------------------------------
    // 材料清单字段（内嵌于主UI，纯滚动）
    // -----------------------------------------------------------------------
    private boolean materialsOpen = false;

    private static final int ROW_HEIGHT    = 20;
    private static final int HEADER_H      = 18;  // 标题栏高度

    private int materialsScroll = 0;
    private final List<MaterialRow> materials = new ArrayList<>();

    // 碰撞区域（每帧重算）
    private Rect2i materialsBounds      = new Rect2i(0, 0, 0, 0);

    private ItemStack hoveredStack = ItemStack.EMPTY;

    // -----------------------------------------------------------------------
    // 构造器
    // -----------------------------------------------------------------------
    public CannonInterfaceScreen(CannonInterfaceMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth  = 176;
        this.imageHeight = 182;
        this.terminal    = null;

        if (CannonInterfaceClientState.hasState()) {
            this.gunpowderState         = CannonInterfaceClientState.getGunpowderState();
            this.craftingState          = CannonInterfaceClientState.getCraftingState();
            this.gunpowderCraftingState = CannonInterfaceClientState.getGunpowderCraftingState();
            this.bulkCraftState         = CannonInterfaceClientState.getBulkCraftState();
            CannonInterfaceClientState.reset();
        }

        this.toggleBulkCraft = new SEToggleButton(
                SEIcon.BULK_CRAFT_ALLOW, SEIcon.BULK_CRAFT_DENY,
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_bulk_craft"),
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_bulk_craft_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_bulk_craft"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_bulk_craft_hint"),
                state -> sendState("bulkCraftState", state), bulkCraftState);

        this.toggleCrafting = new SEToggleButton(
                SEIcon.CRAFTING_ALLOW, SEIcon.CRAFTING_DENY,
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_autocraft"),
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_autocraft_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_autocraft"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_autocraft_hint"),
                state -> sendState("craftingState", state), craftingState);

        this.toggleGunpowder = new SEToggleButton(
                SEIcon.GUNPOWDER_ALLOW, SEIcon.GUNPOWDER_DENY,
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder"),
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder_hint"),
                state -> sendState("gunpowderState", state), gunpowderState);

        this.toggleGunpowderCrafting = new SEToggleButton(
                SEIcon.GUNPOWDER_CRAFTING_ALLOW, SEIcon.GUNPOWDER_CRAFTING_DENY,
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder_crafting"),
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder_crafting_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder_crafting"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder_crafting_hint"),
                state -> sendState("gunpowderCraftingState", state), gunpowderCraftingState);

        this.addToLeftToolbar(toggleBulkCraft);
        this.addToLeftToolbar(toggleCrafting);
        this.addToLeftToolbar(toggleGunpowder);
        this.addToLeftToolbar(toggleGunpowderCrafting);
    }

    // -----------------------------------------------------------------------
    // init
    // -----------------------------------------------------------------------
    @Override
    protected void init() {
        super.init();
        updateSchematicName(null);

        int centerX = this.leftPos + (this.imageWidth / 2) - 8;

        this.playPause = new SEToggleButton(
                SEIcon.PAUSE, SEIcon.PLAY,
                Component.translatable("gui.schematicenergistics.cannon_interface.pause"),
                Component.translatable("gui.schematicenergistics.cannon_interface.pause_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.play"),
                Component.translatable("gui.schematicenergistics.cannon_interface.play_hint"),
                state -> sendCannonState(state, false), false);
        this.playPause.setPosition(centerX - 16, this.topPos + 56);
        this.addRenderableWidget(playPause);

        SESimpleIconButton stop = new SESimpleIconButton(
                SEIcon.STOP,
                Component.translatable("gui.schematicenergistics.cannon_interface.stop"),
                Component.translatable("gui.schematicenergistics.cannon_interface.stop_hint"),
                btn -> sendCannonState(false, true));
        stop.setPosition(centerX + 16, this.topPos + 56);
        this.addRenderableWidget(stop);

        backButton = new SESimpleIconButton(
                SEIcon.BACK,
                Component.translatable("gui.schematicenergistics.cannon_terminal.return_terminal"),
                Component.empty(),
                btn -> PacketDistributor.sendToServer(new ReturnToTerminalPacket(terminal)));
        backButton.setPosition(leftPos + imageWidth - 28, this.topPos - 10);
        backButton.visible = (terminal != null);
        this.addRenderableWidget(backButton);

        materialsButton = new SESimpleIconButton(
                SEIcon.MATERIALS,
                Component.translatable("gui.schematicenergistics.cannon_interface.materials"),
                Component.empty(),
                btn -> toggleMaterials());
        materialsButton.setPosition(leftPos + 8, this.topPos + 56);
        materialsButton.visible = !materialsOpen;
        this.addRenderableWidget(materialsButton);

        materialsCloseButton = new SESimpleIconButton(
                SEIcon.BACK,
                Component.translatable("gui.schematicenergistics.cannon_interface.close"),
                Component.empty(),
                btn -> closeMaterials());
        materialsCloseButton.visible = materialsOpen;
        this.addRenderableWidget(materialsCloseButton);
    }

    // -----------------------------------------------------------------------
    // 网络发包
    // -----------------------------------------------------------------------
    public void sendState(String config, boolean state) {
        PacketDistributor.sendToServer(new CannonInterfaceConfigPacket(state, config));
    }

    public void sendCannonState(boolean state, boolean isStop) {
        if (isStop) {
            PacketDistributor.sendToServer(
                    new CannonStatePacket(SchematicannonBlockEntity.State.STOPPED.toString()));
            return;
        }
        this.playPause.setState(state);
        SchematicannonBlockEntity.State cannonState =
                state ? SchematicannonBlockEntity.State.RUNNING
                      : SchematicannonBlockEntity.State.PAUSED;
        PacketDistributor.sendToServer(new CannonStatePacket(cannonState.toString()));
    }

    // -----------------------------------------------------------------------
    // 渲染
    // -----------------------------------------------------------------------
@Override
public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
    if (!materialsOpen) {
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    hoveredStack = ItemStack.EMPTY;

    if (materialsOpen) {
        renderMaterialsPanel(gfx, mouseX, mouseY);
        if (materialsCloseButton != null) {
            materialsCloseButton.render(gfx, mouseX, mouseY, partialTick);
        }
        if (!hoveredStack.isEmpty()) {
            gfx.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }
    }
}

    // -----------------------------------------------------------------------
    // 输入事件
    // -----------------------------------------------------------------------
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (materialsOpen) {
            if (materialsCloseButton != null && materialsCloseButton.isMouseOver(mouseX, mouseY)) {
                closeMaterials();
                return true;
            }
            if (contains(materialsBounds, mouseX, mouseY)) {
                return true;
            }
            // 点击面板外：关闭面板，事件继续传递
            return super.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (materialsOpen && contains(materialsBounds, mouseX, mouseY)) {
            int visibleRows = calculateVisibleRows();
            int maxScroll = Math.max(0, materials.size() - visibleRows);
            if (scrollY > 0) {
                materialsScroll = Math.max(0, materialsScroll - 1);
            } else if (scrollY < 0) {
                materialsScroll = Math.min(maxScroll, materialsScroll + 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (materialsOpen && keyCode == 256) { // ESC
            closeMaterials();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        if (materialsOpen) {
            PacketDistributor.sendToServer(new MaterialListSubscribePacket(false));
        }
        super.removed();
    }

    // -----------------------------------------------------------------------
    // 状态更新
    // -----------------------------------------------------------------------
    public void updateStates(boolean gunpowderState, boolean craftingState,
            boolean gunpowderCraftingState, boolean bulkCraftState) {
        this.gunpowderState         = gunpowderState;
        this.craftingState          = craftingState;
        if (toggleGunpowder         != null) toggleGunpowder.setState(gunpowderState);
        if (toggleCrafting          != null) toggleCrafting.setState(craftingState);
        if (toggleGunpowderCrafting != null) toggleGunpowderCrafting.setState(gunpowderCraftingState);
        if (toggleBulkCraft         != null) toggleBulkCraft.setState(bulkCraftState);
    }

    public void updateSchematicName(String schematicName) {
        Component text = (schematicName == null || schematicName.isEmpty())
                ? Component.translatable("gui.schematicenergistics.cannon_interface.schematic_name")
                : Component.literal(schematicName);
        setTextContent("schematic_text", limitTextWidth(text, MAX_TEXT_WIDTH));
    }

    public void updateStatusMsg(String statusMsg) {
        Component text = (statusMsg == null || statusMsg.isEmpty())
                ? Component.translatable("gui.schematicenergistics.cannon_interface.missing_cannon")
                : SEUtils.formatCannonStatus(statusMsg);
        setTextContent("status_text", limitTextWidth(text, MAX_TEXT_WIDTH));
    }

    public void updateCannonState(String state) {
        if (playPause != null) {
            playPause.setState(!"PAUSED".equals(state));
        }
    }

    public void updateScreenItem(CompoundTag data, String schematicName, String statusMsg,
            String state, BlockPos terminalPos) {
        this.terminal = terminalPos;
        if (backButton != null) backButton.visible = (terminal != null);
        updateSchematicName(schematicName);
        updateStatusMsg(statusMsg);
        updateCannonState(state);
    }

    public void receiveMaterialsData(int page, int totalPages, List<MaterialListEntry> entries) {
        this.materials.clear();
        var registryAccess = menu.getLogic().getLevel().registryAccess();
        for (var entry : entries) {
            var key       = AEItemKey.fromTag(registryAccess, entry.item());
            ItemStack stack = key != null ? key.toStack() : ItemStack.EMPTY;
            String name   = stack.isEmpty() ? "" : limitNameLength(stack.getHoverName().getString(), 50);
            this.materials.add(new MaterialRow(key, stack, name,
                    entry.available(), entry.required(), entry.gathered(), entry.craftable()));
        }
        int visibleRows = calculateVisibleRows();
        int maxScroll    = Math.max(0, this.materials.size() - visibleRows);
        this.materialsScroll = Math.min(this.materialsScroll, maxScroll);
    }

    // -----------------------------------------------------------------------
    // 材料清单面板渲染（内嵌主UI，覆盖内容区）
    // -----------------------------------------------------------------------
    private void renderMaterialsPanel(GuiGraphics gfx, int mouseX, int mouseY) {
        // 面板占满主 GUI 内容区
        int panelX = this.leftPos;
        int panelY = this.topPos;
        int panelW = this.imageWidth;
        int panelH = this.imageHeight;

        materialsBounds = new Rect2i(panelX, panelY, panelW, panelH);

        // 设置关闭按钮位置
        if (materialsCloseButton != null) {
            int closeSize = 16;
            int closeX    = panelX + panelW - 2 - closeSize;
            int closeY    = panelY - 2 - (HEADER_H - closeSize) / 2;
            materialsCloseButton.setPosition(closeX, closeY);
        }

        // ── 背景：与 AE2 GUI 纹理风格一致的深灰+浅灰双层 ──────────────
        gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF373737);
        gfx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0xFF6B7280);
        gfx.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + panelH - 2, 0xFFDDDEE4);

        // ── 标题栏 ────────────────────────────────────────────────────────
        gfx.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + 2 + HEADER_H, 0xFFC8CAD2);
        gfx.fill(panelX + 2, panelY + 2 + HEADER_H, panelX + panelW - 2, panelY + 3 + HEADER_H, 0xFF373737);

        // 标题文字
        String title = Component.translatable(
                "gui.schematicenergistics.cannon_interface.materials_title").getString();
        gfx.drawString(this.font, title, panelX + 6, panelY + 6, 0xFF202020, false);

        // ── 动态计算列表区域 ──────────────────────────────────────────────
        int listPad = 4;
        int listX   = panelX + listPad;
        int listY   = panelY + 3 + HEADER_H + 2;
        int listW   = panelW - listPad * 2 - 5;
        int listH   = panelH - (listY - panelY) - listPad;

        // 动态计算可见行数（平均分配间距）
        int visibleRows = calculateVisibleRows();
        // 计算实际使用的行高，让间距平均
        int actualRowHeight = listH / visibleRows;

        int maxScroll = Math.max(0, materials.size() - visibleRows);
        materialsScroll = Math.min(materialsScroll, maxScroll);

        for (int row = 0; row < visibleRows; row++) {
            int     index  = materialsScroll + row;
            int     rowY   = listY + row * actualRowHeight;
            boolean hovered = mouseX >= listX && mouseX < listX + listW
                           && mouseY >= rowY  && mouseY < rowY  + actualRowHeight;

            // 行背景（交替色）
            int bg = hovered ? 0xFFC4C8D8
                    : (row % 2 == 0 ? 0xFFD0D2DA : 0xFFC8CAD2);
            gfx.fill(listX, rowY, listX + listW, rowY + actualRowHeight - 1, bg);

            if (index >= materials.size()) continue;

            MaterialRow entry = materials.get(index);
            if (hovered && !entry.stack().isEmpty()) {
                hoveredStack = entry.stack();
            }

            // 垂直居中的偏移量
            int centerOffset = (actualRowHeight - 16) / 2;

            // 物品图标（垂直居中）
            if (!entry.stack().isEmpty()) {
                gfx.renderItem(entry.stack(), listX + 1, rowY + centerOffset);
            }

            // 物品名称（垂直居中）
            int    nameMaxW = listW - 20 - 4 - 50;
            String dispName = truncateByWidth(entry.name(), nameMaxW);
            gfx.drawString(this.font, dispName, listX + 20, rowY + centerOffset + 4, 0xFF202020, false);

            // 数量（右对齐，垂直居中）
            String qty  = formatQty(entry.available()) + "/" + formatQty(entry.required());
            int    qtyW = this.font.width(qty);
            gfx.drawString(this.font, qty,
                    listX + listW - 2 - qtyW, rowY + centerOffset + 4,
                    getQtyColor(entry), false);
        }

        // ── 滚动条 ────────────────────────────────────────────────────────
        if (materials.size() > visibleRows) {
            int sbX    = panelX + panelW - 2 - 3;
            int sbTopY = listY;
            int sbH    = listH;
            gfx.fill(sbX, sbTopY, sbX + 3, sbTopY + sbH, 0xFF8A8C96);
            float ratio    = (float) visibleRows / materials.size();
            float posRatio = maxScroll > 0 ? (float) materialsScroll / maxScroll : 0f;
            int   thumbH   = Math.max(8, (int)(sbH * ratio));
            int   thumbY   = sbTopY + (int)((sbH - thumbH) * posRatio);
            gfx.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, 0xFF373737);
        }
    }

    // -----------------------------------------------------------------------
    // 材料清单开关
    // -----------------------------------------------------------------------
    private void toggleMaterials() {
        if (materialsOpen) closeMaterials(); else openMaterials();
    }

    private void openMaterials() {
        materialsOpen   = true;
        materialsScroll = 0;
        if (materialsButton != null) materialsButton.visible = false;
        if (materialsCloseButton != null) materialsCloseButton.visible = true;
        PacketDistributor.sendToServer(new MaterialListSubscribePacket(true));
    }

    private void closeMaterials() {
        materialsOpen = false;
        if (materialsButton != null) materialsButton.visible = true;
        if (materialsCloseButton != null) materialsCloseButton.visible = false;
        PacketDistributor.sendToServer(new MaterialListSubscribePacket(false));
    }

    // -----------------------------------------------------------------------
    // JEI 集成
    // -----------------------------------------------------------------------
    public ItemStack getHoveredStackForJei() {
        return hoveredStack;
    }

    public List<Rect2i> getMaterialsBoundsForJei() {
        return materialsOpen ? List.of(materialsBounds) : List.of();
    }

    // -----------------------------------------------------------------------
    // 辅助方法
    // -----------------------------------------------------------------------
    private int calculateVisibleRows() {
        int listPad = 4;
        int listY   = this.topPos + 3 + HEADER_H + 2;
        int listH   = this.imageHeight - (listY - this.topPos) - listPad;
        return Math.max(1, listH / ROW_HEIGHT);
    }

    private int getQtyColor(MaterialRow row) {
        if (row.available() >= row.required()) return 0xFF4CAF50;
        return row.craftable() ? 0xFFFFC107 : 0xFFF44336;
    }

    private Component limitTextWidth(Component text, int maxWidth) {
        String s = text.getString();
        if (this.font.width(s) <= maxWidth) return text;
        String ellipsis  = "...";
        String truncated = this.font.plainSubstrByWidth(s, maxWidth - this.font.width(ellipsis));
        return Component.literal(truncated + ellipsis);
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

    private boolean contains(Rect2i rect, double x, double y) {
        return x >= rect.getX() && x < rect.getX() + rect.getWidth()
            && y >= rect.getY() && y < rect.getY() + rect.getHeight();
    }

    // -----------------------------------------------------------------------
    // 数据模型
    // -----------------------------------------------------------------------
    private record MaterialRow(AEItemKey key, ItemStack stack, String name,
            long available, long required, int gathered, boolean craftable) {}
}