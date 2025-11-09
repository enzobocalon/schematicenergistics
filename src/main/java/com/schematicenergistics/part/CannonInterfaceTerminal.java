package com.schematicenergistics.part;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractDisplayPart;
import com.schematicenergistics.blockentity.CannonInterfaceEntity;
import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.core.Registration;
import com.schematicenergistics.lib.SEUtils;
import com.schematicenergistics.lib.TerminalListData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CannonInterfaceTerminal extends AbstractDisplayPart {

    @PartModels
    public static ResourceLocation MODEL_BASE = SchematicEnergistics.makeId("part/display_base");

    @PartModels
    private static final ResourceLocation MODEL_TERMINAL_ON = SchematicEnergistics.makeId("part/cannon_terminal_on");

    @PartModels
    private static final ResourceLocation MODEL_TERMINAL_OFF = SchematicEnergistics.makeId("part/cannon_terminal_off");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_TERMINAL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_TERMINAL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_TERMINAL_ON, MODEL_STATUS_HAS_CHANNEL);

    public CannonInterfaceTerminal(IPartItem<?> partItem, boolean requireChannel) {
        super(partItem, requireChannel);

    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!player.getCommandSenderWorld().isClientSide()) {
            MenuOpener.open(Registration.CANNON_INTERFACE_TERMINAL_MENU.get(), player, MenuLocators.forPart(this));
        }
        return true;
    }

    public List<TerminalListData> getCannonInterfaces() {
        List<TerminalListData> result = new ArrayList<>();

        IGridNode node = this.getMainNode().getNode();
        if (node == null) {
            return result;
        }

        IGrid grid = node.getGrid();
        if (grid == null) {
            return result;
        }

        var entities = grid.getMachines(CannonInterfaceEntity.class);
        var parts = grid.getMachines(CannonInterfacePart.class);
        var dimension = getLevel().dimension().location().toString().split(":")[1];
        for (var entity : entities) {
            if (entity != null) {
                var schematicName = entity.getLogic().getSchematicName();
                var status = entity.getLogic().getStatusMsg();
                if (schematicName == null || schematicName.isEmpty()) {
                    schematicName = "";
                }

                TerminalListData data = new TerminalListData(
                        entity.getBlockPos(),
                        schematicName,
                        SEUtils.InterfaceType.BLOCK,
                        dimension,
                        status,
                        entity.getLogic().getState()
                );
                result.add(data);
            }
        }

        for (var part : parts) {
            if (part != null) {
                var schematicName = part.getLogic().getSchematicName();
                var status = part.getLogic().getStatusMsg();
                if (schematicName == null || schematicName.isEmpty()) {
                    schematicName = "";
                }

                TerminalListData data = new TerminalListData(
                        part.getBlockEntity().getBlockPos(),
                        schematicName,
                        SEUtils.InterfaceType.PART,
                        dimension,
                        status,
                        part.getLogic().getState()
                );

                result.add(data);
            }
        }

        return result;
    }
}