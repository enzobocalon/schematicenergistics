package com.schematicenergistics.screen;
import appeng.api.stacks.AEItemKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.schematicenergistics.network.payloads.OpenMaterialsScreenPacket;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.schematicenergistics.lib.CannonInterfaceClientState;
import com.schematicenergistics.lib.SEUtils;
import com.schematicenergistics.menu.CannonInterfaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.schematicenergistics.network.PacketHandler;
import com.schematicenergistics.network.payloads.CannonInterfaceConfigPacket;
import com.schematicenergistics.network.payloads.CannonStatePacket;
import com.schematicenergistics.network.payloads.ReturnToTerminalPacket;
import com.schematicenergistics.widgets.SEIcon;
import com.schematicenergistics.widgets.SESimpleIconButton;
import com.schematicenergistics.widgets.SEToggleButton;

public class CannonInterfaceScreen extends AEBaseScreen<CannonInterfaceMenu> {
    private AEItemKey item;
    private final SEToggleButton toggleGunpowderCrafting;
    private final SEToggleButton toggleCrafting;
    private final SEToggleButton toggleGunpowder;
    private final SEToggleButton toggleBulkCraft;
    private SEToggleButton playPause;

    private boolean craftingState;
    private boolean gunpowderState;
    private boolean gunpowderCraftingState;
    private boolean bulkCraftState;

    private BlockPos terminal = null;
    private SESimpleIconButton backButton = null;
    private SESimpleIconButton materialsButton = null;

    private static final int MAX_TEXT_WIDTH = 164;

    public CannonInterfaceScreen(CannonInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth = 176;
        this.imageHeight = 182;

        if (CannonInterfaceClientState.hasState()) {
            this.gunpowderState = CannonInterfaceClientState.getGunpowderState();
            this.craftingState = CannonInterfaceClientState.getCraftingState();
            this.gunpowderCraftingState = CannonInterfaceClientState.getGunpowderCraftingState();
            this.bulkCraftState = CannonInterfaceClientState.getBulkCraftState();
            CannonInterfaceClientState.reset();
        }

        this.toggleBulkCraft = CannonInterfaceButtons.toolbarToggle(
                SEIcon.BULK_CRAFT_ALLOW, SEIcon.BULK_CRAFT_DENY,
                "disable_bulk_craft", "enable_bulk_craft",
                state -> sendState("bulkCraftState", state), bulkCraftState,
                this::addToLeftToolbar);

        this.toggleCrafting = CannonInterfaceButtons.toolbarToggle(
                SEIcon.CRAFTING_ALLOW, SEIcon.CRAFTING_DENY,
                "disable_autocraft", "enable_autocraft",
                state -> sendState("craftingState", state), craftingState,
                this::addToLeftToolbar);

        this.toggleGunpowder = CannonInterfaceButtons.toolbarToggle(
                SEIcon.GUNPOWDER_ALLOW, SEIcon.GUNPOWDER_DENY,
                "disable_gunpowder", "enable_gunpowder",
                state -> sendState("gunpowderState", state), gunpowderState,
                this::addToLeftToolbar);

        this.toggleGunpowderCrafting = CannonInterfaceButtons.toolbarToggle(
                SEIcon.GUNPOWDER_CRAFTING_ALLOW, SEIcon.GUNPOWDER_CRAFTING_DENY,
                "disable_gunpowder_crafting", "enable_gunpowder_crafting",
                state -> sendState("gunpowderCraftingState", state), gunpowderCraftingState,
                this::addToLeftToolbar);

    }

    @Override
    protected void init() {
        super.init();
        updateSchematicName(null);

        int centerX = this.leftPos + (this.imageWidth / 2) - 8;

        this.playPause = CannonInterfaceButtons.positionedToggle(
                SEIcon.PAUSE, SEIcon.PLAY,
                "pause", "play",
                state -> sendCannonState(state, false), false,
                centerX - 16, this.topPos + 56,
                this::addRenderableWidget);

        CannonInterfaceButtons.iconButton(
                SEIcon.STOP,
                CannonInterfaceButtons.cannonInterfaceText("stop"),
                CannonInterfaceButtons.cannonInterfaceText("stop_hint"),
                btn -> sendCannonState(false, true),
                centerX + 16, this.topPos + 56,
                this::addRenderableWidget);

        this.backButton = CannonInterfaceButtons.iconButton(
                SEIcon.BACK,
                Component.translatable("gui.schematicenergistics.cannon_terminal.return_terminal"),
                Component.empty(),
                (btn) -> {
                    PacketHandler.sendToServer(new ReturnToTerminalPacket(terminal));
                },
                leftPos + imageWidth - 28, this.topPos - 10,
                this::addRenderableWidget);
        backButton.visible = (terminal != null);

        materialsButton = CannonInterfaceButtons.iconButton(
                SEIcon.MATERIALS,
                CannonInterfaceButtons.cannonInterfaceText("materials"),
                Component.empty(),
                btn -> {
                    var pos = menu.getHostPos();
                    if (pos != null) {
                        PacketHandler.sendToServer(new OpenMaterialsScreenPacket(pos));
                    }
                },
                leftPos + 8, this.topPos + 56,
                this::addRenderableWidget);
    }

