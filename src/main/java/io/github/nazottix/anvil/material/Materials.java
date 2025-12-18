package io.github.nazottix.anvil.material;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import io.github.nazottix.anvil.material.Material.MaterialCategory;
import static io.github.nazottix.anvil.material.Material.TraitEntry;

/**
 * 素材定義
 *
 * バニラMinecraftのアイテムを使用した素材を定義します。
 *
 * 素材構成:
 * - ティア0（木材）: 2種（オーク、竹）
 * - ティア1（石）: 4種（石、フリント、黒曜石、深層岩）
 * - ティア2（鉄）: 5種（鉄、銅、金、鎖、レッドストーン）
 * - ティア3（ダイヤモンド）: 6種（ダイヤモンド、エメラルド、ラピス、アメジスト、プリズマリン、エンダーパール）
 * - ティア4（ネザライト）: 4種（ネザライト、ブレイズ、エンドストーン、シュルカー）
 * - ティア5（伝説）: 2種（ネザースター、ドラゴンヘッド）
 * - 繊維: 1種（糸）
 *
 * 合計: 24種
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.4 マテリアル（素材）システム
 */
public final class Materials {

    // プライベートコンストラクタ（インスタンス化禁止）
    private Materials() {
    }

    // ============================================
    // ティア0: 木材（2種）- ダークオーク削除
    // ============================================

    /** オーク - 最も基本的な木材 */
    public static final Material OAK = Material.builder(loc("oak"))
            .tier(MaterialTier.WOOD)
            .categories(MaterialCategory.WOOD, MaterialCategory.ORGANIC)
            .headStats(60, 2.0f, 0, 1.0f)
            .handleStats(1.0f, 0.0f)
            .bindingStats(1.0f)
            .bowLimbStats(1.0f, 1.0f)
            .bowstringStats(1.0f, 0.8f)
            .hookStats(1.0f, 0.0f)
            .headTraits(TraitEntry.of("anvil:ecological", 1))
            .handleTraits(TraitEntry.of("anvil:lightweight", 1))
            .repairItem(Items.OAK_PLANKS)
            .colors(0xC4A05A, 0xA68B4B)
            .rarityWeight(1.0)
            .build();

    /** 竹 - 軽量だが脆い */
    public static final Material BAMBOO = Material.builder(loc("bamboo"))
            .tier(MaterialTier.WOOD)
            .categories(MaterialCategory.WOOD, MaterialCategory.ORGANIC)
            .headStats(45, 2.2f, 0, 0.8f)
            .handleStats(0.8f, 0.1f)  // 攻撃速度ボーナス
            .bindingStats(0.9f)
            .bowLimbStats(1.2f, 0.9f)  // 引き速度が速い
            .bowstringStats(1.1f, 0.7f)
            .hookStats(1.1f, 0.0f)
            .headTraits(TraitEntry.of("anvil:lightweight", 1))
            .handleTraits(TraitEntry.of("anvil:lightweight", 2))
            .repairItem(Items.BAMBOO)
            .colors(0x7BA05B, 0x5C8A3D)
            .rarityWeight(0.9)
            .build();

    // ============================================
    // ティア1: 石（4種）
    // ============================================

    /** 石 - 基本的な石素材 */
    public static final Material STONE = Material.builder(loc("stone"))
            .tier(MaterialTier.STONE)
            .categories(MaterialCategory.STONE)
            .headStats(130, 4.0f, 1, 1.5f)
            .handleStats(0.9f, -0.1f)  // 脆いハンドル
            .bindingStats(1.0f)
            .bowLimbStats(0.7f, 1.0f)  // 弓には不向き
            .bowstringStats(0.5f, 0.6f)
            .hookStats(0.8f, 0.0f)
            .headTraits(TraitEntry.of("anvil:stonebound", 1))
            .repairItem(Items.COBBLESTONE)
            .colors(0x8F8F8F, 0x6F6F6F)
            .rarityWeight(1.0)
            .build();

