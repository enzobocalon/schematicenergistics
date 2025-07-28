package screen;
import appeng.api.stacks.AEItemKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import lib.CannonInterfaceClientState;
import menu.CannonInterfaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import network.payloads.CannonInterfaceConfigPacket;
import widgets.SEIcon;
import widgets.SEToggleButton;

public class CannonInterfaceScreen extends AEBaseScreen<CannonInterfaceMenu> {
    private AEItemKey item;
    private SEToggleButton toggleGunpowderCrafting;
    private SEToggleButton toggleCrafting;
    private SEToggleButton toggleGunpowder;
    private String schematicName;

    private boolean craftingState;
    private boolean gunpowderState;
    private boolean gunpowderCraftingState;


    public CannonInterfaceScreen(CannonInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth = 176;
        this.imageHeight = 182;

        int centerX = this.leftPos + (this.imageWidth / 2) - 8;

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
                craftingState
        );

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
                gunpowderState
        );

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
                gunpowderCraftingState
        );

        this.addToLeftToolbar(toggleCrafting);
        this.addToLeftToolbar(toggleGunpowder);
        this.addToLeftToolbar(toggleGunpowderCrafting);
    }

    @Override
    protected void init() {
        super.init();
        updateSchematicName(null);
    }

    public void sendState(String config, boolean state) {
        PacketDistributor.sendToServer(
            new CannonInterfaceConfigPacket(
                state, config
            )
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

    public void updateSchematicName(String schematicName) {
        this.schematicName = schematicName;

        Component text = schematicName == null || schematicName.isEmpty() ?
            Component.translatable("gui.schematicenergistics.cannon_interface.schematic_name")
                : Component.literal(schematicName);

        setTextContent("schematic_text", text);
    }

    public void updateScreenItem(CompoundTag data, String schematicName) {
        var item = AEItemKey.fromTag(menu.getLogic().getLevel().registryAccess(), data);
        this.item = item != null ? item : AEItemKey.of(ItemStack.EMPTY);
        updateSchematicName(schematicName);
    }
}
