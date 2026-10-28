package dev.sjinrui.collectionbook.fabric;

import dev.sjinrui.collectionbook.common.CollectionBookMod;
import dev.sjinrui.collectionbook.common.category.CollectionCategory;
import dev.sjinrui.collectionbook.common.registry.CollectionBookItems;
import dev.sjinrui.collectionbook.common.registry.CollectionBookTabs;
import dev.sjinrui.collectionbook.common.server.CollectionScanner;
import dev.sjinrui.collectionbook.common.server.CollectionServerData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric 入口：注册物品 / 创造栏，订阅游戏事件，转成 common 的通用记录。
 * 整个项目使用官方 Mojang mappings，故此处为 Mojmap 类名。
 */
public class CollectionBookFabric implements net.fabricmc.api.ModInitializer {

    private static long tickCounter = 0;

    @Override
    public void onInitialize() {
        CollectionBookMod.init();
        registerContent();
        registerEvents();
    }

    private void registerContent() {
        Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(CollectionBookMod.MOD_ID, CollectionBookItems.BOOK_ID),
                CollectionBookItems.COLLECTION_BOOK);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                new ResourceLocation(CollectionBookMod.MOD_ID, CollectionBookTabs.TAB_ID),
                CollectionBookTabs.MAIN);
    }

    private void registerEvents() {
        // 击杀生物 -> 生物收集
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof ServerPlayer killer) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                if (id != null) {
                    CollectionServerData.record(killer, CollectionCategory.MOB, id.toString());
                }
            }
        });

        // 使用可食用物品 -> 食物收集（右键开始即记录）
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (player instanceof ServerPlayer sp && stack.isEdible()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id != null) {
                    CollectionServerData.record(sp, CollectionCategory.FOOD, id.toString());
                }
            }
            return net.minecraft.world.InteractionResultHolder.pass(stack);
        });

        // 每 20 tick 扫描背包（物品）与所在生物群系
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 20 != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                CollectionScanner.scanInventory(player);
                CollectionScanner.scanBiome(player);
            }
        });

        // 登录 / 退出
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                CollectionServerData.onJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                CollectionServerData.onLogout(handler.player));

        // 服务端停止：全部写盘
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> CollectionServerData.saveAll());
    }
}
