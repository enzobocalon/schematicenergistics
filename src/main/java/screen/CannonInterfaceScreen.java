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
    private SEToggleButton toggleGunpowder;
    private SEToggleButton toggleCrafting;
    private String schematicName;

    private boolean craftingState;
    private boolean gunpowderState;


    public CannonInterfaceScreen(CannonInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth = 176;
        this.imageHeight = 182;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.leftPos + (this.imageWidth / 2) - 8;

        if (CannonInterfaceClientState.hasState()) {
            this.gunpowderState = CannonInterfaceClientState.getGunpowderState();
            this.craftingState = CannonInterfaceClientState.getCraftingState();
            CannonInterfaceClientState.reset();
        }

        this.toggleCrafting = new SEToggleButton(
                SEIcon.CRAFTING_ALLOW,
                SEIcon.CRAFTING_DENY,
                Component.literal("Disable auto crafting"),
                Component.literal("Cannon will no longer craft items"),
                Component.literal("Enable auto crafting"),
                Component.literal("Cannon will craft items"),
                state -> {
                    sendState("craftingState", state);
                },
                craftingState
        );

        this.toggleGunpowder = new SEToggleButton(
                SEIcon.GUNPOWDER_ALLOW,
                SEIcon.GUNPOWDER_DENY,
                Component.literal("Toggle"),
                Component.literal("Toggle the state"),
                state -> {
                    sendState("gunpowderState", state);
                },
                gunpowderState
        );

        this.toggleGunpowder.setPosition(centerX + 16,  this.topPos + 144 / 2 - 8);
        this.toggleCrafting.setPosition(centerX - 16,  this.topPos + 144 / 2 - 8);
        this.addRenderableWidget(toggleCrafting);
        this.addRenderableWidget(toggleGunpowder);
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

        int centerX = this.leftPos + (this.imageWidth / 2) - 8;
        int centerY = this.topPos + 98 / 2 - 8;

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

    public void updateStates(boolean gunpowderState, boolean craftingState) {
        this.gunpowderState = gunpowderState;
        this.craftingState = craftingState;

        if (this.toggleGunpowder != null) {
            this.toggleGunpowder.setState(gunpowderState);
        }
        if (this.toggleCrafting != null) {
            this.toggleCrafting.setState(craftingState);
        }
    }

    public void updateScreenItem(CompoundTag data, String schematicName) {
        var item = AEItemKey.fromTag(menu.getLogic().getLevel().registryAccess(), data);
        this.item = item != null ? item : AEItemKey.of(ItemStack.EMPTY);
        this.schematicName = schematicName;
    }
}
