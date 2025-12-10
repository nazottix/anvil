package io.github.nazottix.anvil.trait;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 特性レジストリ
 *
 * すべての特性を管理するシングルトンレジストリです。
 * 特性の登録、検索、フィルタリング機能を提供します。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.6 特性システム
 */
public class TraitRegistry {

    // ロガー（日本語でログを出力）
    private static final Logger LOGGER = LoggerFactory.getLogger(TraitRegistry.class);

    // シングルトンインスタンス
    private static final TraitRegistry INSTANCE = new TraitRegistry();

    // 特性マップ（ID → Trait）
    private final Map<ResourceLocation, Trait> traits = new LinkedHashMap<>();

    // 初期化フラグ
    private boolean initialized = false;

    // プライベートコンストラクタ（シングルトン）
    private TraitRegistry() {
    }

    /**
     * インスタンスを取得
     *
     * @return TraitRegistryインスタンス
     */
    public static TraitRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * レジストリを初期化
     *
     * MOD初期化時に呼び出されます。
     */
    public void initialize() {
        if (initialized) {
            LOGGER.warn("特性レジストリは既に初期化されています");
            return;
        }

        LOGGER.info("特性レジストリを初期化しています...");

        // 初期特性を登録
        Traits.registerAll(this);

        initialized = true;
        LOGGER.info("特性レジストリの初期化が完了しました。登録特性数: {}", traits.size());
    }

    /**
     * 特性を登録
     *
     * @param trait 登録する特性
     * @throws IllegalArgumentException 同じIDの特性が既に存在する場合
     */
    public void register(Trait trait) {
        ResourceLocation id = trait.getId();

        if (traits.containsKey(id)) {
            throw new IllegalArgumentException("特性ID '" + id + "' は既に登録されています");
        }

        traits.put(id, trait);
        LOGGER.debug("特性を登録しました: {} (カテゴリ: {})", id, trait.getCategory().getDisplayNameJa());
    }

    /**
     * IDから特性を取得
     *
     * @param id 特性ID
     * @return 特性（存在しない場合はOptional.empty()）
     */
    public Optional<Trait> get(ResourceLocation id) {
        return Optional.ofNullable(traits.get(id));
    }

    /**
     * 文字列IDから特性を取得
     *
     * @param id 特性ID（"anvil:magnetic"形式）
     * @return 特性（存在しない場合はOptional.empty()）
     */
    public Optional<Trait> get(String id) {
        return get(ResourceLocation.parse(id));
    }

    /**
     * 全特性を取得
     *
     * @return 全特性のコレクション（変更不可）
     */
    public Collection<Trait> getAll() {
        return Collections.unmodifiableCollection(traits.values());
    }

    /**
     * 全特性IDを取得
     *
     * @return 全特性IDのセット（変更不可）
     */
    public Set<ResourceLocation> getAllIds() {
        return Collections.unmodifiableSet(traits.keySet());
    }

    /**
     * カテゴリでフィルタリング
     *
     * @param category 特性カテゴリ
     * @return 指定カテゴリの特性リスト
     */
    public List<Trait> getByCategory(TraitCategory category) {
        return traits.values().stream()
                .filter(t -> t.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * トリガーでフィルタリング
     *
     * @param trigger 発動トリガー
     * @return 指定トリガーの特性リスト
     */
    public List<Trait> getByTrigger(TraitTrigger trigger) {
        return traits.values().stream()
                .filter(t -> t.getTrigger() == trigger)
                .collect(Collectors.toList());
    }

    /**
     * レアリティでフィルタリング
     *
     * @param minRarity 最小レアリティ
     * @param maxRarity 最大レアリティ
     * @return 条件を満たす特性リスト
     */
    public List<Trait> getByRarityRange(int minRarity, int maxRarity) {
        return traits.values().stream()
                .filter(t -> t.getRarity() >= minRarity && t.getRarity() <= maxRarity)
                .collect(Collectors.toList());
    }

    /**
     * 互換性のある特性リストを取得
     *
     * @param existingTraits 既存の特性リスト
     * @return 互換性のある特性リスト
     */
    public List<Trait> getCompatibleTraits(List<Trait> existingTraits) {
        return traits.values().stream()
                .filter(candidate -> {
                    for (Trait existing : existingTraits) {
                        if (candidate.isIncompatibleWith(existing) || existing.isIncompatibleWith(candidate)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 特性が存在するかチェック
     *
     * @param id 特性ID
     * @return 存在する場合true
     */
    public boolean contains(ResourceLocation id) {
        return traits.containsKey(id);
    }

    /**
     * 登録された特性数を取得
     *
     * @return 特性数
     */
    public int size() {
        return traits.size();
    }

    /**
     * レジストリが初期化済みかチェック
     *
     * @return 初期化済みの場合true
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * レジストリをクリア（テスト用）
     */
    void clear() {
        traits.clear();
        initialized = false;
        LOGGER.warn("特性レジストリがクリアされました（テストモード）");
    }
}