    /** フリント - 鋭い石 */
    public static final Material FLINT = Material.builder(loc("flint"))
            .tier(MaterialTier.STONE)
            .categories(MaterialCategory.STONE)
            .headStats(100, 3.5f, 1, 2.0f)  // 攻撃力高め
            .handleStats(0.7f, 0.0f)
            .bindingStats(1.1f)
            .bowLimbStats(0.6f, 0.9f)
            .bowstringStats(0.4f, 0.5f)
            .hookStats(1.2f, 0.05f)  // 釣り針として優秀
            .headTraits(TraitEntry.of("anvil:jagged", 1))
            .handleTraits(TraitEntry.of("anvil:jagged", 1))
            .repairItem(Items.FLINT)
            .colors(0x5A5A5A, 0x3A3A3A)
            .rarityWeight(0.9)
            .build();

    /** 黒曜石 - 非常に硬い */
    public static final Material OBSIDIAN = Material.builder(loc("obsidian"))
            .tier(MaterialTier.STONE)
            .categories(MaterialCategory.STONE, MaterialCategory.MAGICAL)
            .headStats(200, 3.0f, 1, 2.5f)
            .handleStats(1.3f, -0.2f)  // 重くて遅い
            .bindingStats(1.2f)
            .bowLimbStats(0.5f, 1.2f)
            .bowstringStats(0.3f, 0.4f)
            .hookStats(0.6f, 0.1f)
            .headTraits(
                    TraitEntry.of("anvil:reinforced", 1),
                    TraitEntry.of("anvil:stonebound", 1)
            )
            .handleTraits(TraitEntry.of("anvil:reinforced", 2))
            .repairItem(Items.OBSIDIAN)
            .colors(0x0F0A18, 0x1A0F28)
            .rarityWeight(0.6)
            .build();

    /** 深層岩 - 地下深くの頑丈な石 */
    public static final Material DEEPSLATE = Material.builder(loc("deepslate"))
            .tier(MaterialTier.STONE)
            .categories(MaterialCategory.STONE)
            .headStats(180, 3.8f, 1, 1.8f)
            .handleStats(1.1f, -0.1f)
            .bindingStats(1.15f)
            .bowLimbStats(0.6f, 1.0f)
            .bowstringStats(0.4f, 0.55f)
            .hookStats(0.7f, 0.02f)
            .headTraits(TraitEntry.of("anvil:reinforced", 1))
            .handleTraits(TraitEntry.of("anvil:stonebound", 1))
            .repairItem(Items.COBBLED_DEEPSLATE)
            .colors(0x4A4A4A, 0x3A3A3A)
            .rarityWeight(0.8)
            .build();

    // ============================================
    // ティア2: 鉄（5種）
    // ============================================

    /** 鉄 - バランスの取れた金属 */
    public static final Material IRON = Material.builder(loc("iron"))
            .tier(MaterialTier.IRON)
            .categories(MaterialCategory.METAL)
            .headStats(250, 6.0f, 2, 2.0f)
            .handleStats(1.0f, 0.0f)
            .bindingStats(1.0f)
            .bowLimbStats(0.9f, 1.1f)
            .bowstringStats(0.7f, 0.8f)
            .hookStats(1.0f, 0.05f)
            .headTraits(TraitEntry.of("anvil:magnetic", 1))
            .repairItem(Items.IRON_INGOT)
            .colors(0xD8D8D8, 0xA8A8A8)
            .rarityWeight(1.0)
            .build();

    /** 銅 - 柔軟な金属 */
    public static final Material COPPER = Material.builder(loc("copper"))
            .tier(MaterialTier.IRON)
            .categories(MaterialCategory.METAL)
            .headStats(200, 5.5f, 2, 1.8f)
            .handleStats(0.95f, 0.05f)  // 若干速い
            .bindingStats(1.05f)
            .bowLimbStats(1.0f, 1.0f)
            .bowstringStats(0.8f, 0.85f)
            .hookStats(1.1f, 0.03f)
            .headTraits(TraitEntry.of("anvil:shocking", 1))
            .handleTraits(TraitEntry.of("anvil:lightweight", 1))
            .repairItem(Items.COPPER_INGOT)
            .colors(0xE07850, 0xB86040)
            .rarityWeight(0.95)
            .build();

