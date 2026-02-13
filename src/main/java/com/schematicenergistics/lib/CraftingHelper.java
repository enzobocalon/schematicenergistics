package com.schematicenergistics.lib;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEItemKey;
import com.schematicenergistics.logic.CannonInterfaceLogic;

public class CraftingHelper {
    private CraftingRequest pendingCraft;
    private final CannonInterfaceLogic cannonLogic;
    private ICraftingLink link;
    private ICraftingPlan readyPlan;
    private long readyPlanSinceTick = -1;

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

    public ICraftingPlan getReadyPlan() {
        return this.readyPlan;
    }

    public void setReadyPlan(ICraftingPlan plan) {
        this.readyPlan = plan;
        var level = this.cannonLogic.getLevel();
        this.readyPlanSinceTick = level != null ? level.getGameTime() : -1;
    }

    public void clearReadyPlan() {
        this.readyPlan = null;
        this.readyPlanSinceTick = -1;
    }

    public long getReadyPlanSinceTick() {
        return this.readyPlanSinceTick;
    }
}
