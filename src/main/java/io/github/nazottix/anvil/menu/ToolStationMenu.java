package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.assembly.AssemblyResult;
import io.github.nazottix.anvil.assembly.ToolBuilder;
import io.github.nazottix.anvil.block.entity.ToolStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.item.PartItem;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.PartType;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * ツールステーションメニュー
 *
 * ツール作成画面のサーバー側ロジックを管理します。
 * スロットの配置、アイテムの移動、ツール作成処理を行います。
 *
 * スロット構成:
 * - 0-3: パーツ入力スロット（4スロット、弓等の4パーツツール対応）
 * - 4: 出力スロット
 * - 5: ツール入力スロット
 * - 6-32: プレイヤーインベントリ
 * - 33-41: ホットバー
 */
public class ToolStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ
    private final ContainerData data;

    // 作成中かどうかのフラグ
    private boolean crafting = false;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public ToolStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(ToolStationBlockEntity.INVENTORY_SIZE), new SimpleContainerData(1));
    }

    /**
     * サーバー側コンストラクタ
     */
    public ToolStationMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.TOOL_STATION.get(), containerId);
        this.container = container;
        this.data = data;

        // コンテナのサイズをチェック
        checkContainerSize(container, ToolStationBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(data, 1);

        // コンテナデータを追加（GUI同期用）
        addDataSlots(data);

        // ============================================
        // スロット配置
        // ============================================
        // レイアウト: ツールタイプボタン(上部) → パーツスロット → 出力 → インベントリ
        // Y座標オフセット: +53px（ツールボタン用スペース確保）

        // パーツ入力スロット（4つ横並び - 弓等の4パーツツール対応）
        // スロット0: パーツ1（ヘッド/ボウリム等） - 位置 (21, 70)
        this.addSlot(new PartInputSlot(container, ToolStationBlockEntity.SLOT_PART_1, 21, 70));
        // スロット1: パーツ2（ハンドル等） - 位置 (39, 70)
        this.addSlot(new PartInputSlot(container, ToolStationBlockEntity.SLOT_PART_2, 39, 70));
        // スロット2: パーツ3（バインディング/ボウリム等） - 位置 (57, 70)
        this.addSlot(new PartInputSlot(container, ToolStationBlockEntity.SLOT_PART_3, 57, 70));
        // スロット3: パーツ4（ボウストリング等） - 位置 (75, 70)
        this.addSlot(new PartInputSlot(container, ToolStationBlockEntity.SLOT_PART_4, 75, 70));

        // 出力スロット - 位置 (124, 78)
        this.addSlot(new OutputSlot(container, ToolStationBlockEntity.SLOT_OUTPUT, 124, 78));

        // 修理スロットはRepairStationに移動

        // プレイヤーインベントリ（3行9列）- y=117から
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 117 + row * 18));
            }
        }

        // ホットバー（1行9列）- y=175
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 175));
        }
    }

    // ============================================
    // メニュー操作
    // ============================================

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /**
     * カスタムボタンクリック処理（サーバー側で実行）
     * ボタンID 0: ツール作成
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        // ボタンID 0 = クラフトボタン
        if (buttonId == 0) {
            craftTool();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // ツールステーションスロット（0-5）からの移動
            if (index < 6) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（6-32）からの移動
            else if (index < 33) {
                // まずパーツスロットへ移動を試みる（4スロット）
                if (!this.moveItemStackTo(slotStack, 0, 4, false)) {
                    // ホットバーへ移動
                    if (!this.moveItemStackTo(slotStack, 33, 42, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // ホットバー（33-41）からの移動
            else {
                // まずパーツスロットへ移動を試みる（4スロット）
                if (!this.moveItemStackTo(slotStack, 0, 4, false)) {
                    // プレイヤーインベントリへ移動
                    if (!this.moveItemStackTo(slotStack, 6, 33, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    // ============================================
    // ツール作成
    // ============================================

    /**
     * 現在のパーツでツールを作成
     *
     * @return 作成されたツール、失敗時はEMPTY
     */
    public ItemStack craftTool() {
        // 選択されているツールタイプを取得
        ToolType toolType = getSelectedToolType();
        if (toolType == null) {
            return ItemStack.EMPTY;
        }

        // パーツスロットからアイテムを取得（4スロット対応）
        ItemStack part1 = container.getItem(ToolStationBlockEntity.SLOT_PART_1);
        ItemStack part2 = container.getItem(ToolStationBlockEntity.SLOT_PART_2);
        ItemStack part3 = container.getItem(ToolStationBlockEntity.SLOT_PART_3);
        ItemStack part4 = container.getItem(ToolStationBlockEntity.SLOT_PART_4);

        // 必要なパーツ数を取得
        int requiredPartCount = toolType.getRequiredParts().size();

        // 必要なパーツがあるかチェック
        if (part1.isEmpty() || part2.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // 3パーツ以上必要なツールでパーツ3が空の場合
        if (requiredPartCount >= 3 && part3.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // 4パーツ必要なツール（弓等）でパーツ4が空の場合
        if (requiredPartCount >= 4 && part4.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // パーツアイテムから素材IDとグレードを取得してツールをビルド
        ToolBuilder builder = ToolBuilder.create(toolType);

        // パーツ1（ヘッド/ボウリム等）- 素材とグレードをパーツデータから取得
        String part1Material = getMaterialFromItem(part1, "anvil:iron");
        Grade part1Grade = getGradeFromItem(part1, Grade.C);
        builder.head(part1Material, part1Grade);

        // パーツ2（ハンドル等）- 素材とグレードをパーツデータから取得
        String part2Material = getMaterialFromItem(part2, "anvil:oak");
        Grade part2Grade = getGradeFromItem(part2, Grade.C);
        builder.handle(part2Material, part2Grade);

        // パーツ3（バインディング/ボウリム等）- 素材とグレードをパーツデータから取得
        if (requiredPartCount >= 3 && !part3.isEmpty()) {
            String part3Material = getMaterialFromItem(part3, "anvil:copper");
            Grade part3Grade = getGradeFromItem(part3, Grade.C);
            builder.binding(part3Material, part3Grade);
        }

        // パーツ4（ボウストリング等 - 弓などの4パーツツール用）- 素材とグレードをパーツデータから取得
        if (requiredPartCount >= 4 && !part4.isEmpty()) {
            String part4Material = getMaterialFromItem(part4, "anvil:string");
            Grade part4Grade = getGradeFromItem(part4, Grade.C);
            builder.bowstring(part4Material, part4Grade);
        }

        // ツールをビルド
        ItemStack result = builder.build();
        if (result != null) {
            // パーツを消費
            container.removeItem(ToolStationBlockEntity.SLOT_PART_1, 1);
            container.removeItem(ToolStationBlockEntity.SLOT_PART_2, 1);
            if (requiredPartCount >= 3 && !part3.isEmpty()) {
                container.removeItem(ToolStationBlockEntity.SLOT_PART_3, 1);
            }
            if (requiredPartCount >= 4 && !part4.isEmpty()) {
                container.removeItem(ToolStationBlockEntity.SLOT_PART_4, 1);
            }

            // 出力スロットに設定
            container.setItem(ToolStationBlockEntity.SLOT_OUTPUT, result);
            return result;
        }

        return ItemStack.EMPTY;
    }

    /**
     * アイテムから素材IDを取得
     * PartItemのToolPartデータから素材IDを読み取る
     *
     * @param stack アイテムスタック
     * @param defaultMaterial デフォルト素材ID（パーツデータがない場合に使用）
     * @return 素材ID
     */
    private String getMaterialFromItem(ItemStack stack, String defaultMaterial) {
        // PartItemからToolPartデータを取得
        ToolPart partData = PartItem.getPartData(stack);
        if (partData != null && partData.materialId() != null && !partData.materialId().isEmpty()) {
            return partData.materialId();
        }
        return defaultMaterial;
    }

    /**
     * アイテムからグレードを取得
     * PartItemのToolPartデータからグレードを読み取る
     *
     * @param stack アイテムスタック
     * @param defaultGrade デフォルトグレード（パーツデータがない場合に使用）
     * @return グレード
     */
    private Grade getGradeFromItem(ItemStack stack, Grade defaultGrade) {
        // PartItemからToolPartデータを取得
        ToolPart partData = PartItem.getPartData(stack);
        if (partData != null && partData.grade() != null && !partData.grade().isEmpty()) {
            Grade grade = Grade.fromId(partData.grade());
            if (grade != null) {
                return grade;
            }
        }
        return defaultGrade;
    }

    /**
     * 選択されているツールタイプを取得
     */
    @Nullable
    public ToolType getSelectedToolType() {
        int index = data.get(0);
        ToolType[] types = ToolType.values();
        if (index >= 0 && index < types.length) {
            return types[index];
        }
        return null;
    }

    /**
     * ツールタイプを選択
     */
    public void setSelectedToolType(int index) {
        data.set(0, index);
    }

    /**
     * 指定スロットインデックスに必要なパーツタイプを取得
     * 選択されたツールタイプに基づいて、各スロットに許可されるパーツタイプを返す
     *
     * @param slotIndex スロットインデックス（0-3）
     * @return 許可されるパーツタイプ、ツールタイプ未選択またはスロット不要の場合はnull
     */
    @Nullable
    public PartType getRequiredPartTypeForSlot(int slotIndex) {
        ToolType toolType = getSelectedToolType();
        if (toolType == null) {
            return null;
        }

        var requiredParts = toolType.getRequiredParts();
        if (slotIndex < 0 || slotIndex >= requiredParts.size()) {
            return null;
        }

        return requiredParts.get(slotIndex);
    }

    // ============================================
    // カスタムスロットクラス
    // ============================================

    /**
     * パーツ入力スロット
     * 選択されたツールタイプに応じて、指定のパーツタイプのみ受け入れる
     */
    private class PartInputSlot extends Slot {
        // スロット番号（0-3、パーツスロット用）
        private final int partSlotIndex;

        public PartInputSlot(Container container, int containerIndex, int x, int y) {
            super(container, containerIndex, x, y);
            // containerIndexは0,1,2,3なのでそのままpartSlotIndexとして使用
            this.partSlotIndex = containerIndex;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // パーツアイテムかつ有効なパーツデータを持つ場合のみ受け入れる
            if (!PartItem.hasValidPartData(stack)) {
                return false;
            }

            // 選択されたツールタイプに基づいてパーツタイプを制限
            PartType requiredType = getRequiredPartTypeForSlot(partSlotIndex);
            if (requiredType == null) {
                // ツールタイプ未選択、または不要なスロット
                return false;
            }

            // パーツアイテムのパーツタイプを取得
            if (stack.getItem() instanceof PartItem partItem) {
                return partItem.getPartType() == requiredType;
            }

            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /**
     * 出力スロット
     */
    private static class OutputSlot extends Slot {
        public OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // 出力スロットにはアイテムを直接置けない
            return false;
        }
    }

    // ToolInputSlotはRepairStationMenuに移動
}