    /** 金 - 速いが脆い */
    public static final Material GOLD = Material.builder(loc("gold"))
            .tier(MaterialTier.IRON)
            .categories(MaterialCategory.METAL, MaterialCategory.MAGICAL)
            .headStats(50, 12.0f, 2, 1.5f)  // 速いが耐久低い
            .handleStats(0.6f, 0.15f)  // 非常に速い
            .bindingStats(1.3f)  // 特性増幅高い
            .bowLimbStats(1.3f, 0.8f)
            .bowstringStats(1.2f, 0.9f)
            .hookStats(1.5f, 0.15f)  // 幸運
            .headTraits(TraitEntry.of("anvil:lucky", 1))
            .handleTraits(TraitEntry.of("anvil:lucky", 1))
            .repairItem(Items.GOLD_INGOT)
            .colors(0xFCEE4B, 0xDEAA1F)
            .rarityWeight(0.7)
            .build();

    /** 鎖 - 繊維用（弦、ライン等） */
    public static final Material CHAIN = Material.builder(loc("chain"))
            .tier(MaterialTier.IRON)
            .categories(MaterialCategory.METAL, MaterialCategory.FIBER)
            .headStats(150, 4.0f, 2, 1.5f)
            .handleStats(0.9f, 0.0f)
            .bindingStats(1.2f)
            .bowLimbStats(0.7f, 0.9f)
            .bowstringStats(1.2f, 0.95f)  // 弦として優秀
            .hookStats(0.9f, 0.02f)
            .handleTraits(TraitEntry.of("anvil:reinforced", 1))
            .repairItem(Items.CHAIN)
            .colors(0x4A4A5A, 0x3A3A4A)
            .rarityWeight(0.8)
            .build();

    /** レッドストーン - 魔法的な鉱石 */
    public static final Material REDSTONE = Material.builder(loc("redstone"))
            .tier(MaterialTier.IRON)
            .categories(MaterialCategory.STONE, MaterialCategory.MAGICAL)
            .headStats(180, 5.0f, 2, 1.8f)
            .handleStats(0.85f, 0.1f)
            .bindingStats(1.25f)
            .bowLimbStats(1.1f, 0.95f)
            .bowstringStats(1.0f, 0.85f)
            .hookStats(1.2f, 0.08f)
            .headTraits(TraitEntry.of("anvil:shocking", 2))
            .handleTraits(TraitEntry.of("anvil:momentum", 1))
            .repairItem(Items.REDSTONE)
            .colors(0xFF0000, 0xAA0000)
            .rarityWeight(0.75)
            .build();

    // ============================================
    // ティア3: ダイヤモンド（6種）- エンダーパール追加
    // ============================================

    /** ダイヤモンド - 高品質な宝石 */
    public static final Material DIAMOND = Material.builder(loc("diamond"))
            .tier(MaterialTier.DIAMOND)
            .categories(MaterialCategory.GEM)
            .headStats(1500, 8.0f, 3, 3.0f)
            .handleStats(1.0f, 0.0f)
            .bindingStats(1.1f)
            .bowLimbStats(1.0f, 1.2f)
            .bowstringStats(0.9f, 0.95f)
            .hookStats(1.1f, 0.1f)
            .headTraits(TraitEntry.of("anvil:reinforced", 2))
            .repairItem(Items.DIAMOND)
            .colors(0x4AEDD9, 0x2CB8A8)
            .rarityWeight(0.4)
            .build();

    /** エメラルド - 幸運の宝石 */
    public static final Material EMERALD = Material.builder(loc("emerald"))
            .tier(MaterialTier.DIAMOND)
            .categories(MaterialCategory.GEM, MaterialCategory.MAGICAL)
            .headStats(1200, 7.5f, 3, 2.5f)
            .handleStats(0.9f, 0.05f)
            .bindingStats(1.25f)
            .bowLimbStats(1.1f, 1.1f)
            .bowstringStats(1.0f, 0.9f)
            .hookStats(1.3f, 0.2f)  // 幸運特化
            .headTraits(TraitEntry.of("anvil:lucky", 2))
            .handleTraits(TraitEntry.of("anvil:ecological", 1))
            .repairItem(Items.EMERALD)
            .colors(0x17DD62, 0x0DAA4A)
            .rarityWeight(0.35)
            .build();

