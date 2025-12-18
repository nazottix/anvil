package io.github.nazottix.anvil.material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 素材データクラス
 *
 * ツールパーツに使用できる素材を定義します。
 * 各素材はティア、カテゴリ、各パーツタイプでのステータス、特性を持ちます。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.4 マテリアル（素材）システム
 */
public class Material {

    // 素材ID（例: "anvil:diamond"）
    private final ResourceLocation id;

    // 表示名（ローカライズキー）
    private final String translationKey;

    // 素材ティア（0-6）
    private final MaterialTier tier;

    // 素材カテゴリ（metal, wood, stone等）
    private final Set<MaterialCategory> categories;

    // パーツタイプごとのステータス
    private final MaterialStats.HeadStats headStats;
    private final MaterialStats.HandleStats handleStats;
    private final MaterialStats.BindingStats bindingStats;
    private final MaterialStats.BowLimbStats bowLimbStats;
    private final MaterialStats.BowstringStats bowstringStats;
    private final MaterialStats.HookStats hookStats;
    private final MaterialStats.CoatingStats coatingStats;
    private final MaterialStats.UpgradeStats upgradeStats;

    // この素材が付与する特性（パーツタイプごと）
    private final List<TraitEntry> headTraits;
    private final List<TraitEntry> handleTraits;
    private final List<TraitEntry> extraTraits;

    // 修理アイテム
    private final Item repairItem;

    // 表示色（プライマリ、セカンダリ）
    private final int primaryColor;
    private final int secondaryColor;

    // レアリティ重み（低いほど希少）
    private final double rarityWeight;

    /**
     * Materialコンストラクタ（ビルダー経由で生成推奨）
     */
    private Material(Builder builder) {
        this.id = builder.id;
        this.translationKey = builder.translationKey;
        this.tier = builder.tier;
        this.categories = builder.categories;
        this.headStats = builder.headStats;
        this.handleStats = builder.handleStats;
        this.bindingStats = builder.bindingStats;
        this.bowLimbStats = builder.bowLimbStats;
        this.bowstringStats = builder.bowstringStats;
        this.hookStats = builder.hookStats;
        this.coatingStats = builder.coatingStats;
        this.upgradeStats = builder.upgradeStats;
        this.headTraits = builder.headTraits;
        this.handleTraits = builder.handleTraits;
        this.extraTraits = builder.extraTraits;
        this.repairItem = builder.repairItem;
        this.primaryColor = builder.primaryColor;
        this.secondaryColor = builder.secondaryColor;
        this.rarityWeight = builder.rarityWeight;
    }

    // ============================================
    // ゲッター
    // ============================================

    public ResourceLocation getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public MaterialTier getTier() {
        return tier;
    }

    public Set<MaterialCategory> getCategories() {
        return categories;
    }

    public MaterialStats.HeadStats getHeadStats() {
        return headStats;
    }

    public MaterialStats.HandleStats getHandleStats() {
        return handleStats;
    }

    public MaterialStats.BindingStats getBindingStats() {
        return bindingStats;
    }

    public MaterialStats.BowLimbStats getBowLimbStats() {
        return bowLimbStats;
    }

    public MaterialStats.BowstringStats getBowstringStats() {
        return bowstringStats;
    }

    public MaterialStats.HookStats getHookStats() {
        return hookStats;
    }

    public MaterialStats.CoatingStats getCoatingStats() {
        return coatingStats;
    }

    public MaterialStats.UpgradeStats getUpgradeStats() {
        return upgradeStats;
    }

    public List<TraitEntry> getHeadTraits() {
        return headTraits;
    }

    public List<TraitEntry> getHandleTraits() {
        return handleTraits;
    }

    public List<TraitEntry> getExtraTraits() {
        return extraTraits;
    }

