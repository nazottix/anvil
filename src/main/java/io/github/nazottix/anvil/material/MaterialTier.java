package io.github.nazottix.anvil.material;

/**
 * 素材ティア列挙型
 *
 * 素材の強さ・入手難易度を表す10段階のティアを定義します。
 * ティアが高いほど強力なステータスを持ちますが、入手が困難になります。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.4.2 素材ティア表
 *
 * 変更履歴: 10段階ティア構成を再編成（エンダーティア追加）
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
     * 代表素材: 石、フリント、深層岩
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
     * ティア2: 銅
     * 代表素材: 銅、金、鎖
     * 入手時期: 序盤
     */
    COPPER(
            2,
            "copper",
            "銅",
            1,  // 採掘レベル（石ツール相当）
            0xB87333  // 銅色
    ),

    /**
     * ティア3: 鉄
     * 代表素材: 鉄、レッドストーン、ラピスラズリ
     * 入手時期: 序盤〜中盤
     */
    IRON(
            3,
            "iron",
            "鉄",
            2,  // 採掘レベル（鉄ツール相当）
            0xD4D4D4  // 銀色
    ),

    /**
     * ティア4: ダイヤモンド
     * 代表素材: ダイヤモンド、エメラルド
     * 入手時期: 中盤
     */
    DIAMOND(
            4,
            "diamond",
            "ダイヤモンド",
            3,  // 採掘レベル（ダイヤツール相当）
            0x4AEDD9  // 水色
    ),

    /**
     * ティア5: クォーツ
     * 代表素材: クォーツ、プリズマリン、アメジスト
     * 入手時期: 中盤
     */
    QUARTZ(
            5,
            "quartz",
            "クォーツ",
            3,  // 採掘レベル（ダイヤツール相当）
            0xE8E8E8  // 白色
    ),

    /**
     * ティア6: 黒曜石
     * 代表素材: 黒曜石、ブレイズ、マグマブロック
     * 入手時期: 中盤〜後半
     */
    OBSIDIAN(
            6,
            "obsidian",
            "黒曜石",
            3,  // 採掘レベル（ダイヤツール相当）
            0x1A0A2E  // 暗紫色
    ),

    /**
     * ティア7: エンダー
     * 代表素材: エンダーパール、エンドストーン、シュルカー
     * 入手時期: 後半
     */
    ENDER(
            7,
            "ender",
            "エンダー",
            4,  // 採掘レベル（ネザライトツール相当）
            0x0C5E4E  // 深緑色
    ),

    /**
     * ティア8: 伝説
     * 代表素材: ネザライト
     * 入手時期: 後半〜終盤
     */
    LEGENDARY(
            8,
            "legendary",
            "伝説",
            5,
            0x4A4A4A  // 暗灰色（ネザライト色）
    ),

    /**
     * ティア9: 神話
     * 代表素材: ネザースター、ドラゴン
     * 入手時期: 終盤（ボス討伐報酬）
     */
    MYTHIC(
            9,
            "mythic",
            "神話",
            6,
            0xF1C40F  // 金色
    );

    // 内部ティア値（0-9）
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
     * @param tier ティア値（0-9）
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
