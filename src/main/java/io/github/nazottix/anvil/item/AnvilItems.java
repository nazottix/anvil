package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.item.respec.RespecItem;
import io.github.nazottix.anvil.item.respec.RespecType;
import io.github.nazottix.anvil.tool.PartType;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.HashMap;
import java.util.Map;

/**
 * ANVILアイテム登録クラス
 *
 * MODで追加する全てのアイテムをここで登録します。
 * DeferredRegisterを使用して、ゲーム起動時に自動的に登録されます。
 *
 * アイテムの追加方法:
 * 1. このクラスにDeferredItem定数を追加
 * 2. registerメソッド内でANVIL.ITEMSに登録
 * 3. 必要に応じてローカライズファイルに翻訳を追加
 */
public class AnvilItems {

    // ============================================
    // ANVILツールアイテム
    // ============================================

    /**
     * ANVILピッケル
     * 採掘用ツール。鉱石・石材の破壊でXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_PICKAXE = ANVIL.ITEMS.register(
            "anvil_pickaxe",
            () -> new AnvilToolItem(ToolType.PICKAXE, new Item.Properties())
    );

    /**
     * ANVIL斧
     * 伐採用ツール。木材系ブロックの破壊でXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_AXE = ANVIL.ITEMS.register(
            "anvil_axe",
            () -> new AnvilToolItem(ToolType.AXE, new Item.Properties())
    );

    /**
     * ANVILシャベル
     * 掘削用ツール。土・砂系ブロックの破壊でXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_SHOVEL = ANVIL.ITEMS.register(
            "anvil_shovel",
            () -> new AnvilToolItem(ToolType.SHOVEL, new Item.Properties())
    );

    /**
     * ANVIL剣
     * 近接戦闘用武器。モブへのダメージでXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_SWORD = ANVIL.ITEMS.register(
            "anvil_sword",
            () -> new AnvilToolItem(ToolType.SWORD, new Item.Properties())
    );

    /**
     * ANVILクワ
     * 農耕用ツール。作物の収穫でXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_HOE = ANVIL.ITEMS.register(
            "anvil_hoe",
            () -> new AnvilToolItem(ToolType.HOE, new Item.Properties())
    );

    /**
     * ANVIL弓
     * 遠距離戦闘用武器。矢によるモブキルでXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_BOW = ANVIL.ITEMS.register(
            "anvil_bow",
            () -> new AnvilToolItem(ToolType.BOW, new Item.Properties())
    );

    /**
     * ANVIL釣り竿
     * 釣り用ツール。魚・宝物の獲得でXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_FISHING_ROD = ANVIL.ITEMS.register(
            "anvil_fishing_rod",
            () -> new AnvilToolItem(ToolType.FISHING_ROD, new Item.Properties())
    );

    /**
     * ANVILハサミ
     * 刈り取り用ツール。羊毛・葉の収穫でXPを獲得。
     */
    public static final DeferredItem<AnvilToolItem> ANVIL_SHEARS = ANVIL.ITEMS.register(
            "anvil_shears",
            () -> new AnvilToolItem(ToolType.SHEARS, new Item.Properties())
    );

    // ============================================
    // ジュエルアイテム
    // ============================================

    /**
     * ジュエル
     * スキルツリーのジュエルソケットに装着可能なアイテム。
     * JewelItem.createJewelStack() でジュエルIDを設定して使用します。
     */
    public static final DeferredItem<JewelItem> JEWEL = ANVIL.ITEMS.register(
            "jewel",
            () -> new JewelItem(new Item.Properties())
    );

    // ============================================
    // リスペックアイテム
    // ============================================

