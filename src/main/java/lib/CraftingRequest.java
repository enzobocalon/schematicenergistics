package lib;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEItemKey;
import java.util.concurrent.Future;

public class CraftingRequest {
    private final AEItemKey itemKey;
    private final long amount;
    private final Future<ICraftingPlan> future;

    public CraftingRequest(AEItemKey itemKey, long amount, Future<ICraftingPlan> future) {
        this.itemKey = itemKey;
        this.amount = amount;
        this.future = future;
    }

    public AEItemKey getItem() {
        return this.itemKey;
    }

    public long getAmount() {
        return this.amount;
    }

    public Future<ICraftingPlan> getFuture() {
        return this.future;
    }
}
