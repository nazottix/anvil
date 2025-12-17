package io.github.nazottix.anvil.block;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * ANVILブロック登録クラス
 *
 * すべてのANVILブロックをここで登録します。
 * NeoForgeのDeferredRegisterパターンを使用して遅延登録を行います。
 */
public class AnvilBlocks {

    // ============================================
    // ブロック登録
    // ============================================

    /**
     * ツールステーションブロック
     *
     * ANVILツールの作成・カスタマイズに使用するワークベンチです。
     * プレイヤーはこのブロックでパーツを組み合わせてツールを作成できます。
     */
    public static final DeferredBlock<ToolStationBlock> TOOL_STATION = ANVIL.BLOCKS.register(
            "tool_station",
            () -> new ToolStationBlock(BlockBehaviour.Properties.of()
                    // 木材のような硬さと爆発耐性
                    .strength(2.5f, 2.5f)
                    // ハサミ/斧で素早く破壊
                    .requiresCorrectToolForDrops()
                    // 光を完全に遮断しない
                    .noOcclusion()
            )
    );

    /**
     * スキルツリーステーションブロック
     *
     * ANVILツールのスキルポイント割り振りに使用するワークベンチです。
     * プレイヤーはこのブロックでスキルツリーを閲覧・スキルを取得できます。
     */
    public static final DeferredBlock<SkillTreeStationBlock> SKILL_TREE_STATION = ANVIL.BLOCKS.register(
            "skill_tree_station",
            () -> new SkillTreeStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    /**
     * ジュエルステーションブロック
     *
     * ANVILツールへのジュエル装着に使用するワークベンチです。
     * プレイヤーはこのブロックでジュエルの装着・取り外しができます。
     */
    public static final DeferredBlock<JewelStationBlock> JEWEL_STATION = ANVIL.BLOCKS.register(
            "jewel_station",
            () -> new JewelStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    /**
     * リスペックステーションブロック
     *
     * ANVILツールのスキルリセットに使用するワークベンチです。
     * プレイヤーはこのブロックでリスペックアイテムを使用してスキルをリセットできます。
     */
    public static final DeferredBlock<RespecStationBlock> RESPEC_STATION = ANVIL.BLOCKS.register(
            "respec_station",
            () -> new RespecStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    /**
     * コアボックスステーションブロック
     *
     * ANVILツールのグリッドモジュール配置に使用するワークベンチです。
     * プレイヤーはこのブロックでテトリス風のモジュール配置ができます。
     */
    public static final DeferredBlock<CoreBoxStationBlock> CORE_BOX_STATION = ANVIL.BLOCKS.register(
            "core_box_station",
            () -> new CoreBoxStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    /**
     * MODステーションブロック
     *
     * ANVILツールへのMOD装着に使用するワークベンチです。
     * プレイヤーはこのブロックでMODの装着・管理ができます。
     */
    public static final DeferredBlock<ModStationBlock> MOD_STATION = ANVIL.BLOCKS.register(
            "mod_station",
            () -> new ModStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    /**
     * リペアステーションブロック
     *
     * ANVILツールの修理に使用するワークベンチです。
     * プレイヤーはこのブロックでパーツを消費してツールを修理できます。
     */
    public static final DeferredBlock<RepairStationBlock> REPAIR_STATION = ANVIL.BLOCKS.register(
            "repair_station",
            () -> new RepairStationBlock(BlockBehaviour.Properties.of()
                    .strength(2.5f, 2.5f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    /**
     * 精錬所ブロック
     *
     * バニラ素材を加工素材（ProcessedMaterial）に変換するステーションです。
     * 燃料を消費して時間経過で素材を精錬します。
     * 加工チェーンの最初のステップ: バニラ素材 → 精錬素材(REFINED)
     */
    public static final DeferredBlock<RefineryBlock> REFINERY = ANVIL.BLOCKS.register(
            "refinery",
            () -> new RefineryBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)  // 金属製でやや硬め
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(RefineryBlock.LIT) ? 13 : 0)  // 稼働中は光を発する
            )
    );

    /**
     * パーツ鍛造所ブロック
     *
     * 加工素材をパーツアイテムに変換するステーションです。
     * プレイヤーがパーツタイプを選択して鋳造を行います。
     * 素材のグレードがパーツに継承されます。
     */
    public static final DeferredBlock<PartForgeBlock> PART_FORGE = ANVIL.BLOCKS.register(
            "part_forge",
            () -> new PartForgeBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)  // 金属製でやや硬め
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    /**
     * 鍛造ステーションブロック
     *
     * 精錬素材(REFINED)を鍛造素材(FORGED)に変換するステーションです。
     * ハンマーを消費して時間経過で自動処理します。
     * 加工チェーンの2番目のステップ: 精錬素材 → 鍛造素材
     */
    public static final DeferredBlock<ForgingStationBlock> FORGING_STATION = ANVIL.BLOCKS.register(
            "forging_station",
            () -> new ForgingStationBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)  // 金属製でやや硬め
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(ForgingStationBlock.LIT) ? 13 : 0)  // 稼働中は光を発する
            )
    );

    /**
     * 研磨ステーションブロック
     *
     * 鍛造素材(FORGED)を研磨素材(POLISHED)に変換するステーションです。
     * 研磨剤を消費して品質ボーナスを付与します。
     * 加工チェーンの3番目のステップ: 鍛造素材 → 研磨素材
     */
    public static final DeferredBlock<PolishingStationBlock> POLISHING_STATION = ANVIL.BLOCKS.register(
            "polishing_station",
            () -> new PolishingStationBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 3.5f)  // 金属製でやや硬め
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(PolishingStationBlock.LIT) ? 10 : 0)  // 稼働中は輝く
            )
    );

    // ============================================
    // アイテム登録（ブロックアイテム）
    // ============================================

    /**
     * ツールステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> TOOL_STATION_ITEM = ANVIL.ITEMS.register(
            "tool_station",
            () -> new BlockItem(TOOL_STATION.get(), new Item.Properties())
    );

    /**
     * スキルツリーステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> SKILL_TREE_STATION_ITEM = ANVIL.ITEMS.register(
            "skill_tree_station",
            () -> new BlockItem(SKILL_TREE_STATION.get(), new Item.Properties())
    );

    /**
     * ジュエルステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> JEWEL_STATION_ITEM = ANVIL.ITEMS.register(
            "jewel_station",
            () -> new BlockItem(JEWEL_STATION.get(), new Item.Properties())
    );

    /**
     * リスペックステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> RESPEC_STATION_ITEM = ANVIL.ITEMS.register(
            "respec_station",
            () -> new BlockItem(RESPEC_STATION.get(), new Item.Properties())
    );

    /**
     * コアボックスステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> CORE_BOX_STATION_ITEM = ANVIL.ITEMS.register(
            "core_box_station",
            () -> new BlockItem(CORE_BOX_STATION.get(), new Item.Properties())
    );

    /**
     * MODステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> MOD_STATION_ITEM = ANVIL.ITEMS.register(
            "mod_station",
            () -> new BlockItem(MOD_STATION.get(), new Item.Properties())
    );

    /**
     * リペアステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> REPAIR_STATION_ITEM = ANVIL.ITEMS.register(
            "repair_station",
            () -> new BlockItem(REPAIR_STATION.get(), new Item.Properties())
    );

    /**
     * 精錬所のブロックアイテム
     */
    public static final DeferredItem<BlockItem> REFINERY_ITEM = ANVIL.ITEMS.register(
            "refinery",
            () -> new BlockItem(REFINERY.get(), new Item.Properties())
    );

    /**
     * パーツ鍛造所のブロックアイテム
     */
    public static final DeferredItem<BlockItem> PART_FORGE_ITEM = ANVIL.ITEMS.register(
            "part_forge",
            () -> new BlockItem(PART_FORGE.get(), new Item.Properties())
    );

    /**
     * 鍛造ステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> FORGING_STATION_ITEM = ANVIL.ITEMS.register(
            "forging_station",
            () -> new BlockItem(FORGING_STATION.get(), new Item.Properties())
    );

    /**
     * 研磨ステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> POLISHING_STATION_ITEM = ANVIL.ITEMS.register(
            "polishing_station",
            () -> new BlockItem(POLISHING_STATION.get(), new Item.Properties())
    );

    // ============================================
    // 初期化
    // ============================================

    /**
     * ブロック登録を初期化
     *
     * このメソッドを呼び出すことで、静的フィールドが確実に初期化されます。
     * ANVILクラスのコンストラクタから呼び出されます。
     */
    public static void init() {
        ANVIL.LOGGER.info("ANVIL: ブロックを初期化");
    }

    /**
     * ブロックをイベントバスに登録
     *
     * @param modEventBus MODイベントバス
     */
    public static void register(IEventBus modEventBus) {
        // ブロックとアイテムはANVIL.javaで一括登録されるため、
        // ここでは追加の登録処理は不要
    }
}
