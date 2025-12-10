package io.github.nazottix.anvil.trait;

/**
 * 特性発動トリガー列挙型
 *
 * 特性がいつ発動するかを定義します。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.6 特性システム
 */
public enum TraitTrigger {
    /**
     * 常時発動（パッシブ）
     */
    ALWAYS("always", "常時"),

    /**
     * 攻撃時に発動
     */
    ON_ATTACK("on_attack", "攻撃時"),

    /**
     * ブロック破壊時に発動
     */
    ON_BLOCK_BREAK("on_block_break", "ブロック破壊時"),

    /**
     * ダメージを受けた時に発動
     */
    ON_DAMAGE_TAKEN("on_damage_taken", "被ダメージ時"),

    /**
     * 耐久値消費時に発動
     */
    ON_DURABILITY_USE("on_durability_use", "耐久消費時"),

    /**
     * 経験値獲得時に発動
     */
    ON_XP_GAIN("on_xp_gain", "XP獲得時"),

    /**
     * アンデッドへの攻撃時に発動
     */
    ON_UNDEAD_ATTACK("on_undead_attack", "アンデッド攻撃時"),

    /**
     * 水中にいる時に発動
     */
    WHILE_IN_WATER("while_in_water", "水中時"),

    /**
     * ネザーにいる時に発動
     */
    WHILE_IN_NETHER("while_in_nether", "ネザー時"),

    /**
     * エンドにいる時に発動
     */
    WHILE_IN_END("while_in_end", "エンド時"),

    /**
     * 連続使用時に発動（採掘/攻撃）
     */
    ON_CONSECUTIVE_USE("on_consecutive_use", "連続使用時"),

    /**
     * キル時に発動
     */
    ON_KILL("on_kill", "キル時"),

    /**
     * 釣り成功時に発動
     */
    ON_FISH_CATCH("on_fish_catch", "釣り成功時"),

    /**
     * 刈り取り時に発動
     */
    ON_SHEAR("on_shear", "刈り取り時");

    // トリガーID
    private final String id;

    // 日本語表示名
    private final String displayNameJa;

    TraitTrigger(String id, String displayNameJa) {
        this.id = id;
        this.displayNameJa = displayNameJa;
    }

    public String getId() {
        return id;
    }

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    /**
     * ローカライズキーを取得
     */
    public String getTranslationKey() {
        return "trait_trigger.anvil." + id;
    }

    /**
     * IDからTraitTriggerを取得
     */
    public static TraitTrigger fromId(String id) {
        for (TraitTrigger trigger : values()) {
            if (trigger.id.equals(id)) {
                return trigger;
            }
        }
        return null;
    }
}
