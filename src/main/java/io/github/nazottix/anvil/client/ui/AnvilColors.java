package io.github.nazottix.anvil.client.ui;

/**
 * ANVILカラーパレット
 *
 * UI全体で使用するカラー定数を定義します。
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public final class AnvilColors {

    // ============================================
    // 基本カラー（ARGB形式）
    // ============================================

    /** 背景色 - Void Black */
    public static final int VOID_BLACK = 0xFF0F0F0F;

    /** パネル色 - Anvil Steel */
    public static final int ANVIL_STEEL = 0xFF374151;

    /** アクセント色 - Forge Orange */
    public static final int FORGE_ORANGE = 0xFFFF6B35;

    /** 魔術要素 - Arcane Purple */
    public static final int ARCANE_PURPLE = 0xFF8B5CF6;

    /** 科学要素 - Tech Cyan */
    public static final int TECH_CYAN = 0xFF06B6D4;

    /** テキスト色 - Ether White */
    public static final int ETHER_WHITE = 0xFFF8FAFC;

    /** ゴールド色 - Molten Gold */
    public static final int MOLTEN_GOLD = 0xFFF59E0B;

    // ============================================
    // ステータス表示用カラー
    // ============================================

    /** 耐久値 - 緑系 */
    public static final int DURABILITY_COLOR = 0xFF22C55E;

    /** 採掘速度 - 青系 */
    public static final int MINING_SPEED_COLOR = 0xFF3B82F6;

    /** 攻撃力 - 赤系 */
    public static final int ATTACK_DAMAGE_COLOR = 0xFFEF4444;

    /** 攻撃速度 - オレンジ系 */
    public static final int ATTACK_SPEED_COLOR = 0xFFF97316;

    /** 経験値 - シアン系 */
    public static final int XP_COLOR = 0xFF06B6D4;

    /** レベル - ゴールド系 */
    public static final int LEVEL_COLOR = 0xFFFACC15;

    // ============================================
    // グレードカラー
    // ============================================

    /** グレードE - グレー */
    public static final int GRADE_E = 0xFF9CA3AF;

    /** グレードD - 白 */
    public static final int GRADE_D = 0xFFF3F4F6;

    /** グレードC - 緑 */
    public static final int GRADE_C = 0xFF22C55E;

    /** グレードB - 青 */
    public static final int GRADE_B = 0xFF3B82F6;

    /** グレードA - 紫 */
    public static final int GRADE_A = 0xFF8B5CF6;

    /** グレードS - ゴールド */
    public static final int GRADE_S = 0xFFF59E0B;

    /** グレードSS - オレンジ */
    public static final int GRADE_SS = 0xFFFF6B35;

    /** グレードSSS - 赤 */
    public static final int GRADE_SSS = 0xFFEF4444;

    /** グレードMAX - 虹色（静的表示用） */
    public static final int GRADE_MAX = 0xFFFF69B4;

    // ============================================
    // レアリティカラー
    // ============================================

    /** Common - 白 */
    public static final int RARITY_COMMON = 0xFFFFFFFF;

    /** Uncommon - 緑 */
    public static final int RARITY_UNCOMMON = 0xFF22C55E;

    /** Rare - 青 */
    public static final int RARITY_RARE = 0xFF3B82F6;

    /** Epic - 紫 */
    public static final int RARITY_EPIC = 0xFF8B5CF6;

    /** Legendary - ゴールド */
    public static final int RARITY_LEGENDARY = 0xFFF59E0B;

    /** Mythic - 赤 */
    public static final int RARITY_MYTHIC = 0xFFEF4444;

    /** Unique - シアン */
    public static final int RARITY_UNIQUE = 0xFF06B6D4;

    // ============================================
    // ユーティリティメソッド
    // ============================================

    /**
     * グレードIDからカラーを取得
     *
     * @param gradeId グレードID（e, d, c, b, a, s, ss, sss, max）
     * @return ARGB形式のカラー値
     */
    public static int getGradeColor(String gradeId) {
        return switch (gradeId.toLowerCase()) {
            case "e" -> GRADE_E;
            case "d" -> GRADE_D;
            case "c" -> GRADE_C;
            case "b" -> GRADE_B;
            case "a" -> GRADE_A;
            case "s" -> GRADE_S;
            case "ss" -> GRADE_SS;
            case "sss" -> GRADE_SSS;
            case "max" -> GRADE_MAX;
            default -> ETHER_WHITE;
        };
    }

    /**
     * ARGBからRGB（0xRRGGBB形式）に変換
     *
     * Minecraftのテキストカラーで使用する場合に便利
     *
     * @param argb ARGB形式のカラー値
     * @return RGB形式のカラー値
     */
    public static int toRGB(int argb) {
        return argb & 0x00FFFFFF;
    }

    /**
     * 透明度を適用
     *
     * @param color 元のカラー（ARGB）
     * @param alpha 透明度（0.0 - 1.0）
     * @return 透明度を適用したカラー
     */
    public static int withAlpha(int color, float alpha) {
        int a = (int) (alpha * 255) & 0xFF;
        return (a << 24) | (color & 0x00FFFFFF);
    }

    // コンストラクタを非公開
    private AnvilColors() {}
}
