package com.schematicenergistics.datagen;

import com.schematicenergistics.SchematicEnergistics;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SchematicEnergistics.MOD_ID, existingFileHelper);
    }

    protected void registerModels() {
    }
}
