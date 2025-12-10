package io.github.nazottix.anvil.material;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 素材レジストリ
 *
 * すべての素材を管理するシングルトンレジストリです。
 * 素材の登録、検索、フィルタリング機能を提供します。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public class MaterialRegistry {

    // ロガー（日本語でログを出力）
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialRegistry.class);

    // シングルトンインスタンス
    private static final MaterialRegistry INSTANCE = new MaterialRegistry();

    // 素材マップ（ID → Material）
    private final Map<ResourceLocation, Material> materials = new LinkedHashMap<>();

    // 初期化フラグ
    private boolean initialized = false;

    // プライベートコンストラクタ（シングルトン）
    private MaterialRegistry() {
    }

    /**
     * インスタンスを取得
     *
     * @return MaterialRegistryインスタンス
     */
    public static MaterialRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * レジストリを初期化
     *
     * MOD初期化時に呼び出されます。
     * 重複呼び出しは無視されます。
     */
    public void initialize() {
        if (initialized) {
            LOGGER.warn("素材レジストリは既に初期化されています");
            return;
        }

        LOGGER.info("素材レジストリを初期化しています...");

        // 初期素材を登録
        Materials.registerAll(this);

        initialized = true;
        LOGGER.info("素材レジストリの初期化が完了しました。登録素材数: {}", materials.size());
    }

    /**
     * 素材を登録
     *
     * @param material 登録する素材
     * @throws IllegalArgumentException 同じIDの素材が既に存在する場合
     */
    public void register(Material material) {
        ResourceLocation id = material.getId();

        if (materials.containsKey(id)) {
            throw new IllegalArgumentException("素材ID '" + id + "' は既に登録されています");
        }

        materials.put(id, material);
        LOGGER.debug("素材を登録しました: {} (ティア: {})", id, material.getTier().getDisplayNameJa());
    }

    /**
     * IDから素材を取得
     *
     * @param id 素材ID
     * @return 素材（存在しない場合はOptional.empty()）
     */
    public Optional<Material> get(ResourceLocation id) {
        return Optional.ofNullable(materials.get(id));
    }

    /**
     * 文字列IDから素材を取得
     *
     * @param id 素材ID（"anvil:iron"形式）
     * @return 素材（存在しない場合はOptional.empty()）
     */
    public Optional<Material> get(String id) {
        return get(ResourceLocation.parse(id));
    }

    /**
     * 全素材を取得
     *
     * @return 全素材のコレクション（変更不可）
     */
    public Collection<Material> getAll() {
        return Collections.unmodifiableCollection(materials.values());
    }

    /**
     * 全素材IDを取得
     *
     * @return 全素材IDのセット（変更不可）
     */
    public Set<ResourceLocation> getAllIds() {
        return Collections.unmodifiableSet(materials.keySet());
    }

    /**
     * ティアでフィルタリング
     *
     * @param tier 素材ティア
     * @return 指定ティアの素材リスト
     */
    public List<Material> getByTier(MaterialTier tier) {
        return materials.values().stream()
                .filter(m -> m.getTier() == tier)
                .collect(Collectors.toList());
    }

    /**
     * カテゴリでフィルタリング
     *
     * @param category 素材カテゴリ
     * @return 指定カテゴリを持つ素材リスト
     */
    public List<Material> getByCategory(Material.MaterialCategory category) {
        return materials.values().stream()
                .filter(m -> m.hasCategory(category))
                .collect(Collectors.toList());
    }

    /**
     * ティアとカテゴリでフィルタリング
     *
     * @param tier 素材ティア
     * @param category 素材カテゴリ
     * @return 条件を満たす素材リスト
     */
    public List<Material> getByTierAndCategory(MaterialTier tier, Material.MaterialCategory category) {
        return materials.values().stream()
                .filter(m -> m.getTier() == tier && m.hasCategory(category))
                .collect(Collectors.toList());
    }

    /**
     * 指定採掘レベル以上の素材を取得
     *
     * @param minMiningLevel 最小採掘レベル
     * @return 条件を満たす素材リスト
     */
    public List<Material> getByMinMiningLevel(int minMiningLevel) {
        return materials.values().stream()
                .filter(m -> m.getHeadStats().miningLevel() >= minMiningLevel)
                .collect(Collectors.toList());
    }

    /**
     * レアリティ重みでソートされた素材リストを取得
     *
     * @param ascending trueの場合昇順（レア→コモン）、falseの場合降順
     * @return ソートされた素材リスト
     */
    public List<Material> getSortedByRarity(boolean ascending) {
        Comparator<Material> comparator = Comparator.comparingDouble(Material::getRarityWeight);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return materials.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * 素材が存在するかチェック
     *
     * @param id 素材ID
     * @return 存在する場合true
     */
    public boolean contains(ResourceLocation id) {
        return materials.containsKey(id);
    }

    /**
     * 登録された素材数を取得
     *
     * @return 素材数
     */
    public int size() {
        return materials.size();
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
     *
     * 通常のゲームプレイでは使用しないでください。
     */
    void clear() {
        materials.clear();
        initialized = false;
        LOGGER.warn("素材レジストリがクリアされました（テストモード）");
    }
}
