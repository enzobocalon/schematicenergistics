package com.schematicenergistics.widgets;

import appeng.client.gui.style.Blitter;
import com.schematicenergistics.SchematicEnergistics;
import net.minecraft.resources.ResourceLocation;

public enum SEIcon {
    GUNPOWDER(0,0),
    GUNPOWDER_ALLOW(16,0),
    GUNPOWDER_DENY(32,0),
    CRAFTING_ALLOW(48, 0),
    CRAFTING_DENY(64, 0),
    GUNPOWDER_CRAFTING_ALLOW(80, 0),
    GUNPOWDER_CRAFTING_DENY(96, 0),
    PLAY(112, 0),
    PAUSE(128, 0),
    STOP(144, 0),
    BACK(160, 0),
    BULK_CRAFT_DENY(176, 0),
    BULK_CRAFT_ALLOW(192, 0),
    MATERIALS(208, 0);

    public final int x;
    public final int y;
    public final int width;
    public final int height;


    public static final ResourceLocation  TEXTURE = SchematicEnergistics.makeId("textures/guis/states.png");
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;


    SEIcon(int x, int y) {
        this(x, y, 16, 16);
    }

    SEIcon(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT).src(x, y, width, height);
    }
}
