package dev.sjinrui.collectionbook.common.registry;

import dev.sjinrui.collectionbook.common.item.CollectionBookItem;
import net.minecraft.world.item.Item;

/**
 * 公共侧物品引用。实例在此创建，由平台（forge/fabric）注册进对应注册表。
 */
public final class CollectionBookItems {
    public static final String BOOK_ID = "collection_book";

    public static final Item COLLECTION_BOOK = new CollectionBookItem(new Item.Properties().stacksTo(1));

    private CollectionBookItems() {}
}
