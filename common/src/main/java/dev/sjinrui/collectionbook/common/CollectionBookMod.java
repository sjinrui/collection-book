package dev.sjinrui.collectionbook.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 收集书（Collection Book）通用常量与入口。
 * 本模块不得出现任何 Forge/Fabric 专有 API；加载器差异通过 @ExpectPlatform 隔离。
 */
public final class CollectionBookMod {
    public static final String MOD_ID = "collection_book";
    public static final Logger LOGGER = LoggerFactory.getLogger("CollectionBook");

    private CollectionBookMod() {}

    /** 公共侧初始化（注册网络通道等）。 */
    public static void init() {
        LOGGER.info("Collection Book common core initializing.");
        dev.sjinrui.collectionbook.common.network.CollectionNetwork.register();
    }
}
