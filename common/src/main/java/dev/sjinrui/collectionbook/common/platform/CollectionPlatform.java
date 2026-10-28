package dev.sjinrui.collectionbook.common.platform;

import dev.sjinrui.collectionbook.common.item.CollectionBookItem;
import dev.sjinrui.collectionbook.common.registry.CollectionBookItems;

/**
 * 平台抽象层（@ExpectPlatform）。
 *
 * <p>common 中只声明接口，具体实现由 forge / fabric 模块在<b>同包同名类</b>中提供。
 * 后续 Fabric 代理必须在 fabric 模块实现本类的全部静态方法（方法签名与本文件一致）。
 */
public final class CollectionPlatform {
    private CollectionPlatform() {}

    /**
     * 在客户端打开收集书 GUI。仅客户端调用。
     * 实现参考（forge）：{@code Minecraft.getInstance().setScreen(new CollectionScreen());}
     */
    @dev.architectury.injectables.annotations.ExpectPlatform
    public static void openBookScreen() {
        throw new AssertionError("Expected platform injection; this method must be implemented by forge/fabric.");
    }
}