    public Item getRepairItem() {
        return repairItem;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public double getRarityWeight() {
        return rarityWeight;
    }

    // ============================================
    // ユーティリティメソッド
    // ============================================

    /**
     * このカテゴリに属するかチェック
     *
     * @param category チェックするカテゴリ
     * @return 属する場合true
     */
    public boolean hasCategory(MaterialCategory category) {
        return categories.contains(category);
    }

    /**
     * グレードを適用したヘッドステータスを取得
     *
     * @param grade グレード
     * @return グレード適用後のステータス
     */
    public MaterialStats.HeadStats getHeadStatsWithGrade(Grade grade) {
        return headStats.withGradeMultiplier(grade.getStatMultiplier());
    }

    /**
     * グレードを適用したハンドルステータスを取得
     *
     * @param grade グレード
     * @return グレード適用後のステータス
     */
    public MaterialStats.HandleStats getHandleStatsWithGrade(Grade grade) {
        return handleStats.withGradeMultiplier(grade.getStatMultiplier());
    }

    // ============================================
    // ビルダー
    // ============================================

    /**
     * 新しいビルダーを作成
     *
     * @param id 素材ID
     * @return ビルダーインスタンス
     */
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    /**
     * 素材ビルダー
     *
     * Fluent APIで素材を構築します。
     */
    public static class Builder {
        private final ResourceLocation id;
        private String translationKey;
        private MaterialTier tier = MaterialTier.WOOD;
        private Set<MaterialCategory> categories = Set.of();

        // ステータス（デフォルト値で初期化）
        private MaterialStats.HeadStats headStats = MaterialStats.HeadStats.DEFAULT;
        private MaterialStats.HandleStats handleStats = MaterialStats.HandleStats.DEFAULT;
        private MaterialStats.BindingStats bindingStats = MaterialStats.BindingStats.DEFAULT;
        private MaterialStats.BowLimbStats bowLimbStats = MaterialStats.BowLimbStats.DEFAULT;
        private MaterialStats.BowstringStats bowstringStats = MaterialStats.BowstringStats.DEFAULT;
        private MaterialStats.HookStats hookStats = MaterialStats.HookStats.DEFAULT;
        private MaterialStats.CoatingStats coatingStats = MaterialStats.CoatingStats.DEFAULT;
        private MaterialStats.UpgradeStats upgradeStats = MaterialStats.UpgradeStats.DEFAULT;

        // 特性
        private List<TraitEntry> headTraits = List.of();
        private List<TraitEntry> handleTraits = List.of();
        private List<TraitEntry> extraTraits = List.of();

        // その他
        private Item repairItem = Items.AIR;
        private int primaryColor = 0xFFFFFF;
        private int secondaryColor = 0xCCCCCC;
        private double rarityWeight = 1.0;

        private Builder(ResourceLocation id) {
            this.id = id;
            this.translationKey = "material." + id.getNamespace() + "." + id.getPath();
        }

        public Builder translationKey(String key) {
            this.translationKey = key;
            return this;
        }

        public Builder tier(MaterialTier tier) {
            this.tier = tier;
            return this;
        }

        public Builder categories(MaterialCategory... categories) {
            this.categories = Set.of(categories);
            return this;
        }

        public Builder headStats(int durability, float miningSpeed, int miningLevel, float attackDamage) {
            this.headStats = new MaterialStats.HeadStats(durability, miningSpeed, miningLevel, attackDamage);
            return this;
        }

        public Builder handleStats(float durabilityMultiplier, float attackSpeedModifier) {
            this.handleStats = new MaterialStats.HandleStats(durabilityMultiplier, attackSpeedModifier);
            return this;
        }

        public Builder bindingStats(float traitAmplifier) {
            this.bindingStats = new MaterialStats.BindingStats(traitAmplifier);
            return this;
        }

        public Builder bowLimbStats(float drawSpeed, float range) {
            this.bowLimbStats = new MaterialStats.BowLimbStats(drawSpeed, range);
            return this;
        }

        public Builder bowstringStats(float arrowSpeed, float accuracy) {
            this.bowstringStats = new MaterialStats.BowstringStats(arrowSpeed, accuracy);
            return this;
        }

        public Builder hookStats(float fishingEfficiency, float rareDropChance) {
            this.hookStats = new MaterialStats.HookStats(fishingEfficiency, rareDropChance);
            return this;
        }

        public Builder coatingStats(float allStatsMultiplier) {
            this.coatingStats = new MaterialStats.CoatingStats(allStatsMultiplier);
            return this;
        }

        public Builder upgradeStats(int modSlotBonus) {
            this.upgradeStats = new MaterialStats.UpgradeStats(modSlotBonus);
            return this;
        }

        public Builder headTraits(TraitEntry... traits) {
            this.headTraits = List.of(traits);
            return this;
        }

        public Builder handleTraits(TraitEntry... traits) {
            this.handleTraits = List.of(traits);
            return this;
        }

        public Builder extraTraits(TraitEntry... traits) {
            this.extraTraits = List.of(traits);
            return this;
        }

        public Builder repairItem(Item item) {
            this.repairItem = item;
            return this;
        }

        public Builder colors(int primary, int secondary) {
            this.primaryColor = primary;
            this.secondaryColor = secondary;
            return this;
        }

        public Builder rarityWeight(double weight) {
            this.rarityWeight = weight;
            return this;
        }

        public Material build() {
            return new Material(this);
        }
    }

    // ============================================
    // 内部クラス
    // ============================================

    /**
     * 特性エントリ
     *
     * 素材が付与する特性とそのレベルを表します。
     */
    public record TraitEntry(
            ResourceLocation traitId,
            int level
    ) {
        /**
         * 簡易作成メソッド
         *
         * @param traitId 特性ID（"anvil:magnetic"形式）
         * @param level レベル
         * @return TraitEntry
         */
        public static TraitEntry of(String traitId, int level) {
            return new TraitEntry(ResourceLocation.parse(traitId), level);
        }
    }

    /**
     * 素材カテゴリ
     *
     * 素材の分類を表します。シナジーや特定レシピで使用されます。
     */
    public enum MaterialCategory {
        METAL("metal", "金属"),
        WOOD("wood", "木材"),
        STONE("stone", "石"),
        GEM("gem", "宝石"),
        ORGANIC("organic", "有機物"),
        ALLOY("alloy", "合金"),
        MAGICAL("magical", "魔法"),
        NETHER("nether", "ネザー"),
        END("end", "エンド"),
        FIBER("fiber", "繊維");

        private final String id;
        private final String displayNameJa;

        MaterialCategory(String id, String displayNameJa) {
            this.id = id;
            this.displayNameJa = displayNameJa;
        }

        public String getId() {
            return id;
        }

        public String getDisplayNameJa() {
            return displayNameJa;
        }

        public static Optional<MaterialCategory> fromId(String id) {
            for (MaterialCategory category : values()) {
                if (category.id.equals(id)) {
                    return Optional.of(category);
                }
            }
            return Optional.empty();
        }
    }
}
