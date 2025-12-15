package io.github.nazottix.anvil.skill;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * スキルツリーレジストリ
 *
 * 全ツールタイプのスキルツリーを管理します。
 * Phase 3では Pickaxe と Sword のツリーを実装。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public final class SkillTreeRegistry {

    // ============================================
    // レジストリ
    // ============================================

    private static final Map<ResourceLocation, SkillTree> TREES = new HashMap<>();
    private static final Map<String, ResourceLocation> TOOL_TYPE_TO_TREE = new HashMap<>();

    // ============================================
    // 初期化
    // ============================================

    /**
     * スキルツリーを初期化・登録
     */
    public static void init() {
        // ピッケルツリー
        registerTree(createPickaxeTree());

        // 剣ツリー
        registerTree(createSwordTree());

        // 斧ツリー
        registerTree(createAxeTree());

        // シャベルツリー
        registerTree(createShovelTree());

        // クワツリー
        registerTree(createHoeTree());

        // 弓ツリー
        registerTree(createBowTree());

        // 釣り竿ツリー
        registerTree(createFishingRodTree());

        // ハサミツリー
        registerTree(createShearsTree());

        ANVIL.LOGGER.info("ANVIL: スキルツリーを登録しました（{}種）", TREES.size());
    }

    /**
     * ツリーを登録
     */
    private static void registerTree(SkillTree tree) {
        TREES.put(tree.id(), tree);
        TOOL_TYPE_TO_TREE.put(tree.toolType(), tree.id());
    }

    // ============================================
    // 取得
    // ============================================

    /**
     * IDでツリーを取得
     */
    public static SkillTree get(ResourceLocation id) {
        return TREES.get(id);
    }

    /**
     * IDでツリーを取得（getのエイリアス）
     */
    public static SkillTree getTree(ResourceLocation id) {
        return TREES.get(id);
    }

    /**
     * ツールタイプでツリーを取得
     */
    public static SkillTree getForToolType(String toolType) {
        ResourceLocation treeId = TOOL_TYPE_TO_TREE.get(toolType);
        return treeId != null ? TREES.get(treeId) : null;
    }

    /**
     * 全ツリーを取得
     */
    public static Map<ResourceLocation, SkillTree> getAll() {
        return Collections.unmodifiableMap(TREES);
    }

    // ============================================
    // ピッケルツリー作成
    // ============================================

    /**
     * ピッケル用スキルツリーを作成
     * テーマ: 採掘の極致
     */
    private static SkillTree createPickaxeTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("mining_speed", 5))
                .connections(
                        loc("pickaxe/speed_1"),
                        loc("pickaxe/fortune_1"),
                        loc("pickaxe/efficiency_1")
                )
                .build());

        // ============================================
        // 速度の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/speed_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("mining_speed", 3))
                .connections(loc("pickaxe/speed_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/speed_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.statPercent("mining_speed", 3))
                .connections(loc("pickaxe/speed_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/speed_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.statPercent("mining_speed", 10),
                        SkillEffect.statPercent("attack_speed", 5)
                )
                .connections(loc("pickaxe/speed_3"), loc("pickaxe/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/speed_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.statPercent("mining_speed", 4))
                .connections(loc("pickaxe/keystone_void"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-140, -100)
                .build());

        // ============================================
        // 幸運の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/fortune_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.statPercent("fortune", 5))
                .connections(loc("pickaxe/fortune_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/fortune_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.statPercent("fortune", 5))
                .connections(loc("pickaxe/fortune_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/fortune_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.statPercent("fortune", 15),
                        SkillEffect.statPercent("drop_rate", 10)
                )
                .connections(loc("pickaxe/fortune_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/fortune_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.statPercent("fortune", 8))
                .connections(loc("pickaxe/keystone_midas"))
                .build());

        // ============================================
        // 効率の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/efficiency_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("durability", 5))
                .connections(loc("pickaxe/efficiency_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/efficiency_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("durability", 5))
                .connections(loc("pickaxe/efficiency_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/efficiency_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("durability", 15),
                        SkillEffect.statPercent("durability_consumption", -10)
                )
                .connections(loc("pickaxe/efficiency_3"), loc("pickaxe/jewel_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/efficiency_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("durability", 8))
                .connections(loc("pickaxe/keystone_chain"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/jewel_2")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(140, -100)
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Void Mining: 採掘速度+200%、ドロップ50%消失
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/keystone_void")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.statPercent("mining_speed", 200),
                        SkillEffect.special("void_mining", 50) // 50%消失
                )
                .requirements(1000, 10)
                .connections(loc("pickaxe/mastery"))
                .build());

        // Midas Touch: 鉱石25%で金塊変換、採掘速度-50%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/keystone_midas")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.special("gold_conversion", 25),
                        SkillEffect.statPercent("mining_speed", -50)
                )
                .requirements(1000, 10)
                .connections(loc("pickaxe/mastery"))
                .build());

        // Chain Reaction: 連鎖爆発、自爆ダメージ
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/keystone_chain")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.special("chain_explosion", 3), // 3ブロック連鎖
                        SkillEffect.special("self_damage", 2) // 2ダメージ
                )
                .requirements(1000, 10)
                .connections(loc("pickaxe/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "pickaxe/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("mining_speed", 25),
                        SkillEffect.statPercent("fortune", 20),
                        SkillEffect.statPercent("durability", 30),
                        SkillEffect.special("infinite_vein", 1) // 無限の鉱脈
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "pickaxe_tree", "pickaxe")
                .nodes(nodes)
                .jewelSockets(List.of(
                        loc("pickaxe/jewel_1"),
                        loc("pickaxe/jewel_2")
                ))
                .build();
    }

    // ============================================
    // 剣ツリー作成
    // ============================================

    /**
     * 剣用スキルツリーを作成
     * テーマ: 戦士の道
     */
    private static SkillTree createSwordTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("attack_damage", 5))
                .connections(
                        loc("sword/damage_1"),
                        loc("sword/crit_1"),
                        loc("sword/defense_1")
                )
                .build());

        // ============================================
        // ダメージの道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/damage_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("attack_damage", 4))
                .connections(loc("sword/damage_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/damage_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.statPercent("attack_damage", 4))
                .connections(loc("sword/damage_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/damage_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.statPercent("attack_damage", 15),
                        SkillEffect.statPercent("attack_speed", 5)
                )
                .connections(loc("sword/damage_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/damage_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.statPercent("attack_damage", 6))
                .connections(loc("sword/keystone_berserker"))
                .build());

        // ============================================
        // クリティカルの道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/crit_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.statPercent("critical_chance", 5))
                .connections(loc("sword/crit_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/crit_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.statPercent("critical_damage", 10))
                .connections(loc("sword/crit_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/crit_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.statPercent("critical_chance", 15),
                        SkillEffect.statPercent("critical_damage", 25)
                )
                .connections(loc("sword/crit_3"), loc("sword/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/crit_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.statPercent("critical_damage", 15))
                .connections(loc("sword/keystone_resonant"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-40, -150)
                .build());

        // ============================================
        // 防御の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/defense_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("durability", 5))
                .connections(loc("sword/defense_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/defense_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("damage_resistance", 3))
                .connections(loc("sword/defense_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/defense_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("durability", 15),
                        SkillEffect.statPercent("lifesteal", 3)
                )
                .connections(loc("sword/defense_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/defense_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("damage_resistance", 5))
                .connections(loc("sword/keystone_vampire"))
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Berserker: HP減少で攻撃力UP(最大+300%)、防御力-50%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/keystone_berserker")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.special("berserker_rage", 300), // 最大+300%
                        SkillEffect.statPercent("damage_resistance", -50)
                )
                .requirements(1000, 10)
                .connections(loc("sword/mastery"))
                .build());

        // Resonant Strike: クリティカル率+100%、非クリ時ダメージ-30%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/keystone_resonant")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.statPercent("critical_chance", 100),
                        SkillEffect.special("non_crit_penalty", -30)
                )
                .requirements(1000, 10)
                .connections(loc("sword/mastery"))
                .build());

        // Vampiric Lord: 与ダメの20%回復、攻撃速度-40%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/keystone_vampire")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.statPercent("lifesteal", 20),
                        SkillEffect.statPercent("attack_speed", -40)
                )
                .requirements(1000, 10)
                .connections(loc("sword/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "sword/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("attack_damage", 30),
                        SkillEffect.statPercent("critical_chance", 20),
                        SkillEffect.statPercent("critical_damage", 40),
                        SkillEffect.special("true_strike", 1) // 真の一撃
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "sword_tree", "sword")
                .nodes(nodes)
                .jewelSockets(List.of(loc("sword/jewel_1")))
                .build();
    }

    // ============================================
    // 斧ツリー作成
    // ============================================

    /**
     * 斧用スキルツリーを作成
     * テーマ: 木こりの道
     */
    private static SkillTree createAxeTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("chopping_speed", 5))
                .connections(
                        loc("axe/speed_1"),
                        loc("axe/damage_1"),
                        loc("axe/harvest_1")
                )
                .build());

        // ============================================
        // 伐採速度の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/speed_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("chopping_speed", 4))
                .connections(loc("axe/speed_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/speed_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.statPercent("chopping_speed", 4))
                .connections(loc("axe/speed_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/speed_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.statPercent("chopping_speed", 15),
                        SkillEffect.special("tree_feller", 3) // 3ブロック連鎖伐採
                )
                .connections(loc("axe/speed_3"), loc("axe/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/speed_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.statPercent("chopping_speed", 6))
                .connections(loc("axe/keystone_lumberjack"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-140, -100)
                .build());

        // ============================================
        // 戦闘ダメージの道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/damage_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.statPercent("attack_damage", 5))
                .connections(loc("axe/damage_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/damage_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.statPercent("attack_damage", 5))
                .connections(loc("axe/damage_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/damage_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.statPercent("attack_damage", 20),
                        SkillEffect.statPercent("shield_disable", 100) // 盾無効化確率
                )
                .connections(loc("axe/damage_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/damage_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.statPercent("attack_damage", 8))
                .connections(loc("axe/keystone_cleave"))
                .build());

        // ============================================
        // 収穫の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/harvest_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("log_drops", 5))
                .connections(loc("axe/harvest_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/harvest_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("log_drops", 5))
                .connections(loc("axe/harvest_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/harvest_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("log_drops", 15),
                        SkillEffect.special("sapling_chance", 10) // 苗木ドロップ確率
                )
                .connections(loc("axe/harvest_3"), loc("axe/jewel_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/harvest_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("log_drops", 10))
                .connections(loc("axe/keystone_replant"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/jewel_2")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(140, -100)
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Lumberjack: 木全体を一撃で伐採、クールダウン5秒
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/keystone_lumberjack")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.special("instant_tree_fell", 1),
                        SkillEffect.special("cooldown", 100) // 5秒（ティック）
                )
                .requirements(1000, 10)
                .connections(loc("axe/mastery"))
                .build());

        // Cleave: 範囲攻撃+50%、単体ダメージ-30%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/keystone_cleave")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.special("cleave_range", 3), // 3ブロック範囲
                        SkillEffect.statPercent("attack_damage", -30)
                )
                .requirements(1000, 10)
                .connections(loc("axe/mastery"))
                .build());

        // Auto Replant: 伐採後自動植林、耐久消費+50%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/keystone_replant")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.special("auto_replant", 1),
                        SkillEffect.statPercent("durability_consumption", 50)
                )
                .requirements(1000, 10)
                .connections(loc("axe/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "axe/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("chopping_speed", 30),
                        SkillEffect.statPercent("attack_damage", 25),
                        SkillEffect.statPercent("log_drops", 25),
                        SkillEffect.special("forest_master", 1) // 森の主
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "axe_tree", "axe")
                .nodes(nodes)
                .jewelSockets(List.of(
                        loc("axe/jewel_1"),
                        loc("axe/jewel_2")
                ))
                .build();
    }

    // ============================================
    // シャベルツリー作成
    // ============================================

    /**
     * シャベル用スキルツリーを作成
     * テーマ: 大地の掘削者
     */
    private static SkillTree createShovelTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("digging_speed", 5))
                .connections(
                        loc("shovel/speed_1"),
                        loc("shovel/area_1"),
                        loc("shovel/treasure_1")
                )
                .build());

        // ============================================
        // 掘削速度の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/speed_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("digging_speed", 4))
                .connections(loc("shovel/speed_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/speed_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.statPercent("digging_speed", 4))
                .connections(loc("shovel/speed_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/speed_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.statPercent("digging_speed", 15),
                        SkillEffect.special("instant_path", 1) // 道作り即時
                )
                .connections(loc("shovel/speed_3"), loc("shovel/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/speed_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.statPercent("digging_speed", 6))
                .connections(loc("shovel/keystone_excavator"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-140, -100)
                .build());

        // ============================================
        // 範囲掘削の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/area_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.special("area_dig", 1)) // 1x1追加
                .connections(loc("shovel/area_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/area_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.special("area_dig", 1))
                .connections(loc("shovel/area_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/area_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.special("area_dig", 2), // 3x3掘削
                        SkillEffect.statPercent("durability_consumption", -10)
                )
                .connections(loc("shovel/area_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/area_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.special("area_dig", 1))
                .connections(loc("shovel/keystone_tunnel"))
                .build());

        // ============================================
        // 宝探しの道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/treasure_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("treasure_find", 5))
                .connections(loc("shovel/treasure_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/treasure_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("treasure_find", 5))
                .connections(loc("shovel/treasure_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/treasure_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("treasure_find", 15),
                        SkillEffect.special("buried_treasure_sense", 10) // 埋蔵宝感知範囲
                )
                .connections(loc("shovel/treasure_3"), loc("shovel/jewel_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/treasure_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("treasure_find", 10))
                .connections(loc("shovel/keystone_archaeologist"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/jewel_2")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(140, -100)
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Excavator: 5x5掘削、掘削速度-50%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/keystone_excavator")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.special("area_dig", 5),
                        SkillEffect.statPercent("digging_speed", -50)
                )
                .requirements(1000, 10)
                .connections(loc("shovel/mastery"))
                .build());

        // Tunnel Bore: 直線トンネル掘削、上下方向不可
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/keystone_tunnel")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.special("tunnel_bore", 10), // 10ブロック先まで
                        SkillEffect.special("horizontal_only", 1)
                )
                .requirements(1000, 10)
                .connections(loc("shovel/mastery"))
                .build());

        // Archaeologist: レア宝物発見+200%、通常ドロップ-50%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/keystone_archaeologist")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.statPercent("treasure_find", 200),
                        SkillEffect.statPercent("normal_drops", -50)
                )
                .requirements(1000, 10)
                .connections(loc("shovel/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shovel/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("digging_speed", 30),
                        SkillEffect.special("area_dig", 2),
                        SkillEffect.statPercent("treasure_find", 25),
                        SkillEffect.special("earth_shaper", 1) // 大地の造形師
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "shovel_tree", "shovel")
                .nodes(nodes)
                .jewelSockets(List.of(
                        loc("shovel/jewel_1"),
                        loc("shovel/jewel_2")
                ))
                .build();
    }

    // ============================================
    // クワツリー作成
    // ============================================

    /**
     * クワ用スキルツリーを作成
     * テーマ: 豊穣の農夫
     */
    private static SkillTree createHoeTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("tilling_speed", 5))
                .connections(
                        loc("hoe/tilling_1"),
                        loc("hoe/harvest_1"),
                        loc("hoe/growth_1")
                )
                .build());

        // ============================================
        // 耕作の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/tilling_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("tilling_speed", 4))
                .connections(loc("hoe/tilling_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/tilling_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.special("tilling_area", 1)) // 範囲耕作
                .connections(loc("hoe/tilling_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/tilling_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.special("tilling_area", 2), // 3x3耕作
                        SkillEffect.special("auto_water", 1) // 自動水源生成
                )
                .connections(loc("hoe/tilling_3"), loc("hoe/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/tilling_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.special("tilling_area", 1))
                .connections(loc("hoe/keystone_terraformer"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-140, -100)
                .build());

        // ============================================
        // 収穫の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/harvest_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.statPercent("crop_yield", 5))
                .connections(loc("hoe/harvest_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/harvest_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.statPercent("crop_yield", 5))
                .connections(loc("hoe/harvest_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/harvest_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.statPercent("crop_yield", 15),
                        SkillEffect.special("auto_replant_crop", 1) // 収穫時自動再植え
                )
                .connections(loc("hoe/harvest_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/harvest_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.statPercent("seed_drops", 20))
                .connections(loc("hoe/keystone_reaper"))
                .build());

        // ============================================
        // 成長促進の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/growth_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("growth_boost", 5))
                .connections(loc("hoe/growth_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/growth_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("growth_boost", 5))
                .connections(loc("hoe/growth_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/growth_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("growth_boost", 15),
                        SkillEffect.special("aura_growth", 5) // 5ブロック範囲で成長促進
                )
                .connections(loc("hoe/growth_3"), loc("hoe/jewel_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/growth_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("growth_boost", 10))
                .connections(loc("hoe/keystone_druid"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/jewel_2")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(140, -100)
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Terraformer: 9x9範囲耕作、耐久消費+100%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/keystone_terraformer")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.special("tilling_area", 9),
                        SkillEffect.statPercent("durability_consumption", 100)
                )
                .requirements(1000, 10)
                .connections(loc("hoe/mastery"))
                .build());

        // Reaper: 範囲収穫+100%収量、種ドロップなし
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/keystone_reaper")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.special("harvest_area", 5),
                        SkillEffect.statPercent("crop_yield", 100),
                        SkillEffect.statPercent("seed_drops", -100)
                )
                .requirements(1000, 10)
                .connections(loc("hoe/mastery"))
                .build());

        // Nature's Blessing: 即時成長確率25%、骨粉消費
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/keystone_druid")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.special("instant_growth_chance", 25),
                        SkillEffect.special("consume_bonemeal", 1)
                )
                .requirements(1000, 10)
                .connections(loc("hoe/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "hoe/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("tilling_speed", 30),
                        SkillEffect.statPercent("crop_yield", 30),
                        SkillEffect.statPercent("growth_boost", 30),
                        SkillEffect.special("harvest_deity", 1) // 豊穣神
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "hoe_tree", "hoe")
                .nodes(nodes)
                .jewelSockets(List.of(
                        loc("hoe/jewel_1"),
                        loc("hoe/jewel_2")
                ))
                .build();
    }

    // ============================================
    // 弓ツリー作成
    // ============================================

    /**
     * 弓用スキルツリーを作成
     * テーマ: 狩人の道
     */
    private static SkillTree createBowTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("arrow_damage", 5))
                .connections(
                        loc("bow/power_1"),
                        loc("bow/speed_1"),
                        loc("bow/precision_1")
                )
                .build());

        // ============================================
        // 威力の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/power_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("arrow_damage", 5))
                .connections(loc("bow/power_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/power_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.statPercent("arrow_damage", 5))
                .connections(loc("bow/power_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/power_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.statPercent("arrow_damage", 20),
                        SkillEffect.statPercent("armor_penetration", 10)
                )
                .connections(loc("bow/power_3"), loc("bow/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/power_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.statPercent("arrow_damage", 8))
                .connections(loc("bow/keystone_artillery"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-140, -100)
                .build());

        // ============================================
        // 速射の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/speed_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.statPercent("draw_speed", 5))
                .connections(loc("bow/speed_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/speed_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.statPercent("draw_speed", 5))
                .connections(loc("bow/speed_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/speed_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.statPercent("draw_speed", 15),
                        SkillEffect.special("multishot", 1) // 追加矢1本
                )
                .connections(loc("bow/speed_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/speed_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.statPercent("draw_speed", 10))
                .connections(loc("bow/keystone_barrage"))
                .build());

        // ============================================
        // 精密射撃の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/precision_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("accuracy", 5))
                .connections(loc("bow/precision_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/precision_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("headshot_damage", 10))
                .connections(loc("bow/precision_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/precision_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("headshot_damage", 30),
                        SkillEffect.statPercent("arrow_velocity", 15)
                )
                .connections(loc("bow/precision_3"), loc("bow/jewel_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/precision_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("accuracy", 10))
                .connections(loc("bow/keystone_sniper"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/jewel_2")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(140, -100)
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Artillery: 爆発矢（範囲ダメージ）、自傷あり
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/keystone_artillery")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.special("explosive_arrow", 3), // 3ブロック範囲
                        SkillEffect.special("self_damage", 2)
                )
                .requirements(1000, 10)
                .connections(loc("bow/mastery"))
                .build());

        // Barrage: 5連射、ダメージ-60%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/keystone_barrage")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.special("multishot", 5),
                        SkillEffect.statPercent("arrow_damage", -60)
                )
                .requirements(1000, 10)
                .connections(loc("bow/mastery"))
                .build());

        // Sniper: ヘッドショット即死確率、チャージ時間2倍
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/keystone_sniper")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.special("instant_kill_headshot", 10), // 10%確率
                        SkillEffect.statPercent("draw_speed", -50)
                )
                .requirements(1000, 10)
                .connections(loc("bow/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "bow/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("arrow_damage", 30),
                        SkillEffect.statPercent("draw_speed", 25),
                        SkillEffect.statPercent("accuracy", 25),
                        SkillEffect.special("legendary_hunter", 1) // 伝説のハンター
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "bow_tree", "bow")
                .nodes(nodes)
                .jewelSockets(List.of(
                        loc("bow/jewel_1"),
                        loc("bow/jewel_2")
                ))
                .build();
    }

    // ============================================
    // 釣り竿ツリー作成
    // ============================================

    /**
     * 釣り竿用スキルツリーを作成
     * テーマ: 釣り師の道
     */
    private static SkillTree createFishingRodTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("fishing_speed", 5))
                .connections(
                        loc("fishing_rod/speed_1"),
                        loc("fishing_rod/luck_1"),
                        loc("fishing_rod/treasure_1")
                )
                .build());

        // ============================================
        // 速釣りの道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/speed_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("fishing_speed", 5))
                .connections(loc("fishing_rod/speed_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/speed_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.statPercent("fishing_speed", 5))
                .connections(loc("fishing_rod/speed_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/speed_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.statPercent("fishing_speed", 15),
                        SkillEffect.special("instant_bite", 10) // 即噛みつき確率
                )
                .connections(loc("fishing_rod/speed_3"), loc("fishing_rod/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/speed_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.statPercent("fishing_speed", 10))
                .connections(loc("fishing_rod/keystone_flash"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-140, -100)
                .build());

        // ============================================
        // 幸運の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/luck_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.statPercent("luck_of_sea", 5))
                .connections(loc("fishing_rod/luck_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/luck_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.statPercent("luck_of_sea", 5))
                .connections(loc("fishing_rod/luck_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/luck_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.statPercent("luck_of_sea", 15),
                        SkillEffect.special("double_catch", 10) // ダブルキャッチ確率
                )
                .connections(loc("fishing_rod/luck_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/luck_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.statPercent("luck_of_sea", 10))
                .connections(loc("fishing_rod/keystone_neptune"))
                .build());

        // ============================================
        // 宝釣りの道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/treasure_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("treasure_rate", 5))
                .connections(loc("fishing_rod/treasure_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/treasure_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("treasure_rate", 5))
                .connections(loc("fishing_rod/treasure_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/treasure_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("treasure_rate", 15),
                        SkillEffect.special("enchanted_catch", 5) // エンチャント付きアイテム確率
                )
                .connections(loc("fishing_rod/treasure_3"), loc("fishing_rod/jewel_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/treasure_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("treasure_rate", 10))
                .connections(loc("fishing_rod/keystone_collector"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/jewel_2")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(140, -100)
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Flash Fisher: 即座に釣れる、魚のみ
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/keystone_flash")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.special("instant_catch", 1),
                        SkillEffect.special("fish_only", 1)
                )
                .requirements(1000, 10)
                .connections(loc("fishing_rod/mastery"))
                .build());

        // Neptune's Blessing: 宝物確率3倍、ゴミも3倍
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/keystone_neptune")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.statPercent("treasure_rate", 200),
                        SkillEffect.statPercent("junk_rate", 200)
                )
                .requirements(1000, 10)
                .connections(loc("fishing_rod/mastery"))
                .build());

        // Collector: 釣ったアイテム自動修理、耐久消費2倍
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/keystone_collector")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.special("auto_repair_catch", 1),
                        SkillEffect.statPercent("durability_consumption", 100)
                )
                .requirements(1000, 10)
                .connections(loc("fishing_rod/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "fishing_rod/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("fishing_speed", 30),
                        SkillEffect.statPercent("luck_of_sea", 25),
                        SkillEffect.statPercent("treasure_rate", 25),
                        SkillEffect.special("master_angler", 1) // 釣りの達人
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "fishing_rod_tree", "fishing_rod")
                .nodes(nodes)
                .jewelSockets(List.of(
                        loc("fishing_rod/jewel_1"),
                        loc("fishing_rod/jewel_2")
                ))
                .build();
    }

    // ============================================
    // ハサミツリー作成
    // ============================================

    /**
     * ハサミ用スキルツリーを作成
     * テーマ: 刈り取りの達人
     */
    private static SkillTree createShearsTree() {
        List<SkillNode> nodes = new ArrayList<>();

        // ルートノード
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/root")
                .type(SkillNodeType.ROOT)
                .position(0, 0)
                .effects(SkillEffect.statPercent("shearing_speed", 5))
                .connections(
                        loc("shears/wool_1"),
                        loc("shears/plant_1"),
                        loc("shears/efficiency_1")
                )
                .build());

        // ============================================
        // 羊毛の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/wool_1")
                .type(SkillNodeType.MINOR)
                .position(-60, -40)
                .effects(SkillEffect.statPercent("wool_drops", 5))
                .connections(loc("shears/wool_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/wool_2")
                .type(SkillNodeType.MINOR)
                .position(-80, -80)
                .effects(SkillEffect.statPercent("wool_drops", 5))
                .connections(loc("shears/wool_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/wool_notable")
                .type(SkillNodeType.NOTABLE)
                .position(-100, -120)
                .effects(
                        SkillEffect.statPercent("wool_drops", 15),
                        SkillEffect.special("colored_wool_chance", 10) // 染色済み羊毛確率
                )
                .connections(loc("shears/wool_3"), loc("shears/jewel_1"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/wool_3")
                .type(SkillNodeType.MINOR)
                .position(-120, -160)
                .effects(SkillEffect.statPercent("wool_drops", 10))
                .connections(loc("shears/keystone_shepherd"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/jewel_1")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(-140, -100)
                .build());

        // ============================================
        // 植物刈りの道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/plant_1")
                .type(SkillNodeType.MINOR)
                .position(0, -50)
                .effects(SkillEffect.statPercent("plant_drops", 5))
                .connections(loc("shears/plant_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/plant_2")
                .type(SkillNodeType.MINOR)
                .position(0, -90)
                .effects(SkillEffect.statPercent("plant_drops", 5))
                .connections(loc("shears/plant_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/plant_notable")
                .type(SkillNodeType.NOTABLE)
                .position(0, -130)
                .effects(
                        SkillEffect.statPercent("plant_drops", 15),
                        SkillEffect.special("silk_touch_plant", 1) // 植物シルクタッチ
                )
                .connections(loc("shears/plant_3"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/plant_3")
                .type(SkillNodeType.MINOR)
                .position(0, -170)
                .effects(SkillEffect.statPercent("vine_drops", 20))
                .connections(loc("shears/keystone_gardener"))
                .build());

        // ============================================
        // 効率の道
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/efficiency_1")
                .type(SkillNodeType.MINOR)
                .position(60, -40)
                .effects(SkillEffect.statPercent("durability", 5))
                .connections(loc("shears/efficiency_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/efficiency_2")
                .type(SkillNodeType.MINOR)
                .position(80, -80)
                .effects(SkillEffect.statPercent("durability", 5))
                .connections(loc("shears/efficiency_notable"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/efficiency_notable")
                .type(SkillNodeType.NOTABLE)
                .position(100, -120)
                .effects(
                        SkillEffect.statPercent("durability", 15),
                        SkillEffect.statPercent("shearing_speed", 20)
                )
                .connections(loc("shears/efficiency_3"), loc("shears/jewel_2"))
                .build());

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/efficiency_3")
                .type(SkillNodeType.MINOR)
                .position(120, -160)
                .effects(SkillEffect.statPercent("durability_consumption", -10))
                .connections(loc("shears/keystone_collector"))
                .build());

        // ジュエルソケット
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/jewel_2")
                .type(SkillNodeType.JEWEL_SOCKET)
                .position(140, -100)
                .build());

        // ============================================
        // キーストーン
        // ============================================

        // Master Shepherd: 範囲刈り取り、羊にダメージ
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/keystone_shepherd")
                .type(SkillNodeType.KEYSTONE)
                .position(-150, -220)
                .effects(
                        SkillEffect.special("area_shear", 5), // 5ブロック範囲
                        SkillEffect.special("sheep_damage", 1)
                )
                .requirements(1000, 10)
                .connections(loc("shears/mastery"))
                .build());

        // Master Gardener: 植物ドロップ3倍、耐久消費3倍
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/keystone_gardener")
                .type(SkillNodeType.KEYSTONE)
                .position(0, -230)
                .effects(
                        SkillEffect.statPercent("plant_drops", 200),
                        SkillEffect.statPercent("durability_consumption", 200)
                )
                .requirements(1000, 10)
                .connections(loc("shears/mastery"))
                .build());

        // Collector: 自動収集、移動速度-20%
        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/keystone_collector")
                .type(SkillNodeType.KEYSTONE)
                .position(150, -220)
                .effects(
                        SkillEffect.special("auto_collect", 5), // 5ブロック範囲
                        SkillEffect.statPercent("movement_speed", -20)
                )
                .requirements(1000, 10)
                .connections(loc("shears/mastery"))
                .build());

        // ============================================
        // マスタリーノード
        // ============================================

        nodes.add(new SkillNode.Builder(ANVIL.MODID, "shears/mastery")
                .type(SkillNodeType.MASTERY)
                .position(0, -300)
                .effects(
                        SkillEffect.statPercent("shearing_speed", 30),
                        SkillEffect.statPercent("wool_drops", 25),
                        SkillEffect.statPercent("plant_drops", 25),
                        SkillEffect.special("harvest_master", 1) // 収穫の達人
                )
                .requirements(5000, 25)
                .build());

        return new SkillTree.Builder(ANVIL.MODID, "shears_tree", "shears")
                .nodes(nodes)
                .jewelSockets(List.of(
                        loc("shears/jewel_1"),
                        loc("shears/jewel_2")
                ))
                .build();
    }

    // ============================================
    // ユーティリティ
    // ============================================

    private static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, path);
    }

    // コンストラクタを非公開
    private SkillTreeRegistry() {}
}
