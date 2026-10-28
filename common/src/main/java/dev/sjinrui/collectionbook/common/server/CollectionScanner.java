package dev.sjinrui.collectionbook.common.server;

import dev.sjinrui.collectionbook.common.category.CollectionCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 通用扫描逻辑（common 共享，Forge/Fabric 各自的定时事件调用）。
 * 不依赖任何加载器或第三方 mod API。
 */
public final class CollectionScanner {
    private CollectionScanner() {}

    /**
     * 扫描玩家当前全部槽位（主背包、盔甲、副手），记录出现过的物品。
     * 数据只增不删，因此用掉/丢掉后图鉴记录仍保留。
     */
    public static void scanInventory(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            recordItem(player, stack);
        }
        for (ItemStack stack : player.getInventory().armor) {
            recordItem(player, stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            recordItem(player, stack);
        }
    }

    /** 记录玩家当前所在生物群系。 */
    public static void scanBiome(ServerPlayer player) {
        player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key ->
                CollectionServerData.record(player, CollectionCategory.BIOME, key.location().toString()));
    }

    private static void recordItem(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null) {
            CollectionServerData.record(player, CollectionCategory.ITEM, id.toString());
        }
    }
}
