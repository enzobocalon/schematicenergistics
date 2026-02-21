package com.schematicenergistics.menu;

import appeng.api.stacks.AEItemKey;
import appeng.menu.AEBaseMenu;
import com.schematicenergistics.core.Registration;
import com.schematicenergistics.lib.MaterialListEntry;
import com.schematicenergistics.logic.CannonInterfaceLogic;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import com.schematicenergistics.network.payloads.MaterialListClientPacket;
import com.schematicenergistics.util.ISchematicAccessor;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialsMenu extends AEBaseMenu {
    private static final Logger log = LoggerFactory.getLogger(MaterialsMenu.class);
    private final ICannonInterfaceHost host;
    private int materialsRefreshTicks = 9; // next tick sends immediately

    public MaterialsMenu(int id, Inventory playerInventory, ICannonInterfaceHost host) {
        super(Registration.MATERIALS_MENU.get(), id, playerInventory, host);
        this.host = host;
    }

    public CannonInterfaceLogic getLogic() {
        return host.getLogic();
    }

    public ICannonInterfaceHost getHost() {
        return this.host;
    }

    public BlockPos getHostPos() {
        var entity = this.getHost().getEntity();
        var part = this.getHost().getPart();

        if (entity != null) {
            return entity.getBlockPos();
        } else if (part != null && part.getHost() != null && part.getHost().getBlockEntity() != null) {
            return part.getHost().getBlockEntity().getBlockPos();
        }
        return null;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (getPlayer() instanceof ServerPlayer player && getLogic() != null) {
            materialsRefreshTicks++;
            if (materialsRefreshTicks >= 10) {
                materialsRefreshTicks = 0;
                sendMaterials(player);
            }
        }
    }

    private void sendMaterials(ServerPlayer player) {
        List<MaterialListEntry> all = buildMaterialsList();
        PacketDistributor.sendToPlayer(player,
                new MaterialListClientPacket(0, 1, all));
    }

    private List<MaterialListEntry> buildMaterialsList() {
        var logic = getLogic();
        if (logic == null) {
            return List.of();
        }

        var gridNode = logic.getGridNode();
        if (gridNode == null || gridNode.getGrid() == null) {
            return List.of();
        }

        var grid = gridNode.getGrid();
        var storage = grid.getStorageService().getInventory();
        var craftingService = grid.getCraftingService();
        if (storage == null || craftingService == null) {
            return List.of();
        }

        var cannon = logic.getLinkedCannon();
        if (!(cannon instanceof ISchematicAccessor accessor)) {
            return List.of();
        }

        MaterialChecklist checklist = accessor.schematicenergistics$getChecklist();
        if (checklist == null) {
            return List.of();
        }

        Map<AEItemKey, Integer> gathered = new HashMap<>();
        Map<AEItemKey, Long> required = new HashMap<>();

        for (Object2IntMap.Entry<Item> entry : checklist.required.object2IntEntrySet()) {
            Item item = entry.getKey();
            int totalRequired = entry.getIntValue();
            int alreadyGathered = checklist.gathered.getOrDefault(item, 0);
            int needed = totalRequired - alreadyGathered;
            if (needed <= 0) {
                continue;
            }

            AEItemKey key = AEItemKey.of(new ItemStack(item));
            if (key == null) {
                continue;
            }

            required.merge(key, (long) needed, Long::sum);
            gathered.putIfAbsent(key, alreadyGathered);
        }

        for (Object2IntMap.Entry<Item> entry : checklist.damageRequired.object2IntEntrySet()) {
            Item item = entry.getKey();
            int damageAmount = entry.getIntValue();

            ItemStack stack = new ItemStack(item);
            int maxDamage = stack.getMaxDamage();
            if (maxDamage <= 0) {
                continue;
            }

            int itemsNeeded = (int) Math.ceil(damageAmount / (double) maxDamage);
            int alreadyGathered = checklist.gathered.getOrDefault(item, 0);
            itemsNeeded -= alreadyGathered;

            if (itemsNeeded <= 0) {
                continue;
            }

            AEItemKey key = AEItemKey.of(stack);
            if (key == null) {
                continue;
            }

            required.merge(key, (long) itemsNeeded, Long::sum);
            gathered.putIfAbsent(key, alreadyGathered);
        }

        if (required.isEmpty()) {
            return List.of();
        }

        List<MaterialListEntry> entries = new ArrayList<>(required.size());
        for (Map.Entry<AEItemKey, Long> entry : required.entrySet()) {
            AEItemKey key = entry.getKey();
            long needed = entry.getValue();
            long available = storage.getAvailableStacks().get(key);
            boolean craftable = craftingService.isCraftable(key);
            int gatheredCount = gathered.getOrDefault(key, 0);
            var item = key.toTag(logic.getLevel().registryAccess());
            entries.add(new MaterialListEntry(item, available, needed, gatheredCount, craftable));
        }

        entries.sort(Comparator
                .comparingInt((MaterialListEntry e) -> {
                    if (e.available() >= e.required()) {
                        return 2;
                    }
                    return e.craftable() ? 1 : 0;
                })
                .thenComparingLong(e -> -(e.required() - Math.min(e.available(), e.required()))));

        return entries;
    }
}
