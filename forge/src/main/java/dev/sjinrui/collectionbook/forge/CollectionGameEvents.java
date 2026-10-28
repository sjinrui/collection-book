package dev.sjinrui.collectionbook.forge;

import dev.sjinrui.collectionbook.common.category.CollectionCategory;
import dev.sjinrui.collectionbook.common.server.CollectionServerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge 游戏事件订阅：把加载器事件转成 common 的通用记录调用。
 * 不反射/不依赖任何第三方 mod。
 */
public class CollectionGameEvents {

    private static long tickCounter = 0;

    /** 拾取物品 -> 物品收集。 */
    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack stack = event.getStack();
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null) {
                CollectionServerData.record(player, CollectionCategory.ITEM, id.toString());
            }
        }
    }

    /** 击杀生物 -> 生物收集。 */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
            if (id != null) {
                CollectionServerData.record(killer, CollectionCategory.MOB, id.toString());
            }
        }
    }

    /** 吃完食物 -> 食物收集。 */
    @SubscribeEvent
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack stack = event.getItem();
            if (stack.isEdible()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id != null) {
                    CollectionServerData.record(player, CollectionCategory.FOOD, id.toString());
                }
            }
        }
    }

    /** 玩家定时检测所在生物群系。 */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        tickCounter++;
        if (tickCounter % 20 != 0) return;
        var biomeHolder = player.level().getBiome(player.blockPosition());
        biomeHolder.unwrapKey().ifPresent(key ->
                CollectionServerData.record(player, CollectionCategory.BIOME, key.location().toString()));
    }

    /** 登录：载入并同步。 */
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CollectionServerData.onJoin(player);
        }
    }

    /** 退出：写盘。 */
    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CollectionServerData.onLogout(player);
        }
    }

    /** 服务端停止：全部持久化。 */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        CollectionServerData.saveAll();
    }
}
