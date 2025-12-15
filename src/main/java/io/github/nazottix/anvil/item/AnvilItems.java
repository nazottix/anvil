package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

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
