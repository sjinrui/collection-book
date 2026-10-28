package dev.sjinrui.collectionbook.forge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.sjinrui.collectionbook.common.client.CollectionScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Forge 客户端装配：注册“打开收集书”快捷键，并在按键触发时打开 GUI。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CollectionClientForge {

    public static final KeyMapping OPEN_BOOK = new KeyMapping(
            "key.collection_book",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.collection_book");

    @SubscribeEvent
    public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BOOK);
    }

    /** 游戏内按键检测（Forge 事件总线）。 */
    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Keys {
        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            while (OPEN_BOOK.consumeClick()) {
                mc.setScreen(new CollectionScreen());
            }
        }
    }
}
