package io.github.nazottix.anvil.tool;

import java.util.List;

/**
 * ANVILツールタイプ列挙型
 *
 * 8種類の基本ツールタイプを定義します。
 * 各ツールタイプは固有のパーツ構成、基本容量、XP獲得アクションを持ちます。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.3 ツールタイプ定義
 */
public enum ToolType {
    /**
     * ピッケル - 採掘用ツール
     * パーツ構成: ヘッド + ハンドル + バインディング
     * XP獲得: 鉱石・石材破壊
     */
    PICKAXE(
            "pickaxe",
            "ピッケル",
            30,  // 基本容量
            List.of(PartType.HEAD, PartType.HANDLE, PartType.BINDING),
            ToolCategory.MINING
    ),

    /**
     * 斧 - 伐採用ツール
     * パーツ構成: ヘッド + ハンドル + バインディング
     * XP獲得: 木材系破壊
     */
    AXE(
            "axe",
            "斧",
            28,
            List.of(PartType.HEAD, PartType.HANDLE, PartType.BINDING),
            ToolCategory.MINING
    ),

    /**
     * シャベル - 掘削用ツール
     * パーツ構成: ヘッド + ハンドル
     * XP獲得: 土・砂系破壊
     */
    SHOVEL(
            "shovel",
            "シャベル",
            26,
            List.of(PartType.HEAD, PartType.HANDLE),
            ToolCategory.MINING
    ),

    /**
     * 剣 - 近接戦闘用武器
     * パーツ構成: ブレード + ハンドル + ガード
     * XP獲得: モブキル時ダメージ
     */
    SWORD(
            "sword",
            "剣",
            32,
            List.of(PartType.BLADE, PartType.HANDLE, PartType.GUARD),
            ToolCategory.COMBAT
    ),

    /**
     * クワ - 農耕用ツール
     * パーツ構成: ヘッド + ハンドル
     * XP獲得: 作物収穫
     */
    HOE(
            "hoe",
            "クワ",
            24,
            List.of(PartType.HEAD, PartType.HANDLE),
            ToolCategory.FARMING
    ),

    /**
     * 弓 - 遠距離戦闘用武器
     * パーツ構成: ボウリム×2 + ハンドル + ストリング
     * XP獲得: 矢によるモブキル
     */
    BOW(
            "bow",
            "弓",
            30,
            List.of(PartType.BOW_LIMB, PartType.BOW_LIMB, PartType.HANDLE, PartType.BOWSTRING),
            ToolCategory.COMBAT
    ),

    /**
     * 釣り竿 - 釣り用ツール
     * パーツ構成: ロッド + フック + ライン
     * XP獲得: 魚・宝物獲得
     */
    FISHING_ROD(
            "fishing_rod",
            "釣り竿",
            22,
            List.of(PartType.ROD, PartType.HOOK, PartType.LINE),
            ToolCategory.FISHING
    ),

    /**
     * ハサミ - 刈り取り用ツール
     * パーツ構成: ブレード×2 + ピボット
     * XP獲得: 羊毛・葉収穫
     */
    SHEARS(
            "shears",
            "ハサミ",
            20,
            List.of(PartType.BLADE, PartType.BLADE, PartType.PIVOT),
            ToolCategory.UTILITY
    );

    // ツールのID（例: "pickaxe"）
    private final String id;

    // 日本語表示名
    private final String displayNameJa;

    // 基本MOD容量
    private final int baseCapacity;

    // 必要なパーツタイプのリスト
    private final List<PartType> requiredParts;

    // ツールカテゴリ
    private final ToolCategory category;

    /**
     * ToolTypeコンストラクタ
     *
     * @param id ツールID
     * @param displayNameJa 日本語表示名
     * @param baseCapacity 基本MOD容量
     * @param requiredParts 必要パーツリスト
     * @param category ツールカテゴリ
     */
    ToolType(String id, String displayNameJa, int baseCapacity,
             List<PartType> requiredParts, ToolCategory category) {
        this.id = id;
        this.displayNameJa = displayNameJa;
        this.baseCapacity = baseCapacity;
        this.requiredParts = requiredParts;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public List<PartType> getRequiredParts() {
        return requiredParts;
    }

    public ToolCategory getCategory() {
        return category;
    }

    /**
     * ローカライズキーを取得
     * 例: "tool.anvil.pickaxe"
     */
    public String getTranslationKey() {
        return "tool.anvil." + id;
    }

    /**
     * IDからToolTypeを取得
     *
     * @param id ツールID
     * @return 対応するToolType、見つからない場合はnull
     */
    public static ToolType fromId(String id) {
        for (ToolType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * ツールカテゴリ
     *
     * ツールの主要用途によるグループ分け
     */
    public enum ToolCategory {
        MINING("mining", "採掘"),      // 採掘・伐採・掘削
        COMBAT("combat", "戦闘"),      // 近接・遠距離戦闘
        FARMING("farming", "農業"),    // 農作業
        FISHING("fishing", "釣り"),    // 釣り
        UTILITY("utility", "ユーティリティ"); // その他

        private final String id;
        private final String displayNameJa;

        ToolCategory(String id, String displayNameJa) {
            this.id = id;
            this.displayNameJa = displayNameJa;
        }

        public String getId() {
            return id;
        }

        public String getDisplayNameJa() {
            return displayNameJa;
        }
    }
}
