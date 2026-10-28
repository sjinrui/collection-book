package dev.sjinrui.collectionbook.forge;

import dev.sjinrui.collectionbook.common.CollectionBookMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge 加载器入口。仅做加载器装配：调用公共 init、注册事件总线与客户端入口。
 */
@Mod(CollectionBookMod.MOD_ID)
public class CollectionBookForge {

    public CollectionBookForge() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 公共核心：注册跨加载器网络通道
        CollectionBookMod.init();

        // 注册表事件（物品 / 创造模式物品栏）走 mod 事件总线
        modBus.register(RegistrationHandler.class);

        // 游戏事件（拾取 / 击杀 / 进食 / 群系 / 登录退出）走 Forge 事件总线
        MinecraftForge.EVENT_BUS.register(CollectionGameEvents.class);

        // 客户端装配（仅物理客户端加载）
        if (net.minecraftforge.api.distmarker.Dist.CLIENT == net.minecraftforge.fml.loading.FMLEnvironment.dist) {
            modBus.register(CollectionClientForge.class);
        }
    }
}
