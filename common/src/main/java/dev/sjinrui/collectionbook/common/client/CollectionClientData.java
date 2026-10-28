package dev.sjinrui.collectionbook.common.client;

import dev.sjinrui.collectionbook.common.data.CollectionEntry;

import java.util.Collections;
import java.util.List;

/**
 * 客户端持有的已同步收集数据快照（由服务端网络包下发）。
 */
public final class CollectionClientData {
    private static volatile List<CollectionEntry> entries = Collections.emptyList();

    private CollectionClientData() {}

    public static void update(List<CollectionEntry> newEntries) {
        entries = List.copyOf(newEntries);
    }

    public static List<CollectionEntry> all() {
        return entries;
    }

    public static int countNew() {
        int n = 0;
        for (CollectionEntry e : entries) {
            if (e.isNew()) {
                n++;
            }
        }
        return n;
    }

    /** 确认后客户端本地清除新标记（乐观更新）。 */
    public static void clearNewFlags() {
        for (CollectionEntry e : entries) {
            e.setNew(false);
        }
    }
}
