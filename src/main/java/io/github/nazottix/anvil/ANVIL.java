package io.github.nazottix.anvil;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.github.nazottix.anvil.block.AnvilBlocks;
import io.github.nazottix.anvil.block.entity.AnvilBlockEntities;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.item.PartItem;
import io.github.nazottix.anvil.loot.AnvilLootModifiers;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.PartType;
import io.github.nazottix.anvil.menu.AnvilMenuTypes;
import io.github.nazottix.anvil.skill.SkillTreeRegistry;
import io.github.nazottix.anvil.skill.jewel.JewelRegistry;
import io.github.nazottix.anvil.trait.TraitRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * ANVIL (Advanced Nexus of Variable Implement Leverage) メインクラス
 *
 * Minecraft 1.21.1向けのNeoForge modで、PvEツールのカスタマイズに焦点を当てています。
 * Tinkers' ConstructやPath of Exile等に触発された深いツールクラフティングと
 * レベリングシステムを提供します。
 */
@Mod(ANVIL.MODID)
public class ANVIL {
    // MOD ID - neoforge.mods.tomlのエントリと一致する必要があります
    public static final String MODID = "anvil";

    // ロガー - デバッグやエラー出力に使用
    public static final Logger LOGGER = LogUtils.getLogger();

    // ブロック登録用のDeferredRegister
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // アイテム登録用のDeferredRegister
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // クリエイティブタブ登録用のDeferredRegister
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // ANVILクリエイティブタブ - MODのアイテムを表示するタブ
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANVIL_TAB =
            CREATIVE_MODE_TABS.register("anvil_tab", () -> CreativeModeTab.builder()
                    // タブのタイトル（ローカライズキー）
                    .title(Component.translatable("itemGroup.anvil"))
                    // 戦闘タブの後に配置
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    // タブアイコン（ANVILピッケルを使用）
                    .icon(() -> AnvilItems.ANVIL_PICKAXE.get().getDefaultInstance())
                    // タブに表示するアイテム（ブロックとツール）
                    .displayItems((parameters, output) -> {
                        // ステーションブロック（8種）
                        output.accept(AnvilBlocks.TOOL_STATION_ITEM.get());
                        output.accept(AnvilBlocks.SKILL_TREE_STATION_ITEM.get());
                        output.accept(AnvilBlocks.JEWEL_STATION_ITEM.get());
                        output.accept(AnvilBlocks.RESPEC_STATION_ITEM.get());
                        output.accept(AnvilBlocks.CORE_BOX_STATION_ITEM.get());
                        output.accept(AnvilBlocks.MOD_STATION_ITEM.get());
                        output.accept(AnvilBlocks.REPAIR_STATION_ITEM.get());
                        output.accept(AnvilBlocks.GRADE_STATION_ITEM.get());

                        // パーツ鍛造ステーション（素材加工の簡略化により1種に統合）
                        output.accept(AnvilBlocks.PART_FORGE_ITEM.get());

                        // 全ANVILツールをタブに追加
                        output.accept(AnvilItems.ANVIL_PICKAXE.get());
                        output.accept(AnvilItems.ANVIL_AXE.get());
                        output.accept(AnvilItems.ANVIL_SHOVEL.get());
                        output.accept(AnvilItems.ANVIL_SWORD.get());
                        output.accept(AnvilItems.ANVIL_HOE.get());
                        output.accept(AnvilItems.ANVIL_BOW.get());
                        output.accept(AnvilItems.ANVIL_FISHING_ROD.get());
                        output.accept(AnvilItems.ANVIL_SHEARS.get());

                        // リスペックアイテム
                        output.accept(AnvilItems.MEMORY_SHARD.get());
                        output.accept(AnvilItems.MEMORY_CRYSTAL.get());
                        output.accept(AnvilItems.OBLIVION_ORB.get());

                        // グレードアップグレードアイテム
                        output.accept(AnvilItems.GRADE_ESSENCE.get());
                    })
                    .build());

