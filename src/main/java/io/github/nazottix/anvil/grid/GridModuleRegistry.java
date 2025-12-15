package io.github.nazottix.anvil.grid;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.mod.ModEffect;
import io.github.nazottix.anvil.mod.ModRarity;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * グリッドモジュールレジストリ
 *
 * 全グリッドモジュール定義を管理します。
 * 初期30種のモジュールを定義しています。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md
 */
public final class GridModuleRegistry {

    private static final Map<ResourceLocation, GridModule> MODULES = new HashMap<>();

    static {
        registerAllModules();
    }

    private static void registerAllModules() {
        // ============================================
        // 攻撃系モジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "power_core_s")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.COMMON)
                .weight(2)
                .effects(ModEffect.multiplicative("attack_damage", 3f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "power_core_m")
                .shape(ModuleShape.HORIZONTAL_2)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.UNCOMMON)
                .weight(5)
                .effects(ModEffect.multiplicative("attack_damage", 8f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "power_core_l")
                .shape(ModuleShape.SQUARE_2)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.RARE)
                .weight(12)
                .effects(ModEffect.multiplicative("attack_damage", 15f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "strike_unit")
                .shape(ModuleShape.L_SHAPE)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.UNCOMMON)
                .weight(6)
                .effects(ModEffect.multiplicative("critical_chance", 10f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "crit_amplifier")
                .shape(ModuleShape.T_SHAPE)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.RARE)
                .weight(8)
                .effects(ModEffect.multiplicative("critical_damage", 20f))
                .build());