    /** ラピスラズリ - 経験値関連 */
    public static final Material LAPIS = Material.builder(loc("lapis"))
            .tier(MaterialTier.DIAMOND)
            .categories(MaterialCategory.GEM, MaterialCategory.MAGICAL)
            .headStats(1000, 7.0f, 3, 2.2f)
            .handleStats(0.85f, 0.05f)
            .bindingStats(1.3f)  // 特性増幅高い
            .bowLimbStats(1.0f, 1.0f)
            .bowstringStats(0.95f, 0.85f)
            .hookStats(1.2f, 0.15f)
            .headTraits(
                    TraitEntry.of("anvil:ecological", 2),
                    TraitEntry.of("anvil:mending", 1)
            )
            .handleTraits(TraitEntry.of("anvil:ecological", 1))
            .repairItem(Items.LAPIS_LAZULI)
            .colors(0x1F4DB4, 0x163A8A)
            .rarityWeight(0.45)
            .build();

    /** アメジスト - 精度特化 */
    public static final Material AMETHYST = Material.builder(loc("amethyst"))
            .tier(MaterialTier.DIAMOND)
            .categories(MaterialCategory.GEM, MaterialCategory.MAGICAL)
            .headStats(900, 7.2f, 3, 2.3f)
            .handleStats(0.9f, 0.08f)
            .bindingStats(1.2f)
            .bowLimbStats(1.15f, 1.15f)
            .bowstringStats(1.1f, 1.0f)  // 精度が高い
            .hookStats(1.15f, 0.12f)
            .headTraits(TraitEntry.of("anvil:ethereal", 1))
            .handleTraits(TraitEntry.of("anvil:lightweight", 1))
            .repairItem(Items.AMETHYST_SHARD)
            .colors(0x9966CC, 0x7744AA)
            .rarityWeight(0.5)
            .build();

    /** プリズマリン - 水中特化 */
    public static final Material PRISMARINE = Material.builder(loc("prismarine"))
            .tier(MaterialTier.DIAMOND)
            .categories(MaterialCategory.GEM, MaterialCategory.MAGICAL)
            .headStats(1100, 7.8f, 3, 2.4f)
            .handleStats(1.0f, 0.0f)
            .bindingStats(1.15f)
            .bowLimbStats(0.9f, 1.0f)
            .bowstringStats(0.85f, 0.9f)
            .hookStats(1.5f, 0.25f)  // 釣り特化
            .headTraits(TraitEntry.of("anvil:aquadynamic", 2))
            .handleTraits(TraitEntry.of("anvil:aquadynamic", 1))
            .repairItem(Items.PRISMARINE_SHARD)
            .colors(0x6EB5A5, 0x4A9585)
            .rarityWeight(0.5)
            .build();

    /** エンダーパール - テレポート・エンド素材 */
    public static final Material ENDER_PEARL = Material.builder(loc("ender_pearl"))
            .tier(MaterialTier.DIAMOND)
            .categories(MaterialCategory.MAGICAL, MaterialCategory.END)
            .headStats(800, 7.0f, 3, 2.0f)
            .handleStats(0.85f, 0.1f)  // 軽量で速い
            .bindingStats(1.3f)  // 特性増幅高い
            .bowLimbStats(1.1f, 1.3f)  // 長射程
            .bowstringStats(1.0f, 0.9f)
            .hookStats(1.2f, 0.15f)
            .headTraits(TraitEntry.of("anvil:ethereal", 2))
            .handleTraits(TraitEntry.of("anvil:lightweight", 1))
            .repairItem(Items.ENDER_PEARL)
            .colors(0x0C5E4E, 0x0A4A3D)  // エンダーパールの深緑色
            .rarityWeight(0.35)
            .build();

    // ============================================
    // ティア4: ネザライト（4種）
    // ============================================

