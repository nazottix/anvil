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
 * - 木材: 2種（木材、竹）
 * - 石: 3種（石、フリント、深層岩）
 * - 銅: 3種（銅、金、鎖）
 * - 鉄: 3種（鉄、レッドストーン、ラピス）
 * - ダイヤモンド: 2種（ダイヤモンド、エメラルド）
 * - クォーツ: 3種（クォーツ、プリズマリン、アメジスト）
 * - 黒曜石: 3種（黒曜石、ブレイズ、マグマブロック）
 * - エンダー: 3種（エンダーパール、エンドストーン、シュルカー）
 * - 伝説: 1種（ネザライト）
 * - 神話: 2種（ネザースター、ドラゴン）
 * - 繊維: 1種（糸）
 * - その他: 3種（骨、サボテン、石炭）
 *
 * 合計: 29種
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.4 マテリアル（素材）システム
 *
 * 変更履歴: ティア概念を削除、骨・サボテン・石炭を追加
 */
public final class Materials {

    // プライベートコンストラクタ（インスタンス化禁止）
    private Materials() {
    }

    // ============================================
    // 木材（2種）- 木材（全木材対応）、竹
    // ============================================

    /** 木材 - 最も基本的な素材（全木材の板材で作成・修理可能、竹は除く） */
    public static final Material WOOD = Material.builder(loc("wood"))
            .categories(MaterialCategory.WOOD, MaterialCategory.ORGANIC)
            .headStats(60, 2.0f, 0, 1.0f)
            .handleStats(1.0f, 0.0f)
            .bindingStats(1.0f)
            .bowLimbStats(1.0f, 1.0f)
            .bowstringStats(1.0f, 0.8f)
            .hookStats(1.0f, 0.0f)
            .headTraits(TraitEntry.of("anvil:take_root", 1))  // 根を張る: 60秒毎に1耐久回復
            .handleTraits(
                    TraitEntry.of("anvil:lightweight", 1),
                    TraitEntry.of("anvil:take_root", 1)  // 根を張る
            )
            .extraTraits(TraitEntry.of("anvil:take_root", 1))  // 根を張る
            .repairItem(Items.OAK_PLANKS)  // 代表アイテム、実際は全木材の板材対応（タグで処理）
            .colors(0xC4A05A, 0xA68B4B)
            .rarityWeight(1.0)
            .build();

    /** 竹 - 軽量だが脆い */
    public static final Material BAMBOO = Material.builder(loc("bamboo"))
            .categories(MaterialCategory.WOOD, MaterialCategory.ORGANIC)
            .headStats(45, 2.2f, 0, 0.8f)
            .handleStats(0.8f, 0.1f)  // 攻撃速度ボーナス
            .bindingStats(0.9f)
            .bowLimbStats(1.2f, 0.9f)  // 引き速度が速い
            .bowstringStats(1.1f, 0.7f)
            .hookStats(1.1f, 0.0f)
            .headTraits(
                    TraitEntry.of("anvil:lightweight", 1),
                    TraitEntry.of("anvil:take_root", 2)  // 根を張る Lv2
            )
            .handleTraits(
                    TraitEntry.of("anvil:lightweight", 2),
                    TraitEntry.of("anvil:take_root", 2)  // 根を張る Lv2
            )
            .extraTraits(
                    TraitEntry.of("anvil:lightweight", 1),
                    TraitEntry.of("anvil:take_root", 2)  // 根を張る Lv2
            )
            .repairItem(Items.BAMBOO)
            .colors(0x7BA05B, 0x5C8A3D)
            .rarityWeight(0.9)
            .build();

    // ============================================
    // 石（3種）
    // ============================================

    /** 石 - 基本的な石素材 */
    public static final Material STONE = Material.builder(loc("stone"))
            .categories(MaterialCategory.STONE)
            .headStats(130, 4.0f, 1, 1.5f)
            .handleStats(0.9f, -0.1f)  // 脆いハンドル
            .bindingStats(1.0f)
            .bowLimbStats(0.7f, 1.0f)  // 弓には不向き
            .bowstringStats(0.5f, 0.6f)
            .hookStats(0.8f, 0.0f)
            .repairItem(Items.COBBLESTONE)
            .colors(0x8F8F8F, 0x6F6F6F)
            .rarityWeight(1.0)
            .build();

    /** フリント - 鋭い石 */
    public static final Material FLINT = Material.builder(loc("flint"))
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

