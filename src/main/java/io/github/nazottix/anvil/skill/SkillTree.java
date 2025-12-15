package io.github.nazottix.anvil.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * スキルツリー定義
 *
 * ツールタイプごとのスキルツリーを定義します。
 * 各ツールタイプは独自のスキルツリーを持ちます。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public record SkillTree(
        // ツリーID
        ResourceLocation id,
        // 対象ツールタイプ
        String toolType,
        // 表示名キー
        String nameKey,
        // 背景テクスチャ
        Optional<ResourceLocation> background,
        // ノードリスト
        List<SkillNode> nodes,
        // ジュエルソケット位置（ノードIDリスト）
        List<ResourceLocation> jewelSockets
) {

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<SkillTree> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(SkillTree::id),
                    Codec.STRING.fieldOf("tool_type").forGetter(SkillTree::toolType),
                    Codec.STRING.optionalFieldOf("name_key", "").forGetter(SkillTree::nameKey),
                    ResourceLocation.CODEC.optionalFieldOf("background").forGetter(SkillTree::background),
                    SkillNode.CODEC.listOf().fieldOf("nodes").forGetter(SkillTree::nodes),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("jewel_sockets", List.of())
                            .forGetter(SkillTree::jewelSockets)
            ).apply(instance, SkillTree::new)
    );

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * ノードをIDで取得
     */
    public Optional<SkillNode> getNode(ResourceLocation nodeId) {
        return nodes.stream()
                .filter(node -> node.id().equals(nodeId))
                .findFirst();
    }

    /**
     * ルートノードを取得
     */
    public Optional<SkillNode> getRootNode() {
        return nodes.stream()
                .filter(SkillNode::isRoot)
                .findFirst();
    }

    /**
     * キーストーンを取得
     */
    public List<SkillNode> getKeystones() {
        return nodes.stream()
                .filter(SkillNode::isKeystone)
                .toList();
    }

    /**
     * マスタリーノードを取得
     */
    public List<SkillNode> getMasteryNodes() {
        return nodes.stream()
                .filter(SkillNode::isMastery)
                .toList();
    }

    /**
     * ジュエルソケットノードを取得
     */
    public List<SkillNode> getJewelSocketNodes() {
        return nodes.stream()
                .filter(SkillNode::isJewelSocket)
                .toList();
    }

    /**
     * ノード総数を取得
     */
    public int getTotalNodes() {
        return nodes.size();
    }

    /**
     * 全ノード解放に必要な総ポイントを取得
     */
    public int getTotalCost() {
        return nodes.stream()
                .mapToInt(SkillNode::getActualCost)
                .sum();
    }

    /**
     * 隣接ノードを取得
     */
    public List<SkillNode> getConnectedNodes(ResourceLocation nodeId) {
        Optional<SkillNode> nodeOpt = getNode(nodeId);
        if (nodeOpt.isEmpty()) {
            return List.of();
        }

        List<SkillNode> connected = new ArrayList<>();
        SkillNode node = nodeOpt.get();

        // 接続先
        for (ResourceLocation connId : node.connections()) {
            getNode(connId).ifPresent(connected::add);
        }

        // 逆方向接続（このノードを接続先として持つノード）
        for (SkillNode other : nodes) {
            if (other.connections().contains(nodeId) && !connected.contains(other)) {
                connected.add(other);
            }
        }

        return connected;
    }

    /**
     * 翻訳キーを取得
     */
    public String getTranslationKey() {
        if (!nameKey.isEmpty()) {
            return nameKey;
        }
        return "skilltree." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    // ============================================
    // ビルダー
    // ============================================

    public static class Builder {
        private ResourceLocation id;
        private String toolType;
        private String nameKey = "";
        private Optional<ResourceLocation> background = Optional.empty();
        private List<SkillNode> nodes = new ArrayList<>();
        private List<ResourceLocation> jewelSockets = new ArrayList<>();

        public Builder(ResourceLocation id, String toolType) {
            this.id = id;
            this.toolType = toolType;
        }

        public Builder(String namespace, String path, String toolType) {
            this.id = ResourceLocation.fromNamespaceAndPath(namespace, path);
            this.toolType = toolType;
        }

        public Builder nameKey(String nameKey) {
            this.nameKey = nameKey;
            return this;
        }

        public Builder background(ResourceLocation background) {
            this.background = Optional.of(background);
            return this;
        }

        public Builder addNode(SkillNode node) {
            this.nodes.add(node);
            return this;
        }

        public Builder nodes(List<SkillNode> nodes) {
            this.nodes = new ArrayList<>(nodes);
            return this;
        }

        public Builder jewelSockets(List<ResourceLocation> sockets) {
            this.jewelSockets = new ArrayList<>(sockets);
            return this;
        }

        public SkillTree build() {
            return new SkillTree(id, toolType, nameKey, background, List.copyOf(nodes), List.copyOf(jewelSockets));
        }
    }
}