    /** ネザライト - 最強のバニラ素材 */
    public static final Material NETHERITE = Material.builder(loc("netherite"))
            .tier(MaterialTier.NETHERITE)
            .categories(MaterialCategory.METAL, MaterialCategory.ALLOY, MaterialCategory.NETHER)
            .headStats(2000, 9.0f, 4, 4.0f)
            .handleStats(1.2f, 0.0f)
            .bindingStats(1.2f)
            .bowLimbStats(1.1f, 1.3f)
            .bowstringStats(1.0f, 0.95f)
            .hookStats(1.2f, 0.15f)
            .headTraits(
                    TraitEntry.of("anvil:reinforced", 3),
                    TraitEntry.of("anvil:fiery", 1)
            )
            .handleTraits(TraitEntry.of("anvil:reinforced", 2))
            .repairItem(Items.NETHERITE_INGOT)
            .colors(0x4A4A4A, 0x3A3030)
            .rarityWeight(0.15)
            .build();

    /** ブレイズ - 炎属性 */
    public static final Material BLAZE = Material.builder(loc("blaze"))
            .tier(MaterialTier.NETHERITE)
            .categories(MaterialCategory.ORGANIC, MaterialCategory.NETHER, MaterialCategory.MAGICAL)
            .headStats(1600, 8.5f, 4, 3.5f)
            .handleStats(0.9f, 0.1f)
            .bindingStats(1.15f)
            .bowLimbStats(1.2f, 1.1f)
            .bowstringStats(1.15f, 0.9f)
            .hookStats(0.8f, 0.1f)
            .headTraits(TraitEntry.of("anvil:fiery", 2))
            .handleTraits(TraitEntry.of("anvil:fiery", 1))
            .repairItem(Items.BLAZE_ROD)
            .colors(0xFFA500, 0xDD8800)
            .rarityWeight(0.25)
            .build();

    /** エンドストーン - エンド素材 */
    public static final Material END_STONE = Material.builder(loc("end_stone"))
            .tier(MaterialTier.NETHERITE)
            .categories(MaterialCategory.STONE, MaterialCategory.END)
            .headStats(1700, 7.5f, 4, 3.2f)
            .handleStats(1.1f, -0.05f)
            .bindingStats(1.25f)
            .bowLimbStats(0.9f, 1.4f)  // 長射程
            .bowstringStats(0.85f, 0.95f)
            .hookStats(1.0f, 0.2f)
            .headTraits(TraitEntry.of("anvil:ethereal", 2))
            .handleTraits(TraitEntry.of("anvil:reinforced", 1))
            .repairItem(Items.END_STONE)
            .colors(0xDBDBA5, 0xC5C590)
            .rarityWeight(0.2)
            .build();

    /** シュルカー - ユーティリティ特化 */
    public static final Material SHULKER = Material.builder(loc("shulker"))
            .tier(MaterialTier.NETHERITE)
            .categories(MaterialCategory.ORGANIC, MaterialCategory.END, MaterialCategory.MAGICAL)
            .headStats(1400, 7.0f, 4, 2.8f)
            .handleStats(1.05f, 0.05f)
            .bindingStats(1.35f)  // 高い特性増幅
            .bowLimbStats(1.0f, 1.5f)  // 超長射程
            .bowstringStats(0.95f, 0.85f)
            .hookStats(1.1f, 0.18f)
            .headTraits(TraitEntry.of("anvil:writable", 1))
            .handleTraits(TraitEntry.of("anvil:magnetic", 2))
            .repairItem(Items.SHULKER_SHELL)
            .colors(0x946B94, 0x7A577A)
            .rarityWeight(0.18)
            .build();

    // ============================================
    // ティア5: 伝説（2種）- ネザースター、ドラゴンヘッド
    // ============================================

