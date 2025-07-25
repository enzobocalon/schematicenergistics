package screen;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import menu.CannonInterfaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CannonInterfaceScreen extends AEBaseScreen<CannonInterfaceMenu> {

    public CannonInterfaceScreen(CannonInterfaceMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth = 176;
        this.imageHeight = 182;
    }


    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

}
