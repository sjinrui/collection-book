package dev.sjinrui.collectionbook.common.data;

import dev.sjinrui.collectionbook.common.category.CollectionCategory;

/**
 * 单条收集记录。
 *
 * @param category      收集类别
 * @param resourceId    资源 ID（如 minecraft:iron_ingot / minecraft:pig / minecraft:plains）
 * @param firstCollected 首次获取时间戳（毫秒，用于排序）
 * @param isNew         是否“新收集未确认”
 */
public class CollectionEntry {
    private final CollectionCategory category;
    private final String resourceId;
    private long firstCollected;
    private boolean isNew;

    public CollectionEntry(CollectionCategory category, String resourceId, long firstCollected, boolean isNew) {
        this.category = category;
        this.resourceId = resourceId;
        this.firstCollected = firstCollected;
        this.isNew = isNew;
    }

    public CollectionCategory getCategory() {
        return category;
    }

    public String getResourceId() {
        return resourceId;
    }

    public long getFirstCollected() {
        return firstCollected;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
