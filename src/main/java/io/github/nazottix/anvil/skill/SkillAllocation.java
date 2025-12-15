package io.github.nazottix.anvil.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * スキル配分データコンポーネント
 *
 * ツールごとのスキルポイント配分を管理します。
 * ItemStackに保存されます。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public record SkillAllocation(
        // 対象ツリーID
        ResourceLocation treeId,
        // 解放済みノードID
        Set<ResourceLocation> allocatedNodes,
        // 装着済みジュエル（ソケットID → ジュエルID）
        Map<ResourceLocation, ResourceLocation> equippedJewels,
        // 使用済みスキルポイント
        int usedPoints,
        // リスペック回数
        int respecCount
) {

    // ============================================
    // 定数
    // ============================================

    /** デフォルト配分（空） */
    public static final SkillAllocation EMPTY = new SkillAllocation(
            ResourceLocation.fromNamespaceAndPath("anvil", "none"),
            Set.of(),
            Map.of(),
            0,
            0
    );

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<SkillAllocation> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("tree_id").forGetter(SkillAllocation::treeId),
                    ResourceLocation.CODEC.listOf()
                            .xmap(HashSet::new, ArrayList::new)
                            .fieldOf("allocated_nodes").forGetter(a -> new HashSet<>(a.allocatedNodes())),
                    Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC)
                            .fieldOf("equipped_jewels").forGetter(a -> new HashMap<>(a.equippedJewels())),
                    Codec.INT.optionalFieldOf("used_points", 0).forGetter(SkillAllocation::usedPoints),
                    Codec.INT.optionalFieldOf("respec_count", 0).forGetter(SkillAllocation::respecCount)
            ).apply(instance, (treeId, nodes, jewels, used, respec) ->
                    new SkillAllocation(treeId, Set.copyOf(nodes), Map.copyOf(jewels), used, respec))
    );

    /**
     * ネットワーク通信用StreamCodec
     */
    public static final StreamCodec<ByteBuf, SkillAllocation> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SkillAllocation decode(ByteBuf buf) {
            // ツリーID
            ResourceLocation treeId = ResourceLocation.parse(ByteBufCodecs.STRING_UTF8.decode(buf));

            // 解放済みノード
            int nodeCount = ByteBufCodecs.VAR_INT.decode(buf);
            Set<ResourceLocation> nodes = new HashSet<>(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                nodes.add(ResourceLocation.parse(ByteBufCodecs.STRING_UTF8.decode(buf)));
            }

            // 装着済みジュエル
            int jewelCount = ByteBufCodecs.VAR_INT.decode(buf);
            Map<ResourceLocation, ResourceLocation> jewels = new HashMap<>(jewelCount);
            for (int i = 0; i < jewelCount; i++) {
                ResourceLocation socketId = ResourceLocation.parse(ByteBufCodecs.STRING_UTF8.decode(buf));
                ResourceLocation jewelId = ResourceLocation.parse(ByteBufCodecs.STRING_UTF8.decode(buf));
                jewels.put(socketId, jewelId);
            }

            int usedPoints = ByteBufCodecs.VAR_INT.decode(buf);
            int respecCount = ByteBufCodecs.VAR_INT.decode(buf);

            return new SkillAllocation(treeId, nodes, jewels, usedPoints, respecCount);
        }

        @Override
        public void encode(ByteBuf buf, SkillAllocation allocation) {
            // ツリーID
            ByteBufCodecs.STRING_UTF8.encode(buf, allocation.treeId().toString());

            // 解放済みノード
            ByteBufCodecs.VAR_INT.encode(buf, allocation.allocatedNodes().size());
            for (ResourceLocation nodeId : allocation.allocatedNodes()) {
                ByteBufCodecs.STRING_UTF8.encode(buf, nodeId.toString());
            }

            // 装着済みジュエル
            ByteBufCodecs.VAR_INT.encode(buf, allocation.equippedJewels().size());
            for (var entry : allocation.equippedJewels().entrySet()) {
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey().toString());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.getValue().toString());
            }

            ByteBufCodecs.VAR_INT.encode(buf, allocation.usedPoints());
            ByteBufCodecs.VAR_INT.encode(buf, allocation.respecCount());
        }
    };

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * 新しい配分を作成
     */
    public static SkillAllocation create(ResourceLocation treeId) {
        return new SkillAllocation(treeId, Set.of(), Map.of(), 0, 0);
    }

    // ============================================
    // 操作
    // ============================================

    /**
     * ノードを解放した新しい配分を返す
     *
     * @param nodeId 解放するノードID
     * @param cost ポイントコスト
     * @return 新しいSkillAllocation
     */
    public SkillAllocation withAllocatedNode(ResourceLocation nodeId, int cost) {
        if (allocatedNodes.contains(nodeId)) {
            return this;
        }

        Set<ResourceLocation> newNodes = new HashSet<>(allocatedNodes);
        newNodes.add(nodeId);

        return new SkillAllocation(treeId, Set.copyOf(newNodes), equippedJewels, usedPoints + cost, respecCount);
    }

    /**
     * ノードを解放解除した新しい配分を返す（リスペック）
     *
     * @param nodeId 解除するノードID
     * @param refund 返還ポイント
     * @return 新しいSkillAllocation
     */
    public SkillAllocation withDeallocatedNode(ResourceLocation nodeId, int refund) {
        if (!allocatedNodes.contains(nodeId)) {
            return this;
        }

        Set<ResourceLocation> newNodes = new HashSet<>(allocatedNodes);
        newNodes.remove(nodeId);

        return new SkillAllocation(treeId, Set.copyOf(newNodes), equippedJewels,
                Math.max(0, usedPoints - refund), respecCount + 1);
    }

    /**
     * ジュエルを装着した新しい配分を返す
     */
    public SkillAllocation withEquippedJewel(ResourceLocation socketId, ResourceLocation jewelId) {
        Map<ResourceLocation, ResourceLocation> newJewels = new HashMap<>(equippedJewels);
        newJewels.put(socketId, jewelId);
        return new SkillAllocation(treeId, allocatedNodes, Map.copyOf(newJewels), usedPoints, respecCount);
    }

    /**
     * ジュエルを取り外した新しい配分を返す
     */
    public SkillAllocation withRemovedJewel(ResourceLocation socketId) {
        if (!equippedJewels.containsKey(socketId)) {
            return this;
        }

        Map<ResourceLocation, ResourceLocation> newJewels = new HashMap<>(equippedJewels);
        newJewels.remove(socketId);
        return new SkillAllocation(treeId, allocatedNodes, Map.copyOf(newJewels), usedPoints, respecCount);
    }

    /**
     * 全リセットした新しい配分を返す
     */
    public SkillAllocation reset() {
        return new SkillAllocation(treeId, Set.of(), Map.of(), 0, respecCount + 1);
    }

    // ============================================
    // 情報取得
    // ============================================

    /**
     * ノードが解放済みかどうか
     */
    public boolean isAllocated(ResourceLocation nodeId) {
        return allocatedNodes.contains(nodeId);
    }

    /**
     * 解放済みノード数を取得
     */
    public int getAllocatedCount() {
        return allocatedNodes.size();
    }

    /**
     * ジュエルが装着されているかどうか
     */
    public boolean hasJewel(ResourceLocation socketId) {
        return equippedJewels.containsKey(socketId);
    }

    /**
     * 装着されているジュエルを取得
     */
    public Optional<ResourceLocation> getEquippedJewel(ResourceLocation socketId) {
        return Optional.ofNullable(equippedJewels.get(socketId));
    }
}