    /** ネザースター - ウィザー討伐報酬、最強クラスの素材 */
    public static final Material NETHER_STAR = Material.builder(loc("nether_star"))
            .tier(MaterialTier.LEGENDARY)
            .categories(MaterialCategory.MAGICAL, MaterialCategory.NETHER)
            .headStats(2500, 10.0f, 5, 5.0f)  // 非常に高いステータス
            .handleStats(1.3f, 0.1f)  // 耐久・速度両方ボーナス
            .bindingStats(1.5f)  // 特性増幅最高クラス
            .bowLimbStats(1.3f, 1.5f)  // 高速・長射程
            .bowstringStats(1.2f, 1.0f)
            .hookStats(1.5f, 0.3f)  // 幸運特化
            .headTraits(
                    TraitEntry.of("anvil:holy", 2),
                    TraitEntry.of("anvil:reinforced", 3)
            )
            .handleTraits(TraitEntry.of("anvil:mending", 2))
            .repairItem(Items.NETHER_STAR)
            .colors(0xE8E8E0, 0xD0D0C8)  // 白〜クリーム色
            .rarityWeight(0.05)  // 非常にレア
            .build();

    /** ドラゴンヘッド - エンダードラゴン討伐報酬 */
    public static final Material DRAGON_HEAD = Material.builder(loc("dragon_head"))
            .tier(MaterialTier.LEGENDARY)
            .categories(MaterialCategory.ORGANIC, MaterialCategory.END, MaterialCategory.MAGICAL)
            .headStats(2800, 9.5f, 5, 5.5f)  // 最高クラスの攻撃力
            .handleStats(1.25f, 0.05f)
            .bindingStats(1.45f)
            .bowLimbStats(1.2f, 1.6f)  // 超長射程
            .bowstringStats(1.1f, 0.95f)
            .hookStats(1.3f, 0.25f)
            .headTraits(
                    TraitEntry.of("anvil:ethereal", 3),
                    TraitEntry.of("anvil:fiery", 2)
            )
            .handleTraits(TraitEntry.of("anvil:reinforced", 2))
            .repairItem(Items.DRAGON_HEAD)
            .colors(0x1A1A1A, 0x2A1A2A)  // 暗い紫がかった黒
            .rarityWeight(0.03)  // 超レア
            .build();

    // ============================================
    // 繊維素材（弦、ライン用）
    // ============================================

    /** 糸 - 基本の繊維 */
    public static final Material STRING = Material.builder(loc("string"))
            .tier(MaterialTier.WOOD)
            .categories(MaterialCategory.ORGANIC, MaterialCategory.FIBER)
            .headStats(30, 1.0f, 0, 0.5f)
            .handleStats(0.7f, 0.05f)
            .bindingStats(1.0f)
            .bowLimbStats(0.8f, 0.8f)
            .bowstringStats(1.0f, 0.85f)  // 弦用
            .hookStats(0.8f, 0.0f)
            .repairItem(Items.STRING)
            .colors(0xEEEEEE, 0xDDDDDD)
            .rarityWeight(1.0)
            .build();

    // ============================================
    // ヘルパーメソッド
    // ============================================

    /**
     * ANVIL名前空間でResourceLocationを作成
     *
     * @param path パス
     * @return ResourceLocation
     */
    private static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, path);
    }

    /**
     * すべての素材をレジストリに登録
     *
     * @param registry 登録先レジストリ
     */
    public static void registerAll(MaterialRegistry registry) {
        // ティア0: 木材（2種）
        registry.register(OAK);
        registry.register(BAMBOO);

        // ティア1: 石（4種）
        registry.register(STONE);
        registry.register(FLINT);
        registry.register(OBSIDIAN);
        registry.register(DEEPSLATE);

        // ティア2: 鉄（5種）
        registry.register(IRON);
        registry.register(COPPER);
        registry.register(GOLD);
        registry.register(CHAIN);
        registry.register(REDSTONE);

        // ティア3: ダイヤモンド（6種）
        registry.register(DIAMOND);
        registry.register(EMERALD);
        registry.register(LAPIS);
        registry.register(AMETHYST);
        registry.register(PRISMARINE);
        registry.register(ENDER_PEARL);  // 追加: エンダーパール

        // ティア4: ネザライト（4種）
        registry.register(NETHERITE);
        registry.register(BLAZE);
        registry.register(END_STONE);
        registry.register(SHULKER);

        // ティア5: 伝説（2種）
        registry.register(NETHER_STAR);   // 追加: ネザースター
        registry.register(DRAGON_HEAD);   // 追加: ドラゴンヘッド

        // 繊維（1種）
        registry.register(STRING);
    }
}
