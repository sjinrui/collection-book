package dev.sjinrui.collectionbook.common.network;

import dev.architectury.networking.NetworkManager;
import dev.sjinrui.collectionbook.common.client.CollectionClientData;
import dev.sjinrui.collectionbook.common.data.CollectionEntry;
import dev.sjinrui.collectionbook.common.category.CollectionCategory;
import dev.sjinrui.collectionbook.common.server.CollectionServerData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 公共网络通道：服务端 -> 客户端下发收集快照；客户端 -> 服务端确认新收集。
 * 使用 Architectury 提供的跨加载器网络 API，common 不接触加载器专有网络类。
 */
public final class CollectionNetwork {
    private CollectionNetwork() {}

    public static void register() {
        // 服务端 -> 客户端：覆盖式快照
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CollectionServerData.CHANNEL, (buf, context) -> {
            int count = buf.readVarInt();
            List<CollectionEntry> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                CollectionCategory category = CollectionCategory.byOrdinal(buf.readVarInt());
                String id = buf.readUtf();
                long ts = buf.readLong();
                boolean isNew = buf.readBoolean();
                list.add(new CollectionEntry(category, id, ts, isNew));
            }
            CollectionClientData.update(list);
        });

        // 客户端 -> 服务端：确认全部新收集（包体为空）
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, CollectionServerData.CHANNEL, (buf, context) -> {
            Player player = context.getPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                context.queue(() -> CollectionServerData.ackAll(serverPlayer));
            }
        });
    }

    /** 客户端发送“确认新收集”。 */
    public static void sendAckToServer() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        NetworkManager.sendToServer(CollectionServerData.CHANNEL, buf);
    }
}
