package io.github.nazottix.anvil.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * ツールパーツデータ
 *
 * ツールを構成する個々のパーツの情報を保持します。
 * 各パーツは素材ID、パーツタイプ、グレードを持ちます。
 *
 * 例: ダイヤモンド製のヘッドパーツ（グレードA）
 * - materialId: "minecraft:diamond"
 * - partType: "head"
 * - grade: "a"
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public record ToolPart(
        // 素材ID（例: "anvil:diamond", "anvil:netherite"）
        String materialId,
        // パーツタイプID（例: "head", "handle", "binding"）
        String partType,
        // グレードID（例: "c", "a", "sss"）
        String grade
) {
    /**
     * Codec - データの保存/読み込み用（NBT、JSON）
     */
    public static final Codec<ToolPart> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // 素材ID
                    Codec.STRING.fieldOf("material_id").forGetter(ToolPart::materialId),
                    // パーツタイプID
                    Codec.STRING.fieldOf("part_type").forGetter(ToolPart::partType),
                    // グレードID（デフォルト: "c"）
                    Codec.STRING.optionalFieldOf("grade", "c").forGetter(ToolPart::grade)
            ).apply(instance, ToolPart::new)
    );

    /**
     * StreamCodec - ネットワーク通信用
     */
    public static final StreamCodec<ByteBuf, ToolPart> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ToolPart::materialId,
            ByteBufCodecs.STRING_UTF8, ToolPart::partType,
            ByteBufCodecs.STRING_UTF8, ToolPart::grade,
            ToolPart::new
    );

    /**
     * デフォルトグレード（C）で新しいパーツを作成
     *
     * @param materialId 素材ID
     * @param partType パーツタイプID
     * @return 新しいToolPart
     */
    public static ToolPart withDefaultGrade(String materialId, String partType) {
        return new ToolPart(materialId, partType, "c");
    }

    /**
     * グレードを変更した新しいインスタンスを返す
     *
     * @param newGrade 新しいグレードID
     * @return 新しいToolPart
     */
    public ToolPart withGrade(String newGrade) {
        return new ToolPart(this.materialId, this.partType, newGrade);
    }
}
