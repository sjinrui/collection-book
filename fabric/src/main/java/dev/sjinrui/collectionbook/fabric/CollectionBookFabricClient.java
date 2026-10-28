package dev.sjinrui.collectionbook.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.sjinrui.collectionbook.common.client.CollectionScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Fabric 客户端入口：注册“打开收集书”快捷键（默认 B），按键时打开 GUI。
 */
public class CollectionBookFabricClient implements net.fabricmc.api.ClientModInitializer {

    private static KeyMapping openBook;

    @Override
    public void onInitializeClient() {
        openBook = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.collection_book",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.collection_book"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            while (openBook.consumeClick()) {
                client.setScreen(new CollectionScreen());
            }
        });
    }
}