        // ============================================
        // 防御系モジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "shield_module_s")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.DEFENSE)
                .rarity(ModRarity.COMMON)
                .weight(2)
                .effects(ModEffect.multiplicative("durability", 5f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "shield_module_m")
                .shape(ModuleShape.VERTICAL_2)
                .category(ModuleCategory.DEFENSE)
                .rarity(ModRarity.UNCOMMON)
                .weight(5)
                .effects(ModEffect.multiplicative("durability", 12f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "armor_plate")
                .shape(ModuleShape.HORIZONTAL_3)
                .category(ModuleCategory.DEFENSE)
                .rarity(ModRarity.RARE)
                .weight(10)
                .effects(ModEffect.multiplicative("durability", 20f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "reinforced_frame")
                .shape(ModuleShape.T_SHAPE)
                .category(ModuleCategory.DEFENSE)
                .rarity(ModRarity.UNCOMMON)
                .weight(8)
                .effects(ModEffect.multiplicative("durability", 15f),
                        ModEffect.multiplicative("durability_ignore_chance", 5f))
                .build());

        // ============================================
        // 速度系モジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "speed_chip")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.SPEED)
                .rarity(ModRarity.COMMON)
                .weight(1)
                .effects(ModEffect.multiplicative("mining_speed", 2f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "quick_processor")
                .shape(ModuleShape.HORIZONTAL_2)
                .category(ModuleCategory.SPEED)
                .rarity(ModRarity.UNCOMMON)
                .weight(3)
                .effects(ModEffect.multiplicative("mining_speed", 6f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "overclock_unit")
                .shape(ModuleShape.L_SHAPE)
                .category(ModuleCategory.SPEED)
                .rarity(ModRarity.RARE)
                .weight(5)
                .effects(ModEffect.multiplicative("mining_speed", 12f),
                        ModEffect.multiplicative("attack_speed", 8f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "efficiency_core")
                .shape(ModuleShape.SQUARE_2)
                .category(ModuleCategory.SPEED)
                .rarity(ModRarity.RARE)
                .weight(8)
                .effects(ModEffect.multiplicative("mining_speed", 18f))
                .build());

        // ============================================
        // 容量系モジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "energy_cell_s")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.CAPACITY)
                .rarity(ModRarity.COMMON)
                .weight(1)
                .effects(ModEffect.additive("mod_capacity", 2f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "energy_cell_m")
                .shape(ModuleShape.VERTICAL_2)
                .category(ModuleCategory.CAPACITY)
                .rarity(ModRarity.UNCOMMON)
                .weight(3)
                .effects(ModEffect.additive("mod_capacity", 5f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "power_pack")
                .shape(ModuleShape.L_SHAPE)
                .category(ModuleCategory.CAPACITY)
                .rarity(ModRarity.RARE)
                .weight(5)
                .effects(ModEffect.additive("mod_capacity", 8f))
                .build());

        // ============================================
        // ユーティリティモジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "magnet_unit")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.UTILITY)
                .rarity(ModRarity.UNCOMMON)
                .weight(2)
                .effects(ModEffect.additive("item_magnet_range", 2f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "light_module")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.UTILITY)
                .rarity(ModRarity.COMMON)
                .weight(1)
                .effects(ModEffect.additive("light_level", 15f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "xp_extractor")
                .shape(ModuleShape.HORIZONTAL_2)
                .category(ModuleCategory.UTILITY)
                .rarity(ModRarity.UNCOMMON)
                .weight(3)
                .effects(ModEffect.multiplicative("xp_gain", 10f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "cooling_unit")
                .shape(ModuleShape.L_SHAPE)
                .category(ModuleCategory.UTILITY)
                .rarity(ModRarity.UNCOMMON)
                .weight(6)
                .effects(ModEffect.multiplicative("durability_consumption", -10f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "auto_repair_unit")
                .shape(ModuleShape.SQUARE_2)
                .category(ModuleCategory.UTILITY)
                .rarity(ModRarity.RARE)
                .weight(8)
                .effects(ModEffect.additive("self_repair_rate", 0.3f))
                .build());

        // ============================================
        // 属性モジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "fire_core")
                .shape(ModuleShape.HORIZONTAL_3)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.UNCOMMON)
                .weight(7)
                .effects(ModEffect.additive("fire_damage", 20f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "ice_core")
                .shape(ModuleShape.HORIZONTAL_3)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.UNCOMMON)
                .weight(7)
                .effects(ModEffect.additive("cold_damage", 20f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "shock_core")
                .shape(ModuleShape.HORIZONTAL_3)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.UNCOMMON)
                .weight(7)
                .effects(ModEffect.additive("electric_damage", 20f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "toxic_core")
                .shape(ModuleShape.HORIZONTAL_3)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.UNCOMMON)
                .weight(7)
                .effects(ModEffect.additive("poison_damage", 20f))
                .build());

        // ============================================
        // コネクタモジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "synergy_node")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.CONNECTOR)
                .rarity(ModRarity.UNCOMMON)
                .weight(1)
                .effects(ModEffect.multiplicative("adjacent_bonus", 50f))
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "link_chip")
                .shape(ModuleShape.SINGLE)
                .category(ModuleCategory.CONNECTOR)
                .rarity(ModRarity.COMMON)
                .weight(1)
                .effects(ModEffect.multiplicative("adjacent_bonus", 25f))
                .build());

        // ============================================
        // 大型モジュール
        // ============================================

        register(new GridModule.Builder(ANVIL.MODID, "ultimate_core")
                .shape(ModuleShape.SQUARE_3)
                .category(ModuleCategory.ATTACK)
                .rarity(ModRarity.LEGENDARY)
                .weight(20)
                .effects(
                        ModEffect.multiplicative("attack_damage", 25f),
                        ModEffect.multiplicative("critical_chance", 15f),
                        ModEffect.multiplicative("critical_damage", 30f)
                )
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "fortress_plate")
                .shape(ModuleShape.SQUARE_3)
                .category(ModuleCategory.DEFENSE)
                .rarity(ModRarity.LEGENDARY)
                .weight(25)
                .effects(
                        ModEffect.multiplicative("durability", 40f),
                        ModEffect.multiplicative("durability_ignore_chance", 15f)
                )
                .build());

        register(new GridModule.Builder(ANVIL.MODID, "hyper_processor")
                .shape(ModuleShape.CROSS)
                .category(ModuleCategory.SPEED)
                .rarity(ModRarity.LEGENDARY)
                .weight(15)
                .effects(
                        ModEffect.multiplicative("mining_speed", 25f),
                        ModEffect.multiplicative("attack_speed", 15f)
                )
                .build());
    }

    private static void register(GridModule module) {
        MODULES.put(module.id(), module);
    }

    // ============================================
    // モジュール取得
    // ============================================

    public static GridModule get(ResourceLocation id) {
        return MODULES.get(id);
    }

    public static Map<ResourceLocation, GridModule> getAll() {
        return Map.copyOf(MODULES);
    }

    public static int getModuleCount() {
        return MODULES.size();
    }

    public static List<GridModule> getByCategory(ModuleCategory category) {
        return MODULES.values().stream()
                .filter(m -> m.category() == category)
                .toList();
    }

    public static List<GridModule> getByShape(ModuleShape shape) {
        return MODULES.values().stream()
                .filter(m -> m.shape() == shape)
                .toList();
    }

    public static List<GridModule> getByRarity(ModRarity rarity) {
        return MODULES.values().stream()
                .filter(m -> m.rarity() == rarity)
                .toList();
    }

    private GridModuleRegistry() {}
}
