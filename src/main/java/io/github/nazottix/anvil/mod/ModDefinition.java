package io.github.nazottix.anvil.mod;

import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

/**
 * MOD定義
 *
 * MODの基本情報（名前、レアリティ、効果など）を定義します。
 * このクラスはレジストリに登録され、MODインスタンスのテンプレートとして使用されます。
 *
 * 仕様書参照: docs/05_容量制限システム.md
 */
public record ModDefinition(
        // MODの一意識別子
        ResourceLocation id,
        // MODタイプ
        ModType type,
        // MODレアリティ
        ModRarity rarity,
        // 基本ドレイン（容量消費）
        int baseDrain,
        // 極性
        Polarity polarity,
        // 最大ランク（0〜maxRank）
        int maxRank,
        // MODの効果リスト
        List<ModEffect> effects,
        // セットID（セットMODの場合）
        String setId,
        // 使用可能なツールタイプ（空の場合は全ツール）
        Set<ToolType> compatibleTools
) {

    // ============================================
    // ビルダー
    // ============================================

    /**
     * MOD定義ビルダー
     */
    public static class Builder {
        private ResourceLocation id;
        private ModType type = ModType.STANDARD;
        private ModRarity rarity = ModRarity.COMMON;
        private int baseDrain = 4;
        private Polarity polarity = Polarity.NARAMON;
        private int maxRank = 5;
        private List<ModEffect> effects = List.of();
        private String setId = "";
        private Set<ToolType> compatibleTools = Set.of();

        public Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder(String namespace, String path) {
            this.id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        }

        public Builder type(ModType type) {
            this.type = type;
            return this;
        }

        public Builder rarity(ModRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder baseDrain(int baseDrain) {
            this.baseDrain = baseDrain;
            return this;
        }

        public Builder polarity(Polarity polarity) {
            this.polarity = polarity;
            return this;
        }

        public Builder maxRank(int maxRank) {
            this.maxRank = maxRank;
            return this;
        }

        public Builder effects(List<ModEffect> effects) {
            this.effects = effects;
            return this;
        }

        public Builder effects(ModEffect... effects) {
            this.effects = List.of(effects);
            return this;
        }

        public Builder setId(String setId) {
            this.setId = setId;
            return this;
        }

        public Builder compatibleTools(Set<ToolType> compatibleTools) {
            this.compatibleTools = compatibleTools;
            return this;
        }

        public Builder compatibleTools(ToolType... tools) {
            this.compatibleTools = Set.of(tools);
            return this;
        }

        public ModDefinition build() {
            return new ModDefinition(
                    id, type, rarity, baseDrain, polarity,
                    maxRank, effects, setId, compatibleTools
            );
        }
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * オーラMODかどうか
     */
    public boolean isAura() {
        return type == ModType.AURA;
    }

    /**
     * エクシルスMODかどうか
     */
    public boolean isExilus() {
        return type == ModType.EXILUS;
    }

    /**
     * セットMODかどうか
     */
    public boolean isSetMod() {
        return type == ModType.SET && !setId.isEmpty();
    }

    /**
     * 腐敗MODかどうか
     */
    public boolean isCorrupted() {
        return type == ModType.CORRUPTED;
    }

    /**
     * 指定したツールタイプと互換性があるか
     *
     * @param toolType ツールタイプ
     * @return 互換性がある場合true
     */
    public boolean isCompatibleWith(ToolType toolType) {
        // 互換ツールが指定されていない場合は全ツールと互換
        if (compatibleTools.isEmpty()) {
            return true;
        }
        return compatibleTools.contains(toolType);
    }

    /**
     * ランクに応じたドレイン値を計算
     *
     * @param rank 現在のランク
     * @return 計算後のドレイン値
     */
    public int getDrainAtRank(int rank) {
        // ランクが上がるとドレインも増加（+1/ランク）
        return baseDrain + rank;
    }

    /**
     * デメリットを持つ効果があるか
     */
    public boolean hasDrawbackEffect() {
        return effects.stream().anyMatch(ModEffect::isDrawback);
    }
}
