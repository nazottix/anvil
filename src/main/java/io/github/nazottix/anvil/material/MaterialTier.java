package io.github.nazottix.anvil.material;

/**
 * 素材ティア列挙型
 *
 * 素材の強さ・入手難易度を表す7段階のティアを定義します。
 * ティアが高いほど強力なステータスを持ちますが、入手が困難になります。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.4.2 素材ティア表
 */
public enum MaterialTier {
    /**
     * ティア0: 木材
     * 代表素材: 木材、竹
     * 入手時期: 即時（ゲーム開始直後）
     */
    WOOD(
            0,
            "wood",
            "木材",
            0,  // 採掘レベル（木材ツール相当）
            0x8B4513  // 茶色
    ),

    /**
     * ティア1: 石
     * 代表素材: 石、フリント
     * 入手時期: 序盤
     */
    STONE(
            1,
            "stone",
            "石",
            1,  // 採掘レベル（石ツール相当）
            0x808080  // 灰色
    ),

    /**
     * ティア2: 鉄
     * 代表素材: 鉄、銅、金
     * 入手時期: 序盤〜中盤
     */
    IRON(
            2,
            "iron",
            "鉄",
            2,  // 採掘レベル（鉄ツール相当）
            0xD4D4D4  // 銀色
    ),

    /**
     * ティア3: ダイヤモンド
     * 代表素材: ダイヤモンド、エメラルド、エンダーパール
     * 入手時期: 中盤
     */
    DIAMOND(
            3,
            "diamond",
            "ダイヤモンド",
            3,  // 採掘レベル（ダイヤツール相当）
            0x4AEDD9  // 水色
    ),

    /**
     * ティア4: ネザライト
     * 代表素材: ネザライト、エンドストーン
     * 入手時期: 中盤〜後半
     */
    NETHERITE(
            4,
            "netherite",
            "ネザライト",
            4,  // 採掘レベル（ネザライトツール相当）
            0x4A4A4A  // 暗灰色
    ),

    /**
     * ティア5: 伝説
     * 代表素材: ネザースター、ドラゴンヘッド
     * 入手時期: 後半〜終盤（ボス討伐報酬）
     */
    LEGENDARY(
            5,
            "legendary",
            "伝説",
            5,
            0x9B59B6  // 紫色
    ),

    /**
     * ティア6: 神話
     * 代表素材: 未定（将来の拡張用）
     * 入手時期: 終盤
     * ※将来の拡張用に予約
     */
    MYTHIC(
            6,
            "mythic",
            "神話",
            6,
            0xF1C40F  // 金色
    );

    // 内部ティア値（0-6）
    private final int tier;

    // ティアID
    private final String id;

    // 日本語表示名
    private final String displayNameJa;

    // このティアでの採掘レベル
    private final int miningLevel;

    // ティアを表す色（16進数RGB）
    private final int color;

    /**
     * MaterialTierコンストラクタ
     *
     * @param tier ティア値
     * @param id ティアID
     * @param displayNameJa 日本語表示名
     * @param miningLevel 採掘レベル
     * @param color 表示色
     */
    MaterialTier(int tier, String id, String displayNameJa, int miningLevel, int color) {
        this.tier = tier;
        this.id = id;
        this.displayNameJa = displayNameJa;
        this.miningLevel = miningLevel;
        this.color = color;
    }

    public int getTier() {
        return tier;
    }

    public String getId() {
        return id;
    }

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    public int getMiningLevel() {
        return miningLevel;
    }

    public int getColor() {
        return color;
    }

    /**
     * ローカライズキーを取得
     * 例: "tier.anvil.diamond"
     */
    public String getTranslationKey() {
        return "tier.anvil." + id;
    }

    /**
     * このティアが指定ティアより高いかチェック
     *
     * @param other 比較対象のティア
     * @return このティアが高い場合true
     */
    public boolean isHigherThan(MaterialTier other) {
        return this.tier > other.tier;
    }

    /**
     * このティアが指定ブロックを採掘可能かチェック
     *
     * @param requiredLevel ブロックが要求する採掘レベル
     * @return 採掘可能な場合true
     */
    public boolean canMine(int requiredLevel) {
        return this.miningLevel >= requiredLevel;
    }

    /**
     * ティア値からMaterialTierを取得
     *
     * @param tier ティア値（0-6）
     * @return 対応するMaterialTier、見つからない場合はWOOD
     */
    public static MaterialTier fromTier(int tier) {
        for (MaterialTier t : values()) {
            if (t.tier == tier) {
                return t;
            }
        }
        return WOOD;
    }

    /**
     * IDからMaterialTierを取得
     *
     * @param id ティアID
     * @return 対応するMaterialTier、見つからない場合はnull
     */
    public static MaterialTier fromId(String id) {
        for (MaterialTier t : values()) {
            if (t.id.equals(id)) {
                return t;
            }
        }
        return null;
    }
}