    /** 深層岩 - 地下深くの頑丈な石 */
    public static final Material DEEPSLATE = Material.builder(loc("deepslate"))
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
    // 銅（3種）- 銅、金、鎖
    // ============================================

    /** 銅 - 柔軟な金属 */
    public static final Material COPPER = Material.builder(loc("copper"))
            .categories(MaterialCategory.METAL)
            .headStats(200, 5.5f, 1, 1.8f)  // 変更: 採掘レベル2→1
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
            .categories(MaterialCategory.METAL, MaterialCategory.MAGICAL)
            .headStats(50, 12.0f, 1, 1.5f)  // 変更: 採掘レベル2→1、速いが耐久低い
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
            .categories(MaterialCategory.METAL, MaterialCategory.FIBER)
            .headStats(150, 4.0f, 1, 1.5f)  // 変更: 採掘レベル2→1
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

    // ============================================
    // 鉄（3種）- 鉄、レッドストーン、ラピス
    // ============================================

    /** 鉄 - バランスの取れた金属 */
    public static final Material IRON = Material.builder(loc("iron"))
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

    /** レッドストーン - 魔法的な鉱石 */
    public static final Material REDSTONE = Material.builder(loc("redstone"))
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
    // クォーツ（3種）- クォーツ、プリズマリン、アメジスト
    // ============================================

    /** クォーツ - ネザー産の白い鉱石 */
    public static final Material QUARTZ = Material.builder(loc("quartz"))
            .categories(MaterialCategory.GEM, MaterialCategory.NETHER)
            .headStats(1000, 7.5f, 3, 2.5f)  // 変更: 採掘レベル3
            .handleStats(0.9f, 0.05f)
            .bindingStats(1.15f)
            .bowLimbStats(1.0f, 1.1f)
            .bowstringStats(0.9f, 0.95f)
            .hookStats(1.1f, 0.1f)
            .headTraits(TraitEntry.of("anvil:jagged", 2))  // 鋭い
            .handleTraits(TraitEntry.of("anvil:lightweight", 1))
            .repairItem(Items.QUARTZ)
            .colors(0xEAE5DE, 0xD5CFC5)  // クォーツの白色
            .rarityWeight(0.45)
            .build();

    /** ラピスラズリ - 経験値関連 */
    public static final Material LAPIS = Material.builder(loc("lapis"))
            .categories(MaterialCategory.GEM, MaterialCategory.MAGICAL)
            .headStats(280, 5.5f, 2, 2.0f)  // 変更: ティア3相当のステータス
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
            .categories(MaterialCategory.GEM, MaterialCategory.MAGICAL)
            .headStats(900, 7.2f, 3, 2.3f)  // 変更: 採掘レベル3
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
            .categories(MaterialCategory.GEM, MaterialCategory.MAGICAL)
            .headStats(1100, 7.8f, 3, 2.4f)  // 変更: 採掘レベル3
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

    // ============================================
    // ダイヤモンド（2種）- ダイヤモンド、エメラルド
    // ============================================

    /** ダイヤモンド - 高品質な宝石 */
    public static final Material DIAMOND = Material.builder(loc("diamond"))
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

    // ============================================
    // 黒曜石（3種）- 黒曜石、ブレイズ、マグマブロック
    // ============================================

    /** 黒曜石 - 非常に硬い */
    public static final Material OBSIDIAN = Material.builder(loc("obsidian"))
            .categories(MaterialCategory.STONE, MaterialCategory.MAGICAL)
            .headStats(1300, 5.0f, 3, 3.0f)
            .handleStats(1.3f, -0.2f)  // 重くて遅い
            .bindingStats(1.2f)
            .bowLimbStats(0.5f, 1.2f)
            .bowstringStats(0.3f, 0.4f)
            .hookStats(0.6f, 0.1f)
            .headTraits(
                    TraitEntry.of("anvil:reinforced", 2),
                    TraitEntry.of("anvil:stonebound", 2)
            )
            .handleTraits(TraitEntry.of("anvil:reinforced", 3))
            .repairItem(Items.OBSIDIAN)
            .colors(0x0F0A18, 0x1A0F28)
            .rarityWeight(0.4)
            .build();

