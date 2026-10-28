package dev.sjinrui.collectionbook.common.client;

/**
 * 排序方式：默认 / 最新在前 / 最早在前。
 */
public enum CollectionSortMode {
    DEFAULT("collection_book.sort.default"),
    NEWEST_FIRST("collection_book.sort.newest"),
    OLDEST_FIRST("collection_book.sort.oldest");

    public final String translationKey;

    CollectionSortMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public CollectionSortMode next() {
        CollectionSortMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
