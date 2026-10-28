package dev.sjinrui.collectionbook.common.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sjinrui.collectionbook.common.CollectionBookMod;
import dev.sjinrui.collectionbook.common.category.CollectionCategory;
import dev.sjinrui.collectionbook.common.data.CollectionEntry;
import dev.sjinrui.collectionbook.common.data.PlayerCollectionData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.architectury.networking.NetworkManager;

/**
 * 服务端玩家收集数据中心：按玩家记录、持久化到磁盘，并通过网络包同步到客户端。
 *
 * <p>数据在服务器端按玩家保存（多人可靠），文件位于运行目录下 {@code collection_book/<uuid>.json}。
 */
public final class CollectionServerData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BASE_DIR = Path.of("collection_book");
    private static final Map<UUID, PlayerCollectionData> DATA = new ConcurrentHashMap<>();

    public static final ResourceLocation CHANNEL =
            new ResourceLocation(CollectionBookMod.MOD_ID, "main");

    private CollectionServerData() {}

    public static PlayerCollectionData get(UUID uuid) {
        return DATA.computeIfAbsent(uuid, k -> load(k));
    }

    /**
     * 记录一次收集。仅在新条目时下发同步。
     *
     * @return 是否为新收集。
     */
    public static boolean record(ServerPlayer player, CollectionCategory category, String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) {
            return false;
        }
        PlayerCollectionData data = get(player.getUUID());
        boolean added = data.record(category, resourceId, System.currentTimeMillis());
        if (added) {
            syncToPlayer(player);
        }
        return added;
    }

    /** 玩家登录：载入数据并同步一次。 */
    public static void onJoin(ServerPlayer player) {
        get(player.getUUID());
        syncToPlayer(player);
    }

    /** 玩家退出：写盘并移除内存态。 */
    public static void onLogout(ServerPlayer player) {
        save(player.getUUID());
        DATA.remove(player.getUUID());
    }

    /** 服务端停止：全部写盘。 */
    public static void saveAll() {
        for (UUID uuid : DATA.keySet()) {
            save(uuid);
        }
    }

    /** 客户端确认“新 N”：清除该玩家全部新标记后重新同步。 */
    public static void ackAll(ServerPlayer player) {
        PlayerCollectionData data = get(player.getUUID());
        data.ackAll();
        syncToPlayer(player);
    }

    /** 把该玩家当前数据序列化为网络包并发给客户端。 */
    public static void syncToPlayer(ServerPlayer player) {
        PlayerCollectionData data = get(player.getUUID());
        List<CollectionEntry> all = data.all();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(all.size());
        for (CollectionEntry e : all) {
            buf.writeVarInt(e.getCategory().ordinal());
            buf.writeUtf(e.getResourceId());
            buf.writeLong(e.getFirstCollected());
            buf.writeBoolean(e.isNew());
        }
        NetworkManager.sendToPlayer(player, CHANNEL, buf);
    }

    // ---------------- 持久化（JSON） ----------------

    private static PlayerCollectionData load(UUID uuid) {
        PlayerCollectionData data = new PlayerCollectionData();
        Path file = BASE_DIR.resolve(uuid + ".json");
        if (!Files.exists(file)) {
            return data;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                return data;
            }
            JsonObject obj = root.getAsJsonObject();
            for (CollectionCategory category : CollectionCategory.values()) {
                JsonElement el = obj.get(category.key);
                if (el == null || !el.isJsonArray()) {
                    continue;
                }
                for (JsonElement entryEl : el.getAsJsonArray()) {
                    JsonObject eo = entryEl.getAsJsonObject();
                    String id = eo.get("id").getAsString();
                    long ts = eo.get("ts").getAsLong();
                    boolean isNew = eo.has("new") && eo.get("new").getAsBoolean();
                    data.record(category, id, ts);
                    CollectionEntry e = data.get(category, id);
                    if (e != null && !isNew) {
                        e.setNew(false);
                    }
                }
            }
        } catch (IOException e) {
            CollectionBookMod.LOGGER.warn("Failed to load collection data for {}", uuid, e);
        }
        return data;
    }

    private static void save(UUID uuid) {
        PlayerCollectionData data = DATA.get(uuid);
        if (data == null) {
            return;
        }
        try {
            Files.createDirectories(BASE_DIR);
            JsonObject root = new JsonObject();
            Map<CollectionCategory, JsonArray> arrays = new HashMap<>();
            for (CollectionCategory category : CollectionCategory.values()) {
                JsonArray arr = new JsonArray();
                arrays.put(category, arr);
                root.add(category.key, arr);
            }
            for (CollectionEntry e : data.all()) {
                JsonObject eo = new JsonObject();
                eo.addProperty("id", e.getResourceId());
                eo.addProperty("ts", e.getFirstCollected());
                eo.addProperty("new", e.isNew());
            }
            Path file = BASE_DIR.resolve(uuid + ".json");
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            CollectionBookMod.LOGGER.warn("Failed to save collection data for {}", uuid, e);
        }
    }
}
