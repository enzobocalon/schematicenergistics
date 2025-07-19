package part;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;
import com.schematicenergistics.SchematicEnergistics;
import logic.CannonInterfaceLogic;
import net.minecraft.resources.ResourceLocation;

public class CannonInterfacePart extends AEBasePart{

    private CannonInterfaceLogic cannonLogic;

    public CannonInterfacePart(IPartItem<?> partItem) {
        super(partItem);
    }

    @PartModels
    private static final IPartModel MODEL_BASE = new PartModel(AppEng.makeId("part/interface_base"));

    @PartModels
    private static final ResourceLocation MODEL_INTERFACE = ResourceLocation.fromNamespaceAndPath(SchematicEnergistics.MOD_ID, "part/cannon_interface");

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0);
    }

    @Override
    public IPartModel getStaticModels() {
        return new PartModel(MODEL_BASE.requireCableConnection(), MODEL_INTERFACE);
    }

}
