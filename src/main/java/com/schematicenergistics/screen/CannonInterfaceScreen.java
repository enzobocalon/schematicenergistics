package com.schematicenergistics.screen;

import appeng.api.stacks.AEItemKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
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
import net.neoforged.neoforge.network.PacketDistributor;
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
    private SEToggleButton playPause;
    private SESimpleIconButton backButton = null;

    private boolean craftingState;
    private boolean gunpowderState;
    private boolean gunpowderCraftingState;

    private static final int MAX_TEXT_WIDTH = 164;

    private BlockPos terminal = null;

    public CannonInterfaceScreen(CannonInterfaceMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth = 176;
        this.imageHeight = 182;
        this.terminal = null;

        if (CannonInterfaceClientState.hasState()) {
            this.gunpowderState = CannonInterfaceClientState.getGunpowderState();
            this.craftingState = CannonInterfaceClientState.getCraftingState();
            this.gunpowderCraftingState = CannonInterfaceClientState.getGunpowderCraftingState();
            CannonInterfaceClientState.reset();
        }

        this.toggleCrafting = new SEToggleButton(
                SEIcon.CRAFTING_ALLOW,
                SEIcon.CRAFTING_DENY,
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_autocraft"),
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_autocraft_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_autocraft"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_autocraft_hint"),
                state -> {
                    sendState("craftingState", state);
                },
                craftingState);

        this.toggleGunpowder = new SEToggleButton(
                SEIcon.GUNPOWDER_ALLOW,
                SEIcon.GUNPOWDER_DENY,
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder"),
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder_hint"),
                state -> {
                    sendState("gunpowderState", state);
                },
                gunpowderState);

        this.toggleGunpowderCrafting = new SEToggleButton(
                SEIcon.GUNPOWDER_CRAFTING_ALLOW,
                SEIcon.GUNPOWDER_CRAFTING_DENY,
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder_crafting"),
                Component.translatable("gui.schematicenergistics.cannon_interface.disable_gunpowder_crafting_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder_crafting"),
                Component.translatable("gui.schematicenergistics.cannon_interface.enable_gunpowder_crafting_hint"),
                state -> {
                    sendState("gunpowderCraftingState", state);
                },
                gunpowderCraftingState);

        this.addToLeftToolbar(toggleCrafting);
        this.addToLeftToolbar(toggleGunpowder);
        this.addToLeftToolbar(toggleGunpowderCrafting);

    }

    @Override
    protected void init() {
        super.init();
        updateSchematicName(null);

        int centerX = this.leftPos + (this.imageWidth / 2) - 8;

        this.playPause = new SEToggleButton(
                SEIcon.PAUSE,
                SEIcon.PLAY,
                Component.translatable("gui.schematicenergistics.cannon_interface.pause"),
                Component.translatable("gui.schematicenergistics.cannon_interface.pause_hint"),
                Component.translatable("gui.schematicenergistics.cannon_interface.play"),
                Component.translatable("gui.schematicenergistics.cannon_interface.play_hint"),
                (state) -> sendCannonState(state, false),
                false);

        this.playPause.setPosition(
                centerX - 16,
                this.topPos + 56);

        this.addRenderableWidget(playPause);

        // Stop the cannon
        SESimpleIconButton stop = new SESimpleIconButton(
                SEIcon.STOP,
                Component.translatable("gui.schematicenergistics.cannon_interface.stop"),
                Component.translatable("gui.schematicenergistics.cannon_interface.stop_hint"),
                (btn) -> sendCannonState(false, true) // Stop the cannon
        );

        stop.setPosition(centerX + 16, this.topPos + 56);
        this.addRenderableWidget(stop);

        backButton = new SESimpleIconButton(
                SEIcon.BACK,
                Component.translatable("gui.schematicenergistics.cannon_terminal.return_terminal"),
                Component.empty(),
                (btn) -> {
                    PacketDistributor.sendToServer(new ReturnToTerminalPacket(terminal));
                });

        backButton.setPosition(leftPos + imageWidth - 28, this.topPos - 10);
        backButton.visible = (terminal != null);
        this.addRenderableWidget(backButton);
    }

    public void sendState(String config, boolean state) {
        PacketDistributor.sendToServer(
                new CannonInterfaceConfigPacket(
                        state, config));
    }

    public void sendCannonState(boolean state, boolean isStop) {
        if (isStop) {
            var stoppedState = SchematicannonBlockEntity.State.STOPPED;
            PacketDistributor.sendToServer(
                    new CannonStatePacket(stoppedState.toString()));
            return;
        }

        this.playPause.setState(state);
        SchematicannonBlockEntity.State cannonState = state ? SchematicannonBlockEntity.State.RUNNING
                : SchematicannonBlockEntity.State.PAUSED;

        PacketDistributor.sendToServer(
                new CannonStatePacket(cannonState.toString()));
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
                guiGraphics.renderTooltip(this.font,
                        Component.translatable("gui.schematicenergistics.cannon_interface.no_item"), mouseX, mouseY);
            } else {
                guiGraphics.renderTooltip(this.font, this.item.getDisplayName(), mouseX, mouseY);
            }
        }
    }

    public void updateStates(boolean gunpowderState, boolean craftingState, boolean gunpowderCraftingState) {
        this.gunpowderState = gunpowderState;
        this.craftingState = craftingState;

        if (this.toggleGunpowder != null) {
            this.toggleGunpowder.setState(gunpowderState);
        }
        if (this.toggleCrafting != null) {
            this.toggleCrafting.setState(craftingState);
        }
        if (this.toggleGunpowderCrafting != null) {
            this.toggleGunpowderCrafting.setState(gunpowderCraftingState);
        }
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

    public void updateSchematicName(String schematicName) {

        Component text = schematicName == null || schematicName.isEmpty()
                ? Component.translatable("gui.schematicenergistics.cannon_interface.schematic_name")
                : Component.literal(schematicName);

        Component limitedText = limitTextWidth(text, MAX_TEXT_WIDTH);
        setTextContent("schematic_text", limitedText);
    }

    public void updateStatusMsg(String statusMsg) {
        Component text = statusMsg == null || statusMsg.isEmpty()
                ? Component.translatable("gui.schematicenergistics.cannon_interface.missing_cannon")
                : SEUtils.formatCannonStatus(statusMsg);

        Component limitedText = limitTextWidth(text, MAX_TEXT_WIDTH);
        setTextContent("status_text", limitedText);
    }

    public void updateCannonState(String state) {
        boolean cState = !"PAUSED".equals(state);
        this.playPause.setState(cState);
    }

    public void updateScreenItem(CompoundTag data, String schematicName, String statusMsg, String state,
            BlockPos terminalPos) {
        var item = AEItemKey.fromTag(menu.getLogic().getLevel().registryAccess(), data);
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
