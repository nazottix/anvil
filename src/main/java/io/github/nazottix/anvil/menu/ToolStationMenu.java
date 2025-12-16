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

        // ツール入力スロット（修理用） - 位置 (21, 96)
        this.addSlot(new ToolInputSlot(container, ToolStationBlockEntity.SLOT_TOOL_INPUT, 21, 96));

        // プレイヤーインベントリ（3行9列）- y=137から
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 137 + row * 18));
            }
        }

        // ホットバー（1行9列）- y=195
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 195));
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

        // TODO: パーツアイテムから素材IDを取得するロジックを実装
        // 現在は仮実装として鉄/オークを使用
        ToolBuilder builder = ToolBuilder.create(toolType);

        // パーツ1（ヘッド/ボウリム等）
        String part1Material = getMaterialFromItem(part1, "anvil:iron");
        builder.head(part1Material, Grade.C);

        // パーツ2（ハンドル等）
        String part2Material = getMaterialFromItem(part2, "anvil:oak");
        builder.handle(part2Material, Grade.C);

        // パーツ3（バインディング/ボウリム等）
        if (requiredPartCount >= 3 && !part3.isEmpty()) {
            String part3Material = getMaterialFromItem(part3, "anvil:copper");
            builder.binding(part3Material, Grade.C);
        }

        // パーツ4（ボウストリング等 - 弓などの4パーツツール用）
        if (requiredPartCount >= 4 && !part4.isEmpty()) {
            String part4Material = getMaterialFromItem(part4, "anvil:string");
            builder.bowstring(part4Material, Grade.C);
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
