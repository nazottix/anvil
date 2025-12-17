package io.github.nazottix.anvil.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * アフィックス効果レコード
 *
 * アフィックスが付与するステータス効果を定義します。
 *
 * @param statId 影響するステータスID
 * @param value 効果の値
 * @param isMultiplier true: 乗算効果（%）、false: 加算効果
 */
public record AffixEffect(
        String statId,
        float value,
        boolean isMultiplier
) {

    // ============================================
    // シリアライズ用Codec
    // ============================================

    public static final Codec<AffixEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("stat_id").forGetter(AffixEffect::statId),
                    Codec.FLOAT.fieldOf("value").forGetter(AffixEffect::value),
                    Codec.BOOL.fieldOf("is_multiplier").forGetter(AffixEffect::isMultiplier)
            ).apply(instance, AffixEffect::new)
    );

    public static final StreamCodec<ByteBuf, AffixEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AffixEffect::statId,
            ByteBufCodecs.FLOAT, AffixEffect::value,
            ByteBufCodecs.BOOL, AffixEffect::isMultiplier,
            AffixEffect::new
    );

    // ============================================
    // ユーティリティメソッド
    // ============================================

    /**
     * 効果を適用した値を計算
     *
     * @param baseValue 基本値
     * @return 効果適用後の値
     */
    public float applyTo(float baseValue) {
        if (isMultiplier) {
            // 乗算効果：baseValue * (1 + value/100)
            return baseValue * (1.0f + value / 100.0f);
        } else {
            // 加算効果：baseValue + value
            return baseValue + value;
        }
    }

    /**
     * 表示用の値文字列を取得
     *
     * @return 表示文字列（例："+15%"、"+50"）
     */
    public String getDisplayValue() {
        if (isMultiplier) {
            return String.format("%+.1f%%", value);
        } else {
            if (value == (int) value) {
                return String.format("%+d", (int) value);
            }
            return String.format("%+.1f", value);
        }
    }

    /**
     * ローカライズキーを取得
     */
    public String getStatTranslationKey() {
        return "stat.anvil." + statId;
    }
}
