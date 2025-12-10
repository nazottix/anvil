package io.github.nazottix.anvil.assembly;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.tool.PartType;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ツールビルダー
 *
 * パーツを指定してツールのItemStackを構築するためのビルダークラスです。
 * Fluent APIで使いやすいインターフェースを提供します。
 *
 * 使用例:
 * ```java
 * ItemStack pickaxe = ToolBuilder.create(ToolType.PICKAXE)
 *     .head("anvil:iron", Grade.A)
 *     .handle("anvil:oak", Grade.B)
 *     .binding("anvil:copper", Grade.C)
 *     .build();
 * ```
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public class ToolBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolBuilder.class);
    private static final Random RANDOM = new Random();

    private final ToolType toolType;
    private final List<ToolPart> parts = new ArrayList<>();

    /**
     * プライベートコンストラクタ
     */
    private ToolBuilder(ToolType toolType) {
        this.toolType = toolType;
    }

    /**
     * ビルダーを作成
     *
     * @param toolType ツールタイプ
     * @return ToolBuilder
     */
    public static ToolBuilder create(ToolType toolType) {
        return new ToolBuilder(toolType);
    }

    /**
     * ツールタイプIDでビルダーを作成
     *
     * @param toolTypeId ツールタイプID
     * @return ToolBuilder
     */
    public static ToolBuilder create(String toolTypeId) {
        ToolType type = ToolType.fromId(toolTypeId);
        if (type == null) {
            throw new IllegalArgumentException("無効なツールタイプID: " + toolTypeId);
        }
        return new ToolBuilder(type);
    }

    // ============================================
    // パーツ追加メソッド
    // ============================================

    /**
     * ヘッドパーツを追加
     */
    public ToolBuilder head(String materialId, Grade grade) {
        return addPart(materialId, PartType.HEAD, grade);
    }

    /**
     * ヘッドパーツを追加（グレードはランダム）
     */
    public ToolBuilder head(String materialId) {
        return head(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ヘッドパーツを追加（素材オブジェクト使用）
     */
    public ToolBuilder head(Material material, Grade grade) {
        return head(material.getId().toString(), grade);
    }

    /**
     * ハンドルパーツを追加
     */
    public ToolBuilder handle(String materialId, Grade grade) {
        return addPart(materialId, PartType.HANDLE, grade);
    }

    /**
     * ハンドルパーツを追加（グレードはランダム）
     */
    public ToolBuilder handle(String materialId) {
        return handle(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ハンドルパーツを追加（素材オブジェクト使用）
     */
    public ToolBuilder handle(Material material, Grade grade) {
        return handle(material.getId().toString(), grade);
    }

    /**
     * バインディングパーツを追加
     */
    public ToolBuilder binding(String materialId, Grade grade) {
        return addPart(materialId, PartType.BINDING, grade);
    }

    /**
     * バインディングパーツを追加（グレードはランダム）
     */
    public ToolBuilder binding(String materialId) {
        return binding(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ブレードパーツを追加
     */
    public ToolBuilder blade(String materialId, Grade grade) {
        return addPart(materialId, PartType.BLADE, grade);
    }

    /**
     * ブレードパーツを追加（グレードはランダム）
     */
    public ToolBuilder blade(String materialId) {
        return blade(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ガードパーツを追加
     */
    public ToolBuilder guard(String materialId, Grade grade) {
        return addPart(materialId, PartType.GUARD, grade);
    }

    /**
     * ガードパーツを追加（グレードはランダム）
     */
    public ToolBuilder guard(String materialId) {
        return guard(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ボウリムパーツを追加
     */
    public ToolBuilder bowLimb(String materialId, Grade grade) {
        return addPart(materialId, PartType.BOW_LIMB, grade);
    }

    /**
     * ボウリムパーツを追加（グレードはランダム）
     */
    public ToolBuilder bowLimb(String materialId) {
        return bowLimb(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ボウストリングパーツを追加
     */
    public ToolBuilder bowstring(String materialId, Grade grade) {
        return addPart(materialId, PartType.BOWSTRING, grade);
    }

    /**
     * ボウストリングパーツを追加（グレードはランダム）
     */
    public ToolBuilder bowstring(String materialId) {
        return bowstring(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ロッドパーツを追加
     */
    public ToolBuilder rod(String materialId, Grade grade) {
        return addPart(materialId, PartType.ROD, grade);
    }

    /**
     * ロッドパーツを追加（グレードはランダム）
     */
    public ToolBuilder rod(String materialId) {
        return rod(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * フックパーツを追加
     */
    public ToolBuilder hook(String materialId, Grade grade) {
        return addPart(materialId, PartType.HOOK, grade);
    }

    /**
     * フックパーツを追加（グレードはランダム）
     */
    public ToolBuilder hook(String materialId) {
        return hook(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ラインパーツを追加
     */
    public ToolBuilder line(String materialId, Grade grade) {
        return addPart(materialId, PartType.LINE, grade);
    }

    /**
     * ラインパーツを追加（グレードはランダム）
     */
    public ToolBuilder line(String materialId) {
        return line(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * ピボットパーツを追加
     */
    public ToolBuilder pivot(String materialId, Grade grade) {
        return addPart(materialId, PartType.PIVOT, grade);
    }

    /**
     * ピボットパーツを追加（グレードはランダム）
     */
    public ToolBuilder pivot(String materialId) {
        return pivot(materialId, Grade.rollGrade(RANDOM));
    }

    /**
     * 汎用パーツ追加メソッド
     */
    public ToolBuilder addPart(String materialId, PartType partType, Grade grade) {
        parts.add(new ToolPart(materialId, partType.getId(), grade.getId()));
        return this;
    }

    /**
     * ToolPartを直接追加
     */
    public ToolBuilder addPart(ToolPart part) {
        parts.add(part);
        return this;
    }

    // ============================================
    // ビルドメソッド
    // ============================================

    /**
     * ツールをビルド
     *
     * @return 構築されたItemStack、失敗時はnull
     */
    public ItemStack build() {
        // パーツを検証して組み立て
        AssemblyResult result = ToolAssembler.assemble(toolType, parts);

        if (result.isFailure()) {
            LOGGER.warn("ツールの組み立てに失敗しました: {}", result.errorMessage());
            return null;
        }

        // 対応するアイテムを取得
        Item item = getItemForToolType(toolType);
        if (item == null) {
            LOGGER.error("ツールタイプに対応するアイテムが見つかりません: {}", toolType.getId());
            return null;
        }

        // ItemStackを作成
        ItemStack stack = new ItemStack(item);

        // ツールデータを設定
        AnvilToolData toolData = AnvilToolData.create(toolType.getId(), parts);
        stack.set(AnvilDataComponents.TOOL_DATA.get(), toolData);

        // 計算されたステータスを設定
        stack.set(AnvilDataComponents.CALCULATED_STATS.get(), result.stats());

        LOGGER.debug("ツールを作成しました: {} (パーツ数: {})", toolType.getId(), parts.size());

        return stack;
    }

    /**
     * 組み立て結果を取得（ItemStackを作成せずに結果のみ）
     *
     * @return 組み立て結果
     */
    public AssemblyResult buildResult() {
        return ToolAssembler.assemble(toolType, parts);
    }

    /**
     * ツールタイプに対応するアイテムを取得
     */
    private static Item getItemForToolType(ToolType toolType) {
        return switch (toolType) {
            case PICKAXE -> AnvilItems.ANVIL_PICKAXE.get();
            case AXE -> AnvilItems.ANVIL_AXE.get();
            case SHOVEL -> AnvilItems.ANVIL_SHOVEL.get();
            case SWORD -> AnvilItems.ANVIL_SWORD.get();
            case HOE -> AnvilItems.ANVIL_HOE.get();
            case BOW -> AnvilItems.ANVIL_BOW.get();
            case FISHING_ROD -> AnvilItems.ANVIL_FISHING_ROD.get();
            case SHEARS -> AnvilItems.ANVIL_SHEARS.get();
        };
    }

    // ============================================
    // ユーティリティメソッド
    // ============================================

    /**
     * 現在のパーツ数を取得
     */
    public int getPartCount() {
        return parts.size();
    }

    /**
     * 必要なパーツがすべて揃っているかチェック
     */
    public boolean isComplete() {
        return ToolAssembler.validateParts(toolType, parts).isValid();
    }

    /**
     * 不足しているパーツタイプを取得
     */
    public List<PartType> getMissingParts() {
        List<PartType> required = new ArrayList<>(toolType.getRequiredParts());
        for (ToolPart part : parts) {
            PartType type = PartType.fromId(part.partType());
            required.remove(type);
        }
        return required;
    }
}
