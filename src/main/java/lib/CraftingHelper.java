package lib;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.stacks.AEItemKey;
import logic.CannonInterfaceLogic;

public class CraftingHelper {
    private CraftingRequest pendingCraft;
    private final CannonInterfaceLogic cannonLogic;
    private ICraftingLink link;

    public CraftingHelper(CannonInterfaceLogic logic) {
        this.cannonLogic = logic;
    }

    public void startCraft(AEItemKey key, long amount, CalculationStrategy strategy) {
        var level = this.cannonLogic.getLevel();
        if (key == null || amount <= 0 || level == null || strategy == null) {
            return;
        }

        var node = this.cannonLogic.getGridNode();
        if (node == null) return;

        var grid = node.getGrid();
        if (grid == null) return;

        var service = grid.getCraftingService();
        if (!service.isCraftable(key)) {
            return;
        }

        var future = service.beginCraftingCalculation(
                this.cannonLogic.getLevel(),
                cannonLogic::getActionSource,
                key,
                amount,
                strategy
        );

        if (future != null) {
            this.pendingCraft = new CraftingRequest(key, amount, future);
        }
    }


    public void clearPendingCraft() {
        this.pendingCraft = null;
    }

    public void setLink(ICraftingLink link) {
        this.link = link;
    }

    public ICraftingLink getLink() {
        return this.link;
    }

    public CraftingRequest getPendingCraft() {
        return this.pendingCraft;
    }
}
