package com.schematicenergistics.lib;

public class CannonInterfaceClientState {
    private static boolean gunpowderState = false;
    private static boolean craftingState = false;
    private static boolean gunpowderCraftingState = false;
    private static boolean bulkCraftState = false;
    private static boolean received = false;

    public static void setState(boolean gunpowder, boolean crafting, boolean gunpowderCrafting, boolean bulkCraftingState) {
        gunpowderState = gunpowder;
        craftingState = crafting;
        gunpowderCraftingState = gunpowderCrafting;
        bulkCraftState = bulkCraftingState;
        received = true;
    }

    public static boolean hasState() {
        return received;
    }

    public static boolean getGunpowderState() {
        return gunpowderState;
    }

    public static boolean getCraftingState() {
        return craftingState;
    }

    public static boolean getBulkCraftState() {
        return bulkCraftState;
    }

    public static boolean getGunpowderCraftingState() {
        return gunpowderCraftingState;
    }

    public static void reset() {
        received = false;
    }
}
