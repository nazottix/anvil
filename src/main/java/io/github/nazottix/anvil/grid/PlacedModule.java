package io.github.nazottix.anvil.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 配置されたモジュール
 *
 * グリッドに実際に配置されているモジュールを表します。
 */
public record PlacedModule(
        // モジュールID
        ResourceLocation moduleId,
        // 配置X座標
        int x,
        // 配置Y座標
        int y,
        // 回転（0=0°, 1=90°, 2=180°, 3=270°）
        int rotation
) {

    // ============================================
    // Codec
    // ============================================

    public static final Codec<PlacedModule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("module_id").forGetter(PlacedModule::moduleId),
                    Codec.INT.fieldOf("x").forGetter(PlacedModule::x),
                    Codec.INT.fieldOf("y").forGetter(PlacedModule::y),
                    Codec.INT.optionalFieldOf("rotation", 0).forGetter(PlacedModule::rotation)
            ).apply(instance, PlacedModule::new)
    );

    public static final StreamCodec<ByteBuf, PlacedModule> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlacedModule decode(ByteBuf buf) {
            String namespace = ByteBufCodecs.STRING_UTF8.decode(buf);
            String path = ByteBufCodecs.STRING_UTF8.decode(buf);
            int x = ByteBufCodecs.VAR_INT.decode(buf);
            int y = ByteBufCodecs.VAR_INT.decode(buf);
            int rotation = ByteBufCodecs.VAR_INT.decode(buf);
            return new PlacedModule(
                    ResourceLocation.fromNamespaceAndPath(namespace, path),
                    x, y, rotation
            );
        }

        @Override
        public void encode(ByteBuf buf, PlacedModule module) {
            ByteBufCodecs.STRING_UTF8.encode(buf, module.moduleId().getNamespace());
            ByteBufCodecs.STRING_UTF8.encode(buf, module.moduleId().getPath());
            ByteBufCodecs.VAR_INT.encode(buf, module.x());
            ByteBufCodecs.VAR_INT.encode(buf, module.y());
            ByteBufCodecs.VAR_INT.encode(buf, module.rotation());
        }
    };

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * 回転なしで配置
     */
    public static PlacedModule create(ResourceLocation moduleId, int x, int y) {
        return new PlacedModule(moduleId, x, y, 0);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 回転を変更した新しいインスタンスを返す
     *
     * @param newRotation 新しい回転（0-3）
     * @return 新しいPlacedModule
     */
    public PlacedModule withRotation(int newRotation) {
        return new PlacedModule(this.moduleId, this.x, this.y, newRotation % 4);
    }

    /**
     * 位置を変更した新しいインスタンスを返す
     *
     * @param newX 新しいX座標
     * @param newY 新しいY座標
     * @return 新しいPlacedModule
     */
    public PlacedModule withPosition(int newX, int newY) {
        return new PlacedModule(this.moduleId, newX, newY, this.rotation);
    }

    /**
     * 回転を90度時計回りに回した新しいインスタンスを返す
     */
    public PlacedModule rotateClockwise() {
        return withRotation((rotation + 1) % 4);
    }

    /**
     * 回転を90度反時計回りに回した新しいインスタンスを返す
     */
    public PlacedModule rotateCounterClockwise() {
        return withRotation((rotation + 3) % 4);
    }
}
