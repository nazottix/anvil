package io.github.nazottix.anvil.affix;

import java.util.*;
import java.util.stream.Collectors;

/**
 * アフィックスレジストリ
 *
 * 全アフィックスを管理し、検索・フィルタリング機能を提供します。
 *
 * 仕様書参照: docs/06_レアリティシステム.md - 8.3 アフィックスシステム
 */
public class AffixRegistry {

    private static final Map<String, Affix> AFFIXES = new HashMap<>();
    private static boolean initialized = false;

    // ============================================
    // 初期化
    // ============================================

    /**
     * レジストリを初期化（MOD起動時に呼び出し）
     */
    public static void initialize() {
        if (initialized) return;

        registerPrefixes();
        registerSuffixes();

        initialized = true;
    }

    /**
     * プレフィックス（接頭辞）を登録
     */
    private static void registerPrefixes() {
        // 鋭利な (Sharp) - 攻撃力+5-50%
        register(Affix.builder("sharp", AffixType.PREFIX)
                .allTiers("attack_damage", 5.0f, 50.0f, true)
                .minLevel(1)
                .build());

        // 堅固な (Sturdy) - 耐久力+10-100%
        register(Affix.builder("sturdy", AffixType.PREFIX)
                .allTiers("durability", 10.0f, 100.0f, true)
                .minLevel(1)
                .build());

        // 迅速な (Swift) - 速度+3-30%
        register(Affix.builder("swift", AffixType.PREFIX)
                .allTiers("attack_speed", 3.0f, 30.0f, true)
                .minLevel(1)
                .build());

        // 幸運な (Lucky) - ドロップ率+2-20%
        register(Affix.builder("lucky", AffixType.PREFIX)
                .allTiers("drop_rate", 2.0f, 20.0f, true)
                .minLevel(1)
                .build());

        // 効率的な (Efficient) - 採掘速度+5-50%
        register(Affix.builder("efficient", AffixType.PREFIX)
                .allTiers("mining_speed", 5.0f, 50.0f, true)
                .minLevel(1)
                .build());

        // 精密な (Precise) - クリティカル率+2-20%
        register(Affix.builder("precise", AffixType.PREFIX)
                .allTiers("critical_chance", 2.0f, 20.0f, true)
                .minLevel(500)
                .build());

        // 強靭な (Resilient) - ノックバック耐性+5-50%
        register(Affix.builder("resilient", AffixType.PREFIX)
                .allTiers("knockback_resistance", 5.0f, 50.0f, true)
                .minLevel(1000)
                .build());

        // 破壊的な (Devastating) - クリティカルダメージ+10-50%（高レベル専用）
        register(Affix.builder("devastating", AffixType.PREFIX)
                .tierRange(1, 5, "critical_damage", 10.0f, 50.0f, true)
                .minLevel(5000)
                .build());

        // 圧倒的な (Overwhelming) - 全ステータス+5-15%（最高レベル専用）
        register(Affix.builder("overwhelming", AffixType.PREFIX)
                .tierRange(1, 3, "all_stats", 5.0f, 15.0f, true)
                .minLevel(8000)
                .build());

        // 恐怖の (Fearsome) - モブ逃走効果
        register(Affix.builder("fearsome", AffixType.PREFIX)
                .allTiers("fear_radius", 2.0f, 8.0f, false)
                .minLevel(2000)
                .build());

        // 輝く (Glowing) - 暗視効果範囲
        register(Affix.builder("glowing", AffixType.PREFIX)
                .allTiers("light_level", 5.0f, 15.0f, false)
                .minLevel(500)
                .build());

        // 軽量な (Lightweight) - 重量軽減
        register(Affix.builder("lightweight", AffixType.PREFIX)
                .allTiers("weight_reduction", 5.0f, 30.0f, true)
                .minLevel(1500)
                .build());
    }

