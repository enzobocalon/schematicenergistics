package menu;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.menu.AEBaseMenu;
import blockentity.CannonInterfaceEntity;
import core.Registration;
import logic.CannonInterfaceLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import part.CannonInterfacePart;

public class CannonInterfaceMenu extends AEBaseMenu {
    private final Level level;

    private static final int INV_START_X = 8;
    private static final int INV_START_Y = 98;

    private static final int HOTBAR_START_X = 8;
    private static final int HOTBAR_START_Y = 156;

    private static final int SLOT_SPACING = 18;


    public CannonInterfaceMenu(int id, Inventory playerInv, FriendlyByteBuf data) {
        this(Registration.CANNON_INTERFACE_MENU.get(), id, playerInv, getLogicFromBuf(playerInv, data));
    }

    public CannonInterfaceMenu(MenuType<?> menuType, int id, Inventory playerInv, Object host) {
        super(menuType, id, playerInv, host);

        this.level = playerInv.player.level();

        this.createPlayerInventorySlots(playerInv);
    }

    private static CannonInterfaceLogic getLogicFromBuf(Inventory inv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof CannonInterfaceEntity entity) {
            System.out.println("Found Cannon Interface Entity at " + pos);
            return entity.getLogic();
        } else if (be instanceof IPartHost partHost) {
//            STILL REQUIRES SOME TESTING
            Direction direction = buf.readEnum(Direction.class);
            IPart part = partHost.getPart(direction.getOpposite());

            if (part instanceof CannonInterfacePart cannonPart) {
                System.out.println("Found Cannon Interface Part at " + pos + " in direction " + direction);
                return cannonPart.getLogic();
            }
        } else {
            throw new IllegalArgumentException("Invalid Buf from Cannon Interface at " + pos);
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        INV_START_X + col * SLOT_SPACING,
                        INV_START_Y + row * SLOT_SPACING));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i,
                    HOTBAR_START_X + i * SLOT_SPACING,
                    HOTBAR_START_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
