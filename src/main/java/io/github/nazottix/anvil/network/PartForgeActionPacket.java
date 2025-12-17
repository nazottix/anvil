package io.github.nazottix.anvil.network;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.menu.PartForgeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * パーツ鍛造所アクションパケット
 *
 * クライアントからサーバーへ送信されるパケットです。
 * パーツタイプの選択と鋳造実行を処理します。
 */
public record PartForgeActionPacket(int action, int data) implements CustomPacketPayload {

    // アクションタイプ
    public static final int ACTION_SELECT = 0;  // パーツタイプ選択
    public static final int ACTION_FORGE = 1;   // 鋳造実行

    // パケットID
    public static final Type<PartForgeActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "part_forge_action"));

    // ストリームコーデック
    public static final StreamCodec<FriendlyByteBuf, PartForgeActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, PartForgeActionPacket::action,
                    ByteBufCodecs.INT, PartForgeActionPacket::data,
                    PartForgeActionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * サーバー側でパケットを処理
     */
    public static void handle(PartForgeActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                // プレイヤーが開いているメニューがPartForgeMenuかチェック
                if (serverPlayer.containerMenu instanceof PartForgeMenu menu) {
                    switch (packet.action()) {
                        case ACTION_SELECT -> {
                            // パーツタイプを選択
                            menu.selectPartType(packet.data());
                            ANVIL.LOGGER.debug("パーツタイプ選択: index={}", packet.data());
                        }
                        case ACTION_FORGE -> {
                            // 鋳造実行
                            boolean success = menu.forge();
                            ANVIL.LOGGER.debug("鋳造実行: success={}", success);
                        }
                    }
                }
            }
        });
    }
}