    // ANVILパーツクリエイティブタブ - 全パーツ×素材の組み合わせを表示
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANVIL_PARTS_TAB =
            CREATIVE_MODE_TABS.register("anvil_parts_tab", () -> CreativeModeTab.builder()
                    // タブのタイトル（ローカライズキー）
                    .title(Component.translatable("itemGroup.anvil_parts"))
                    // メインANVILタブの後に配置
                    .withTabsBefore(ANVIL_TAB.getKey())
                    // タブアイコン（パーツヘッドを使用）
                    .icon(() -> AnvilItems.PART_HEAD.get().getDefaultInstance())
                    // タブに表示するアイテム（全パーツ×素材の組み合わせ）
                    .displayItems((parameters, output) -> {
                        // 素材レジストリが初期化されていない場合は空のタブ
                        if (!MaterialRegistry.getInstance().isInitialized()) {
                            return;
                        }

                        // コアパーツタイプのみを使用（COATING, UPGRADEを除外）
                        PartType[] corePartTypes = {
                                PartType.HEAD,
                                PartType.HANDLE,
                                PartType.BINDING,
                                PartType.BLADE,
                                PartType.GUARD,
                                PartType.BOW_LIMB,
                                PartType.BOWSTRING,
                                PartType.ROD,
                                PartType.HOOK,
                                PartType.LINE,
                                PartType.PIVOT
                        };

                        // 各パーツタイプごとに全素材の組み合わせを追加
                        for (PartType partType : corePartTypes) {
                            // 素材をレアリティ順（低→高）でソート
                            for (Material material : MaterialRegistry.getInstance().getSortedByRarity(false)) {
                                // グレードCのパーツを作成
                                output.accept(PartItem.createPartStack(
                                        partType,
                                        material.getId().toString(),
                                        Grade.C
                                ));
                            }
                        }
                    })
                    .build());

    /**
     * MODコンストラクタ - MODがロードされたときに最初に実行されるコード
     *
     * @param modEventBus MODイベントバス（登録やライフサイクルイベント用）
     * @param modContainer MODコンテナ（設定登録用）
     */
    public ANVIL(IEventBus modEventBus, ModContainer modContainer) {
        // 共通セットアップイベントを登録
        modEventBus.addListener(this::commonSetup);

        // 各クラスを初期化（staticフィールドの初期化を確実に行う）
        AnvilItems.init();
        AnvilBlocks.init();

        // 各DeferredRegisterをMODイベントバスに登録
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // ブロックエンティティを登録
        AnvilBlockEntities.register(modEventBus);

        // メニュータイプを登録
        AnvilMenuTypes.register(modEventBus);

        // Data Componentsを登録（ツールデータ保存用）
        AnvilDataComponents.register(modEventBus);

        // Loot Modifiersを登録（ジュエルドロップ用）
        AnvilLootModifiers.register(modEventBus);

        // ネットワークパケットを登録（パーツ鍛造所など）
        io.github.nazottix.anvil.network.AnvilNetworking.register(modEventBus);

        // ゲームイベント（サーバー起動など）を受け取るために登録
        NeoForge.EVENT_BUS.register(this);

        // 設定ファイルを登録
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * 共通セットアップ - クライアント/サーバー両方で実行される初期化処理
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ANVIL: 共通セットアップを開始");
        LOGGER.info("ANVIL: 最大レベル = {}", Config.MAX_LEVEL.get());
        LOGGER.info("ANVIL: XP倍率 = {}", Config.XP_MULTIPLIER.get());

        // 素材レジストリを初期化（初期30種の素材を登録）
        MaterialRegistry.getInstance().initialize();

        // 特性レジストリを初期化（初期30種の特性を登録）
        TraitRegistry.getInstance().initialize();

        // スキルツリーレジストリを初期化（ピッケル・剣のツリーを登録）
        SkillTreeRegistry.init();

        // ジュエルレジストリを初期化（スキルツリー用ジュエルを登録）
        JewelRegistry.init();

        // アフィックスレジストリを初期化（ツールのアフィックス生成用）
        io.github.nazottix.anvil.affix.AffixRegistry.initialize();
    }

    /**
     * サーバー起動イベント - サーバーが起動したときに実行
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ANVIL: サーバー起動");
    }
}
