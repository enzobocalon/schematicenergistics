package com.schematicenergistics.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.schematicenergistics.lib.SEUtils;
import com.schematicenergistics.menu.CannonInterfaceMenu;
import com.schematicenergistics.network.payloads.OpenMaterialsScreenPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import com.schematicenergistics.network.payloads.CannonInterfaceConfigPacket;
import com.schematicenergistics.network.payloads.CannonStatePacket;
import com.schematicenergistics.network.payloads.ReturnToTerminalPacket;
import com.schematicenergistics.widgets.SEIcon;
import com.schematicenergistics.widgets.SESimpleIconButton;
import com.schematicenergistics.widgets.SEToggleButton;
import com.schematicenergistics.lib.CannonInterfaceClientState;

public class CannonInterfaceScreen extends AEBaseScreen<CannonInterfaceMenu> {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------
    private final SEToggleButton toggleGunpowderCrafting;
    private final SEToggleButton toggleCrafting;
    private final SEToggleButton toggleGunpowder;
    private final SEToggleButton toggleBulkCraft;
    private SEToggleButton playPause;
    private SESimpleIconButton backButton = null;
    private SESimpleIconButton materialsButton = null;

    private boolean craftingState;
    private boolean gunpowderState;
    private boolean gunpowderCraftingState;
    private boolean bulkCraftState;

    private static final int MAX_TEXT_WIDTH = 164;

    private BlockPos terminal = null;

    // -----------------------------------------------------------------------
    // Constructor
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
                btn -> {
                    var pos = menu.getHostPos();
                    if (pos != null) {
                        PacketDistributor.sendToServer(
                                new OpenMaterialsScreenPacket(pos));
                    }
                });
        materialsButton.setPosition(leftPos + 9, this.topPos + 56); // 8px + 1px for the dark outer border
        this.addRenderableWidget(materialsButton);
    }

    // -----------------------------------------------------------------------
    // Network
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
    // State updates
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

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private Component limitTextWidth(Component text, int maxWidth) {
        String s = text.getString();
        if (this.font.width(s) <= maxWidth) return text;
        String ellipsis  = "...";
        String truncated = this.font.plainSubstrByWidth(s, maxWidth - this.font.width(ellipsis));
        return Component.literal(truncated + ellipsis);
    }
}