    public void sendState(String config, boolean state) {
        PacketHandler.sendToServer(
                new CannonInterfaceConfigPacket(
                        state, config
                )
        );
    }

    public void sendCannonState(boolean state, boolean isStop) {
        if (isStop) {
            var stoppedState = SchematicannonBlockEntity.State.STOPPED;
            PacketHandler.sendToServer(
                    new CannonStatePacket(stoppedState.toString())
            );
            return;
        }

        this.playPause.setState(state);
        SchematicannonBlockEntity.State cannonState = state ? SchematicannonBlockEntity.State.RUNNING : SchematicannonBlockEntity.State.PAUSED;

        PacketHandler.sendToServer(
                new CannonStatePacket(cannonState.toString())
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int centerX = this.leftPos + 147;
        int centerY = this.topPos + 22;

        if (this.item != null && !this.item.toStack().isEmpty()) {
            guiGraphics.renderItem(this.item.toStack(), centerX, centerY);
        }

        if (mouseX >= centerX && mouseX < centerX + 16 && mouseY >= centerY && mouseY < centerY + 16) {
            if (this.item == null || this.item.toStack().isEmpty()) {
                guiGraphics.renderTooltip(this.font, Component.translatable("gui.schematicenergistics.cannon_interface.no_item"), mouseX, mouseY);
            } else {
                guiGraphics.renderTooltip(this.font, this.item.getDisplayName(), mouseX, mouseY);
            }
        }
    }

    public void updateStates(boolean gunpowderState, boolean craftingState, boolean gunpowderCraftingState, boolean bulkCraftState) {
        this.gunpowderState = gunpowderState;
        this.craftingState = craftingState;
        this.gunpowderCraftingState = gunpowderCraftingState;
        this.bulkCraftState = bulkCraftState;

        if (this.toggleGunpowder != null) {
            this.toggleGunpowder.setState(gunpowderState);
        }
        if (this.toggleCrafting != null) {
            this.toggleCrafting.setState(craftingState);
        }
        if (this.toggleGunpowderCrafting != null) {
            this.toggleGunpowderCrafting.setState(gunpowderCraftingState);
        }
        if (this.toggleBulkCraft != null) {
            this.toggleBulkCraft.setState(bulkCraftState);
        }
    }

    public void updateSchematicName(String schematicName) {
        Component text = schematicName == null || schematicName.isEmpty() ?
                Component.translatable("gui.schematicenergistics.cannon_interface.schematic_name")
                : Component.literal(schematicName);

        Component limitedText = limitTextWidth(text, MAX_TEXT_WIDTH);
        setTextContent("schematic_text", limitedText);
    }

    private Component limitTextWidth(Component originalText, int maxWidth) {
        String textString = originalText.getString();

        if (this.font.width(textString) <= maxWidth) {
            return originalText;
        }

        String ellipsis = "...";
        int ellipsisWidth = this.font.width(ellipsis);
        int availableWidth = maxWidth - ellipsisWidth;

        String truncatedText = this.font.plainSubstrByWidth(textString, availableWidth);

        return Component.literal(truncatedText + ellipsis);
    }

    public void updateStatusMsg(String statusMsg) {
        Component text = statusMsg == null || statusMsg.isEmpty() ?
                Component.translatable("gui.schematicenergistics.cannon_interface.missing_cannon")
                : SEUtils.formatCannonStatus(statusMsg);

        Component limitedText = limitTextWidth(text, MAX_TEXT_WIDTH);
        setTextContent("status_text", limitedText);
    }

    public void updateCannonState(String state) {
        boolean cState = !"PAUSED".equals(state);
        this.playPause.setState(cState);
    }

    public void updateScreenItem(CompoundTag data, String schematicName, String statusMsg, String state, BlockPos terminalPos) {
        var item = AEItemKey.fromTag(data);
        this.item = item != null ? item : AEItemKey.of(ItemStack.EMPTY);

        this.terminal = terminalPos;

        if (backButton != null) {
            backButton.visible = (terminal != null);
        }

        updateSchematicName(schematicName);
        updateStatusMsg(statusMsg);
        updateCannonState(state);
    }
}
