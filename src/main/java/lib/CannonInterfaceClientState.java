package lib;

public class CannonInterfaceClientState {
    private static boolean gunpowderState = false;
    private static boolean craftingState = false;
    private static boolean received = false;

    public static void setState(boolean gunpowder, boolean crafting) {
        gunpowderState = gunpowder;
        craftingState = crafting;
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

    public static void reset() {
        received = false;
    }
}
