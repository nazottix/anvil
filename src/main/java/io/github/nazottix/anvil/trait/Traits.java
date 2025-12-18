package io.github.nazottix.anvil.trait;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.resources.ResourceLocation;

/**
 * 初期特性定義
 *
 * Phase 1で実装する初期30種の特性を定義します。
 *
 * 特性構成:
 * - 戦闘系: 8種
 * - 採掘系: 7種
 * - 防御系: 4種
 * - ユーティリティ: 8種
 * - 環境適応: 3種
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.6.2 特性詳細一覧
 */
public final class Traits {

    // プライベートコンストラクタ（インスタンス化禁止）
    private Traits() {
    }

    // ============================================
    // 戦闘系特性（8種）
    // ============================================

    /**
     * 吸血 - ダメージの一部をHPとして回復
     * レベルスケーリング: +2%/Lv
     */
    public static final Trait LIFESTEAL = Trait.builder(loc("lifesteal"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ON_ATTACK)
            .maxLevel(5)
            .effectPerLevel(2.0f)  // 2%/Lv
            .effectUnit("%")
            .color(0xCC0000)  // 深紅
            .rarity(3)
            .build();

    /**
     * 神聖 - アンデッドに追加ダメージ
     * レベルスケーリング: +15%/Lv
     */
    public static final Trait HOLY = Trait.builder(loc("holy"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ON_UNDEAD_ATTACK)
            .maxLevel(5)
            .effectPerLevel(15.0f)  // 15%/Lv
            .effectUnit("%")
            .color(0xFFFF99)  // 淡黄色
            .rarity(2)
            .build();

    /**
     * 炎熱 - 火属性ダメージ付与
     * レベルスケーリング: +1ダメージ/Lv
     */
    public static final Trait FIERY = Trait.builder(loc("fiery"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ON_ATTACK)
            .maxLevel(5)
            .effectPerLevel(1.0f)  // +1ダメージ/Lv
            .effectUnit("ダメージ")
            .color(0xFF6600)  // オレンジ
            .rarity(2)
            .build();

    /**
     * 氷結 - スロー効果付与
     * レベルスケーリング: +1秒/Lv
     */
    public static final Trait FREEZING = Trait.builder(loc("freezing"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ON_ATTACK)
            .maxLevel(5)
            .effectPerLevel(1.0f)  // +1秒/Lv
            .effectUnit("秒")
            .color(0x99CCFF)  // 水色
            .rarity(2)
            .incompatibleWith(loc("fiery"))  // 炎熱と相互排他
            .build();

    /**
     * 電撃 - 周囲に連鎖ダメージ
     * レベルスケーリング: 範囲+0.5ブロック/Lv
     */
    public static final Trait SHOCKING = Trait.builder(loc("shocking"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ON_ATTACK)
            .maxLevel(5)
            .effectPerLevel(0.5f)  // +0.5ブロック/Lv
            .effectUnit("ブロック")
            .color(0xFFFF00)  // 黄色
            .rarity(3)
            .build();

    /**
     * 猛毒 - 毒効果付与
     * レベルスケーリング: +2ダメージ/Lv
     */
    public static final Trait VENOMOUS = Trait.builder(loc("venomous"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ON_ATTACK)
            .maxLevel(5)
            .effectPerLevel(2.0f)  // +2ダメージ/Lv
            .effectUnit("ダメージ")
            .color(0x00CC00)  // 緑
            .rarity(2)
            .build();

    /**
     * 荒削り - 損傷時攻撃UP、速度DOWN
     * レベルスケーリング: ±2%/損傷%
     */
    public static final Trait JAGGED = Trait.builder(loc("jagged"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(3)
            .effectPerLevel(2.0f)  // ±2%
            .effectUnit("%")
            .color(0x666666)  // 灰色
            .rarity(1)
            .incompatibleWith(loc("stonebound"))  // 石縛りと相互排他
            .build();

    /**
     * クリティカル - クリティカル率上昇
     * レベルスケーリング: +5%/Lv
     */
    public static final Trait CRITICAL = Trait.builder(loc("critical"))
            .category(TraitCategory.COMBAT)
            .trigger(TraitTrigger.ON_ATTACK)
            .maxLevel(5)
            .effectPerLevel(5.0f)  // +5%/Lv
            .effectUnit("%")
            .color(0xFF3300)  // 赤橙
            .rarity(3)
            .build();

    // ============================================
    // 採掘系特性（7種）
    // ============================================

    /**
     * 自動精錬 - ドロップを精錬
     * レベルスケーリング: 確率+20%/Lv
     */
    public static final Trait AUTO_SMELT = Trait.builder(loc("auto_smelt"))
            .category(TraitCategory.MINING)
            .trigger(TraitTrigger.ON_BLOCK_BREAK)
            .maxLevel(5)
            .effectPerLevel(20.0f)  // +20%/Lv
            .effectUnit("%")
            .color(0xFF9900)  // 橙
            .rarity(3)
            .build();

    /**
     * 勢い - 連続使用で速度UP
     * レベルスケーリング: +5%/連続使用
     */
    public static final Trait MOMENTUM = Trait.builder(loc("momentum"))
            .category(TraitCategory.MINING)
            .trigger(TraitTrigger.ON_CONSECUTIVE_USE)
            .maxLevel(5)
            .effectPerLevel(5.0f)  // +5%/Lv
            .effectUnit("%")
            .color(0x0099FF)  // 青
            .rarity(2)
            .build();

    /**
     * 石縛り - 損傷時速度UP、攻撃DOWN
     * レベルスケーリング: ±3%/損傷%
     */
    public static final Trait STONEBOUND = Trait.builder(loc("stonebound"))
            .category(TraitCategory.MINING)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(3)
            .effectPerLevel(3.0f)  // ±3%
            .effectUnit("%")
            .color(0x999999)  // 灰
            .rarity(1)
            .incompatibleWith(loc("jagged"))  // 荒削りと相互排他
            .build();

    /**
     * 霊体 - 透明ブロック無視（ガラス等を素早く破壊）
     * レベルスケーリング: 確率+10%/Lv
     */
    public static final Trait ETHEREAL = Trait.builder(loc("ethereal"))
            .category(TraitCategory.MINING)
            .trigger(TraitTrigger.ON_BLOCK_BREAK)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // +10%/Lv
            .effectUnit("%")
            .color(0xCCCCFF)  // 淡紫
            .rarity(3)
            .build();

    /**
     * 幸運 - ドロップ増加
     * レベルスケーリング: 幸運+0.5/Lv
     */
    public static final Trait LUCKY = Trait.builder(loc("lucky"))
            .category(TraitCategory.MINING)
            .trigger(TraitTrigger.ON_BLOCK_BREAK)
            .maxLevel(5)
            .effectPerLevel(0.5f)  // +0.5/Lv
            .effectUnit("")
            .color(0x00FF00)  // 緑
            .rarity(4)
            .build();

    /**
     * 磁力 - アイテム自動吸引
     * レベルスケーリング: 範囲+1ブロック/Lv
     */
    public static final Trait MAGNETIC = Trait.builder(loc("magnetic"))
            .category(TraitCategory.MINING)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(5)
            .effectPerLevel(1.0f)  // +1ブロック/Lv
            .effectUnit("ブロック")
            .color(0x666699)  // 青灰色
            .rarity(2)
            .build();

    /**
     * スライム - スライムスポーン確率
     * レベルスケーリング: +5%/Lv
     */
    public static final Trait SLIMY = Trait.builder(loc("slimy"))
            .category(TraitCategory.MINING)
            .trigger(TraitTrigger.ON_BLOCK_BREAK)
            .maxLevel(3)
            .effectPerLevel(5.0f)  // +5%/Lv
            .effectUnit("%")
            .color(0x66CC66)  // 黄緑
            .rarity(1)
            .build();

    // ============================================
    // 防御系特性（4種）
    // ============================================

    /**
     * 強化 - 耐久消費無視確率
     * レベルスケーリング: +10%/Lv
     */
    public static final Trait REINFORCED = Trait.builder(loc("reinforced"))
            .category(TraitCategory.DEFENSE)
            .trigger(TraitTrigger.ON_DURABILITY_USE)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // +10%/Lv
            .effectUnit("%")
            .color(0x9966CC)  // 紫
            .rarity(2)
            .build();

    /**
     * 耐久 - 耐久消費軽減
     * レベルスケーリング: -10%/Lv
     */
    public static final Trait UNBREAKING = Trait.builder(loc("unbreaking"))
            .category(TraitCategory.DEFENSE)
            .trigger(TraitTrigger.ON_DURABILITY_USE)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // -10%/Lv
            .effectUnit("%")
            .color(0x6666FF)  // 青
            .rarity(2)
            .build();

    /**
     * 軽量 - 攻撃速度・移動速度UP
     * レベルスケーリング: +5%/Lv
     */
    public static final Trait LIGHTWEIGHT = Trait.builder(loc("lightweight"))
            .category(TraitCategory.DEFENSE)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(5)
            .effectPerLevel(5.0f)  // +5%/Lv
            .effectUnit("%")
            .color(0xFFFFCC)  // 淡黄
            .rarity(1)
            .build();

    /**
     * 反射 - ダメージの一部を反射
     * レベルスケーリング: +5%/Lv
     */
    public static final Trait THORNS = Trait.builder(loc("thorns"))
            .category(TraitCategory.DEFENSE)
            .trigger(TraitTrigger.ON_DAMAGE_TAKEN)
            .maxLevel(5)
            .effectPerLevel(5.0f)  // +5%/Lv
            .effectUnit("%")
            .color(0x996633)  // 茶色
            .rarity(2)
            .build();

    // ============================================
    // ユーティリティ特性（8種）
    // ============================================

    /**
     * 生態 - 経験値獲得量UP
     * レベルスケーリング: +10%/Lv
     */
    public static final Trait ECOLOGICAL = Trait.builder(loc("ecological"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ON_XP_GAIN)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // +10%/Lv
            .effectUnit("%")
            .color(0x33CC33)  // 緑
            .rarity(2)
            .build();

    /**
     * 書込可能 - MODスロット追加
     * レベルスケーリング: +1スロット/Lv
     */
    public static final Trait WRITABLE = Trait.builder(loc("writable"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(3)
            .effectPerLevel(1.0f)  // +1スロット/Lv
            .effectUnit("スロット")
            .color(0xFFCC00)  // 黄橙
            .rarity(4)
            .build();

    /**
     * 修繕 - XPで耐久回復
     * レベルスケーリング: 効率+10%/Lv
     */
    public static final Trait MENDING = Trait.builder(loc("mending"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ON_XP_GAIN)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // +10%/Lv
            .effectUnit("%")
            .color(0x99FF99)  // 淡緑
            .rarity(3)
            .build();

    /**
     * 発光 - 暗視効果付与
     * レベルスケーリング: 範囲+2ブロック/Lv
     */
    public static final Trait GLOWING = Trait.builder(loc("glowing"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(3)
            .effectPerLevel(2.0f)  // +2ブロック/Lv
            .effectUnit("ブロック")
            .color(0xFFFF66)  // 黄色
            .rarity(1)
            .build();

    /**
     * シルクタッチ - ブロックをそのままドロップ
     * レベルスケーリング: 確率+20%/Lv
     */
    public static final Trait SILK_TOUCH = Trait.builder(loc("silk_touch"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ON_BLOCK_BREAK)
            .maxLevel(5)
            .effectPerLevel(20.0f)  // +20%/Lv
            .effectUnit("%")
            .color(0xCCFFCC)  // 淡緑
            .rarity(4)
            .incompatibleWith(loc("lucky"))  // 幸運と相互排他
            .build();

    /**
     * 釣り師 - 釣り効率UP
     * レベルスケーリング: +15%/Lv
     */
    public static final Trait ANGLER = Trait.builder(loc("angler"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ON_FISH_CATCH)
            .maxLevel(5)
            .effectPerLevel(15.0f)  // +15%/Lv
            .effectUnit("%")
            .color(0x3399FF)  // 水色
            .rarity(2)
            .build();

    /**
     * 刈り取り - 刈り取り効率UP、追加ドロップ
     * レベルスケーリング: +10%/Lv
     */
    public static final Trait SHEARING = Trait.builder(loc("shearing"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ON_SHEAR)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // +10%/Lv
            .effectUnit("%")
            .color(0xFFCCCC)  // 淡桃
            .rarity(2)
            .build();

    /**
     * 修復 - 時間経過で耐久回復
     * レベルスケーリング: 1耐久/30秒/Lv
     */
    public static final Trait SELF_REPAIR = Trait.builder(loc("self_repair"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(5)
            .effectPerLevel(1.0f)  // +1耐久/30秒/Lv
            .effectUnit("耐久/30秒")
            .color(0x66CCCC)  // シアン
            .rarity(4)
            .build();

    /**
     * 根を張る - 木材素材の固有特性。時間経過で耐久回復
     * レベルスケーリング: 1耐久/(60/Lv)秒
     * Lv1: 60秒毎、Lv2: 30秒毎、Lv3: 20秒毎、Lv4: 15秒毎、Lv5: 12秒毎
     */
    public static final Trait TAKE_ROOT = Trait.builder(loc("take_root"))
            .category(TraitCategory.UTILITY)
            .trigger(TraitTrigger.ALWAYS)
            .maxLevel(5)
            .effectPerLevel(1.0f)  // 回復間隔 = 60/Lv 秒
            .effectUnit("秒間隔")
            .color(0x8B4513)  // 茶色（木の色）
            .rarity(1)  // 木材の基本特性なので低レアリティ
            .build();

    // ============================================
    // 環境適応特性（3種）
    // ============================================

    /**
     * 水力学 - 水中速度UP
     * レベルスケーリング: +20%/Lv
     */
    public static final Trait AQUADYNAMIC = Trait.builder(loc("aquadynamic"))
            .category(TraitCategory.ENVIRONMENTAL)
            .trigger(TraitTrigger.WHILE_IN_WATER)
            .maxLevel(5)
            .effectPerLevel(20.0f)  // +20%/Lv
            .effectUnit("%")
            .color(0x0066CC)  // 青
            .rarity(2)
            .build();

    /**
     * ネザー適応 - ネザーでステータスUP
     * レベルスケーリング: +10%/Lv
     */
    public static final Trait NETHER_AFFINITY = Trait.builder(loc("nether_affinity"))
            .category(TraitCategory.ENVIRONMENTAL)
            .trigger(TraitTrigger.WHILE_IN_NETHER)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // +10%/Lv
            .effectUnit("%")
            .color(0xCC3300)  // 赤橙
            .rarity(3)
            .build();

    /**
     * エンド適応 - エンドでステータスUP
     * レベルスケーリング: +10%/Lv
     */
    public static final Trait END_AFFINITY = Trait.builder(loc("end_affinity"))
            .category(TraitCategory.ENVIRONMENTAL)
            .trigger(TraitTrigger.WHILE_IN_END)
            .maxLevel(5)
            .effectPerLevel(10.0f)  // +10%/Lv
            .effectUnit("%")
            .color(0x9933CC)  // 紫
            .rarity(3)
            .build();

    // ============================================
    // ヘルパーメソッド
    // ============================================

    /**
     * ANVIL名前空間でResourceLocationを作成
     *
     * @param path パス
     * @return ResourceLocation
     */
    private static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, path);
    }

    /**
     * すべての特性をレジストリに登録
     *
     * @param registry 登録先レジストリ
     */
    public static void registerAll(TraitRegistry registry) {
        // 戦闘系（8種）
        registry.register(LIFESTEAL);
        registry.register(HOLY);
        registry.register(FIERY);
        registry.register(FREEZING);
        registry.register(SHOCKING);
        registry.register(VENOMOUS);
        registry.register(JAGGED);
        registry.register(CRITICAL);

        // 採掘系（7種）
        registry.register(AUTO_SMELT);
        registry.register(MOMENTUM);
        registry.register(STONEBOUND);
        registry.register(ETHEREAL);
        registry.register(LUCKY);
        registry.register(MAGNETIC);
        registry.register(SLIMY);

        // 防御系（4種）
        registry.register(REINFORCED);
        registry.register(UNBREAKING);
        registry.register(LIGHTWEIGHT);
        registry.register(THORNS);

        // ユーティリティ（9種）
        registry.register(ECOLOGICAL);
        registry.register(WRITABLE);
        registry.register(MENDING);
        registry.register(GLOWING);
        registry.register(SILK_TOUCH);
        registry.register(ANGLER);
        registry.register(SHEARING);
        registry.register(SELF_REPAIR);
        registry.register(TAKE_ROOT);

        // 環境適応（3種）
        registry.register(AQUADYNAMIC);
        registry.register(NETHER_AFFINITY);
        registry.register(END_AFFINITY);
    }
}
