package dev.sjinrui.collectionbook.common.platform;

import dev.sjinrui.collectionbook.common.client.CollectionScreen;
import net.minecraft.client.Minecraft;

/**
 * {@link ExpectPlatform} 的 Forge 平台实现。
 * 必须与 common 中的抽象类同包同名，方法签名一致。
 */
public final class CollectionPlatform {
    private CollectionPlatform() {}

    public static void openBookScreen() {
        Minecraft.getInstance().setScreen(new CollectionScreen());
    }
}
