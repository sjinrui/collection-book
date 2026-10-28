package dev.sjinrui.collectionbook.common.data;

import dev.sjinrui.collectionbook.common.category.CollectionCategory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个玩家的全部收集数据（服务端持有并持久化）。
 */
public class PlayerCollectionData {
    private final Map<CollectionCategory, Map<String, CollectionEntry>> byCategory =
            new EnumMap<>(CollectionCategory.class);

    /**
     * 记录一次收集。若已存在则保留原时间戳，不刷新。
     *
     * @return 新建返回 true；已存在返回 false。
     */
    public boolean record(CollectionCategory category, String resourceId, long now) {
        Map<String, CollectionEntry> map =
                byCategory.computeIfAbsent(category, k -> new LinkedHashMap<>());
        CollectionEntry existing = map.get(resourceId);
        if (existing != null) {
            return false;
        }
        map.put(resourceId, new CollectionEntry(category, resourceId, now, true));
        return true;
    }

    public CollectionEntry get(CollectionCategory category, String resourceId) {
        Map<String, CollectionEntry> map = byCategory.get(category);
        return map == null ? null : map.get(resourceId);
    }

    public int countNew() {
        int n = 0;
        for (Map<String, CollectionEntry> map : byCategory.values()) {
            for (CollectionEntry e : map.values()) {
                if (e.isNew()) {
                    n++;
                }
            }
        }
        return n;
    }

    /** 确认全部新收集条目。 */
    public void ackAll() {
        for (Map<String, CollectionEntry> map : byCategory.values()) {
            for (CollectionEntry e : map.values()) {
                e.setNew(false);
            }
        }
    }

    public List<CollectionEntry> all() {
        List<CollectionEntry> out = new ArrayList<>();
        for (Map<String, CollectionEntry> map : byCategory.values()) {
            out.addAll(map.values());
        }
        return out;
    }
}
