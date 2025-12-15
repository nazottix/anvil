package io.github.nazottix.anvil.skill.jewel;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.skill.SkillEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * ジュエルレジストリ
 *
 * 全てのジュエルタイプを管理します。
 * ジュエルはスキルツリーのジュエルソケットに装着してステータスを強化します。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public final class JewelRegistry {

    // ============================================
    // レジストリ
    // ============================================

    private static final Map<ResourceLocation, JewelData> JEWELS = new HashMap<>();
    private static final Map<JewelData.JewelRarity, List<JewelData>> BY_RARITY = new EnumMap<>(JewelData.JewelRarity.class);

    // ============================================
    // 初期化
    // ============================================

    /**
     * ジュエルレジストリを初期化
     */
    public static void init() {
        // 通常ジュエル（基本ステータス）
        registerNormalJewels();

        // マジックジュエル（強化ステータス）
        registerMagicJewels();

        // レアジュエル（複合効果）
        registerRareJewels();

        // ユニークジュエル（特殊効果）
        registerUniqueJewels();

        // プライモーディアルジュエル（半径効果）
        registerPrimordialJewels();

        ANVIL.LOGGER.info("ANVIL: ジュエルを登録しました（{}種）", JEWELS.size());
    }

    /**
     * ジュエルを登録
     */
    private static void register(JewelData jewel) {
        JEWELS.put(jewel.id(), jewel);
        BY_RARITY.computeIfAbsent(jewel.rarity(), k -> new ArrayList<>()).add(jewel);
    }

    // ============================================
    // 通常ジュエル
    // ============================================

    private static void registerNormalJewels() {
        // 採掘速度ジュエル
        register(new JewelData(
                loc("mining_speed_jewel"),
                "jewel.anvil.mining_speed",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("mining_speed", 5)),
                List.of(),
                0,
                ""
        ));

        // 攻撃力ジュエル
        register(new JewelData(
                loc("attack_damage_jewel"),
                "jewel.anvil.attack_damage",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("attack_damage", 5)),
                List.of(),
                0,
                ""
        ));

        // 耐久値ジュエル
        register(new JewelData(
                loc("durability_jewel"),
                "jewel.anvil.durability",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("durability", 8)),
                List.of(),
                0,
                ""
        ));

        // 幸運ジュエル
        register(new JewelData(
                loc("fortune_jewel"),
                "jewel.anvil.fortune",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("fortune", 5)),
                List.of("pickaxe"),
                0,
                ""
        ));

        // クリティカル率ジュエル
        register(new JewelData(
                loc("crit_chance_jewel"),
                "jewel.anvil.crit_chance",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("crit_chance", 5)),
                List.of("sword"),
                0,
                ""
        ));

        // 伐採速度ジュエル
        register(new JewelData(
                loc("chopping_speed_jewel"),
                "jewel.anvil.chopping_speed",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("chopping_speed", 5)),
                List.of("axe"),
                0,
                ""
        ));

        // 釣り速度ジュエル
        register(new JewelData(
                loc("fishing_speed_jewel"),
                "jewel.anvil.fishing_speed",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("fishing_speed", 5)),
                List.of("fishing_rod"),
                0,
                ""
        ));

        // 刈り取り効率ジュエル
        register(new JewelData(
                loc("shearing_speed_jewel"),
                "jewel.anvil.shearing_speed",
                JewelData.JewelRarity.NORMAL,
                List.of(SkillEffect.statPercent("shearing_speed", 5)),
                List.of("shears"),
                0,
                ""
        ));
    }

    // ============================================
    // マジックジュエル
    // ============================================

    private static void registerMagicJewels() {
        // 強化採掘ジュエル
        register(new JewelData(
                loc("enhanced_mining_jewel"),
                "jewel.anvil.enhanced_mining",
                JewelData.JewelRarity.MAGIC,
                List.of(
                        SkillEffect.statPercent("mining_speed", 8),
                        SkillEffect.statPercent("durability", 3)
                ),
                List.of("pickaxe"),
                0,
                ""
        ));

        // 強化戦闘ジュエル
        register(new JewelData(
                loc("enhanced_combat_jewel"),
                "jewel.anvil.enhanced_combat",
                JewelData.JewelRarity.MAGIC,
                List.of(
                        SkillEffect.statPercent("attack_damage", 8),
                        SkillEffect.statPercent("attack_speed", 3)
                ),
                List.of("sword", "axe"),
                0,
                ""
        ));

        // 強化耐久ジュエル
        register(new JewelData(
                loc("enhanced_durability_jewel"),
                "jewel.anvil.enhanced_durability",
                JewelData.JewelRarity.MAGIC,
                List.of(
                        SkillEffect.statPercent("durability", 12),
                        SkillEffect.statPercent("durability_consumption", -5)
                ),
                List.of(),
                0,
                ""
        ));

        // クリティカルダメージジュエル
        register(new JewelData(
                loc("crit_damage_jewel"),
                "jewel.anvil.crit_damage",
                JewelData.JewelRarity.MAGIC,
                List.of(
                        SkillEffect.statPercent("crit_chance", 3),
                        SkillEffect.statPercent("crit_damage", 10)
                ),
                List.of("sword", "bow"),
                0,
                ""
        ));

        // 弓強化ジュエル
        register(new JewelData(
                loc("enhanced_bow_jewel"),
                "jewel.anvil.enhanced_bow",
                JewelData.JewelRarity.MAGIC,
                List.of(
                        SkillEffect.statPercent("arrow_damage", 8),
                        SkillEffect.statPercent("draw_speed", 5)
                ),
                List.of("bow"),
                0,
                ""
        ));

        // 農業強化ジュエル
        register(new JewelData(
                loc("enhanced_farming_jewel"),
                "jewel.anvil.enhanced_farming",
                JewelData.JewelRarity.MAGIC,
                List.of(
                        SkillEffect.statPercent("crop_yield", 8),
                        SkillEffect.statPercent("growth_boost", 5)
                ),
                List.of("hoe"),
                0,
                ""
        ));
    }

    // ============================================
    // レアジュエル
    // ============================================

    private static void registerRareJewels() {
        // マイナーズドリームジュエル
        register(new JewelData(
                loc("miners_dream_jewel"),
                "jewel.anvil.miners_dream",
                JewelData.JewelRarity.RARE,
                List.of(
                        SkillEffect.statPercent("mining_speed", 12),
                        SkillEffect.statPercent("fortune", 8),
                        SkillEffect.statPercent("drop_rate", 5)
                ),
                List.of("pickaxe"),
                0,
                ""
        ));

        // ウォーリアーズプライドジュエル
        register(new JewelData(
                loc("warriors_pride_jewel"),
                "jewel.anvil.warriors_pride",
                JewelData.JewelRarity.RARE,
                List.of(
                        SkillEffect.statPercent("attack_damage", 12),
                        SkillEffect.statPercent("crit_chance", 8),
                        SkillEffect.statPercent("lifesteal", 3)
                ),
                List.of("sword"),
                0,
                ""
        ));

        // ネイチャーズブレッシングジュエル
        register(new JewelData(
                loc("natures_blessing_jewel"),
                "jewel.anvil.natures_blessing",
                JewelData.JewelRarity.RARE,
                List.of(
                        SkillEffect.statPercent("chopping_speed", 10),
                        SkillEffect.statPercent("log_drops", 15),
                        SkillEffect.special("sapling_chance", 10)
                ),
                List.of("axe"),
                0,
                ""
        ));

        // トレジャーハンタージュエル
        register(new JewelData(
                loc("treasure_hunter_jewel"),
                "jewel.anvil.treasure_hunter",
                JewelData.JewelRarity.RARE,
                List.of(
                        SkillEffect.statPercent("treasure_rate", 15),
                        SkillEffect.statPercent("luck_of_sea", 10),
                        SkillEffect.statPercent("fishing_speed", 8)
                ),
                List.of("fishing_rod"),
                0,
                ""
        ));

        // パーフェクトバランスジュエル
        register(new JewelData(
                loc("perfect_balance_jewel"),
                "jewel.anvil.perfect_balance",
                JewelData.JewelRarity.RARE,
                List.of(
                        SkillEffect.statPercent("mining_speed", 5),
                        SkillEffect.statPercent("attack_damage", 5),
                        SkillEffect.statPercent("durability", 10),
                        SkillEffect.statPercent("attack_speed", 5)
                ),
                List.of(),
                0,
                ""
        ));
    }

    // ============================================
    // ユニークジュエル
    // ============================================

    private static void registerUniqueJewels() {
        // ヴォイドタッチジュエル
        register(new JewelData(
                loc("void_touch_jewel"),
                "jewel.anvil.void_touch",
                JewelData.JewelRarity.UNIQUE,
                List.of(
                        SkillEffect.statPercent("mining_speed", 25),
                        SkillEffect.special("void_drops", 15)
                ),
                List.of("pickaxe"),
                0,
                ""
        ));

        // バーサーカーズソウルジュエル
        register(new JewelData(
                loc("berserkers_soul_jewel"),
                "jewel.anvil.berserkers_soul",
                JewelData.JewelRarity.UNIQUE,
                List.of(
                        SkillEffect.statPercent("attack_damage", 20),
                        SkillEffect.special("berserker_damage", 50),
                        SkillEffect.statPercent("durability", -10)
                ),
                List.of("sword", "axe"),
                0,
                ""
        ));

        // イーグルアイジュエル
        register(new JewelData(
                loc("eagle_eye_jewel"),
                "jewel.anvil.eagle_eye",
                JewelData.JewelRarity.UNIQUE,
                List.of(
                        SkillEffect.statPercent("accuracy", 20),
                        SkillEffect.statPercent("headshot_damage", 30),
                        SkillEffect.special("instant_kill_headshot", 3)
                ),
                List.of("bow"),
                0,
                ""
        ));

        // ネプチューンズフェイバージュエル
        register(new JewelData(
                loc("neptunes_favor_jewel"),
                "jewel.anvil.neptunes_favor",
                JewelData.JewelRarity.UNIQUE,
                List.of(
                        SkillEffect.statPercent("treasure_rate", 30),
                        SkillEffect.special("double_catch", 15),
                        SkillEffect.statPercent("junk_rate", 20)
                ),
                List.of("fishing_rod"),
                0,
                ""
        ));

        // エターナルブレードジュエル
        register(new JewelData(
                loc("eternal_blade_jewel"),
                "jewel.anvil.eternal_blade",
                JewelData.JewelRarity.UNIQUE,
                List.of(
                        SkillEffect.statPercent("durability", 50),
                        SkillEffect.statPercent("durability_consumption", -25),
                        SkillEffect.statPercent("attack_speed", -10)
                ),
                List.of(),
                0,
                ""
        ));
    }

    // ============================================
    // プライモーディアルジュエル
    // ============================================

    private static void registerPrimordialJewels() {
        // プライモーディアル・マイニングジュエル
        register(new JewelData(
                loc("primordial_mining_jewel"),
                "jewel.anvil.primordial_mining",
                JewelData.JewelRarity.PRIMORDIAL,
                List.of(
                        SkillEffect.statPercent("mining_speed", 15),
                        SkillEffect.statPercent("fortune", 10)
                ),
                List.of("pickaxe"),
                2, // 半径2のノードにも効果
                ""
        ));

        // プライモーディアル・コンバットジュエル
        register(new JewelData(
                loc("primordial_combat_jewel"),
                "jewel.anvil.primordial_combat",
                JewelData.JewelRarity.PRIMORDIAL,
                List.of(
                        SkillEffect.statPercent("attack_damage", 15),
                        SkillEffect.statPercent("crit_damage", 20)
                ),
                List.of("sword", "axe"),
                2,
                ""
        ));

        // プライモーディアル・ハーベストジュエル
        register(new JewelData(
                loc("primordial_harvest_jewel"),
                "jewel.anvil.primordial_harvest",
                JewelData.JewelRarity.PRIMORDIAL,
                List.of(
                        SkillEffect.statPercent("crop_yield", 20),
                        SkillEffect.statPercent("seed_drops", 15),
                        SkillEffect.special("aura_growth", 3)
                ),
                List.of("hoe"),
                2,
                ""
        ));

        // プライモーディアル・フォーチュンジュエル
        register(new JewelData(
                loc("primordial_fortune_jewel"),
                "jewel.anvil.primordial_fortune",
                JewelData.JewelRarity.PRIMORDIAL,
                List.of(
                        SkillEffect.statPercent("drop_rate", 15),
                        SkillEffect.statPercent("treasure_find", 15),
                        SkillEffect.statPercent("luck_of_sea", 15)
                ),
                List.of(),
                3, // 半径3のノードにも効果
                ""
        ));
    }

    // ============================================
    // 取得
    // ============================================

    /**
     * IDでジュエルを取得
     */
    public static Optional<JewelData> get(ResourceLocation id) {
        return Optional.ofNullable(JEWELS.get(id));
    }

    /**
     * 全ジュエルを取得
     */
    public static Collection<JewelData> getAll() {
        return Collections.unmodifiableCollection(JEWELS.values());
    }

    /**
     * レアリティでジュエルを取得
     */
    public static List<JewelData> getByRarity(JewelData.JewelRarity rarity) {
        return Collections.unmodifiableList(BY_RARITY.getOrDefault(rarity, List.of()));
    }

    /**
     * ツールタイプに装着可能なジュエルを取得
     */
    public static List<JewelData> getForToolType(String toolType) {
        return JEWELS.values().stream()
                .filter(j -> j.canEquipTo(toolType))
                .toList();
    }

    /**
     * ランダムなジュエルを取得（ドロップ用）
     */
    public static Optional<JewelData> getRandomJewel(Random random, float rarityBonus) {
        if (JEWELS.isEmpty()) {
            return Optional.empty();
        }

        // レアリティ決定
        float roll = random.nextFloat() * 100 + rarityBonus;
        JewelData.JewelRarity rarity;

        if (roll >= 99) {
            rarity = JewelData.JewelRarity.PRIMORDIAL;
        } else if (roll >= 95) {
            rarity = JewelData.JewelRarity.UNIQUE;
        } else if (roll >= 85) {
            rarity = JewelData.JewelRarity.RARE;
        } else if (roll >= 60) {
            rarity = JewelData.JewelRarity.MAGIC;
        } else {
            rarity = JewelData.JewelRarity.NORMAL;
        }

        List<JewelData> candidates = BY_RARITY.getOrDefault(rarity, List.of());
        if (candidates.isEmpty()) {
            // フォールバック
            candidates = BY_RARITY.getOrDefault(JewelData.JewelRarity.NORMAL, List.of());
        }

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    // ============================================
    // ユーティリティ
    // ============================================

    private static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, path);
    }

    // コンストラクタを非公開
    private JewelRegistry() {}
}