    /**
     * サフィックス（接尾辞）を登録
     */
    private static void registerSuffixes() {
        // 〜の炎 (of Flames) - 火属性+5-50
        register(Affix.builder("flames", AffixType.SUFFIX)
                .allTiers("fire_damage", 5.0f, 50.0f, false)
                .minLevel(1)
                .build());

        // 〜の氷 (of Frost) - 氷属性+5-50
        register(Affix.builder("frost", AffixType.SUFFIX)
                .allTiers("frost_damage", 5.0f, 50.0f, false)
                .minLevel(1)
                .build());

        // 〜の雷 (of Thunder) - 雷属性+5-50
        register(Affix.builder("thunder", AffixType.SUFFIX)
                .allTiers("lightning_damage", 5.0f, 50.0f, false)
                .minLevel(1)
                .build());

        // 〜の毒 (of Venom) - 毒ダメージ+3-30
        register(Affix.builder("venom", AffixType.SUFFIX)
                .allTiers("poison_damage", 3.0f, 30.0f, false)
                .minLevel(1000)
                .build());

        // 〜の吸収 (of Leeching) - ライフスティール+1-5%
        register(Affix.builder("leeching", AffixType.SUFFIX)
                .tierRange(1, 5, "life_steal", 1.0f, 5.0f, true)
                .minLevel(3000)
                .build());

        // 〜の永続 (of Eternity) - 耐久消費-10-30%
        register(Affix.builder("eternity", AffixType.SUFFIX)
                .tierRange(1, 3, "durability_consumption", -30.0f, -10.0f, true)
                .minLevel(7000)
                .build());

        // 〜の覇者 (of Dominion) - 経験値+50%（最高ティアのみ）
        register(Affix.builder("dominion", AffixType.SUFFIX)
                .addTier(AffixTier.T1, new AffixEffect("exp_bonus", 50.0f, true))
                .minLevel(9500)
                .build());

        // 〜の収穫 (of Harvesting) - シルクタッチ確率
        register(Affix.builder("harvesting", AffixType.SUFFIX)
                .allTiers("silk_touch_chance", 5.0f, 50.0f, true)
                .minLevel(2000)
                .build());

        // 〜の幸運 (of Fortune) - 幸運レベル+
        register(Affix.builder("fortune", AffixType.SUFFIX)
                .allTiers("fortune_level", 1.0f, 5.0f, false)
                .minLevel(1500)
                .build());

        // 〜の探知 (of Sensing) - 鉱石探知範囲
        register(Affix.builder("sensing", AffixType.SUFFIX)
                .allTiers("ore_sense_range", 3.0f, 15.0f, false)
                .minLevel(3000)
                .build());

        // 〜の修繕 (of Mending) - 自動修復
        register(Affix.builder("mending", AffixType.SUFFIX)
                .allTiers("auto_repair", 0.1f, 1.0f, false)
                .minLevel(4000)
                .build());

        // 〜の経験 (of Experience) - 経験値ボーナス
        register(Affix.builder("experience", AffixType.SUFFIX)
                .allTiers("exp_bonus", 5.0f, 30.0f, true)
                .minLevel(500)
                .build());
    }

    // ============================================
    // 登録・取得
    // ============================================

    /**
     * アフィックスを登録
     */
    private static void register(Affix affix) {
        AFFIXES.put(affix.getId(), affix);
    }

    /**
     * IDでアフィックスを取得
     *
     * @param id アフィックスID
     * @return アフィックス、見つからない場合はnull
     */
    public static Affix get(String id) {
        return AFFIXES.get(id);
    }

    /**
     * 全アフィックスを取得
     */
    public static Collection<Affix> getAll() {
        return Collections.unmodifiableCollection(AFFIXES.values());
    }

    /**
     * タイプでアフィックスをフィルタ
     *
     * @param type アフィックスタイプ
     * @return 該当するアフィックスのリスト
     */
    public static List<Affix> getByType(AffixType type) {
        return AFFIXES.values().stream()
                .filter(a -> a.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * レベルで出現可能なアフィックスを取得
     *
     * @param level ツールレベル
     * @return 出現可能なアフィックスのリスト
     */
    public static List<Affix> getAvailableForLevel(int level) {
        return AFFIXES.values().stream()
                .filter(a -> a.canAppearAtLevel(level))
                .collect(Collectors.toList());
    }

    /**
     * タイプとレベルでフィルタ
     *
     * @param type アフィックスタイプ
     * @param level ツールレベル
     * @return 該当するアフィックスのリスト
     */
    public static List<Affix> getAvailable(AffixType type, int level) {
        return AFFIXES.values().stream()
                .filter(a -> a.getType() == type && a.canAppearAtLevel(level))
                .collect(Collectors.toList());
    }

    /**
     * 登録されているアフィックス数を取得
     */
    public static int getCount() {
        return AFFIXES.size();
    }
}
