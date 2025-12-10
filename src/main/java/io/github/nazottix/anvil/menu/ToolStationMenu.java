package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.assembly.AssemblyResult;
import io.github.nazottix.anvil.assembly.ToolBuilder;
import io.github.nazottix.anvil.block.entity.ToolStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
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
 * - 0-2: パーツ入力スロット
 * - 3: 出力スロット
 * - 4: ツール入力スロット
 * - 5-31: プレイヤーインベントリ
 * - 32-40: ホットバー
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

        // パーツ入力スロット（3つ横並び）
        // スロット0: ヘッド - 位置 (30, 17)
        this.addSlot(new PartInputSlot(container, ToolStationBlockEntity.SLOT_PART_1, 30, 17));
        // スロット1: ハンドル - 位置 (48, 17)
        this.addSlot(new PartInputSlot(container, ToolStationBlockEntity.SLOT_PART_2, 48, 17));
        // スロット2: バインディング - 位置 (66, 17)
        this.addSlot(new PartInputSlot(container, ToolStationBlockEntity.SLOT_PART_3, 66, 17));

        // 出力スロット - 位置 (124, 35)
        this.addSlot(new OutputSlot(container, ToolStationBlockEntity.SLOT_OUTPUT, 124, 35));

        // ツール入力スロット（修理用） - 位置 (30, 53)
        this.addSlot(new ToolInputSlot(container, ToolStationBlockEntity.SLOT_TOOL_INPUT, 30, 53));

        // プレイヤーインベントリ（3行9列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // ホットバー（1行9列）
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    // ============================================
    // メニュー操作
    // ============================================

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // ツールステーションスロット（0-4）からの移動
            if (index < 5) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（5-31）からの移動
            else if (index < 32) {
                // まずパーツスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 3, false)) {
                    // ホットバーへ移動
                    if (!this.moveItemStackTo(slotStack, 32, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // ホットバー（32-40）からの移動
            else {
                // まずパーツスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 3, false)) {
                    // プレイヤーインベントリへ移動
                    if (!this.moveItemStackTo(slotStack, 5, 32, false)) {
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

        // パーツスロットからアイテムを取得
        ItemStack part1 = container.getItem(ToolStationBlockEntity.SLOT_PART_1);
        ItemStack part2 = container.getItem(ToolStationBlockEntity.SLOT_PART_2);
        ItemStack part3 = container.getItem(ToolStationBlockEntity.SLOT_PART_3);

        // 必要なパーツがあるかチェック
        if (part1.isEmpty() || part2.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // TODO: パーツアイテムから素材IDを取得するロジックを実装
        // 現在は仮実装として鉄/オークを使用
        ToolBuilder builder = ToolBuilder.create(toolType);

        // ヘッドパーツ
        String headMaterial = getMaterialFromItem(part1, "anvil:iron");
        builder.head(headMaterial, Grade.C);

        // ハンドルパーツ
        String handleMaterial = getMaterialFromItem(part2, "anvil:oak");
        builder.handle(handleMaterial, Grade.C);

        // バインディングパーツ（オプション）
        if (!part3.isEmpty() && toolType.getRequiredParts().size() > 2) {
            String bindingMaterial = getMaterialFromItem(part3, "anvil:copper");
            builder.binding(bindingMaterial, Grade.C);
        }

        // ツールをビルド
        ItemStack result = builder.build();
        if (result != null) {
            // パーツを消費
            container.removeItem(ToolStationBlockEntity.SLOT_PART_1, 1);
            container.removeItem(ToolStationBlockEntity.SLOT_PART_2, 1);
            if (!part3.isEmpty()) {
                container.removeItem(ToolStationBlockEntity.SLOT_PART_3, 1);
            }

            // 出力スロットに設定
            container.setItem(ToolStationBlockEntity.SLOT_OUTPUT, result);
            return result;
        }

        return ItemStack.EMPTY;
    }

    /**
     * アイテムから素材IDを取得（仮実装）
     */
    private String getMaterialFromItem(ItemStack stack, String defaultMaterial) {
        // TODO: アイテムのデータコンポーネントから素材IDを取得
        // 現在は仮実装
        return defaultMaterial;
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

    // ============================================
    // カスタムスロットクラス
    // ============================================

    /**
     * パーツ入力スロット
     */
    private static class PartInputSlot extends Slot {
        public PartInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // TODO: パーツアイテムのみ受け入れるようにする
            return true;
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

    /**
     * ツール入力スロット（修理用）
     */
    private static class ToolInputSlot extends Slot {
        public ToolInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // ANVILツールのみ受け入れる
            return stack.has(AnvilDataComponents.TOOL_DATA.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