    /** ブレイズ - 炎属性 */
    public static final Material BLAZE = Material.builder(loc("blaze"))
            .categories(MaterialCategory.ORGANIC, MaterialCategory.NETHER, MaterialCategory.MAGICAL)
            .headStats(1300, 7.5f, 3, 3.2f)  // ティア6相当のステータス
            .handleStats(0.9f, 0.1f)
            .bindingStats(1.15f)
            .bowLimbStats(1.2f, 1.1f)
            .bowstringStats(1.15f, 0.9f)
            .hookStats(0.8f, 0.1f)
            .headTraits(TraitEntry.of("anvil:fiery", 2))  // 炎属性
            .handleTraits(TraitEntry.of("anvil:fiery", 1))
            .repairItem(Items.BLAZE_ROD)
            .colors(0xFFA500, 0xDD8800)  // オレンジ色
            .rarityWeight(0.35)
            .build();

    /** マグマブロック - 溶岩の熱を持つブロック */
    public static final Material MAGMA_BLOCK = Material.builder(loc("magma_block"))
            .categories(MaterialCategory.STONE, MaterialCategory.NETHER, MaterialCategory.MAGICAL)
            .headStats(950, 6.5f, 3, 3.0f)
            .handleStats(1.0f, -0.05f)  // 熱くて扱いにくい
            .bindingStats(1.1f)
            .bowLimbStats(0.8f, 1.0f)
            .bowstringStats(0.7f, 0.8f)
            .hookStats(0.9f, 0.08f)
            .headTraits(TraitEntry.of("anvil:fiery", 2))  // 炎属性
            .handleTraits(TraitEntry.of("anvil:fiery", 1))
            .repairItem(Items.MAGMA_BLOCK)
            .colors(0xC54D0D, 0x8B3707)  // マグマブロックのオレンジ/赤色
            .rarityWeight(0.5)
            .build();

    // ============================================
    // エンダー（3種）- エンダーパール、エンドストーン、シュルカー
    // ============================================

    /** エンダーパール - テレポート・エンド素材 */
    public static final Material ENDER_PEARL = Material.builder(loc("ender_pearl"))
            .categories(MaterialCategory.MAGICAL, MaterialCategory.END)
            .headStats(1400, 7.5f, 4, 2.5f)  // ティア7相当のステータス
            .handleStats(0.9f, 0.1f)  // 軽量で速い
            .bindingStats(1.3f)  // 特性増幅高い
            .bowLimbStats(1.1f, 1.3f)  // 長射程
            .bowstringStats(1.0f, 0.9f)
            .hookStats(1.2f, 0.15f)
            .headTraits(TraitEntry.of("anvil:ethereal", 2))
            .handleTraits(TraitEntry.of("anvil:lightweight", 1))
            .repairItem(Items.ENDER_PEARL)
            .colors(0x0C5E4E, 0x0A4A3D)  // エンダーパールの深緑色
            .rarityWeight(0.25)
            .build();

    /** エンドストーン - エンド素材 */
    public static final Material END_STONE = Material.builder(loc("end_stone"))
            .categories(MaterialCategory.STONE, MaterialCategory.END)
            .headStats(1500, 7.8f, 4, 3.0f)  // ティア7相当のステータス
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
            .categories(MaterialCategory.ORGANIC, MaterialCategory.END, MaterialCategory.MAGICAL)
            .headStats(1450, 7.2f, 4, 2.8f)  // ティア7相当のステータス
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
    // 伝説（1種）- ネザライト
    // ============================================

    /** ネザライト - 最強のバニラ素材 */
    public static final Material NETHERITE = Material.builder(loc("netherite"))
            .categories(MaterialCategory.METAL, MaterialCategory.ALLOY, MaterialCategory.NETHER)
            .headStats(2000, 9.0f, 5, 4.0f)  // ティア8相当のステータス
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
            .colors(0x4A4A4A, 0x3A3030)  // ネザライトの暗灰色
            .rarityWeight(0.1)
            .build();

    // ============================================
    // 神話（2種）- ネザースター、ドラゴン
    // ============================================

    /** ネザースター - ウィザー討伐報酬、最強クラスの素材 */
    public static final Material NETHER_STAR = Material.builder(loc("nether_star"))
            .categories(MaterialCategory.MAGICAL, MaterialCategory.NETHER)
            .headStats(2500, 10.0f, 6, 5.0f)  // ティア9相当のステータス
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

    /** ドラゴン - エンダードラゴン討伐報酬（ドラゴンヘッドから作成） */
    public static final Material DRAGON = Material.builder(loc("dragon"))
            .categories(MaterialCategory.ORGANIC, MaterialCategory.END, MaterialCategory.MAGICAL)
            .headStats(2800, 9.5f, 6, 5.5f)  // ティア9相当のステータス
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
            .repairItem(Items.DRAGON_HEAD)  // 修理素材はドラゴンヘッド
            .colors(0x1A1A1A, 0x2A1A2A)  // 暗い紫がかった黒
            .rarityWeight(0.03)  // 超レア
            .build();

