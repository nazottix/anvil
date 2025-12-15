package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.mod.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * MODカスタマイズメニュー
 *
 * ツールにMODを装着・変更する画面のサーバー側ロジック。
 * Warframe風のMODスロットシステムを実装。
 *
 * スロット構成:
 * - 0: ツール入力スロット（カスタマイズ対象）
 * - 1-36: プレイヤーインベントリ + ホットバー
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class ModCustomizationMenu extends AbstractContainerMenu {

    // コンテナ（ツールスロット用）
    private final Container toolContainer;

    // 同期データ
    // 0: 選択中のスロットインデックス
    // 1: カテゴリフィルタ（0=全て、1=攻撃、2=防御...）
    private final ContainerData data;

    // 現在編集中のMOD構成（クライアント/サーバー両方で保持）
    private ModConfiguration editingConfig;

    // 利用可能なMODリスト（クライアント側で表示用）
    private List<ModDefinition> availableMods;

    /**
     * クライアント側コンストラクタ
     */
    public ModCustomizationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(2));
    }

    /**
     * サーバー側コンストラクタ
     */
    public ModCustomizationMenu(int containerId, Inventory playerInventory, Container toolContainer, ContainerData data) {
        super(AnvilMenuTypes.MOD_CUSTOMIZATION.get(), containerId);
        this.toolContainer = toolContainer;
        this.data = data;

        checkContainerSize(toolContainer, 1);
        checkContainerDataCount(data, 2);

        addDataSlots(data);

        // ツール入力スロット - 画面中央左
        this.addSlot(new ToolSlot(toolContainer, 0, 26, 35));

        // プレイヤーインベントリ
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // ホットバー
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        // 初期化
        this.editingConfig = ModConfiguration.DEFAULT;
        this.availableMods = new ArrayList<>(ModRegistry.getAll().values());
    }

    // ============================================
    // ツール変更検知
    // ============================================

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        // ツールが変更されたらMOD構成を読み込み
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.MOD_CONFIG.get())) {
            this.editingConfig = toolStack.get(AnvilDataComponents.MOD_CONFIG.get());
        } else {
            this.editingConfig = ModConfiguration.DEFAULT;
        }
    }

    // ============================================
    // MOD操作
    // ============================================

    /**
     * 指定スロットにMODを装着
     *
     * @param slotIndex スロットインデックス（0=オーラ、1-8=通常、9=エクシルス）
     * @param modId MODのID
     * @param rank MODのランク
     * @return 成功した場合true
     */
    public boolean installMod(int slotIndex, ResourceLocation modId, int rank) {
        ModDefinition def = ModRegistry.get(modId);
        if (def == null) {
            return false;
        }

        InstalledMod mod = new InstalledMod(modId, rank);

        // スロットタイプに応じた処理
        if (slotIndex == 0) {
            // オーラスロット
            if (def.type() != ModType.AURA) {
                return false;
            }
            editingConfig = editingConfig.withAuraMod(mod);
        } else if (slotIndex == 9) {
            // エクシルススロット
            if (def.type() != ModType.EXILUS) {
                return false;
            }
            editingConfig = editingConfig.withExilusMod(mod);
        } else {
            // 通常スロット（1-8）
            int normalIndex = slotIndex - 1;
            if (normalIndex < 0 || normalIndex >= editingConfig.normalSlots().size()) {
                return false;
            }
            if (def.type() == ModType.AURA || def.type() == ModType.EXILUS) {
                return false;
            }

            // 容量チェック
            int currentCapacity = getUsedCapacity();
            int maxCapacity = getMaxCapacity();

            ModSlot slot = editingConfig.normalSlots().get(normalIndex);
            int newDrain = Polarity.calculateDrain(def.getDrainAtRank(mod.rank()), def.polarity(), slot.polarity());

            // 既存MODのドレインを引いて新しいMODのドレインを足す
            int existingDrain = 0;
            if (slot.installedMod() != null) {
                ModDefinition existingDef = ModRegistry.get(slot.installedMod());
                if (existingDef != null) {
                    existingDrain = Polarity.calculateDrain(existingDef.getDrainAtRank(slot.installedMod().rank()), existingDef.polarity(), slot.polarity());
                }
            }

            if (currentCapacity - existingDrain + newDrain > maxCapacity) {
                return false; // 容量オーバー
            }

            // MODを装着
            editingConfig = editingConfig.withNormalMod(normalIndex, mod);
        }

        return true;
    }

    /**
     * 指定スロットからMODを取り外す
     *
     * @param slotIndex スロットインデックス
     * @return 取り外されたMOD（なければnull）
     */
    public InstalledMod removeMod(int slotIndex) {
        InstalledMod removed = null;

        if (slotIndex == 0) {
            // オーラスロット
            removed = editingConfig.auraSlot().installedMod();
            editingConfig = editingConfig.withAuraMod(null);
        } else if (slotIndex == 9) {
            // エクシルススロット
            if (editingConfig.exilusSlot() != null) {
                removed = editingConfig.exilusSlot().installedMod();
                editingConfig = editingConfig.withExilusMod(null);
            }
        } else {
            // 通常スロット
            int normalIndex = slotIndex - 1;
            if (normalIndex >= 0 && normalIndex < editingConfig.normalSlots().size()) {
                removed = editingConfig.normalSlots().get(normalIndex).installedMod();
                editingConfig = editingConfig.withNormalMod(normalIndex, null);
            }
        }

        return removed;
    }

    /**
     * スロットの極性を変更（フォーマ使用）
     *
     * @param slotIndex スロットインデックス
     * @param newPolarity 新しい極性
     * @return 成功した場合true
     */
    public boolean changePolarityWithForma(int slotIndex, Polarity newPolarity) {
        // TODO: フォーマアイテムの消費チェック

        // withSlotPolarityを使用して極性を変更
        if (slotIndex >= 1 && slotIndex <= 8) {
            int normalIndex = slotIndex - 1;
            if (normalIndex < editingConfig.normalSlots().size()) {
                editingConfig = editingConfig.withSlotPolarity(normalIndex, newPolarity);
                return true;
            }
        }
        // オーラスロットの極性変更は現在サポートされていない（withSlotPolarityはnormalSlotsのみ）

        return false;
    }

    /**
     * 変更を適用してツールに保存
     */
    public void applyChanges() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            toolStack.set(AnvilDataComponents.MOD_CONFIG.get(), editingConfig);
        }
    }

    /**
     * 変更をリセット
     */
    public void resetChanges() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.MOD_CONFIG.get())) {
            this.editingConfig = toolStack.get(AnvilDataComponents.MOD_CONFIG.get());
        } else {
            this.editingConfig = ModConfiguration.DEFAULT;
        }
    }

    // ============================================
    // 情報取得
    // ============================================

    /**
     * 現在のMOD構成を取得
     */
    public ModConfiguration getEditingConfig() {
        return editingConfig;
    }

    /**
     * ツールのレベルを取得
     */
    public int getToolLevel() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            AnvilToolData data = toolStack.get(AnvilDataComponents.TOOL_DATA.get());
            return data.level();
        }
        return 1;
    }

    /**
     * 現在の容量使用量を取得
     */
    public int getUsedCapacity() {
        // ModRegistryからModDefinitionを取得する関数を渡す
        return CapacityCalculator.calculateUsedCapacity(editingConfig, mod -> ModRegistry.get(mod.modId()));
    }

    /**
     * 最大容量を取得
     */
    public int getMaxCapacity() {
        // calculateTotalCapacityを使用（簡易版）
        return CapacityCalculator.calculateTotalCapacity(getToolLevel(), editingConfig);
    }

    /**
     * 解放済みスロット数を取得
     */
    public int getUnlockedSlots() {
        // getUnlockedSlotsForLevelを使用
        return CapacityCalculator.getUnlockedSlotsForLevel(getToolLevel());
    }

    /**
     * 利用可能なMODリストを取得
     */
    public List<ModDefinition> getAvailableMods() {
        return availableMods;
    }

    /**
     * 選択中のスロットインデックスを取得
     */
    public int getSelectedSlotIndex() {
        return data.get(0);
    }

    /**
     * スロットを選択
     */
    public void setSelectedSlotIndex(int index) {
        data.set(0, index);
    }

    /**
     * カテゴリフィルタを取得
     */
    public int getCategoryFilter() {
        return data.get(1);
    }

    /**
     * カテゴリフィルタを設定
     */
    public void setCategoryFilter(int filter) {
        data.set(1, filter);
    }

    // ============================================
    // AbstractContainerMenu実装
    // ============================================

    @Override
    public boolean stillValid(Player player) {
        return toolContainer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // ツールスロット(0)からの移動
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ/ホットバーからの移動
            else {
                // ANVILツールならツールスロットへ
                if (slotStack.has(AnvilDataComponents.TOOL_DATA.get())) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // それ以外はインベントリ/ホットバー間で移動
                else if (index < 28) {
                    if (!this.moveItemStackTo(slotStack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(slotStack, 1, 28, false)) {
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
    // カスタムスロット
    // ============================================

    /**
     * ツールスロット（ANVILツールのみ受け入れ）
     */
    private static class ToolSlot extends Slot {
        public ToolSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.has(AnvilDataComponents.TOOL_DATA.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
