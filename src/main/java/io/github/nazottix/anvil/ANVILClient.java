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
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.item.PartItem;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.menu.AnvilMenuTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Optional;

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

    /**
     * アイテムカラーハンドラー登録イベント
     *
     * パーツアイテムの色を素材に応じてティントするために使用します。
     */
    @SubscribeEvent
    static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // パーツアイテムのカラーハンドラーを登録
        // 素材のprimaryColorでティントする
        event.register((stack, tintIndex) -> {
            // layer0のみをティント（tintIndex == 0）
            if (tintIndex != 0) {
                return 0xFFFFFF;
            }

            // パーツデータを取得
            ToolPart partData = PartItem.getPartData(stack);
            if (partData == null || partData.materialId() == null || partData.materialId().isEmpty()) {
                // デフォルト色（白 = ティントなし）
                return 0xFFFFFF;
            }

            // 素材の色を取得
            // MaterialRegistryのインスタンスから素材を取得
            Optional<Material> materialOpt = MaterialRegistry.getInstance().get(ResourceLocation.parse(partData.materialId()));
            if (materialOpt.isPresent()) {
                return materialOpt.get().getPrimaryColor();
            }

            // 素材が見つからない場合はデフォルト色
            return 0xFFFFFF;
        },
        // 全パーツアイテムに適用
        AnvilItems.PART_HEAD.get(),
        AnvilItems.PART_HANDLE.get(),
        AnvilItems.PART_BINDING.get(),
        AnvilItems.PART_BLADE.get(),
        AnvilItems.PART_GUARD.get(),
        AnvilItems.PART_BOW_LIMB.get(),
        AnvilItems.PART_BOWSTRING.get(),
        AnvilItems.PART_ROD.get(),
        AnvilItems.PART_HOOK.get(),
        AnvilItems.PART_LINE.get(),
        AnvilItems.PART_PIVOT.get()
        );

        ANVIL.LOGGER.info("ANVIL: パーツアイテムカラーハンドラーを登録（11種）");
    }
}
