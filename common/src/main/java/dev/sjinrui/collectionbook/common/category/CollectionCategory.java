package dev.sjinrui.collectionbook.common.category;

/**
 * 收集类别。通用、事件驱动，不依赖任何第三方 mod。
 */
public enum CollectionCategory {
    ITEM("item", "collection_book.tab.item"),
    MOB("mob", "collection_book.tab.mob"),
    FOOD("food", "collection_book.tab.food"),
    BIOME("biome", "collection_book.tab.biome");

    public final String key;
    public final String translationKey;

    CollectionCategory(String key, String translationKey) {
        this.key = key;
        this.translationKey = translationKey;
    }

    public static CollectionCategory byOrdinal(int ordinal) {
        CollectionCategory[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return ITEM;
        }
        return values[ordinal];
    }
}
