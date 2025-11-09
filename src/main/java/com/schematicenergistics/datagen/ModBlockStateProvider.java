package com.schematicenergistics.datagen;

import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.core.Registration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SchematicEnergistics.MOD_ID, exFileHelper);
    }

    protected void registerStatesAndModels() {
        this.blockWithItem(Registration.CANNON_INTERFACE);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        this.simpleBlockWithItem(deferredBlock.get(), this.cubeAll(deferredBlock.get()));
    }
}
