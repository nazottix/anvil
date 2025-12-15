package io.github.nazottix.anvil.skill;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.assembly.CalculatedStats;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * スキル効果適用クラス
 *
 * 解放済みスキルノードの効果を集計し、ツールステータスに適用します。
 *
 * 計算順序:
 * 1. FLAT（固定値加算）
 * 2. PERCENT（パーセント加算）
 * 3. MULTIPLY（乗算）
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public class SkillEffectApplier {

    // ============================================
    // ステータスID定数
    // ============================================

    // 共通ステータス
    public static final String STAT_DURABILITY = "durability";
    public static final String STAT_DURABILITY_CONSUMPTION = "durability_consumption";

    // 採掘系
    public static final String STAT_MINING_SPEED = "mining_speed";
    public static final String STAT_MINING_LEVEL = "mining_level";
    public static final String STAT_FORTUNE = "fortune";
    public static final String STAT_DROP_RATE = "drop_rate";

    // 戦闘系
    public static final String STAT_ATTACK_DAMAGE = "attack_damage";
    public static final String STAT_ATTACK_SPEED = "attack_speed";
    public static final String STAT_CRIT_CHANCE = "crit_chance";
    public static final String STAT_CRIT_DAMAGE = "crit_damage";
    public static final String STAT_LIFESTEAL = "lifesteal";
    public static final String STAT_ARMOR_PENETRATION = "armor_penetration";

    // 斧系
    public static final String STAT_CHOPPING_SPEED = "chopping_speed";
    public static final String STAT_LOG_DROPS = "log_drops";
    public static final String STAT_SHIELD_DISABLE = "shield_disable";

    // シャベル系
    public static final String STAT_DIGGING_SPEED = "digging_speed";
    public static final String STAT_TREASURE_FIND = "treasure_find";

    // クワ系
    public static final String STAT_TILLING_SPEED = "tilling_speed";
    public static final String STAT_CROP_YIELD = "crop_yield";
    public static final String STAT_SEED_DROPS = "seed_drops";
    public static final String STAT_GROWTH_BOOST = "growth_boost";

    // 弓系
    public static final String STAT_ARROW_DAMAGE = "arrow_damage";
    public static final String STAT_DRAW_SPEED = "draw_speed";
    public static final String STAT_ACCURACY = "accuracy";
    public static final String STAT_ARROW_VELOCITY = "arrow_velocity";
    public static final String STAT_HEADSHOT_DAMAGE = "headshot_damage";

    // 釣り竿系
    public static final String STAT_FISHING_SPEED = "fishing_speed";
    public static final String STAT_LUCK_OF_SEA = "luck_of_sea";
    public static final String STAT_TREASURE_RATE = "treasure_rate";
    public static final String STAT_JUNK_RATE = "junk_rate";

    // ハサミ系
    public static final String STAT_SHEARING_SPEED = "shearing_speed";
    public static final String STAT_WOOL_DROPS = "wool_drops";
    public static final String STAT_PLANT_DROPS = "plant_drops";
    public static final String STAT_VINE_DROPS = "vine_drops";

    // その他
    public static final String STAT_MOVEMENT_SPEED = "movement_speed";
    public static final String STAT_NORMAL_DROPS = "normal_drops";

    // ============================================
    // 集計結果クラス
    // ============================================

    /**
     * 集計されたスキル効果
     */
    public record AggregatedEffects(
            // ステータス修正（ID → 修正値リスト）
            Map<String, StatModifiers> statModifiers,
            // 特殊効果（ID → 値）
            Map<String, Float> specialEffects,
            // 属性ダメージ（ID → 値）
            Map<String, Float> elementDamage
    ) {
        public static final AggregatedEffects EMPTY = new AggregatedEffects(
                Map.of(), Map.of(), Map.of()
        );

        /**
         * 特定のステータス修正を取得
         */
        public StatModifiers getStatModifiers(String statId) {
            return statModifiers.getOrDefault(statId, StatModifiers.ZERO);
        }

        /**
         * 特殊効果値を取得
         */
        public float getSpecialEffect(String effectId) {
            return specialEffects.getOrDefault(effectId, 0f);
        }

        /**
         * 特殊効果が有効かどうか
         */
        public boolean hasSpecialEffect(String effectId) {
            return specialEffects.containsKey(effectId) && specialEffects.get(effectId) > 0;
        }

        /**
         * 属性ダメージを取得
         */
        public float getElementDamage(String elementId) {
            return elementDamage.getOrDefault(elementId, 0f);
        }
    }

    /**
     * ステータス修正値
     */
    public record StatModifiers(
            float flat,      // 固定値加算
            float percent,   // パーセント加算
            float multiply   // 乗算
    ) {
        public static final StatModifiers ZERO = new StatModifiers(0, 0, 0);

        /**
         * 基本値に修正を適用
         */
        public float apply(float base) {
            // 計算順序: (base + flat) * (1 + percent/100) * (1 + multiply/100)
            float result = base + flat;
            result *= (1 + percent / 100f);
            result *= (1 + multiply / 100f);
            return result;
        }

        /**
         * 修正値を合算
         */
        public StatModifiers add(StatModifiers other) {
            return new StatModifiers(
                    this.flat + other.flat,
                    this.percent + other.percent,
                    this.multiply + other.multiply
            );
        }
    }

    // ============================================
    // 効果集計
    // ============================================

    /**
     * ItemStackからスキル効果を集計
     *
     * @param stack ツールItemStack
     * @return 集計された効果
     */
    public static AggregatedEffects aggregateEffects(ItemStack stack) {
        // スキル配分を取得
        SkillAllocation allocation = stack.get(AnvilDataComponents.SKILL_ALLOCATION.get());
        if (allocation == null || allocation.allocatedNodes().isEmpty()) {
            return AggregatedEffects.EMPTY;
        }

        // スキルツリーを取得
        SkillTree tree = SkillTreeRegistry.getTree(allocation.treeId());
        if (tree == null) {
            ANVIL.LOGGER.warn("スキルツリーが見つかりません: {}", allocation.treeId());
            return AggregatedEffects.EMPTY;
        }

        return aggregateEffects(tree, allocation);
    }

    /**
     * スキルツリーと配分から効果を集計
     *
     * @param tree スキルツリー
     * @param allocation スキル配分
     * @return 集計された効果
     */
    public static AggregatedEffects aggregateEffects(SkillTree tree, SkillAllocation allocation) {
        // 集計用マップ
        Map<String, StatModifiers> statModifiers = new HashMap<>();
        Map<String, Float> specialEffects = new HashMap<>();
        Map<String, Float> elementDamage = new HashMap<>();

        // 解放済みノードの効果を集計
        for (ResourceLocation nodeId : allocation.allocatedNodes()) {
            SkillNode node = tree.getNode(nodeId).orElse(null);
            if (node == null) {
                continue;
            }

            // ノードの全効果を処理
            for (SkillEffect effect : node.effects()) {
                switch (effect.type()) {
                    case STAT -> {
                        // ステータス修正を集計
                        StatModifiers current = statModifiers.getOrDefault(effect.targetId(), StatModifiers.ZERO);
                        StatModifiers addition = switch (effect.operation()) {
                            case FLAT -> new StatModifiers(effect.value(), 0, 0);
                            case PERCENT -> new StatModifiers(0, effect.value(), 0);
                            case MULTIPLY -> new StatModifiers(0, 0, effect.value());
                            case SPECIAL -> StatModifiers.ZERO;
                        };
                        statModifiers.put(effect.targetId(), current.add(addition));
                    }
                    case SPECIAL -> {
                        // 特殊効果は最大値または合算（効果による）
                        float current = specialEffects.getOrDefault(effect.targetId(), 0f);
                        // 多くの特殊効果は合算
                        specialEffects.put(effect.targetId(), current + effect.value());
                    }
                    case ELEMENT -> {
                        // 属性ダメージは合算
                        float current = elementDamage.getOrDefault(effect.targetId(), 0f);
                        elementDamage.put(effect.targetId(), current + effect.value());
                    }
                    case RESOURCE -> {
                        // リソース修正はSTATと同様に処理
                        StatModifiers current = statModifiers.getOrDefault(effect.targetId(), StatModifiers.ZERO);
                        StatModifiers addition = switch (effect.operation()) {
                            case FLAT -> new StatModifiers(effect.value(), 0, 0);
                            case PERCENT -> new StatModifiers(0, effect.value(), 0);
                            case MULTIPLY -> new StatModifiers(0, 0, effect.value());
                            case SPECIAL -> StatModifiers.ZERO;
                        };
                        statModifiers.put(effect.targetId(), current.add(addition));
                    }
                }
            }
        }

        return new AggregatedEffects(
                Map.copyOf(statModifiers),
                Map.copyOf(specialEffects),
                Map.copyOf(elementDamage)
        );
    }

    // ============================================
    // ステータス適用
    // ============================================

    /**
     * 基本ステータスにスキル効果を適用
     *
     * @param baseStats 基本ステータス
     * @param effects 集計された効果
     * @return 効果適用後のステータス
     */
    public static CalculatedStats applyToStats(CalculatedStats baseStats, AggregatedEffects effects) {
        CalculatedStats.Builder builder = CalculatedStats.builder();

        // 耐久値
        float durability = effects.getStatModifiers(STAT_DURABILITY).apply(baseStats.durability());
        builder.durability(Math.max(1, (int) durability));

        // 採掘速度（mining_speedとchopping_speed、digging_speedを統合）
        float miningSpeedMod = effects.getStatModifiers(STAT_MINING_SPEED).percent
                + effects.getStatModifiers(STAT_CHOPPING_SPEED).percent
                + effects.getStatModifiers(STAT_DIGGING_SPEED).percent;
        StatModifiers combinedMiningMod = new StatModifiers(
                effects.getStatModifiers(STAT_MINING_SPEED).flat,
                miningSpeedMod,
                effects.getStatModifiers(STAT_MINING_SPEED).multiply
        );
        builder.miningSpeed(Math.max(0.1f, combinedMiningMod.apply(baseStats.miningSpeed())));

        // 採掘レベル
        builder.miningLevel(baseStats.miningLevel());

        // 攻撃力
        float attackDamage = effects.getStatModifiers(STAT_ATTACK_DAMAGE).apply(baseStats.attackDamage());
        builder.attackDamage(Math.max(0.5f, attackDamage));

        // 攻撃速度
        float attackSpeed = effects.getStatModifiers(STAT_ATTACK_SPEED).apply(baseStats.attackSpeed());
        builder.attackSpeed(Math.max(0.1f, attackSpeed));

        // MOD容量
        builder.modCapacity(baseStats.modCapacity());

        // 引き速度（弓）
        float drawSpeed = effects.getStatModifiers(STAT_DRAW_SPEED).apply(baseStats.drawSpeed());
        builder.drawSpeed(Math.max(0.1f, drawSpeed));

        // 射程（弓）
        builder.range(baseStats.range());

        // 釣り効率
        float fishingEff = effects.getStatModifiers(STAT_FISHING_SPEED).apply(baseStats.fishingEfficiency());
        builder.fishingEfficiency(Math.max(0.1f, fishingEff));

        // 切断効率（ハサミ）
        float cuttingEff = effects.getStatModifiers(STAT_SHEARING_SPEED).apply(baseStats.cuttingEfficiency());
        builder.cuttingEfficiency(Math.max(0.1f, cuttingEff));

        return builder.build();
    }

    /**
     * ItemStackの基本ステータスにスキル効果を適用
     *
     * @param stack ツールItemStack
     * @param baseStats 基本ステータス
     * @return 効果適用後のステータス
     */
    public static CalculatedStats applySkillEffects(ItemStack stack, CalculatedStats baseStats) {
        AggregatedEffects effects = aggregateEffects(stack);
        return applyToStats(baseStats, effects);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 特定のステータス修正率を取得（UI表示用）
     *
     * @param stack ツールItemStack
     * @param statId ステータスID
     * @return パーセント修正値の合計
     */
    public static float getTotalPercentBonus(ItemStack stack, String statId) {
        AggregatedEffects effects = aggregateEffects(stack);
        StatModifiers modifiers = effects.getStatModifiers(statId);
        return modifiers.percent;
    }

    /**
     * 特殊効果が有効かどうかを確認
     *
     * @param stack ツールItemStack
     * @param effectId 特殊効果ID
     * @return 有効ならtrue
     */
    public static boolean hasSpecialEffect(ItemStack stack, String effectId) {
        AggregatedEffects effects = aggregateEffects(stack);
        return effects.hasSpecialEffect(effectId);
    }

    /**
     * 特殊効果の値を取得
     *
     * @param stack ツールItemStack
     * @param effectId 特殊効果ID
     * @return 効果値（無効なら0）
     */
    public static float getSpecialEffectValue(ItemStack stack, String effectId) {
        AggregatedEffects effects = aggregateEffects(stack);
        return effects.getSpecialEffect(effectId);
    }

    // コンストラクタを非公開
    private SkillEffectApplier() {}
}
