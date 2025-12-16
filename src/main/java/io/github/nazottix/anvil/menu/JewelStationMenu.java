package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.block.entity.JewelStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.item.JewelItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * ジュエルステーションメニュー
 *
 * ジュエル装着画面のサーバー側ロジックを管理します。
 * ツールへのジュエル装着・取り外しを行います。
 *
 * スロット構成:
 * - 0: ツール入力スロット
 * - 1-4: ジュエル入力スロット（4スロット）
 * - 5-31: プレイヤーインベントリ
 * - 32-40: ホットバー
 */
public class JewelStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ
    private final ContainerData data;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public JewelStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(JewelStationBlockEntity.INVENTORY_SIZE), new SimpleContainerData(0));
    }

    /**
     * サーバー側コンストラクタ
     */
    public JewelStationMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.JEWEL_STATION.get(), containerId);
        this.container = container;
        this.data = data;

        // コンテナのサイズをチェック
        checkContainerSize(container, JewelStationBlockEntity.INVENTORY_SIZE);

        // コンテナデータを追加（GUI同期用）
        addDataSlots(data);

        // ============================================
        // スロット配置
        // ============================================

        // ツール入力スロット - 位置 (80, 35)
        this.addSlot(new ToolInputSlot(container, JewelStationBlockEntity.SLOT_TOOL, 80, 35));

        // ジュエル入力スロット（4つ横並び） - 位置 (26, 70)から
        this.addSlot(new JewelInputSlot(container, JewelStationBlockEntity.SLOT_JEWEL_1, 26, 70));
        this.addSlot(new JewelInputSlot(container, JewelStationBlockEntity.SLOT_JEWEL_2, 62, 70));
        this.addSlot(new JewelInputSlot(container, JewelStationBlockEntity.SLOT_JEWEL_3, 98, 70));
        this.addSlot(new JewelInputSlot(container, JewelStationBlockEntity.SLOT_JEWEL_4, 134, 70));

        // プレイヤーインベントリ（3行9列）- y=117から（14ピクセル下にずらした）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 117 + row * 18));
            }
        }

        // ホットバー（1行9列）- y=175（14ピクセル下にずらした）
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // ステーションスロット（0-4）からの移動
            if (index < 5) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（5-31）からの移動
            else if (index < 32) {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // ジュエルスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 5, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 32, 41, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            // ホットバー（32-40）からの移動
            else {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // ジュエルスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 5, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 5, 32, false)) {
                            return ItemStack.EMPTY;
                        }
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
    // ジュエル装着/取り外し
    // ============================================

    /**
     * ジュエルスロットからツールにジュエルを装着
     *
     * @param slotIndex ジュエルスロットのインデックス（0-3）
     * @return 装着成功したかどうか
     */
    public boolean equipJewelFromSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 4) {
            return false;
        }

        // ツールスロットからツールを取得
        ItemStack toolStack = container.getItem(JewelStationBlockEntity.SLOT_TOOL);
        if (toolStack.isEmpty() || !toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            ANVIL.LOGGER.info("ジュエル装着失敗: ツールがセットされていません");
            return false;
        }

        // ジュエルスロットからジュエルを取得
        int jewelSlotIndex = JewelStationBlockEntity.SLOT_JEWEL_1 + slotIndex;
        ItemStack jewelStack = container.getItem(jewelSlotIndex);
        if (jewelStack.isEmpty()) {
            ANVIL.LOGGER.info("ジュエル装着失敗: ジュエルがセットされていません");
            return false;
        }

        // ジュエルIDを取得
        ResourceLocation jewelId = JewelItem.getJewelId(jewelStack);
        if (jewelId == null) {
            ANVIL.LOGGER.info("ジュエル装着失敗: 無効なジュエルです");
            return false;
        }

        // ツールデータを取得してジュエルを装着
        AnvilToolData toolData = toolStack.get(AnvilDataComponents.TOOL_DATA.get());
        if (!toolData.hasEmptyJewelSlot()) {
            ANVIL.LOGGER.info("ジュエル装着失敗: ジュエルスロットが満杯です");
            return false;
        }

        // ジュエルを装着
        AnvilToolData newToolData = toolData.equipJewel(jewelId.toString());
        toolStack.set(AnvilDataComponents.TOOL_DATA.get(), newToolData);

        // ジュエルを1つ消費
        jewelStack.shrink(1);
        container.setChanged();

        ANVIL.LOGGER.info("ジュエル装着成功: {} をツールに装着しました", jewelId);
        return true;
    }

    /**
     * ツールから指定スロットのジュエルを取り外す
     *
     * @param slotIndex ツールのジュエルスロットインデックス（0-3）
     * @return 取り外したジュエルのItemStack、失敗時はEMPTY
     */
    public ItemStack unequipJewelToSlot(int slotIndex) {
        // ツールスロットからツールを取得
        ItemStack toolStack = container.getItem(JewelStationBlockEntity.SLOT_TOOL);
        if (toolStack.isEmpty() || !toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            return ItemStack.EMPTY;
        }

        // ツールデータを取得
        AnvilToolData toolData = toolStack.get(AnvilDataComponents.TOOL_DATA.get());
        if (slotIndex < 0 || slotIndex >= toolData.equippedJewels().size()) {
            return ItemStack.EMPTY;
        }

        // ジュエルIDを取得
        String jewelIdStr = toolData.equippedJewels().get(slotIndex);
        ResourceLocation jewelId = ResourceLocation.parse(jewelIdStr);

        // ジュエルを取り外し
        AnvilToolData newToolData = toolData.unequipJewelAt(slotIndex);
        toolStack.set(AnvilDataComponents.TOOL_DATA.get(), newToolData);
        container.setChanged();

        // ジュエルItemStackを作成
        ItemStack jewelStack = JewelItem.createJewelStack(jewelId);
        ANVIL.LOGGER.info("ジュエル取り外し成功: {} をツールから取り外しました", jewelId);
        return jewelStack;
    }

    /**
     * 全てのジュエルスロットからツールにジュエルを装着
     *
     * @return 装着したジュエルの数
     */
    public int equipAllJewels() {
        int equipped = 0;
        for (int i = 0; i < 4; i++) {
            if (equipJewelFromSlot(i)) {
                equipped++;
            }
        }
        return equipped;
    }

    /**
     * ツールに装着されているジュエル情報を取得
     *
     * @return 装着されているジュエルIDのリスト
     */
    public java.util.List<String> getEquippedJewels() {
        ItemStack toolStack = container.getItem(JewelStationBlockEntity.SLOT_TOOL);
        if (toolStack.isEmpty() || !toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            return java.util.List.of();
        }
        AnvilToolData toolData = toolStack.get(AnvilDataComponents.TOOL_DATA.get());
        return toolData.equippedJewels();
    }

    // ============================================
    // カスタムスロットクラス
    // ============================================

    /**
     * ツール入力スロット
     * ANVILツールのみ受け入れる
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

    /**
     * ジュエル入力スロット
     * ジュエルアイテムのみ受け入れる
     */
    private static class JewelInputSlot extends Slot {
        public JewelInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // ジュエルアイテムのみ受け入れる
            return JewelItem.getJewelId(stack) != null;
        }

        @Override
        public int getMaxStackSize() {
            return 16; // ジュエルは16個までスタック可能
        }
    }
}
