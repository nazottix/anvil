package io.github.nazottix.anvil.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * アフィックスインスタンスレコード
 *
 * ツールに実際に付与されたアフィックスを表します。
 * アフィックスの定義とティアの組み合わせを保持します。
 *
 * @param affixId アフィックスID
 * @param tierLevel ティアレベル（1-10）
 */
public record AffixInstance(
        String affixId,
        int tierLevel
) {

    // ============================================
    // シリアライズ用Codec
    // ============================================

    public static final Codec<AffixInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("affix_id").forGetter(AffixInstance::affixId),
                    Codec.INT.fieldOf("tier").forGetter(AffixInstance::tierLevel)
            ).apply(instance, AffixInstance::new)
    );

    public static final StreamCodec<ByteBuf, AffixInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AffixInstance::affixId,
            ByteBufCodecs.INT, AffixInstance::tierLevel,
            AffixInstance::new
    );

    // ============================================
    // コンストラクタ
    // ============================================

    /**
     * AffixとAffixTierから作成するコンストラクタ
     */
    public AffixInstance(Affix affix, AffixTier tier) {
        this(affix.getId(), tier.getTier());
    }

    // ============================================
    // ゲッター
    // ============================================

    /**
     * アフィックス定義を取得
     *
     * @return アフィックス、見つからない場合はnull
     */
    public Affix affix() {
        return AffixRegistry.get(affixId);
    }

    /**
     * ティアを取得
     *
     * @return ティア
     */
    public AffixTier tier() {
        return AffixTier.fromTier(tierLevel);
    }

    /**
     * 効果を取得
     *
     * @return 効果、見つからない場合はnull
     */
    public AffixEffect effect() {
        Affix a = affix();
        if (a == null) return null;
        return a.getEffect(tier());
    }

    /**
     * アフィックスタイプを取得
     *
     * @return アフィックスタイプ、見つからない場合はnull
     */
    public AffixType type() {
        Affix a = affix();
        return a != null ? a.getType() : null;
    }

    // ============================================
    // 表示用メソッド
    // ============================================

    /**
     * 表示名を取得（ローカライズキー）
     */
    public String getDisplayNameKey() {
        Affix a = affix();
        return a != null ? a.getTranslationKey() : "affix.anvil.unknown";
    }

    /**
     * 効果の表示文字列を取得
     */
    public String getEffectDisplay() {
        AffixEffect e = effect();
        return e != null ? e.getDisplayValue() : "???";
    }

    /**
     * ツールチップ用の完全な表示文字列を取得
     * 例：「鋭利な (T5): 攻撃力+25%」
     */
    public String getTooltipLine() {
        Affix a = affix();
        AffixEffect e = effect();
        if (a == null || e == null) {
            return "Unknown Affix";
        }

        return String.format("%s (T%d): %s %s",
                a.getNameKey(),
                tierLevel,
                e.statId(),
                e.getDisplayValue());
    }
}
