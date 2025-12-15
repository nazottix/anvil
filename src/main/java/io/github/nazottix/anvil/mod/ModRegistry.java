package io.github.nazottix.anvil.mod;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MODレジストリ
 *
 * 全MOD定義を管理します。
 * 初期30種のMODを定義しています。
 *
 * 仕様書参照: docs/05_容量制限システム.md
 */
public final class ModRegistry {

    // MOD定義のマップ
    private static final Map<ResourceLocation, ModDefinition> MODS = new HashMap<>();

    // ============================================
    // 初期化
    // ============================================

    static {
        // MOD登録
        registerAllMods();
    }

    /**
     * 全MODを登録
     */
    private static void registerAllMods() {
        // ============================================
        // オーラMOD（6種）
        // ============================================

        register(new ModDefinition.Builder(ANVIL.MODID, "mining_aura")
                .type(ModType.AURA)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(7)  // オーラは追加容量
                .polarity(Polarity.NARAMON)
                .maxRank(5)
                .effects(ModEffect.multiplicative("mining_speed", 10f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "damage_aura")
                .type(ModType.AURA)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(9)
                .polarity(Polarity.MADURAI)
                .maxRank(5)
                .effects(ModEffect.multiplicative("attack_damage", 12f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "efficiency_aura")
                .type(ModType.AURA)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(7)
                .polarity(Polarity.VAZARIN)
                .maxRank(5)
                .effects(ModEffect.multiplicative("durability_consumption", -15f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "fortune_aura")
                .type(ModType.AURA)
                .rarity(ModRarity.RARE)
                .baseDrain(8)
                .polarity(Polarity.NARAMON)
                .maxRank(5)
                .effects(ModEffect.multiplicative("drop_rate", 8f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "speed_aura")
                .type(ModType.AURA)
                .rarity(ModRarity.COMMON)
                .baseDrain(6)
                .polarity(Polarity.NARAMON)
                .maxRank(5)
                .effects(ModEffect.multiplicative("movement_speed", 8f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "vampiric_aura")
                .type(ModType.AURA)
                .rarity(ModRarity.RARE)
                .baseDrain(8)
                .polarity(Polarity.MADURAI)
                .maxRank(5)
                .effects(ModEffect.multiplicative("lifesteal", 3f))
                .build());

        // ============================================
        // 攻撃系MOD（6種）
        // ============================================

        register(new ModDefinition.Builder(ANVIL.MODID, "serration")
                .type(ModType.STANDARD)
                .rarity(ModRarity.COMMON)
                .baseDrain(4)
                .polarity(Polarity.MADURAI)
                .maxRank(10)
                .effects(ModEffect.multiplicative("attack_damage", 60f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "point_strike")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.MADURAI)
                .maxRank(5)
                .effects(ModEffect.multiplicative("critical_chance", 50f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "vital_sense")
                .type(ModType.STANDARD)
                .rarity(ModRarity.RARE)
                .baseDrain(6)
                .polarity(Polarity.MADURAI)
                .maxRank(5)
                .effects(ModEffect.multiplicative("critical_damage", 80f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "pressure_point")
                .type(ModType.STANDARD)
                .rarity(ModRarity.COMMON)
                .baseDrain(4)
                .polarity(Polarity.MADURAI)
                .maxRank(5)
                .effects(ModEffect.multiplicative("attack_damage", 30f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "fury")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.MADURAI)
                .maxRank(5)
                .effects(ModEffect.multiplicative("attack_speed", 30f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "reach")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(4)
                .polarity(Polarity.NARAMON)
                .maxRank(3)
                .effects(ModEffect.additive("attack_range", 1.5f))
                .build());

        // ============================================
        // 属性MOD（5種）
        // ============================================

        register(new ModDefinition.Builder(ANVIL.MODID, "molten_impact")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.UNAIRU)
                .maxRank(5)
                .effects(ModEffect.additive("fire_damage", 45f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "north_wind")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.UNAIRU)
                .maxRank(5)
                .effects(ModEffect.additive("cold_damage", 45f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "shocking_touch")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.UNAIRU)
                .maxRank(5)
                .effects(ModEffect.additive("electric_damage", 45f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "toxic_bite")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.UNAIRU)
                .maxRank(5)
                .effects(ModEffect.additive("poison_damage", 45f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "vicious_frost")
                .type(ModType.COMPOSITE)
                .rarity(ModRarity.RARE)
                .baseDrain(7)
                .polarity(Polarity.UNAIRU)
                .maxRank(5)
                .effects(
                        ModEffect.additive("cold_damage", 30f),
                        ModEffect.multiplicative("critical_chance", 15f)
                )
                .build());

        // ============================================
        // 採掘系MOD（5種）
        // ============================================

        register(new ModDefinition.Builder(ANVIL.MODID, "haste")
                .type(ModType.STANDARD)
                .rarity(ModRarity.COMMON)
                .baseDrain(4)
                .polarity(Polarity.NARAMON)
                .maxRank(5)
                .effects(ModEffect.multiplicative("mining_speed", 30f))
                .compatibleTools(ToolType.PICKAXE, ToolType.AXE, ToolType.SHOVEL)
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "efficiency")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.NARAMON)
                .maxRank(5)
                .effects(ModEffect.multiplicative("mining_speed", 50f))
                .compatibleTools(ToolType.PICKAXE)
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "vein_miner")
                .type(ModType.TOOL_SPECIFIC)
                .rarity(ModRarity.LEGENDARY)
                .baseDrain(12)
                .polarity(Polarity.ZENURIK)
                .maxRank(5)
                .effects(ModEffect.additive("vein_mining_blocks", 8f))
                .compatibleTools(ToolType.PICKAXE)
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "timber")
                .type(ModType.TOOL_SPECIFIC)
                .rarity(ModRarity.LEGENDARY)
                .baseDrain(12)
                .polarity(Polarity.ZENURIK)
                .maxRank(5)
                .effects(ModEffect.additive("tree_felling_blocks", 16f))
                .compatibleTools(ToolType.AXE)
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "excavation")
                .type(ModType.TOOL_SPECIFIC)
                .rarity(ModRarity.RARE)
                .baseDrain(8)
                .polarity(Polarity.ZENURIK)
                .maxRank(3)
                .effects(ModEffect.additive("excavation_area", 1f))
                .compatibleTools(ToolType.SHOVEL)
                .build());

        // ============================================
        // 防御・耐久系MOD（4種）
        // ============================================

        register(new ModDefinition.Builder(ANVIL.MODID, "steel_fiber")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.VAZARIN)
                .maxRank(10)
                .effects(ModEffect.multiplicative("durability", 60f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "reinforced")
                .type(ModType.STANDARD)
                .rarity(ModRarity.RARE)
                .baseDrain(6)
                .polarity(Polarity.VAZARIN)
                .maxRank(5)
                .effects(ModEffect.multiplicative("durability_ignore_chance", 25f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "quick_repair")
                .type(ModType.STANDARD)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.VAZARIN)
                .maxRank(5)
                .effects(ModEffect.additive("self_repair_rate", 0.5f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "adaptation")
                .type(ModType.STANDARD)
                .rarity(ModRarity.LEGENDARY)
                .baseDrain(10)
                .polarity(Polarity.VAZARIN)
                .maxRank(10)
                .effects(ModEffect.multiplicative("damage_resistance", 45f))
                .build());

        // ============================================
        // ユーティリティMOD（4種）
        // ============================================

        register(new ModDefinition.Builder(ANVIL.MODID, "magnetism")
                .type(ModType.EXILUS)
                .rarity(ModRarity.UNCOMMON)
                .baseDrain(5)
                .polarity(Polarity.NARAMON)
                .maxRank(3)
                .effects(ModEffect.additive("item_magnet_range", 4f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "auto_smelt")
                .type(ModType.EXILUS)
                .rarity(ModRarity.RARE)
                .baseDrain(7)
                .polarity(Polarity.ZENURIK)
                .maxRank(1)
                .effects(ModEffect.additive("auto_smelt_chance", 100f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "night_vision")
                .type(ModType.EXILUS)
                .rarity(ModRarity.COMMON)
                .baseDrain(3)
                .polarity(Polarity.NARAMON)
                .maxRank(1)
                .effects(ModEffect.additive("night_vision", 1f))
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "xp_booster")
                .type(ModType.EXILUS)
                .rarity(ModRarity.RARE)
                .baseDrain(6)
                .polarity(Polarity.NARAMON)
                .maxRank(5)
                .effects(ModEffect.multiplicative("xp_gain", 25f))
                .build());

        // ============================================
        // 腐敗MOD（Corrupted）- 大ボーナス+デメリット
        // ============================================

        register(new ModDefinition.Builder(ANVIL.MODID, "blind_rage")
                .type(ModType.CORRUPTED)
                .rarity(ModRarity.RARE)
                .baseDrain(8)
                .polarity(Polarity.MADURAI)
                .maxRank(10)
                .effects(
                        ModEffect.multiplicative("attack_damage", 99f),
                        ModEffect.multiplicative("durability_consumption", 55f)
                )
                .build());

        register(new ModDefinition.Builder(ANVIL.MODID, "narrow_minded")
                .type(ModType.CORRUPTED)
                .rarity(ModRarity.RARE)
                .baseDrain(8)
                .polarity(Polarity.VAZARIN)
                .maxRank(10)
                .effects(
                        ModEffect.multiplicative("durability", 99f),
                        ModEffect.multiplicative("attack_range", -66f)
                )
                .build());
    }

    /**
     * MOD定義を登録
     */
    private static void register(ModDefinition mod) {
        MODS.put(mod.id(), mod);
    }

    // ============================================
    // MOD取得
    // ============================================

    /**
     * IDからMOD定義を取得
     *
     * @param id MOD ID
     * @return MOD定義、見つからない場合はnull
     */
    public static ModDefinition get(ResourceLocation id) {
        return MODS.get(id);
    }

    /**
     * InstalledModからMOD定義を取得
     *
     * @param mod インストール済みMOD
     * @return MOD定義、見つからない場合はnull
     */
    public static ModDefinition get(InstalledMod mod) {
        return mod != null ? MODS.get(mod.modId()) : null;
    }

    /**
     * 全MOD定義を取得
     *
     * @return MOD定義のマップ
     */
    public static Map<ResourceLocation, ModDefinition> getAll() {
        return Map.copyOf(MODS);
    }

    /**
     * MOD数を取得
     */
    public static int getModCount() {
        return MODS.size();
    }

    /**
     * タイプ別にMODを取得
     *
     * @param type MODタイプ
     * @return 対応するMODのリスト
     */
    public static List<ModDefinition> getByType(ModType type) {
        return MODS.values().stream()
                .filter(mod -> mod.type() == type)
                .toList();
    }

    /**
     * レアリティ別にMODを取得
     *
     * @param rarity レアリティ
     * @return 対応するMODのリスト
     */
    public static List<ModDefinition> getByRarity(ModRarity rarity) {
        return MODS.values().stream()
                .filter(mod -> mod.rarity() == rarity)
                .toList();
    }

    /**
     * 極性別にMODを取得
     *
     * @param polarity 極性
     * @return 対応するMODのリスト
     */
    public static List<ModDefinition> getByPolarity(Polarity polarity) {
        return MODS.values().stream()
                .filter(mod -> mod.polarity() == polarity)
                .toList();
    }

    /**
     * ツールタイプ互換のMODを取得
     *
     * @param toolType ツールタイプ
     * @return 互換MODのリスト
     */
    public static List<ModDefinition> getCompatibleMods(ToolType toolType) {
        return MODS.values().stream()
                .filter(mod -> mod.isCompatibleWith(toolType))
                .toList();
    }

    // コンストラクタを非公開
    private ModRegistry() {}
}
