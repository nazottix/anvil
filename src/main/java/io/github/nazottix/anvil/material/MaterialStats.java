package io.github.nazottix.anvil.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 素材ステータスデータクラス
 *
 * パーツタイプごとの素材ステータスを保持します。
 * 各パーツタイプ（ヘッド、ハンドル、バインディング等）は
 * 異なるステータスを持ちます。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.4.1 素材データ構造
 */
public class MaterialStats {

    /**
     * ヘッドパーツ用ステータス
     *
     * @param durability 基本耐久値
     * @param miningSpeed 採掘速度
     * @param miningLevel 採掘レベル（0-6）
     * @param attackDamage 攻撃力
     */
    public record HeadStats(
            int durability,
            float miningSpeed,
            int miningLevel,
            float attackDamage
    ) {
        // デフォルト値でのインスタンス作成
        public static final HeadStats DEFAULT = new HeadStats(100, 1.0f, 0, 1.0f);

        // Codec for serialization（NBT、JSON保存用）
        public static final Codec<HeadStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("durability").forGetter(HeadStats::durability),
                        Codec.FLOAT.fieldOf("mining_speed").forGetter(HeadStats::miningSpeed),
                        Codec.INT.fieldOf("mining_level").forGetter(HeadStats::miningLevel),
                        Codec.FLOAT.fieldOf("attack_damage").forGetter(HeadStats::attackDamage)
                ).apply(instance, HeadStats::new)
        );

        /**
         * グレード乗数を適用したステータスを返す
         *
         * @param gradeMultiplier グレード乗数
         * @return 乗数適用後のステータス
         */
        public HeadStats withGradeMultiplier(double gradeMultiplier) {
            return new HeadStats(
                    (int) (durability * gradeMultiplier),
                    (float) (miningSpeed * gradeMultiplier),
                    miningLevel,  // 採掘レベルは乗数の影響を受けない
                    (float) (attackDamage * gradeMultiplier)
            );
        }
    }

    /**
     * ハンドルパーツ用ステータス
     *
     * @param durabilityMultiplier 耐久値乗数（1.0 = 100%）
     * @param attackSpeedModifier 攻撃速度修正値
     */
    public record HandleStats(
            float durabilityMultiplier,
            float attackSpeedModifier
    ) {
        // デフォルト値
        public static final HandleStats DEFAULT = new HandleStats(1.0f, 0.0f);

        // Codec for serialization
        public static final Codec<HandleStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("durability_multiplier").forGetter(HandleStats::durabilityMultiplier),
                        Codec.FLOAT.fieldOf("attack_speed_modifier").forGetter(HandleStats::attackSpeedModifier)
                ).apply(instance, HandleStats::new)
        );

        /**
         * グレード乗数を適用
         */
        public HandleStats withGradeMultiplier(double gradeMultiplier) {
            // 乗数は1.0を基準に調整、修正値はそのまま乗算
            double adjustedMultiplier = 1.0 + (durabilityMultiplier - 1.0) * gradeMultiplier;
            return new HandleStats(
                    (float) adjustedMultiplier,
                    (float) (attackSpeedModifier * gradeMultiplier)
            );
        }
    }

    /**
     * バインディングパーツ用ステータス
     *
     * @param traitAmplifier 特性増幅値（1.0 = 100%）
     */
    public record BindingStats(
            float traitAmplifier
    ) {
        // デフォルト値
        public static final BindingStats DEFAULT = new BindingStats(1.0f);

        // Codec for serialization
        public static final Codec<BindingStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("trait_amplifier").forGetter(BindingStats::traitAmplifier)
                ).apply(instance, BindingStats::new)
        );

        /**
         * グレード乗数を適用
         */
        public BindingStats withGradeMultiplier(double gradeMultiplier) {
            double adjusted = 1.0 + (traitAmplifier - 1.0) * gradeMultiplier;
            return new BindingStats((float) adjusted);
        }
    }

    /**
     * 弓リム用ステータス
     *
     * @param drawSpeed 引き速度（高いほど速い）
     * @param range 射程距離
     */
    public record BowLimbStats(
            float drawSpeed,
            float range
    ) {
        // デフォルト値
        public static final BowLimbStats DEFAULT = new BowLimbStats(1.0f, 1.0f);

        // Codec for serialization
        public static final Codec<BowLimbStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("draw_speed").forGetter(BowLimbStats::drawSpeed),
                        Codec.FLOAT.fieldOf("range").forGetter(BowLimbStats::range)
                ).apply(instance, BowLimbStats::new)
        );

        public BowLimbStats withGradeMultiplier(double gradeMultiplier) {
            return new BowLimbStats(
                    (float) (drawSpeed * gradeMultiplier),
                    (float) (range * gradeMultiplier)
            );
        }
    }

    /**
     * 弓弦用ステータス
     *
     * @param arrowSpeed 矢速度
     * @param accuracy 精度（1.0 = 100%命中）
     */
    public record BowstringStats(
            float arrowSpeed,
            float accuracy
    ) {
        // デフォルト値
        public static final BowstringStats DEFAULT = new BowstringStats(1.0f, 1.0f);

        // Codec for serialization
        public static final Codec<BowstringStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("arrow_speed").forGetter(BowstringStats::arrowSpeed),
                        Codec.FLOAT.fieldOf("accuracy").forGetter(BowstringStats::accuracy)
                ).apply(instance, BowstringStats::new)
        );

        public BowstringStats withGradeMultiplier(double gradeMultiplier) {
            return new BowstringStats(
                    (float) (arrowSpeed * gradeMultiplier),
                    // 精度は1.0を超えないように制限
                    (float) Math.min(1.0, accuracy * gradeMultiplier)
            );
        }
    }

    /**
     * 釣り針用ステータス
     *
     * @param fishingEfficiency 釣り効率
     * @param rareDropChance レアドロップ確率ボーナス
     */
    public record HookStats(
            float fishingEfficiency,
            float rareDropChance
    ) {
        // デフォルト値
        public static final HookStats DEFAULT = new HookStats(1.0f, 0.0f);

        // Codec for serialization
        public static final Codec<HookStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("fishing_efficiency").forGetter(HookStats::fishingEfficiency),
                        Codec.FLOAT.fieldOf("rare_drop_chance").forGetter(HookStats::rareDropChance)
                ).apply(instance, HookStats::new)
        );

        public HookStats withGradeMultiplier(double gradeMultiplier) {
            return new HookStats(
                    (float) (fishingEfficiency * gradeMultiplier),
                    (float) (rareDropChance * gradeMultiplier)
            );
        }
    }

    /**
     * 追加パーツ（コーティング）用ステータス
     *
     * @param allStatsMultiplier 全ステータス乗数
     */
    public record CoatingStats(
            float allStatsMultiplier
    ) {
        // デフォルト値
        public static final CoatingStats DEFAULT = new CoatingStats(1.0f);

        // Codec for serialization
        public static final Codec<CoatingStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.fieldOf("all_stats_multiplier").forGetter(CoatingStats::allStatsMultiplier)
                ).apply(instance, CoatingStats::new)
        );

        public CoatingStats withGradeMultiplier(double gradeMultiplier) {
            double adjusted = 1.0 + (allStatsMultiplier - 1.0) * gradeMultiplier;
            return new CoatingStats((float) adjusted);
        }
    }

    /**
     * 追加パーツ（アップグレード）用ステータス
     *
     * @param modSlotBonus 追加MODスロット数
     */
    public record UpgradeStats(
            int modSlotBonus
    ) {
        // デフォルト値
        public static final UpgradeStats DEFAULT = new UpgradeStats(0);

        // Codec for serialization
        public static final Codec<UpgradeStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("mod_slot_bonus").forGetter(UpgradeStats::modSlotBonus)
                ).apply(instance, UpgradeStats::new)
        );

        // MODスロットはグレードで変動しない
        public UpgradeStats withGradeMultiplier(double gradeMultiplier) {
            return this;
        }
    }
}
