package io.github.nazottix.anvil;

import io.github.nazottix.anvil.client.screen.CoreBoxStationScreen;
import io.github.nazottix.anvil.client.screen.GridCustomizationScreen;
import io.github.nazottix.anvil.client.screen.JewelStationScreen;
import io.github.nazottix.anvil.client.screen.ModCustomizationScreen;
import io.github.nazottix.anvil.client.screen.ModStationScreen;
import io.github.nazottix.anvil.client.screen.RespecStationScreen;
import io.github.nazottix.anvil.client.screen.SkillTreeScreen;
import io.github.nazottix.anvil.client.screen.SkillTreeStationScreen;
import io.github.nazottix.anvil.client.screen.ToolStationScreen;
import io.github.nazottix.anvil.menu.AnvilMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * ANVILクライアント専用クラス
 *
 * このクラスは専用サーバーでは読み込まれません。
 * クライアント専用のコード（UI、レンダリングなど）はここに記述します。
 */
@Mod(value = ANVIL.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ANVIL.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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
    }

    /**
     * メニュースクリーン登録イベント
     *
     * メニュータイプとスクリーンの関連付けを行います。
     */
    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        // ツールステーションスクリーンを登録
        event.register(AnvilMenuTypes.TOOL_STATION.get(), ToolStationScreen::new);

        // MODカスタマイズスクリーンを登録
        event.register(AnvilMenuTypes.MOD_CUSTOMIZATION.get(), ModCustomizationScreen::new);

        // グリッドカスタマイズスクリーンを登録
        event.register(AnvilMenuTypes.GRID_CUSTOMIZATION.get(), GridCustomizationScreen::new);

        // スキルツリースクリーンを登録
        event.register(AnvilMenuTypes.SKILL_TREE.get(), SkillTreeScreen::new);

        // ステーションスクリーンを登録（5種）
        event.register(AnvilMenuTypes.SKILL_TREE_STATION.get(), SkillTreeStationScreen::new);
        event.register(AnvilMenuTypes.JEWEL_STATION.get(), JewelStationScreen::new);
        event.register(AnvilMenuTypes.RESPEC_STATION.get(), RespecStationScreen::new);
        event.register(AnvilMenuTypes.CORE_BOX_STATION.get(), CoreBoxStationScreen::new);
        event.register(AnvilMenuTypes.MOD_STATION.get(), ModStationScreen::new);

        ANVIL.LOGGER.info("ANVIL: メニュースクリーンを登録（9種）");
    }
}
