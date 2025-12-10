package io.github.nazottix.anvil;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * ANVILクライアント専用クラス
 *
 * このクラスは専用サーバーでは読み込まれません。
 * クライアント専用のコード（UI、レンダリングなど）はここに記述します。
 */
@Mod(value = ANVIL.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ANVIL.MODID, value = Dist.CLIENT)
public class ANVILClient {

    /**
     * クライアント専用コンストラクタ
     *
     * @param container MODコンテナ
     */
    public ANVILClient(ModContainer container) {
        // NeoForgeの設定画面を有効化
        // Mods画面 > ANVILを選択 > 設定ボタンでアクセス可能
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * クライアントセットアップイベント - クライアント専用の初期化処理
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ANVIL.LOGGER.info("ANVIL: クライアントセットアップ完了");
        // TODO: Phase 2以降でカスタムUI登録などを追加
    }
}
