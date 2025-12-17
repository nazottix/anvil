package io.github.nazottix.anvil.affix;

import java.util.*;

/**
 * アフィックス生成器
 *
 * ツールレベルとレアリティに基づいてアフィックスをランダム生成します。
 *
 * 仕様書参照: docs/06_レアリティシステム.md - 8.3 アフィックスシステム
 */
public class AffixGenerator {

    private final Random random;

    /**
     * デフォルトコンストラクタ
     */
    public AffixGenerator() {
        this.random = new Random();
    }

    /**
     * シード付きコンストラクタ
     *
     * @param seed ランダムシード
     */
    public AffixGenerator(long seed) {
        this.random = new Random(seed);
    }

    // ============================================
    // アフィックス生成
    // ============================================

    /**
     * レアリティに応じたアフィックスセットを生成
     *
     * @param rarity ツールレアリティ
     * @param toolLevel ツールレベル
     * @return 生成されたアフィックスインスタンスのリスト
     */
    public List<AffixInstance> generateAffixes(ToolRarity rarity, int toolLevel) {
        List<AffixInstance> result = new ArrayList<>();

        if (!rarity.canHaveAffixes()) {
            return result;
        }

        // プレフィックス生成
        int prefixCount = randomAffixCount(rarity.getMaxPrefixes(), rarity);
        List<Affix> prefixes = selectRandomAffixes(AffixType.PREFIX, prefixCount, toolLevel);
        for (Affix affix : prefixes) {
            AffixTier tier = selectRandomTier(affix, toolLevel);
            if (tier != null) {
                result.add(new AffixInstance(affix, tier));
            }
        }

        // サフィックス生成
        int suffixCount = randomAffixCount(rarity.getMaxSuffixes(), rarity);
        List<Affix> suffixes = selectRandomAffixes(AffixType.SUFFIX, suffixCount, toolLevel);
        for (Affix affix : suffixes) {
            AffixTier tier = selectRandomTier(affix, toolLevel);
            if (tier != null) {
                result.add(new AffixInstance(affix, tier));
            }
        }

        return result;
    }

    /**
     * 単一のアフィックスをランダム生成
     *
     * @param type アフィックスタイプ
     * @param toolLevel ツールレベル
     * @return 生成されたアフィックスインスタンス、生成不可の場合はnull
     */
    public AffixInstance generateSingleAffix(AffixType type, int toolLevel) {
        List<Affix> available = AffixRegistry.getAvailable(type, toolLevel);
        if (available.isEmpty()) {
            return null;
        }

        Affix affix = selectWeighted(available, toolLevel);
        AffixTier tier = selectRandomTier(affix, toolLevel);
        if (tier == null) {
            return null;
        }

        return new AffixInstance(affix, tier);
    }

    /**
     * 既存のアフィックスをリロール（値のみ再生成）
     *
     * @param existing 既存のアフィックスインスタンス
     * @param toolLevel ツールレベル
     * @return リロール後のアフィックスインスタンス
     */
    public AffixInstance rerollAffixTier(AffixInstance existing, int toolLevel) {
        AffixTier newTier = selectRandomTier(existing.affix(), toolLevel);
        return newTier != null ? new AffixInstance(existing.affix(), newTier) : existing;
    }

    // ============================================
    // 内部メソッド
    // ============================================

    /**
     * アフィックス数をランダム決定
     */
    private int randomAffixCount(int maxCount, ToolRarity rarity) {
        if (maxCount <= 0) {
            return 0;
        }

        // レアリティによって最小数を調整
        int minCount = switch (rarity) {
            case MAGIC -> 1;
            case RARE -> 2;
            case LEGACY -> 3;
            default -> 0;
        };

        return minCount + random.nextInt(maxCount - minCount + 1);
    }

    /**
     * ランダムにアフィックスを選択（重複なし）
     */
    private List<Affix> selectRandomAffixes(AffixType type, int count, int toolLevel) {
        List<Affix> available = new ArrayList<>(AffixRegistry.getAvailable(type, toolLevel));
        List<Affix> selected = new ArrayList<>();

        for (int i = 0; i < count && !available.isEmpty(); i++) {
            Affix affix = selectWeighted(available, toolLevel);
            selected.add(affix);
            available.remove(affix);  // 重複防止
        }

        return selected;
    }

    /**
     * 重み付きランダム選択
     */
    private Affix selectWeighted(List<Affix> affixes, int toolLevel) {
        if (affixes.isEmpty()) {
            return null;
        }
        if (affixes.size() == 1) {
            return affixes.get(0);
        }

        // 単純なランダム選択（将来的に重み付けを実装可能）
        return affixes.get(random.nextInt(affixes.size()));
    }

    /**
     * ティアをランダム選択
     */
    private AffixTier selectRandomTier(Affix affix, int toolLevel) {
        List<AffixTier> availableTiers = new ArrayList<>();
        float totalWeight = 0;

        for (AffixTier tier : affix.getTierEffects().keySet()) {
            if (tier.canAppearAtLevel(toolLevel)) {
                availableTiers.add(tier);
                totalWeight += tier.getDropWeight();
            }
        }

        if (availableTiers.isEmpty()) {
            return null;
        }

        // 重み付きランダム選択
        float roll = random.nextFloat() * totalWeight;
        float cumulative = 0;

        for (AffixTier tier : availableTiers) {
            cumulative += tier.getDropWeight();
            if (roll < cumulative) {
                return tier;
            }
        }

        // フォールバック
        return availableTiers.get(availableTiers.size() - 1);
    }
}
