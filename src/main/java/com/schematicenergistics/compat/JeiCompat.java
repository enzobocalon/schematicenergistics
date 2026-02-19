package com.schematicenergistics.compat;

import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.screen.CannonInterfaceScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

@JeiPlugin
public class JeiCompat implements IModPlugin {
    private static IJeiRuntime jeiRuntime;
    private static IIngredientManager ingredientManager;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SchematicEnergistics.MOD_ID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JeiCompat.jeiRuntime = jeiRuntime;
        JeiCompat.ingredientManager = jeiRuntime.getIngredientManager();
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiCompat.jeiRuntime = null;
        JeiCompat.ingredientManager = null;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CannonInterfaceScreen.class, new CannonInterfaceGuiHandler());
    }

    public static boolean isJeiLoaded() {
        return jeiRuntime != null;
    }

    @SuppressWarnings("deprecation")
    private static class CannonInterfaceGuiHandler implements IGuiContainerHandler<CannonInterfaceScreen> {
        @Override
        public List<Rect2i> getGuiExtraAreas(CannonInterfaceScreen screen) {
            return screen.getMaterialsBoundsForJei();
        }

        @Override
        public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
                CannonInterfaceScreen screen,
                double mouseX,
                double mouseY
        ) {
            ItemStack hoveredStack = screen.getHoveredStackForJei();
            if (hoveredStack != null && !hoveredStack.isEmpty() && ingredientManager != null) {
                Optional<ITypedIngredient<ItemStack>> typedIngredient = 
                    ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, hoveredStack);
                if (typedIngredient.isPresent()) {
                    return Optional.of(new SimpleClickableIngredient(typedIngredient.get(), (int) mouseX, (int) mouseY));
                }
            }
            return Optional.empty();
        }
    }

    @SuppressWarnings("deprecation")
    private record SimpleClickableIngredient(ITypedIngredient<ItemStack> typedIngredient, int x, int y) 
            implements IClickableIngredient<ItemStack> {
        @Override
        public ITypedIngredient<ItemStack> getTypedIngredient() {
            return typedIngredient;
        }

        @Override
        public Rect2i getArea() {
            return new Rect2i(x, y, 16, 16);
        }
    }
}
