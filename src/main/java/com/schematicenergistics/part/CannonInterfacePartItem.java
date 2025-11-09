package com.schematicenergistics.part;

import appeng.api.parts.IPartItem;
import appeng.items.parts.PartItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Function;

public class CannonInterfacePartItem extends PartItem<CannonInterfacePart> {

    public CannonInterfacePartItem(Properties properties, Class<CannonInterfacePart> partClass, Function<IPartItem<CannonInterfacePart>, CannonInterfacePart> factory) {
        super(properties, partClass, factory);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("com.schematicenergistics.block.schematicenergistics.cannon_interface.tooltip")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        );
    }
}
