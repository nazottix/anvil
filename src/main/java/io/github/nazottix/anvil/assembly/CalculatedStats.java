package io.github.nazottix.anvil.assembly;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 計算済みツールステータス
 *
 * パーツ、素材、グレードから計算された最終的なツールステータスを保持します。
 * このデータはツールアイテムに保存され、ゲーム中に参照されます。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public record CalculatedStats(
        // 最終耐久値
        int durability,
        // 採掘速度
        float miningSpeed,
        // 採掘レベル（0-6）
        int miningLevel,
        // 攻撃力
        float attackDamage,
        // 攻撃速度
        float attackSpeed,
        // MOD容量
        int modCapacity,
        // 引き速度（弓用）
        float drawSpeed,
        // 射程（弓用）
        float range,
        // 釣り効率（釣り竿用）
        float fishingEfficiency,
        // 切断効率（ハサミ用）
        float cuttingEfficiency
) {
    /**
     * デフォルト値
     */
    public static final CalculatedStats DEFAULT = new CalculatedStats(
            100, 1.0f, 0, 1.0f, 1.0f, 30, 1.0f, 1.0f, 1.0f, 1.0f
    );

    /**
     * Codec - データ保存用
     */
    public static final Codec<CalculatedStats> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("durability", 100).forGetter(CalculatedStats::durability),
                    Codec.FLOAT.optionalFieldOf("mining_speed", 1.0f).forGetter(CalculatedStats::miningSpeed),
                    Codec.INT.optionalFieldOf("mining_level", 0).forGetter(CalculatedStats::miningLevel),
                    Codec.FLOAT.optionalFieldOf("attack_damage", 1.0f).forGetter(CalculatedStats::attackDamage),
                    Codec.FLOAT.optionalFieldOf("attack_speed", 1.0f).forGetter(CalculatedStats::attackSpeed),
                    Codec.INT.optionalFieldOf("mod_capacity", 30).forGetter(CalculatedStats::modCapacity),
                    Codec.FLOAT.optionalFieldOf("draw_speed", 1.0f).forGetter(CalculatedStats::drawSpeed),
                    Codec.FLOAT.optionalFieldOf("range", 1.0f).forGetter(CalculatedStats::range),
                    Codec.FLOAT.optionalFieldOf("fishing_efficiency", 1.0f).forGetter(CalculatedStats::fishingEfficiency),
                    Codec.FLOAT.optionalFieldOf("cutting_efficiency", 1.0f).forGetter(CalculatedStats::cuttingEfficiency)
            ).apply(instance, CalculatedStats::new)
    );

    /**
     * StreamCodec - ネットワーク通信用
     *
     * 10フィールドを超えるため、カスタム実装を使用します
     */
    public static final StreamCodec<ByteBuf, CalculatedStats> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CalculatedStats decode(ByteBuf buf) {
            // 各フィールドをバッファから読み取り
            int durability = ByteBufCodecs.VAR_INT.decode(buf);
            float miningSpeed = ByteBufCodecs.FLOAT.decode(buf);
            int miningLevel = ByteBufCodecs.VAR_INT.decode(buf);
            float attackDamage = ByteBufCodecs.FLOAT.decode(buf);
            float attackSpeed = ByteBufCodecs.FLOAT.decode(buf);
            int modCapacity = ByteBufCodecs.VAR_INT.decode(buf);
            float drawSpeed = ByteBufCodecs.FLOAT.decode(buf);
            float range = ByteBufCodecs.FLOAT.decode(buf);
            float fishingEfficiency = ByteBufCodecs.FLOAT.decode(buf);
            float cuttingEfficiency = ByteBufCodecs.FLOAT.decode(buf);

            return new CalculatedStats(
                durability, miningSpeed, miningLevel, attackDamage, attackSpeed,
                modCapacity, drawSpeed, range, fishingEfficiency, cuttingEfficiency
            );
        }

        @Override
        public void encode(ByteBuf buf, CalculatedStats stats) {
            // 各フィールドをバッファに書き込み
            ByteBufCodecs.VAR_INT.encode(buf, stats.durability());
            ByteBufCodecs.FLOAT.encode(buf, stats.miningSpeed());
            ByteBufCodecs.VAR_INT.encode(buf, stats.miningLevel());
            ByteBufCodecs.FLOAT.encode(buf, stats.attackDamage());
            ByteBufCodecs.FLOAT.encode(buf, stats.attackSpeed());
            ByteBufCodecs.VAR_INT.encode(buf, stats.modCapacity());
            ByteBufCodecs.FLOAT.encode(buf, stats.drawSpeed());
            ByteBufCodecs.FLOAT.encode(buf, stats.range());
            ByteBufCodecs.FLOAT.encode(buf, stats.fishingEfficiency());
            ByteBufCodecs.FLOAT.encode(buf, stats.cuttingEfficiency());
        }
    };

    /**
     * ビルダーを作成
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * CalculatedStatsビルダー
     */
    public static class Builder {
        private int durability = 100;
        private float miningSpeed = 1.0f;
        private int miningLevel = 0;
        private float attackDamage = 1.0f;
        private float attackSpeed = 1.0f;
        private int modCapacity = 30;
        private float drawSpeed = 1.0f;
        private float range = 1.0f;
        private float fishingEfficiency = 1.0f;
        private float cuttingEfficiency = 1.0f;

        public Builder durability(int durability) {
            this.durability = durability;
            return this;
        }

        public Builder miningSpeed(float miningSpeed) {
            this.miningSpeed = miningSpeed;
            return this;
        }

        public Builder miningLevel(int miningLevel) {
            this.miningLevel = miningLevel;
            return this;
        }

        public Builder attackDamage(float attackDamage) {
            this.attackDamage = attackDamage;
            return this;
        }

        public Builder attackSpeed(float attackSpeed) {
            this.attackSpeed = attackSpeed;
            return this;
        }

        public Builder modCapacity(int modCapacity) {
            this.modCapacity = modCapacity;
            return this;
        }

        public Builder drawSpeed(float drawSpeed) {
            this.drawSpeed = drawSpeed;
            return this;
        }

        public Builder range(float range) {
            this.range = range;
            return this;
        }

        public Builder fishingEfficiency(float fishingEfficiency) {
            this.fishingEfficiency = fishingEfficiency;
            return this;
        }

        public Builder cuttingEfficiency(float cuttingEfficiency) {
            this.cuttingEfficiency = cuttingEfficiency;
            return this;
        }

        public CalculatedStats build() {
            return new CalculatedStats(
                    durability, miningSpeed, miningLevel, attackDamage, attackSpeed,
                    modCapacity, drawSpeed, range, fishingEfficiency, cuttingEfficiency
            );
        }
    }
}