    /**
     * 記憶の断片
     * 部分リスペック（10ポイント）を実行するアイテム。
     * ドロップまたはクラフトで入手可能。
     */
    public static final DeferredItem<RespecItem> MEMORY_SHARD = ANVIL.ITEMS.register(
            "memory_shard",
            () -> new RespecItem(RespecType.PARTIAL, new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    /**
     * 記憶の結晶
     * 完全リスペックを実行するアイテム。
     * レアドロップまたは高コストクラフトで入手可能。
     */
    public static final DeferredItem<RespecItem> MEMORY_CRYSTAL = ANVIL.ITEMS.register(
            "memory_crystal",
            () -> new RespecItem(RespecType.FULL, new Item.Properties().rarity(Rarity.RARE))
    );

    /**
     * 忘却のオーブ
     * キーストーンノードのみをリスペックするアイテム。
     * ボスドロップで入手可能。
     */
    public static final DeferredItem<RespecItem> OBLIVION_ORB = ANVIL.ITEMS.register(
            "oblivion_orb",
            () -> new RespecItem(RespecType.KEYSTONE_ONLY, new Item.Properties().rarity(Rarity.EPIC))
    );

    // ============================================
    // パーツアイテム
    // ツールを構成するパーツ（素材で色が変わる）
    // ============================================

    /**
     * ヘッドパーツ
     * ピッケル・斧・シャベル・クワのメイン部品
     */
    public static final DeferredItem<PartItem> PART_HEAD = ANVIL.ITEMS.register(
            "part_head",
            () -> new PartItem(PartType.HEAD, new Item.Properties())
    );

    /**
     * ハンドルパーツ
     * 全ツール共通の柄部品
     */
    public static final DeferredItem<PartItem> PART_HANDLE = ANVIL.ITEMS.register(
            "part_handle",
            () -> new PartItem(PartType.HANDLE, new Item.Properties())
    );

    /**
     * バインディングパーツ
     * 採掘ツールのヘッドとハンドルを繋ぐ部品
     */
    public static final DeferredItem<PartItem> PART_BINDING = ANVIL.ITEMS.register(
            "part_binding",
            () -> new PartItem(PartType.BINDING, new Item.Properties())
    );

    /**
     * ブレードパーツ
     * 剣・ハサミの刃部品
     */
    public static final DeferredItem<PartItem> PART_BLADE = ANVIL.ITEMS.register(
            "part_blade",
            () -> new PartItem(PartType.BLADE, new Item.Properties())
    );

    /**
     * ガードパーツ
     * 剣の鍔（つば）部品
     */
    public static final DeferredItem<PartItem> PART_GUARD = ANVIL.ITEMS.register(
            "part_guard",
            () -> new PartItem(PartType.GUARD, new Item.Properties())
    );

    /**
     * ボウリムパーツ
     * 弓の腕部品
     */
    public static final DeferredItem<PartItem> PART_BOW_LIMB = ANVIL.ITEMS.register(
            "part_bow_limb",
            () -> new PartItem(PartType.BOW_LIMB, new Item.Properties())
    );

    /**
     * ボウストリングパーツ
     * 弓の弦部品
     */
    public static final DeferredItem<PartItem> PART_BOWSTRING = ANVIL.ITEMS.register(
            "part_bowstring",
            () -> new PartItem(PartType.BOWSTRING, new Item.Properties())
    );

    /**
     * ロッドパーツ
     * 釣り竿の竿部品
     */
    public static final DeferredItem<PartItem> PART_ROD = ANVIL.ITEMS.register(
            "part_rod",
            () -> new PartItem(PartType.ROD, new Item.Properties())
    );

    /**
     * フックパーツ
     * 釣り竿の針部品
     */
    public static final DeferredItem<PartItem> PART_HOOK = ANVIL.ITEMS.register(
            "part_hook",
            () -> new PartItem(PartType.HOOK, new Item.Properties())
    );

    /**
     * ラインパーツ
     * 釣り竿の糸部品
     */
    public static final DeferredItem<PartItem> PART_LINE = ANVIL.ITEMS.register(
            "part_line",
            () -> new PartItem(PartType.LINE, new Item.Properties())
    );

    /**
     * ピボットパーツ
     * ハサミの軸部品
     */
    public static final DeferredItem<PartItem> PART_PIVOT = ANVIL.ITEMS.register(
            "part_pivot",
            () -> new PartItem(PartType.PIVOT, new Item.Properties())
    );

    // ============================================
    // 削除済み: 加工素材アイテム（素材加工の簡略化のため）
    // - ProcessedMaterial
    // - SmithingHammer
    // - PolishingAgent (Basic, Fine, Superior, Perfect)
    // ============================================

    // パーツタイプからアイテムへのマッピング
    private static final Map<PartType, DeferredItem<PartItem>> PART_ITEM_MAP = new HashMap<>();

    // 静的初期化ブロックでマッピングを設定
    static {
        PART_ITEM_MAP.put(PartType.HEAD, PART_HEAD);
        PART_ITEM_MAP.put(PartType.HANDLE, PART_HANDLE);
        PART_ITEM_MAP.put(PartType.BINDING, PART_BINDING);
        PART_ITEM_MAP.put(PartType.BLADE, PART_BLADE);
        PART_ITEM_MAP.put(PartType.GUARD, PART_GUARD);
        PART_ITEM_MAP.put(PartType.BOW_LIMB, PART_BOW_LIMB);
        PART_ITEM_MAP.put(PartType.BOWSTRING, PART_BOWSTRING);
        PART_ITEM_MAP.put(PartType.ROD, PART_ROD);
        PART_ITEM_MAP.put(PartType.HOOK, PART_HOOK);
        PART_ITEM_MAP.put(PartType.LINE, PART_LINE);
        PART_ITEM_MAP.put(PartType.PIVOT, PART_PIVOT);
    }

    /**
     * パーツタイプからパーツアイテムを取得
     *
     * @param partType パーツタイプ
     * @return 対応するパーツアイテム、見つからない場合はnull
     */
    public static Item getPartItem(PartType partType) {
        DeferredItem<PartItem> holder = PART_ITEM_MAP.get(partType);
        return holder != null ? holder.get() : null;
    }

    /**
     * アイテムを初期化
     *
     * このメソッドはクラスロード時に呼び出され、
     * 全てのstaticフィールドが初期化されることを保証します。
     * ANVIL.javaのコンストラクタから呼び出してください。
     */
    public static void init() {
        ANVIL.LOGGER.info("ANVIL: アイテムを初期化");
    }
}