    // ============================================
    // 繊維素材（弦、ライン用）
    // ============================================

    /** 糸 - 基本の繊維 */
    public static final Material STRING = Material.builder(loc("string"))
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
    // その他素材（3種）- 骨、サボテン、石炭
    // ============================================

    /** 骨 - 軽量だが脆い有機素材 */
    public static final Material BONE = Material.builder(loc("bone"))
            .categories(MaterialCategory.ORGANIC)
            .headStats(80, 3.0f, 0, 1.8f)  // 攻撃力やや高め
            .handleStats(0.75f, 0.1f)  // 軽量で速い
            .bindingStats(1.0f)
            .bowLimbStats(0.9f, 0.9f)
            .bowstringStats(0.6f, 0.7f)
            .hookStats(1.1f, 0.05f)  // 釣り針として使える
            .headTraits(TraitEntry.of("anvil:jagged", 1))  // 骨は鋭い
            .handleTraits(TraitEntry.of("anvil:lightweight", 1))
            .repairItem(Items.BONE)
            .colors(0xE3DAC9, 0xD4C4A8)  // 骨の色
            .rarityWeight(0.9)
            .build();

    /** サボテン - 棘による追加ダメージ */
    public static final Material CACTUS = Material.builder(loc("cactus"))
            .categories(MaterialCategory.ORGANIC, MaterialCategory.WOOD)
            .headStats(50, 2.5f, 0, 2.0f)  // 低耐久だが攻撃力高め
            .handleStats(0.6f, 0.0f)  // ハンドルには不向き（棘が痛い）
            .bindingStats(0.9f)
            .bowLimbStats(0.7f, 0.8f)
            .bowstringStats(0.5f, 0.6f)
            .hookStats(1.2f, 0.08f)  // 棘で引っかかりやすい
            .headTraits(TraitEntry.of("anvil:jagged", 2))  // 棘による追加ダメージ
            .handleTraits(TraitEntry.of("anvil:jagged", 1))
            .repairItem(Items.CACTUS)
            .colors(0x5B8731, 0x3D5E1F)  // サボテンの緑色
            .rarityWeight(0.85)
            .build();

    /** 石炭 - 燃焼効果を持つ素材 */
    public static final Material COAL = Material.builder(loc("coal"))
            .categories(MaterialCategory.STONE)
            .headStats(100, 3.5f, 1, 1.5f)
            .handleStats(0.8f, 0.0f)
            .bindingStats(1.1f)
            .bowLimbStats(0.6f, 0.9f)
            .bowstringStats(0.5f, 0.6f)
            .hookStats(0.7f, 0.0f)
            .headTraits(TraitEntry.of("anvil:fiery", 1))  // 燃焼効果
            .handleTraits(TraitEntry.of("anvil:auto_smelt", 1))  // 自動精錬
            .repairItem(Items.COAL)
            .colors(0x2D2D2D, 0x1A1A1A)  // 石炭の黒色
            .rarityWeight(0.95)
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
        // 木材（2種）
        registry.register(WOOD);
        registry.register(BAMBOO);

        // 石（3種）
        registry.register(STONE);
        registry.register(FLINT);
        registry.register(DEEPSLATE);

        // 銅（3種）
        registry.register(COPPER);
        registry.register(GOLD);
        registry.register(CHAIN);

        // 鉄（3種）
        registry.register(IRON);
        registry.register(REDSTONE);
        registry.register(LAPIS);

        // ダイヤモンド（2種）
        registry.register(DIAMOND);
        registry.register(EMERALD);

        // クォーツ（3種）
        registry.register(QUARTZ);
        registry.register(PRISMARINE);
        registry.register(AMETHYST);

        // 黒曜石（3種）
        registry.register(OBSIDIAN);
        registry.register(BLAZE);
        registry.register(MAGMA_BLOCK);

        // エンダー（3種）
        registry.register(ENDER_PEARL);
        registry.register(END_STONE);
        registry.register(SHULKER);

        // 伝説（1種）
        registry.register(NETHERITE);

        // 神話（2種）
        registry.register(NETHER_STAR);
        registry.register(DRAGON);

        // 繊維（1種）
        registry.register(STRING);

        // その他（3種）
        registry.register(BONE);
        registry.register(CACTUS);
        registry.register(COAL);
    }
}
