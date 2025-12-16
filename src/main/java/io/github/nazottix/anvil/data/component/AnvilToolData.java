package io.github.nazottix.anvil.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * ANVILツールのデータコンポーネント
 *
 * ツールに保存されるカスタムデータを定義します。
 * 段階的にフィールドを追加していきます。
 *
 * Phase 0: レベル、経験値
 * Phase 1: ツールタイプ、パーツ情報
 * Phase 2: ジュエルスロット、スキルポイント（現在のフェーズ）
 *
 * 使用例:
 * - ツールのレベルを取得: itemStack.get(AnvilDataComponents.TOOL_DATA).level()
 * - パーツ一覧を取得: itemStack.get(AnvilDataComponents.TOOL_DATA).parts()
 * - 装着ジュエルを取得: itemStack.get(AnvilDataComponents.TOOL_DATA).equippedJewels()
 *
 * 将来追加予定のフィールド:
 * - Phase 3: MODスロット、グリッドデータ
 * - Phase 4: レアリティ
 */
public record AnvilToolData(
        // ツールタイプID（例: "pickaxe", "sword"）
        String toolType,
        // ツールの現在レベル（1〜10,000）
        int level,
        // 現在の経験値（次のレベルまでの累積値）
        long currentXp,
        // 累計経験値（全て獲得したXPの合計）
        long totalXp,
        // ツールを構成するパーツのリスト
        List<ToolPart> parts,
        // 装着されたジュエルIDのリスト（最大4個）
        List<String> equippedJewels,
        // 未使用スキルポイント
        int skillPoints
) {
    /**
     * デフォルト値でインスタンスを作成するための定数
     * ピッケルタイプ、レベル1、経験値0、パーツなし、ジュエルなし、スキルポイント0
     */
    public static final AnvilToolData DEFAULT = new AnvilToolData("pickaxe", 1, 0L, 0L, List.of(), List.of(), 0);

    /**
     * 最大ジュエルスロット数
     */
    public static final int MAX_JEWEL_SLOTS = 4;

    /**
     * Codec - データの保存/読み込み用（NBT、JSON）
     *
     * RecordCodecBuilderを使用して、recordの各フィールドを
     * シリアライズ/デシリアライズする方法を定義します。
     */
    public static final Codec<AnvilToolData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // "tool_type"フィールド: String型、デフォルト値"pickaxe"
                    Codec.STRING.optionalFieldOf("tool_type", "pickaxe").forGetter(AnvilToolData::toolType),
                    // "level"フィールド: int型、デフォルト値1
                    Codec.INT.optionalFieldOf("level", 1).forGetter(AnvilToolData::level),
                    // "current_xp"フィールド: long型、デフォルト値0
                    Codec.LONG.optionalFieldOf("current_xp", 0L).forGetter(AnvilToolData::currentXp),
                    // "total_xp"フィールド: long型、デフォルト値0
                    Codec.LONG.optionalFieldOf("total_xp", 0L).forGetter(AnvilToolData::totalXp),
                    // "parts"フィールド: ToolPartのリスト、デフォルト値は空リスト
                    ToolPart.CODEC.listOf().optionalFieldOf("parts", List.of()).forGetter(AnvilToolData::parts),
                    // "equipped_jewels"フィールド: ジュエルIDのリスト、デフォルト値は空リスト
                    Codec.STRING.listOf().optionalFieldOf("equipped_jewels", List.of()).forGetter(AnvilToolData::equippedJewels),
                    // "skill_points"フィールド: int型、デフォルト値0
                    Codec.INT.optionalFieldOf("skill_points", 0).forGetter(AnvilToolData::skillPoints)
            ).apply(instance, AnvilToolData::new)
    );

    /**
     * StreamCodec - ネットワーク通信用（クライアント-サーバー間）
     *
     * バイナリ形式でデータを送受信するためのコーデック。
     * Codecより高速だが、人間が読める形式ではありません。
     *
     * 注意: StreamCodec.compositeは最大6フィールドまでしかサポートしないため、
     * 5フィールド以上の場合はカスタム実装を使用します。
     */
    public static final StreamCodec<ByteBuf, AnvilToolData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AnvilToolData decode(ByteBuf buf) {
            // 各フィールドを順番にデコード
            String toolType = ByteBufCodecs.STRING_UTF8.decode(buf);
            int level = ByteBufCodecs.VAR_INT.decode(buf);
            long currentXp = ByteBufCodecs.VAR_LONG.decode(buf);
            long totalXp = ByteBufCodecs.VAR_LONG.decode(buf);
            List<ToolPart> parts = ToolPart.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            List<String> equippedJewels = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
            int skillPoints = ByteBufCodecs.VAR_INT.decode(buf);
            return new AnvilToolData(toolType, level, currentXp, totalXp, parts, equippedJewels, skillPoints);
        }

        @Override
        public void encode(ByteBuf buf, AnvilToolData data) {
            // 各フィールドを順番にエンコード
            ByteBufCodecs.STRING_UTF8.encode(buf, data.toolType());
            ByteBufCodecs.VAR_INT.encode(buf, data.level());
            ByteBufCodecs.VAR_LONG.encode(buf, data.currentXp());
            ByteBufCodecs.VAR_LONG.encode(buf, data.totalXp());
            ToolPart.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.parts());
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, data.equippedJewels());
            ByteBufCodecs.VAR_INT.encode(buf, data.skillPoints());
        }
    };

    /**
     * 新しいツールデータを作成するビルダー
     *
     * @param toolType ツールタイプID
     * @param parts パーツリスト
     * @return 新しいAnvilToolData（レベル1、経験値0、ジュエルなし、スキルポイント0）
     */
    public static AnvilToolData create(String toolType, List<ToolPart> parts) {
        // totalXpを0で初期化、ジュエルなし、スキルポイント0
        return new AnvilToolData(toolType, 1, 0L, 0L, parts, List.of(), 0);
    }

    /**
     * 経験値を追加した新しいインスタンスを返す
     *
     * recordはイミュータブル（不変）なので、値を変更する場合は
     * 新しいインスタンスを作成して返します。
     *
     * @param xp 追加する経験値
     * @return 経験値が追加された新しいAnvilToolData
     */
    public AnvilToolData addXp(long xp) {
        // currentXpとtotalXpの両方に加算
        return new AnvilToolData(this.toolType, this.level, this.currentXp + xp, this.totalXp + xp,
                this.parts, this.equippedJewels, this.skillPoints);
    }

    /**
     * レベルアップした新しいインスタンスを返す
     *
     * @param newLevel 新しいレベル
     * @param remainingXp レベルアップ後の残り経験値
     * @return 新しいAnvilToolData
     */
    public AnvilToolData levelUp(int newLevel, long remainingXp) {
        // totalXpはそのまま維持、レベルアップ時にスキルポイントを追加
        int newSkillPoints = this.skillPoints + 1;
        return new AnvilToolData(this.toolType, newLevel, remainingXp, this.totalXp,
                this.parts, this.equippedJewels, newSkillPoints);
    }

    /**
     * XP情報を更新した新しいインスタンスを返す
     *
     * LevelingManagerから使用される、レベル、現在XP、累計XPを一括更新するメソッド
     *
     * @param newLevel 新しいレベル
     * @param newCurrentXp 新しい現在XP
     * @param newTotalXp 新しい累計XP
     * @return 新しいAnvilToolData
     */
    public AnvilToolData withXp(int newLevel, long newCurrentXp, long newTotalXp) {
        return new AnvilToolData(this.toolType, newLevel, newCurrentXp, newTotalXp,
                this.parts, this.equippedJewels, this.skillPoints);
    }

    /**
     * パーツを更新した新しいインスタンスを返す
     *
     * @param newParts 新しいパーツリスト
     * @return 新しいAnvilToolData
     */
    public AnvilToolData withParts(List<ToolPart> newParts) {
        return new AnvilToolData(this.toolType, this.level, this.currentXp, this.totalXp,
                newParts, this.equippedJewels, this.skillPoints);
    }

    /**
     * 指定されたパーツタイプのパーツを取得
     *
     * @param partType パーツタイプID
     * @return 見つかったパーツ、なければnull
     */
    public ToolPart getPartByType(String partType) {
        for (ToolPart part : parts) {
            if (part.partType().equals(partType)) {
                return part;
            }
        }
        return null;
    }

    // ============================================
    // ジュエル関連メソッド
    // ============================================

    /**
     * ジュエルを装着した新しいインスタンスを返す
     *
     * @param jewelId 装着するジュエルのID
     * @return 新しいAnvilToolData、スロットが満杯の場合はthis
     */
    public AnvilToolData equipJewel(String jewelId) {
        if (equippedJewels.size() >= MAX_JEWEL_SLOTS) {
            return this; // スロット満杯
        }
        if (equippedJewels.contains(jewelId)) {
            return this; // 既に装着済み
        }
        List<String> newJewels = new java.util.ArrayList<>(equippedJewels);
        newJewels.add(jewelId);
        return new AnvilToolData(this.toolType, this.level, this.currentXp, this.totalXp,
                this.parts, newJewels, this.skillPoints);
    }

    /**
     * ジュエルを取り外した新しいインスタンスを返す
     *
     * @param jewelId 取り外すジュエルのID
     * @return 新しいAnvilToolData
     */
    public AnvilToolData unequipJewel(String jewelId) {
        if (!equippedJewels.contains(jewelId)) {
            return this; // 装着されていない
        }
        List<String> newJewels = new java.util.ArrayList<>(equippedJewels);
        newJewels.remove(jewelId);
        return new AnvilToolData(this.toolType, this.level, this.currentXp, this.totalXp,
                this.parts, newJewels, this.skillPoints);
    }

    /**
     * 指定スロットのジュエルを取り外した新しいインスタンスを返す
     *
     * @param slotIndex スロットインデックス（0-3）
     * @return 新しいAnvilToolData
     */
    public AnvilToolData unequipJewelAt(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= equippedJewels.size()) {
            return this; // 無効なインデックス
        }
        List<String> newJewels = new java.util.ArrayList<>(equippedJewels);
        newJewels.remove(slotIndex);
        return new AnvilToolData(this.toolType, this.level, this.currentXp, this.totalXp,
                this.parts, newJewels, this.skillPoints);
    }

    /**
     * 全てのジュエルを取り外した新しいインスタンスを返す
     *
     * @return 新しいAnvilToolData
     */
    public AnvilToolData clearJewels() {
        return new AnvilToolData(this.toolType, this.level, this.currentXp, this.totalXp,
                this.parts, List.of(), this.skillPoints);
    }

    /**
     * ジュエルスロットに空きがあるか
     */
    public boolean hasEmptyJewelSlot() {
        return equippedJewels.size() < MAX_JEWEL_SLOTS;
    }

    /**
     * 指定されたジュエルが装着されているか
     */
    public boolean hasJewelEquipped(String jewelId) {
        return equippedJewels.contains(jewelId);
    }

    // ============================================
    // スキルポイント関連メソッド
    // ============================================

    /**
     * スキルポイントを消費した新しいインスタンスを返す
     *
     * @param points 消費するポイント数
     * @return 新しいAnvilToolData、ポイント不足の場合はthis
     */
    public AnvilToolData useSkillPoints(int points) {
        if (this.skillPoints < points) {
            return this; // ポイント不足
        }
        return new AnvilToolData(this.toolType, this.level, this.currentXp, this.totalXp,
                this.parts, this.equippedJewels, this.skillPoints - points);
    }

    /**
     * スキルポイントをリセット（全て還元）した新しいインスタンスを返す
     *
     * @param totalPoints リセット後の総ポイント
     * @return 新しいAnvilToolData
     */
    public AnvilToolData resetSkillPoints(int totalPoints) {
        return new AnvilToolData(this.toolType, this.level, this.currentXp, this.totalXp,
                this.parts, this.equippedJewels, totalPoints);
    }
}
