package io.github.nazottix.anvil.network;

import io.github.nazottix.anvil.ANVIL;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * ANVILネットワーク登録クラス
 *
 * MODで使用するネットワークパケットを登録します。
 */
public class AnvilNetworking {

    /**
     * ネットワークパケットを登録
     *
     * @param modEventBus MODイベントバス
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AnvilNetworking::registerPayloadHandlers);
    }

    /**
     * ペイロードハンドラ登録イベント
     */
    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ANVIL.MODID);

        // パーツ鍛造所アクションパケット（クライアント→サーバー）
        registrar.playToServer(
                PartForgeActionPacket.TYPE,
                PartForgeActionPacket.STREAM_CODEC,
                PartForgeActionPacket::handle
        );

        ANVIL.LOGGER.info("ANVIL: ネットワークパケットを登録");
    }
}
