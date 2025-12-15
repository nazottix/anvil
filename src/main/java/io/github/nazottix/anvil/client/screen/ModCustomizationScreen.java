package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.menu.ModCustomizationMenu;
import io.github.nazottix.anvil.mod.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * MODカスタマイズスクリーン
 *
 * Warframe風のMODスロットUI。
 * ツールにMODを装着・取り外しできる。
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class ModCustomizationScreen extends AbstractContainerScreen<ModCustomizationMenu> {

    // テクスチャ（仮：後でカスタムテクスチャに置き換え）
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "textures/gui/mod_customization.png");

    // UI定数
    private static final int SLOT_SIZE = 36;
    private static final int SLOT_SPACING = 4;
    private static final int MOD_LIST_WIDTH = 100;
    private static final int MOD_LIST_HEIGHT = 160;

    // MODスロット表示位置
    private static final int AURA_SLOT_X = 60;
    private static final int AURA_SLOT_Y = 20;
    private static final int NORMAL_SLOTS_X = 60;
    private static final int NORMAL_SLOTS_Y = 60;
    private static final int EXILUS_SLOT_X = 200;
    private static final int EXILUS_SLOT_Y = 20;

    // MODリスト表示位置
    private static final int MOD_LIST_X = 250;
    private static final int MOD_LIST_Y = 20;

    // 選択中のMOD（ドラッグ用）
    private ModDefinition selectedMod = null;
    private int selectedModRank = 0;

    // MODリストのスクロール位置
    private int modListScrollOffset = 0;

    // フィルタリングされたMODリスト
    private List<ModDefinition> filteredMods = new ArrayList<>();

    /**
     * コンストラクタ
     */
    public ModCustomizationScreen(ModCustomizationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUIサイズを拡大
        this.imageWidth = 360;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 82;
    }

    @Override
    protected void init() {
        super.init();

        // 適用ボタン
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.anvil.apply"),
                button -> {
                    this.menu.applyChanges();
                    ANVIL.LOGGER.info("MOD構成を適用しました");
                }
        ).bounds(this.leftPos + 8, this.topPos + 110, 50, 20).build());

        // リセットボタン
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.anvil.reset"),
                button -> {
                    this.menu.resetChanges();
                    ANVIL.LOGGER.info("MOD構成をリセットしました");
                }
        ).bounds(this.leftPos + 8, this.topPos + 132, 50, 20).build());

        // MODリスト更新
        updateFilteredMods();
    }

    // ============================================
    // 描画
    // ============================================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 背景色で塗りつぶし
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight,
                AnvilColors.VOID_BLACK | 0xFF000000);

        // パネル背景
        guiGraphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2, this.topPos + this.imageHeight - 2,
                AnvilColors.ANVIL_STEEL | 0xFF000000);

        // MODスロット描画
        renderModSlots(guiGraphics, mouseX, mouseY);

        // MODリスト描画
        renderModList(guiGraphics, mouseX, mouseY);

        // 容量バー描画
        renderCapacityBar(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // ドラッグ中のMOD描画
        if (selectedMod != null) {
            renderDraggedMod(guiGraphics, mouseX, mouseY);
        }

        // MODスロットのツールチップ
        renderModSlotTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトル
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AnvilColors.ETHER_WHITE, false);

        // インベントリラベル
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                AnvilColors.ETHER_WHITE, false);

        // 容量表示
        int used = this.menu.getUsedCapacity();
        int max = this.menu.getMaxCapacity();
        String capacityText = String.format("容量: %d / %d", used, max);
        guiGraphics.drawString(this.font, capacityText, NORMAL_SLOTS_X, NORMAL_SLOTS_Y - 12,
                used > max ? 0xFF5555 : AnvilColors.TECH_CYAN, false);

        // スロット数表示
        int unlocked = this.menu.getUnlockedSlots();
        String slotsText = String.format("スロット: %d / 8", unlocked);
        guiGraphics.drawString(this.font, slotsText, NORMAL_SLOTS_X + 100, NORMAL_SLOTS_Y - 12,
                AnvilColors.ETHER_WHITE, false);
    }

    /**
     * MODスロットを描画
     */
    private void renderModSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ModConfiguration config = this.menu.getEditingConfig();
        int guiX = this.leftPos;
        int guiY = this.topPos;

        // オーラスロット
        renderSingleSlot(guiGraphics, guiX + AURA_SLOT_X, guiY + AURA_SLOT_Y,
                config.auraSlot(), "オーラ", mouseX, mouseY, 0);

        // 通常スロット（4x2配置）
        int unlockedSlots = this.menu.getUnlockedSlots();
        for (int i = 0; i < 8; i++) {
            int row = i / 4;
            int col = i % 4;
            int x = guiX + NORMAL_SLOTS_X + col * (SLOT_SIZE + SLOT_SPACING);
            int y = guiY + NORMAL_SLOTS_Y + row * (SLOT_SIZE + SLOT_SPACING);

            boolean locked = i >= unlockedSlots;
            ModSlot slot = i < config.normalSlots().size() ? config.normalSlots().get(i) : null;
            renderSingleSlot(guiGraphics, x, y, slot, locked ? "ロック" : null, mouseX, mouseY, i + 1);
        }

        // エクシルススロット
        if (config.exilusSlot() != null) {
            renderSingleSlot(guiGraphics, guiX + EXILUS_SLOT_X, guiY + EXILUS_SLOT_Y,
                    config.exilusSlot(), "EX", mouseX, mouseY, 9);
        }
    }

    /**
     * 単一のMODスロットを描画
     */
    private void renderSingleSlot(GuiGraphics guiGraphics, int x, int y, ModSlot slot,
                                   String label, int mouseX, int mouseY, int slotIndex) {
        // スロット背景
        int bgColor = AnvilColors.VOID_BLACK | 0xFF000000;
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);

        // 極性表示
        if (slot != null && slot.polarity() != null) {
            int polarityColor = getPolarityColor(slot.polarity()) | 0xFF000000;
            guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, polarityColor);

            // 極性シンボル
            String symbol = getPolaritySymbol(slot.polarity());
            guiGraphics.drawString(this.font, symbol, x + 2, y + 2, 0xFFFFFF, true);
        }

        // 装着MOD表示
        if (slot != null && slot.installedMod() != null) {
            ModDefinition def = ModRegistry.get(slot.installedMod());
            if (def != null) {
                // MOD背景（レアリティ色）
                int rarityColor = getRarityColor(def.rarity()) | 0xCC000000;
                guiGraphics.fill(x + 4, y + 4, x + SLOT_SIZE - 4, y + SLOT_SIZE - 4, rarityColor);

                // MOD名（短縮）
                String modName = def.id().getPath();
                if (modName.length() > 6) {
                    modName = modName.substring(0, 6);
                }
                guiGraphics.drawString(this.font, modName, x + 6, y + 10, 0xFFFFFF, true);

                // ランク表示
                String rankText = "R" + slot.installedMod().rank();
                guiGraphics.drawString(this.font, rankText, x + 6, y + 22, 0xFFFF00, true);
            }
        }

        // ラベル表示
        if (label != null) {
            guiGraphics.drawString(this.font, label, x, y - 10, AnvilColors.ETHER_WHITE, false);
        }

        // 選択中ハイライト
        int selectedIndex = this.menu.getSelectedSlotIndex();
        if (selectedIndex == slotIndex) {
            guiGraphics.renderOutline(x - 1, y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, AnvilColors.FORGE_ORANGE | 0xFF000000);
        }

        // ホバーハイライト
        if (isMouseOver(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE)) {
            guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x44FFFFFF);
        }
    }

    /**
     * MODリストを描画
     */
    private void renderModList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.leftPos + MOD_LIST_X;
        int y = this.topPos + MOD_LIST_Y;

        // リスト背景
        guiGraphics.fill(x, y, x + MOD_LIST_WIDTH, y + MOD_LIST_HEIGHT, AnvilColors.VOID_BLACK | 0xFF000000);

        // ヘッダー
        guiGraphics.drawString(this.font, "MOD一覧", x + 4, y + 4, AnvilColors.TECH_CYAN, false);

        // MOD一覧
        int itemHeight = 20;
        int visibleItems = (MOD_LIST_HEIGHT - 20) / itemHeight;
        int startIndex = modListScrollOffset;

        for (int i = 0; i < visibleItems && startIndex + i < filteredMods.size(); i++) {
            ModDefinition mod = filteredMods.get(startIndex + i);
            int itemY = y + 20 + i * itemHeight;

            // MOD背景
            int bgCol = getRarityColor(mod.rarity()) | 0x88000000;
            guiGraphics.fill(x + 2, itemY, x + MOD_LIST_WIDTH - 2, itemY + itemHeight - 2, bgCol);

            // MOD名
            String name = mod.id().getPath();
            if (name.length() > 12) {
                name = name.substring(0, 12) + "..";
            }
            guiGraphics.drawString(this.font, name, x + 4, itemY + 2, 0xFFFFFF, true);

            // ドレイン表示
            String drainText = mod.baseDrain() + "D";
            guiGraphics.drawString(this.font, drainText, x + 4, itemY + 10, 0xCCCCCC, true);

            // ホバーハイライト
            if (isMouseOver(mouseX, mouseY, x + 2, itemY, MOD_LIST_WIDTH - 4, itemHeight - 2)) {
                guiGraphics.fill(x + 2, itemY, x + MOD_LIST_WIDTH - 2, itemY + itemHeight - 2, 0x44FFFFFF);
            }
        }
    }

    /**
     * 容量バーを描画
     */
    private void renderCapacityBar(GuiGraphics guiGraphics) {
        int x = this.leftPos + NORMAL_SLOTS_X;
        int y = this.topPos + NORMAL_SLOTS_Y + 90;
        int width = 160;
        int height = 8;

        int used = this.menu.getUsedCapacity();
        int max = this.menu.getMaxCapacity();

        // 背景
        guiGraphics.fill(x, y, x + width, y + height, 0xFF333333);

        // 使用量バー
        float ratio = max > 0 ? (float) used / max : 0;
        int barWidth = (int) (width * Math.min(ratio, 1.0f));
        int barColor = ratio > 1.0f ? 0xFFFF5555 :
                ratio > 0.8f ? 0xFFFFAA00 :
                        AnvilColors.TECH_CYAN | 0xFF000000;
        guiGraphics.fill(x, y, x + barWidth, y + height, barColor);

        // 枠
        guiGraphics.renderOutline(x, y, width, height, 0xFFFFFFFF);
    }

    /**
     * ドラッグ中のMODを描画
     */
    private void renderDraggedMod(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (selectedMod == null) return;

        int size = 32;
        int x = mouseX - size / 2;
        int y = mouseY - size / 2;

        // 背景
        int rarityColor = getRarityColor(selectedMod.rarity()) | 0xCC000000;
        guiGraphics.fill(x, y, x + size, y + size, rarityColor);

        // MOD名
        guiGraphics.drawString(this.font, selectedMod.id().getPath().substring(0, Math.min(6, selectedMod.id().getPath().length())),
                x + 2, y + 8, 0xFFFFFF, true);
    }

    /**
     * MODスロットのツールチップを描画
     */
    private void renderModSlotTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ModConfiguration config = this.menu.getEditingConfig();
        int guiX = this.leftPos;
        int guiY = this.topPos;

        // オーラスロットのツールチップ
        if (isMouseOver(mouseX, mouseY, guiX + AURA_SLOT_X, guiY + AURA_SLOT_Y, SLOT_SIZE, SLOT_SIZE)) {
            renderSlotTooltip(guiGraphics, mouseX, mouseY, config.auraSlot());
        }

        // 通常スロットのツールチップ
        for (int i = 0; i < 8 && i < config.normalSlots().size(); i++) {
            int row = i / 4;
            int col = i % 4;
            int x = guiX + NORMAL_SLOTS_X + col * (SLOT_SIZE + SLOT_SPACING);
            int y = guiY + NORMAL_SLOTS_Y + row * (SLOT_SIZE + SLOT_SPACING);

            if (isMouseOver(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE)) {
                renderSlotTooltip(guiGraphics, mouseX, mouseY, config.normalSlots().get(i));
            }
        }
    }

    /**
     * スロットのツールチップを描画
     */
    private void renderSlotTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, ModSlot slot) {
        if (slot == null) return;

        List<Component> tooltip = new ArrayList<>();

        if (slot.polarity() != null) {
            // getSymbol()を使用して極性シンボルを表示
            tooltip.add(Component.literal("極性: " + slot.polarity().getSymbol() + " (" + slot.polarity().getId() + ")"));
        }

        if (slot.installedMod() != null) {
            ModDefinition def = ModRegistry.get(slot.installedMod());
            if (def != null) {
                tooltip.add(Component.literal(def.id().getPath()).withStyle(s -> s.withColor(getRarityColor(def.rarity()))));
                tooltip.add(Component.literal("ランク: " + slot.installedMod().rank() + "/" + def.maxRank()));
                // Polarity.calculateDrainを使用してドレインを計算
                int baseDrain = def.getDrainAtRank(slot.installedMod().rank());
                int actualDrain = Polarity.calculateDrain(baseDrain, def.polarity(), slot.polarity());
                tooltip.add(Component.literal("ドレイン: " + actualDrain));

                // 効果一覧
                for (ModEffect effect : def.effects()) {
                    float value = effect.getScaledValue(slot.installedMod().rank(), def.maxRank());
                    // EffectTypeでMULTIPLICATIVEかどうかを判定
                    boolean isMultiplicative = effect.effectType() == ModEffect.EffectType.MULTIPLICATIVE;
                    String effectText = effect.statId() + ": " + (isMultiplicative ? "+" : "") +
                            String.format("%.1f", value) + (isMultiplicative ? "%" : "");
                    tooltip.add(Component.literal(effectText).withStyle(s -> s.withColor(0xAAAAAA)));
                }
            }
        } else {
            tooltip.add(Component.literal("空きスロット"));
        }

        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    // ============================================
    // マウス操作
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int guiX = this.leftPos;
        int guiY = this.topPos;

        // MODリストクリック
        if (isMouseOver(mx, my, guiX + MOD_LIST_X, guiY + MOD_LIST_Y + 20, MOD_LIST_WIDTH, MOD_LIST_HEIGHT - 20)) {
            int itemHeight = 20;
            int clickedIndex = (my - (guiY + MOD_LIST_Y + 20)) / itemHeight + modListScrollOffset;
            if (clickedIndex >= 0 && clickedIndex < filteredMods.size()) {
                selectedMod = filteredMods.get(clickedIndex);
                selectedModRank = 0;
                return true;
            }
        }

        // MODスロットクリック
        // オーラスロット
        if (isMouseOver(mx, my, guiX + AURA_SLOT_X, guiY + AURA_SLOT_Y, SLOT_SIZE, SLOT_SIZE)) {
            handleSlotClick(0, button);
            return true;
        }

        // 通常スロット
        for (int i = 0; i < 8; i++) {
            int row = i / 4;
            int col = i % 4;
            int x = guiX + NORMAL_SLOTS_X + col * (SLOT_SIZE + SLOT_SPACING);
            int y = guiY + NORMAL_SLOTS_Y + row * (SLOT_SIZE + SLOT_SPACING);

            if (isMouseOver(mx, my, x, y, SLOT_SIZE, SLOT_SIZE)) {
                handleSlotClick(i + 1, button);
                return true;
            }
        }

        // エクシルススロット
        if (isMouseOver(mx, my, guiX + EXILUS_SLOT_X, guiY + EXILUS_SLOT_Y, SLOT_SIZE, SLOT_SIZE)) {
            handleSlotClick(9, button);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * スロットクリック処理
     */
    private void handleSlotClick(int slotIndex, int button) {
        if (button == 0) { // 左クリック
            if (selectedMod != null) {
                // MODを装着
                if (this.menu.installMod(slotIndex, selectedMod.id(), selectedModRank)) {
                    ANVIL.LOGGER.info("MODを装着: {} -> スロット {}", selectedMod.id(), slotIndex);
                }
                selectedMod = null;
            } else {
                // スロット選択
                this.menu.setSelectedSlotIndex(slotIndex);
            }
        } else if (button == 1) { // 右クリック
            // MODを取り外し
            InstalledMod removed = this.menu.removeMod(slotIndex);
            if (removed != null) {
                ANVIL.LOGGER.info("MODを取り外し: {} <- スロット {}", removed.modId(), slotIndex);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // MODリストのスクロール
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int guiX = this.leftPos;
        int guiY = this.topPos;

        if (isMouseOver(mx, my, guiX + MOD_LIST_X, guiY + MOD_LIST_Y, MOD_LIST_WIDTH, MOD_LIST_HEIGHT)) {
            int itemHeight = 20;
            int visibleItems = (MOD_LIST_HEIGHT - 20) / itemHeight;
            int maxScroll = Math.max(0, filteredMods.size() - visibleItems);

            modListScrollOffset -= (int) scrollY;
            modListScrollOffset = Math.max(0, Math.min(modListScrollOffset, maxScroll));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * MODリストをフィルタリング
     */
    private void updateFilteredMods() {
        filteredMods.clear();
        int filter = this.menu.getCategoryFilter();

        for (ModDefinition mod : this.menu.getAvailableMods()) {
            if (filter == 0) {
                filteredMods.add(mod);
            }
            // TODO: カテゴリフィルタ実装
        }

        modListScrollOffset = 0;
    }

    /**
     * マウスが指定領域内かチェック
     */
    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * 極性の色を取得
     */
    private int getPolarityColor(Polarity polarity) {
        return switch (polarity) {
            case MADURAI -> 0xFF4444;   // 赤
            case VAZARIN -> 0x4444FF;   // 青
            case NARAMON -> 0xFFFFFF;   // 白
            case ZENURIK -> 0x8844FF;   // 紫
            case UNAIRU -> 0x44FF44;    // 緑
        };
    }

    /**
     * 極性のシンボルを取得
     */
    private String getPolaritySymbol(Polarity polarity) {
        return switch (polarity) {
            case MADURAI -> "▽";
            case VAZARIN -> "D";
            case NARAMON -> "—";
            case ZENURIK -> "~";
            case UNAIRU -> "◇";
        };
    }

    /**
     * レアリティの色を取得
     */
    private int getRarityColor(ModRarity rarity) {
        return switch (rarity) {
            case COMMON -> 0x888888;
            case UNCOMMON -> 0x55AA55;
            case RARE -> 0x5555FF;
            case LEGENDARY -> 0xFFAA00;
        };
    }
}
