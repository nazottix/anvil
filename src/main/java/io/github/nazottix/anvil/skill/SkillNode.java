package io.github.nazottix.anvil.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * スキルノード定義
 *
 * スキルツリーの1ノードを定義します。
 * Path of Exile風のノードシステム。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public record SkillNode(
        // ノードID
        ResourceLocation id,
        // ノードタイプ
        SkillNodeType type,
        // 表示名キー
        String nameKey,
        // 説明キー
        String descriptionKey,
        // ツリー上の位置
        int posX,
        int posY,
        // 効果リスト
        List<SkillEffect> effects,
        // 接続先ノードID
        List<ResourceLocation> connections,
        // ポイントコスト（-1でタイプデフォルト）
        int cost,
        // 解放条件
        NodeRequirements requirements,
        // アイコンテクスチャパス（nullでデフォルト）
        Optional<ResourceLocation> icon
) {

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<SkillNode> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(SkillNode::id),
                    Codec.STRING.fieldOf("type")
                            .xmap(SkillNodeType::fromId, SkillNodeType::getId)
                            .forGetter(SkillNode::type),
                    Codec.STRING.optionalFieldOf("name_key", "")
                            .forGetter(SkillNode::nameKey),
                    Codec.STRING.optionalFieldOf("description_key", "")
                            .forGetter(SkillNode::descriptionKey),
                    Codec.INT.fieldOf("x").forGetter(SkillNode::posX),
                    Codec.INT.fieldOf("y").forGetter(SkillNode::posY),
                    SkillEffect.CODEC.listOf().optionalFieldOf("effects", List.of())
                            .forGetter(SkillNode::effects),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("connections", List.of())
                            .forGetter(SkillNode::connections),
                    Codec.INT.optionalFieldOf("cost", -1).forGetter(SkillNode::cost),
                    NodeRequirements.CODEC.optionalFieldOf("requirements", NodeRequirements.NONE)
                            .forGetter(SkillNode::requirements),
                    ResourceLocation.CODEC.optionalFieldOf("icon")
                            .forGetter(SkillNode::icon)
            ).apply(instance, SkillNode::new)
    );

    // ============================================
    // 解放条件
    // ============================================

    /**
     * ノード解放条件
     */
    public record NodeRequirements(
            int minLevel,
            int prerequisiteNodes,
            List<ResourceLocation> requiredNodes
    ) {
        public static final NodeRequirements NONE = new NodeRequirements(0, 0, List.of());

        public static final Codec<NodeRequirements> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("min_level", 0).forGetter(NodeRequirements::minLevel),
                        Codec.INT.optionalFieldOf("prerequisite_nodes", 0).forGetter(NodeRequirements::prerequisiteNodes),
                        ResourceLocation.CODEC.listOf().optionalFieldOf("required_nodes", List.of())
                                .forGetter(NodeRequirements::requiredNodes)
                ).apply(instance, NodeRequirements::new)
        );
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 実際のポイントコストを取得
     */
    public int getActualCost() {
        return cost >= 0 ? cost : type.getBaseCost();
    }

    /**
     * 翻訳キーを取得（自動生成）
     */
    public String getTranslationKey() {
        if (!nameKey.isEmpty()) {
            return nameKey;
        }
        return "skill." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    /**
     * 説明翻訳キーを取得
     */
    public String getDescriptionTranslationKey() {
        if (!descriptionKey.isEmpty()) {
            return descriptionKey;
        }
        return getTranslationKey() + ".desc";
    }

    /**
     * ルートノードかどうか
     */
    public boolean isRoot() {
        return type == SkillNodeType.ROOT;
    }

    /**
     * キーストーンかどうか
     */
    public boolean isKeystone() {
        return type == SkillNodeType.KEYSTONE;
    }

    /**
     * ジュエルソケットかどうか
     */
    public boolean isJewelSocket() {
        return type == SkillNodeType.JEWEL_SOCKET;
    }

    /**
     * マスタリーノードかどうか
     */
    public boolean isMastery() {
        return type == SkillNodeType.MASTERY;
    }

    // ============================================
    // ビルダー
    // ============================================

    public static class Builder {
        private ResourceLocation id;
        private SkillNodeType type = SkillNodeType.MINOR;
        private String nameKey = "";
        private String descriptionKey = "";
        private int posX = 0;
        private int posY = 0;
        private List<SkillEffect> effects = List.of();
        private List<ResourceLocation> connections = List.of();
        private int cost = -1;
        private NodeRequirements requirements = NodeRequirements.NONE;
        private Optional<ResourceLocation> icon = Optional.empty();

        public Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder(String namespace, String path) {
            this.id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        }

        public Builder type(SkillNodeType type) {
            this.type = type;
            return this;
        }

        public Builder nameKey(String nameKey) {
            this.nameKey = nameKey;
            return this;
        }

        public Builder descriptionKey(String descriptionKey) {
            this.descriptionKey = descriptionKey;
            return this;
        }

        public Builder position(int x, int y) {
            this.posX = x;
            this.posY = y;
            return this;
        }

        public Builder effects(List<SkillEffect> effects) {
            this.effects = effects;
            return this;
        }

        public Builder effects(SkillEffect... effects) {
            this.effects = List.of(effects);
            return this;
        }

        public Builder connections(List<ResourceLocation> connections) {
            this.connections = connections;
            return this;
        }

        public Builder connections(ResourceLocation... connections) {
            this.connections = List.of(connections);
            return this;
        }

        public Builder cost(int cost) {
            this.cost = cost;
            return this;
        }

        public Builder requirements(NodeRequirements requirements) {
            this.requirements = requirements;
            return this;
        }

        public Builder requirements(int minLevel, int prerequisiteNodes) {
            this.requirements = new NodeRequirements(minLevel, prerequisiteNodes, List.of());
            return this;
        }

        public Builder icon(ResourceLocation icon) {
            this.icon = Optional.of(icon);
            return this;
        }

        public SkillNode build() {
            return new SkillNode(id, type, nameKey, descriptionKey, posX, posY,
                    effects, connections, cost, requirements, icon);
        }
    }
}
