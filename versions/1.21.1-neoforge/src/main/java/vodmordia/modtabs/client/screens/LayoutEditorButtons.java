package vodmordia.modtabs.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.api.tabs_menu.GlobalSettingsPanel;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.config.TabDisplayVisibility;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Layout editor controls. Bottom-right column hosts save/cancel/reset; the floating
 * "Layout Options" panel (drawn by {@link TabsMenu#renderEditModeOverlay}) hosts the
 * three per-screen options: icon rotation, tab visibility, custom icon path.
 */
public final class LayoutEditorButtons {
    private static final int CORNER_BTN_W = 36;
    private static final int CORNER_BTN_H = 12;
    private static final int CORNER_MARGIN = 4;
    private static final int CORNER_GAP = 2;

    private LayoutEditorButtons() {}

    public static void addToScreen(Screen screen, java.util.function.Consumer<AbstractWidget> add) {
        // Bottom-right column: save / cancel / reset.
        int x = screen.width - CORNER_BTN_W - CORNER_MARGIN;
        int ySave = screen.height - CORNER_BTN_H - CORNER_MARGIN;
        int yCancel = ySave - (CORNER_BTN_H + CORNER_GAP);
        int yReset = yCancel - (CORNER_BTN_H + CORNER_GAP);
        add.accept(new EditOnly(screen, x, ySave, CORNER_BTN_W, CORNER_BTN_H, Component.literal("save"),
                btn -> TabsMenu.saveEdit(screen)));
        add.accept(new EditOnly(screen, x, yCancel, CORNER_BTN_W, CORNER_BTN_H, Component.literal("cancel"),
                btn -> TabsMenu.exitEditMode()));
        add.accept(new EditOnly(screen, x, yReset, CORNER_BTN_W, CORNER_BTN_H, Component.literal("reset"),
                btn -> TabsMenu.resetEdit(screen)));

        // Bottom-left corner: cogwheel that opens the global-settings modal.
        int cogSize = 18;
        int cogMargin = 4;
        add.accept(new CogwheelButton(screen, cogMargin, screen.height - cogSize - cogMargin, cogSize, cogSize));

        // Floating "Layout Options" panel. The widgets store their position *relative*
        // to the panel origin (which moves at runtime — vertical-center, slide-collapse)
        // and re-anchor themselves each frame in their renderWidget/isMouseOver overrides.
        int panelW = TabsMenu.PANEL_W;
        int titleH = TabsMenu.PANEL_TITLE_H;
        int rowH = TabsMenu.PANEL_ROW_H;
        int labelW = TabsMenu.PANEL_LABEL_W;
        int pad = TabsMenu.PANEL_PAD;
        int relControlX = pad + labelW;
        int controlW = panelW - pad - labelW - pad;
        int relRowYBase = titleH + pad;
        int relRow1Y = relRowYBase;
        int relRow2Y = relRowYBase + rowH;
        int relRow3Y = relRowYBase + rowH * 2;
        int relRow4Y = relRowYBase + rowH * 3;
        int controlH = 12;

        add.accept(new IconRotationCycle(screen, relControlX, relRow1Y, controlW, controlH));
        // Tuck direction lives on the per-screen layout JSON, not on a configKey, so
        // it works for every screen even those without a TabsMenu configKey mapping.
        add.accept(new TuckDirectionCycle(screen, relControlX, relRow3Y, controlW, controlH));

        String configKey = TabsMenu.getConfigKeyForScreen(screen);
        if (configKey != null) {
            add.accept(new VisibilityCycle(screen, relControlX, relRow2Y, controlW, controlH, configKey));
            int squareW = controlH;
            int gap = 2;
            int dropW = controlW - (squareW * 2) - (gap * 2);
            add.accept(new CustomIconDropdown(screen, relControlX, relRow4Y, dropW, controlH, configKey));
            add.accept(new CustomIconRefresh(screen, relControlX + dropW + gap, relRow4Y, squareW, controlH));
            add.accept(new CustomIconFolder(screen, relControlX + dropW + gap + squareW + gap, relRow4Y, squareW, controlH));

            // Scale-factor row (row 5) sits below the preview. Y mirrors drawOptionsPanel's
            // scaleRowY: row4 + PAD + previewH + PAD. The "%" glyph is drawn by
            // drawOptionsPanel; reserve 12 px for it so the EditBox doesn't overlap.
            int relRow5Y = relRowYBase + rowH * 4 + TabsMenu.PANEL_PAD + TabsMenu.PANEL_PREVIEW_H + TabsMenu.PANEL_PAD;
            int scaleW = controlW - 12;
            add.accept(new IconScaleEditBox(screen, relControlX, relRow5Y, scaleW, controlH, configKey));

            // Icon-nudge row (row 6): four small numeric inputs for per-direction pixel
            // offsets. Layout mirrors drawOptionsPanel's letter pass — same pitch, same box
            // width — so the boxes line up under their U/D/L/R glyphs.
            int relRow6Y = relRow5Y + rowH;
            int nudgeLetterW = 8;
            int nudgeBoxW = 22;
            int nudgeCellW = nudgeLetterW + nudgeBoxW;
            int nudgePitch = (controlW - nudgeCellW) / 3;
            String[] dirSuffix = { "Up", "Down", "Left", "Right" };
            for (int i = 0; i < 4; i++) {
                int cellRelX = relControlX + i * nudgePitch + nudgeLetterW;
                add.accept(new IconNudgeEditBox(screen, cellRelX, relRow6Y, nudgeBoxW, controlH, configKey, dirSuffix[i]));
            }
        }

        // Anchor row (row 7): GUI/SCREEN toggle. Lives on the screen layout JSON like
        // tuck direction, so it works for screens with or without a TabsMenu configKey.
        int relRow7Y = relRowYBase + rowH * 4 + TabsMenu.PANEL_PAD + TabsMenu.PANEL_PREVIEW_H + TabsMenu.PANEL_PAD + rowH * 2;
        add.accept(new AnchorCycle(screen, relControlX, relRow7Y, controlW, controlH));

        // Tab Order row (row 8): L→R vs R→L. Pairs with rotation: a 180° bar with R→L
        // puts inventory back on the visible left after the flip.
        int relRow8Y = relRow7Y + rowH;
        add.accept(new TabOrderCycle(screen, relControlX, relRow8Y, controlW, controlH));

        // Max-tabs-per-page row (row 9): per-screen pagination cap. 0 means unlimited.
        int relRow9Y = relRow8Y + rowH;
        add.accept(new MaxTabsPerPageEditBox(screen, relControlX, relRow9Y, controlW, controlH));
    }

    public static class IconRotationCycle extends EditOnly {
        private final int relX, relY;

        public IconRotationCycle(Screen screen, int relX, int relY, int w, int h) {
            super(screen, 0, 0, w, h, Component.literal("0°"), btn -> TabsMenu.cycleIconRotation());
            this.relX = relX;
            this.relY = relY;
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            this.setMessage(Component.literal(TabsMenu.currentIconRotation() + "°"));
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    /**
     * Toggle button for tab visual order (LEFT_TO_RIGHT / RIGHT_TO_LEFT). RIGHT_TO_LEFT is
     * useful with a 180° bar rotation: the first tab (e.g. inventory) lands at the
     * rightmost natural-frame slot and ends up visually on the left after rotation.
     */
    public static class TabOrderCycle extends EditOnly {
        private final int relX, relY;

        public TabOrderCycle(Screen screen, int relX, int relY, int w, int h) {
            super(screen, 0, 0, w, h, Component.literal("L→R"),
                    btn -> TabsMenu.cycleTabOrder());
            this.relX = relX;
            this.relY = relY;
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            this.setMessage(Component.literal(
                    TabsMenu.currentTabOrder() == vodmordia.modtabs.layout.TabOrder.LEFT_TO_RIGHT
                            ? "L→R"
                            : "R→L"));
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    /**
     * Toggle button for the per-screen anchor frame (GUI / SCREEN). Clicking re-baselines
     * the layout's offsets through {@link TabsMenu#cycleAnchor()} so the bar stays put;
     * the editor's GUI/screen outline indicator updates on the next frame.
     */
    public static class AnchorCycle extends EditOnly {
        private final int relX, relY;

        public AnchorCycle(Screen screen, int relX, int relY, int w, int h) {
            super(screen, 0, 0, w, h, Component.literal("GUI"),
                    btn -> TabsMenu.cycleAnchor());
            this.relX = relX;
            this.relY = relY;
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            this.setMessage(Component.literal(
                    TabsMenu.currentAnchor() == vodmordia.modtabs.layout.Anchor.GUI_RELATIVE
                            ? "GUI"
                            : "SCREEN"));
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    /**
     * Cycle button that rotates per-screen tuck direction through up/down/left/right.
     * State lives on the screen's saved layout JSON, written immediately on click.
     */
    public static class TuckDirectionCycle extends EditOnly {
        private final int relX, relY;

        public TuckDirectionCycle(Screen screen, int relX, int relY, int w, int h) {
            super(screen, 0, 0, w, h, Component.literal("DOWN"),
                    btn -> TabsMenu.cycleTuckDirection());
            this.relX = relX;
            this.relY = relY;
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            this.setMessage(Component.literal(TabsMenu.currentTuckDirection().name()));
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    public static class VisibilityCycle extends EditOnly {
        private final String configKey;
        private final int relX, relY;

        public VisibilityCycle(Screen screen, int relX, int relY, int w, int h, String configKey) {
            super(screen, 0, 0, w, h, Component.literal(""), btn -> {});
            this.configKey = configKey;
            this.relX = relX;
            this.relY = relY;
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void onPress() {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            TabDisplayVisibility cur = readVisibility(configKey);
            TabDisplayVisibility next = switch (cur) {
                case YES -> TabDisplayVisibility.TUCK;
                case TUCK -> TabDisplayVisibility.NO;
                case NO -> TabDisplayVisibility.YES;
            };
            writeVisibility(configKey, next);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            this.setMessage(Component.literal(readVisibility(configKey).name()));
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    /**
     * Dropdown button that lists "(none)" + the .png files in {@code config/modtabs/icons/}.
     * Click toggles the popup; click an item to select it; clicks outside close the popup.
     * Outside-click dismissal is handled by {@link #handleClickPre} called from the editor's
     * mouse-pressed event handler — vanilla's per-widget mouseClicked dispatch can't detect
     * clicks that miss every widget.
     */
    public static class CustomIconDropdown extends EditOnly {
        private static CustomIconDropdown openInstance = null;
        private static final int ITEM_H = 12;
        private final String configKey;
        private final int relX, relY;

        public CustomIconDropdown(Screen screen, int relX, int relY, int w, int h, String configKey) {
            super(screen, 0, 0, w, h, Component.literal(""), btn -> {});
            this.configKey = configKey;
            this.relX = relX;
            this.relY = relY;
        }

        private boolean isOpen() { return openInstance == this; }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void onPress() {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            openInstance = isOpen() ? null : this;
        }

        /**
         * Returns -1 for the "(none)" item, 0..n-1 for the file at that index, or
         * Integer.MIN_VALUE if (mx,my) is outside the popup. Caller must have called
         * syncPosition() first.
         */
        private int popupItemIndex(double mx, double my) {
            List<String> files = listIconFiles();
            int x = getX();
            int y = getY() + getHeight();
            int w = getWidth();
            if (mx < x || mx >= x + w) return Integer.MIN_VALUE;
            int rel = (int) (my - y);
            if (rel < 0) return Integer.MIN_VALUE;
            int total = files.size() + 1;
            if (rel >= total * ITEM_H) return Integer.MIN_VALUE;
            return (rel / ITEM_H) - 1;
        }

        /**
         * Selects an item and closes the popup. {@code idx} of -1 means "(none)".
         */
        private void select(int idx) {
            List<String> files = listIconFiles();
            String value = (idx < 0 || idx >= files.size()) ? "" : files.get(idx);
            writeCustomIcon(configKey, value);
            openInstance = null;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            if (isOpen() && button == 0) {
                int idx = popupItemIndex(mouseX, mouseY);
                if (idx != Integer.MIN_VALUE) {
                    select(idx);
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            if (super.isMouseOver(mouseX, mouseY)) return true;
            return isOpen() && popupItemIndex(mouseX, mouseY) != Integer.MIN_VALUE;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            String v = readCustomIcon(configKey);
            String label = (v == null || v.isEmpty()) ? "(none)" : v;
            var font = Minecraft.getInstance().font;
            int maxW = getWidth() - 14;
            while (font.width(label) > maxW && label.length() > 4) {
                label = label.substring(0, label.length() - 4) + "...";
            }
            this.setMessage(Component.literal(label));
            super.renderWidget(gui, mouseX, mouseY, partial);
            if (isOpen()) renderPopup(gui, mouseX, mouseY);
        }

        private void renderPopup(GuiGraphics gui, int mouseX, int mouseY) {
            List<String> files = listIconFiles();
            var font = Minecraft.getInstance().font;
            int x = getX();
            int y = getY() + getHeight();
            int w = getWidth();
            int total = files.size() + 1;
            int totalH = total * ITEM_H;
            gui.pose().pushPose();
            // Lift above neighbouring panel widgets that render in the same pass.
            gui.pose().translate(0, 0, 50);
            gui.fill(x - 1, y - 1, x + w + 1, y + totalH + 1, 0xFF44FF66);
            gui.fill(x, y, x + w, y + totalH, 0xEE101418);
            for (int i = 0; i < total; i++) {
                int rowY = y + i * ITEM_H;
                String text = (i == 0) ? "(none)" : files.get(i - 1);
                String drawn = text;
                int maxW = w - 4;
                while (font.width(drawn) > maxW && drawn.length() > 4) {
                    drawn = drawn.substring(0, drawn.length() - 4) + "...";
                }
                boolean hover = mouseX >= x && mouseX < x + w
                        && mouseY >= rowY && mouseY < rowY + ITEM_H;
                if (hover) gui.fill(x, rowY, x + w, rowY + ITEM_H, 0x66FFFFFF);
                gui.drawString(font, drawn, x + 3, rowY + 2, 0xFFFFFFFF, false);
            }
            gui.pose().popPose();
        }

        /**
         * Pre-screen click handler: the editor's mouse-pressed listener calls this so
         * that popup item clicks are dispatched even when a tab/widget registered earlier
         * in {@code screen.children()} would otherwise consume the click. Also closes the
         * popup when the click misses the dropdown entirely. Returns true if the event
         * should be consumed.
         */
        public static boolean handleClickPre(Screen screen, double mx, double my, int button) {
            CustomIconDropdown open = openInstance;
            if (open == null) return false;
            open.syncPosition();
            if (button == 0) {
                int idx = open.popupItemIndex(mx, my);
                if (idx != Integer.MIN_VALUE) {
                    open.select(idx);
                    return true;
                }
            }
            if (!open.isMouseOver(mx, my)) {
                openInstance = null;
            }
            return false;
        }

        public static boolean isOpenSomewhere() {
            return openInstance != null;
        }

        public static void closeOpen() {
            openInstance = null;
        }
    }

    /** Refresh button — re-scans the icons folder so newly-added files appear in the cycle. */
    public static class CustomIconRefresh extends EditOnly {
        private final int relX, relY;

        public CustomIconRefresh(Screen screen, int relX, int relY, int w, int h) {
            super(screen, 0, 0, w, h, Component.literal("↻"),
                    btn -> refreshIconFiles());
            this.relX = relX;
            this.relY = relY;
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    /**
     * Opens the icons folder ({@code config/modtabs/icons/}) in the user's file browser.
     * Convenient when the user wants to drop a new PNG in and then refresh the dropdown.
     * Custom-rendered folder glyph (no super.renderWidget) so it doesn't try to fit a
     * tiny font character into 12 px.
     */
    public static class CustomIconFolder extends EditOnly {
        private final int relX, relY;

        public CustomIconFolder(Screen screen, int relX, int relY, int w, int h) {
            super(screen, 0, 0, w, h, Component.literal(""),
                    btn -> openIconsFolder());
            this.relX = relX;
            this.relY = relY;
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(getScreenInternal()) + relX);
            setY(TabsMenu.currentPanelY(getScreenInternal()) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            syncPosition();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int bgCol = isMouseOver(mouseX, mouseY) ? 0xFF606060 : 0xFF404040;
            int borderCol = 0xFF000000;
            gui.fill(x, y, x + w, y + h, bgCol);
            gui.fill(x, y, x + w, y + 1, borderCol);
            gui.fill(x, y + h - 1, x + w, y + h, borderCol);
            gui.fill(x, y, x + 1, y + h, borderCol);
            gui.fill(x + w - 1, y, x + w, y + h, borderCol);
            // Tiny folder glyph centered. 8 px wide, 6 px tall.
            int gx = x + (w - 8) / 2;
            int gy = y + (h - 6) / 2;
            int fill = 0xFFE8C063;
            int outline = 0xFF222222;
            gui.fill(gx + 1, gy, gx + 4, gy + 1, outline);
            gui.fill(gx, gy + 1, gx + 8, gy + 2, outline);
            gui.fill(gx, gy + 2, gx + 1, gy + 6, outline);
            gui.fill(gx + 7, gy + 2, gx + 8, gy + 6, outline);
            gui.fill(gx, gy + 5, gx + 8, gy + 6, outline);
            gui.fill(gx + 1, gy + 2, gx + 7, gy + 5, fill);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(getScreenInternal())) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    private static void openIconsFolder() {
        try {
            // getIconsDirectory creates the folder if missing — exactly what we want
            // before exposing it to the user.
            java.nio.file.Path dir = vodmordia.modtabs.utils.DynamicTextureLoader.getIconsDirectory();
            net.minecraft.Util.getPlatform().openFile(dir.toFile());
        } catch (Exception e) {
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open icons folder: " + e.getMessage());
        }
    }

    /**
     * Numeric input for the per-screen "max tabs per page" cap. Stored on
     * {@link vodmordia.modtabs.layout.ScreenLayout#maxTabsPerPage}. {@code 0} = unlimited
     * (no pagination). Writing fires a screen re-init so the bar rebuilds with the new
     * page size.
     */
    public static class MaxTabsPerPageEditBox extends EditBox {
        private final Screen screen;
        private final int relX, relY;
        private boolean suppressResponder = false;

        public MaxTabsPerPageEditBox(Screen screen, int relX, int relY, int w, int h) {
            super(Minecraft.getInstance().font, 0, 0, w, h, Component.literal(""));
            this.screen = screen;
            this.relX = relX;
            this.relY = relY;
            this.setMaxLength(2);
            this.setFilter(s -> s.matches("\\d{0,2}"));
            this.suppressResponder = true;
            this.setValue(String.valueOf(TabsMenu.currentMaxTabsPerPage()));
            this.suppressResponder = false;
            this.setResponder(v -> {
                if (suppressResponder) return;
                int parsed;
                try {
                    parsed = v.isEmpty() ? 0 : Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    parsed = 0;
                }
                TabsMenu.setMaxTabsPerPage(parsed);
            });
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(screen) + relX);
            setY(TabsMenu.currentPanelY(screen) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(screen)) return;
            syncPosition();
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(screen)) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setFocused(boolean focused) {
            boolean wasFocused = isFocused();
            super.setFocused(focused);
            // On focus-lost, re-init the screen so the bar's pagination picks up the
            // typed value. The responder writes the cap to JSON on every keystroke,
            // but re-init has to wait until the user is done typing or focus drops on
            // every character (and the EditBox value resets, blocking input).
            if (wasFocused && !focused) {
                TabsMenu.reinitCurrentScreen();
            }
        }
    }

    /**
     * Numeric input for the per-tab icon scale. Stored in {@link ModTabsConfig} as an
     * integer percentage (default 100). The EditBox accepts only digits up to 3 chars,
     * filters out leading garbage, and writes back on every change. Empty/invalid input
     * is treated as 100 so the icon doesn't disappear mid-edit.
     */
    public static class IconScaleEditBox extends EditBox {
        private final Screen screen;
        private final int relX, relY;
        private boolean suppressResponder = false;

        public IconScaleEditBox(Screen screen, int relX, int relY, int w, int h, String configKey) {
            super(Minecraft.getInstance().font, 0, 0, w, h, Component.literal(""));
            this.screen = screen;
            this.relX = relX;
            this.relY = relY;
            this.setMaxLength(3);
            this.setFilter(s -> s.matches("\\d{0,3}"));
            this.suppressResponder = true;
            this.setValue(String.valueOf(readScale(configKey)));
            this.suppressResponder = false;
            this.setResponder(v -> {
                if (suppressResponder) return;
                int parsed;
                try {
                    parsed = v.isEmpty() ? 100 : Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    parsed = 100;
                }
                writeScale(configKey, Math.max(0, Math.min(999, parsed)));
            });
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(screen) + relX);
            setY(TabsMenu.currentPanelY(screen) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(screen)) return;
            syncPosition();
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(screen)) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    /**
     * Numeric input for one direction of the per-tab icon nudge (in px). The four
     * directions (Up/Down/Left/Right) are stored as separate config fields; the renderer
     * collapses them to net (dx, dy). Treats empty input as 0.
     */
    public static class IconNudgeEditBox extends EditBox {
        private final Screen screen;
        private final int relX, relY;
        private boolean suppressResponder = false;

        public IconNudgeEditBox(Screen screen, int relX, int relY, int w, int h, String configKey, String dirSuffix) {
            super(Minecraft.getInstance().font, 0, 0, w, h, Component.literal(""));
            this.screen = screen;
            this.relX = relX;
            this.relY = relY;
            String fieldName = configKey + "TabIconNudge" + dirSuffix;
            this.setMaxLength(3);
            this.setFilter(s -> s.matches("\\d{0,3}"));
            this.suppressResponder = true;
            this.setValue(String.valueOf(readNudgeField(fieldName)));
            this.suppressResponder = false;
            this.setResponder(v -> {
                if (suppressResponder) return;
                int parsed;
                try { parsed = v.isEmpty() ? 0 : Integer.parseInt(v); }
                catch (NumberFormatException e) { parsed = 0; }
                writeNudgeField(fieldName, Math.max(0, Math.min(999, parsed)));
            });
        }

        private void syncPosition() {
            setX(TabsMenu.currentPanelX(screen) + relX);
            setY(TabsMenu.currentPanelY(screen) + relY);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(screen)) return;
            syncPosition();
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!TabsMenu.isEditing(screen)) return false;
            syncPosition();
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    private static int readNudgeField(String fieldName) {
        try {
            Field f = ModTabsConfig.class.getField(fieldName);
            Object v = f.get(null);
            if (v instanceof Integer i) return i;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return 0;
    }

    private static void writeNudgeField(String fieldName, int value) {
        try {
            Field f = ModTabsConfig.class.getField(fieldName);
            f.setInt(null, value);
            ModTabsConfig.write("modtabs");
            Config.Baked.bakeClient();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }

    private static int readScale(String configKey) {
        try {
            Field f = ModTabsConfig.class.getField(configKey + "TabIconScale");
            Object v = f.get(null);
            if (v instanceof Integer i) return i;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return 100;
    }

    private static void writeScale(String configKey, int value) {
        try {
            Field f = ModTabsConfig.class.getField(configKey + "TabIconScale");
            f.setInt(null, value);
            ModTabsConfig.write("modtabs");
            Config.Baked.bakeClient();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }

    private static List<String> cachedIconFiles = null;

    private static List<String> listIconFiles() {
        if (cachedIconFiles == null) refreshIconFiles();
        return cachedIconFiles;
    }

    public static void refreshIconFiles() {
        List<String> result = new ArrayList<>();
        try {
            Path dir = Paths.get("config/modtabs/icons");
            if (Files.isDirectory(dir)) {
                try (var stream = Files.list(dir)) {
                    stream.filter(Files::isRegularFile)
                            .map(p -> p.getFileName().toString())
                            .filter(name -> name.toLowerCase().endsWith(".png"))
                            .sorted()
                            .forEach(result::add);
                }
            }
        } catch (Exception ignored) {}
        cachedIconFiles = result;
    }

    /** Toggle button that swaps its label between "edit" and "exit" based on mode. */
    public static class EditToggle extends Button {
        private final Screen screen;

        public EditToggle(Screen screen, int x, int y, int w, int h) {
            super(x, y, w, h, Component.literal("edit"), btn -> {
                if (TabsMenu.isEditing(screen)) {
                    TabsMenu.exitEditMode();
                } else {
                    TabsMenu.enterEditMode(screen);
                }
            }, DEFAULT_NARRATION);
            this.screen = screen;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            this.setMessage(Component.literal(TabsMenu.isEditing(screen) ? "exit" : "edit"));
            super.renderWidget(gui, mouseX, mouseY, partial);
        }
    }

    /** Button visible and clickable only while the layout editor is active for its screen. */
    public static class EditOnly extends Button {
        private final Screen screen;

        public EditOnly(Screen screen, int x, int y, int w, int h, Component label, OnPress onPress) {
            super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
            this.screen = screen;
        }

        protected Screen getScreenInternal() { return screen; }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(screen)) return;
            super.renderWidget(gui, mouseX, mouseY, partial);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return TabsMenu.isEditing(screen) && super.isMouseOver(mouseX, mouseY);
        }
    }

    /** Bottom-left cog button — opens the global-settings modal. */
    public static class CogwheelButton extends EditOnly {
        public CogwheelButton(Screen screen, int x, int y, int w, int h) {
            super(screen, x, y, w, h, Component.literal(""),
                    btn -> GlobalSettingsPanel.open());
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            // Subtle frame so the cog sits on a visible button surface even on busy screens.
            int bg = isMouseOver(mouseX, mouseY) ? 0xCC2A3A30 : 0xC0101418;
            int border = 0xFF44FF66;
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            gui.fill(x, y, x + w, y + h, bg);
            gui.fill(x, y, x + w, y + 1, border);
            gui.fill(x, y + h - 1, x + w, y + h, border);
            gui.fill(x, y, x + 1, y + h, border);
            gui.fill(x + w - 1, y, x + w, y + h, border);
            // Source PNG is 25×25; scale via pose to fit inside the button frame.
            int iconSize = Math.max(8, Math.min(w, h) - 4);
            int iconX = x + (w - iconSize) / 2;
            int iconY = y + (h - iconSize) / 2;
            gui.pose().pushPose();
            gui.pose().translate(iconX, iconY, 0);
            float s = iconSize / 25f;
            gui.pose().scale(s, s, 1f);
            gui.blit(GlobalSettingsPanel.cogwheelTexture(), 0, 0, 0, 0, 25, 25, 25, 25);
            gui.pose().popPose();
        }
    }

    // ---- Reflection helpers for ModTabsConfig fields ----------------------------------

    private static TabDisplayVisibility readVisibility(String configKey) {
        try {
            Field f = ModTabsConfig.class.getField(configKey + "TabDisplayVisibility");
            Object v = f.get(null);
            if (v instanceof TabDisplayVisibility tdv) return tdv;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return TabDisplayVisibility.YES;
    }

    private static void writeVisibility(String configKey, TabDisplayVisibility value) {
        try {
            Field f = ModTabsConfig.class.getField(configKey + "TabDisplayVisibility");
            f.set(null, value);
            ModTabsConfig.write("modtabs");
            Config.Baked.bakeClient();
            // Re-init the current screen so initScreenButtons re-runs against the new
            // visibility — covers all three states (NO removes the TabButtons from
            // children, YES/TUCK re-adds them, isInTuckMode/animationManager are reset).
            // Edit-mode statics survive setScreen, so the editor stays open.
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen != null) {
                mc.setScreen(mc.screen);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }

    private static String readCustomIcon(String configKey) {
        try {
            Field f = ModTabsConfig.class.getField(configKey + "TabCustomIcon");
            Object v = f.get(null);
            if (v instanceof String s) return s;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return "";
    }

    private static void writeCustomIcon(String configKey, String value) {
        try {
            Field f = ModTabsConfig.class.getField(configKey + "TabCustomIcon");
            f.set(null, value);
            ModTabsConfig.write("modtabs");
            Config.Baked.bakeClient();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }
}
