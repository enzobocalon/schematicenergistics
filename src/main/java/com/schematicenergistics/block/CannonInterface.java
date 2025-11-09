package com.schematicenergistics.block;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import com.schematicenergistics.blockentity.CannonInterfaceEntity;
import com.schematicenergistics.core.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CannonInterface extends AEBaseEntityBlock<CannonInterfaceEntity> {
    public CannonInterface(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public BlockEntityType<CannonInterfaceEntity> getBlockEntityType() {
        return Registration.CANNON_INTERFACE_ENTITY.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        var be = getBlockEntity(level, pos);

        if (be != null) {
            if (!level.isClientSide()) {
                if (be.getLogic() != null) {
                    be.getLogic().setTerminalPos(null);
                }
                be.openMenu( player, MenuLocators.forBlockEntity(be));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
