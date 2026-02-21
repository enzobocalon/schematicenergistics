package com.schematicenergistics.util;

import com.schematicenergistics.logic.CannonInterfaceLogic;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;

public interface ISchematicAccessor {
    MaterialChecklist schematicenergistics$getChecklist();
    void schematicenergistics$setCannonInterface(CannonInterfaceLogic logic);
}
