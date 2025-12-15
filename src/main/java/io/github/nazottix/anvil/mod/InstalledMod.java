package io.github.nazottix.anvil.mod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 装着されたMODのインスタンス
 *
 * ツールに実際に装着されているMODを表します。
 * MOD定義への参照と、現在のランクを保持します。
 */
public record InstalledMod(
        // MOD定義のID
        ResourceLocation modId,
        // 現在のランク（0〜maxRank）
        int rank
) {

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<InstalledMod> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("mod_id").forGetter(InstalledMod::modId),
                    Codec.INT.optionalFieldOf("rank", 0).forGetter(InstalledMod::rank)
            ).apply(instance, InstalledMod::new)
    );

    /**
     * ネットワーク通信用StreamCodec
     */
    public static final StreamCodec<ByteBuf, InstalledMod> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public InstalledMod decode(ByteBuf buf) {
            String namespace = ByteBufCodecs.STRING_UTF8.decode(buf);
            String path = ByteBufCodecs.STRING_UTF8.decode(buf);
            int rank = ByteBufCodecs.VAR_INT.decode(buf);
            return new InstalledMod(ResourceLocation.fromNamespaceAndPath(namespace, path), rank);
        }

        @Override
        public void encode(ByteBuf buf, InstalledMod mod) {
            ByteBufCodecs.STRING_UTF8.encode(buf, mod.modId().getNamespace());
            ByteBufCodecs.STRING_UTF8.encode(buf, mod.modId().getPath());
            ByteBufCodecs.VAR_INT.encode(buf, mod.rank());
        }
    };

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * ランク0のMODを作成
     *
     * @param modId MOD定義のID
     * @return 新しいInstalledMod
     */
    public static InstalledMod create(ResourceLocation modId) {
        return new InstalledMod(modId, 0);
    }

    /**
     * 文字列IDからランク0のMODを作成
     *
     * @param namespace 名前空間
     * @param path パス
     * @return 新しいInstalledMod
     */
    public static InstalledMod create(String namespace, String path) {
        return new InstalledMod(ResourceLocation.fromNamespaceAndPath(namespace, path), 0);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * ランクを上げた新しいインスタンスを返す
     *
     * @param newRank 新しいランク
     * @return 新しいInstalledMod
     */
    public InstalledMod withRank(int newRank) {
        return new InstalledMod(this.modId, newRank);
    }

    /**
     * ランクを1上げた新しいインスタンスを返す
     *
     * @param maxRank 最大ランク（超えない）
     * @return 新しいInstalledMod
     */
    public InstalledMod rankUp(int maxRank) {
        int newRank = Math.min(this.rank + 1, maxRank);
        return new InstalledMod(this.modId, newRank);
    }

    /**
     * 最大ランクに達しているか
     *
     * @param maxRank 最大ランク
     * @return 最大ランクの場合true
     */
    public boolean isMaxRank(int maxRank) {
        return rank >= maxRank;
    }
}
