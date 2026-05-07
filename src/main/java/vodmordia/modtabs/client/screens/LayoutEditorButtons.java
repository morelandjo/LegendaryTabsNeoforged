package vodmordia.modtabs.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.config.TabDisplayVisibility;

import java.lang.reflect.Field;

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
        int controlH = 12;

        add.accept(new IconRotationCycle(screen, relControlX, relRow1Y, controlW, controlH));

        String configKey = TabsMenu.getConfigKeyForScreen(screen);
        if (configKey != null) {
            add.accept(new VisibilityCycle(screen, relControlX, relRow2Y, controlW, controlH, configKey));
            add.accept(new CustomIconEditBox(screen, relControlX, relRow3Y, controlW, controlH, configKey));
        }
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

    public static class CustomIconEditBox extends EditBox {
        private final Screen screen;
        private final int relX, relY;

        public CustomIconEditBox(Screen screen, int relX, int relY, int w, int h, String configKey) {
            super(Minecraft.getInstance().font, 0, 0, w, h, Component.literal(""));
            this.screen = screen;
            this.relX = relX;
            this.relY = relY;
            this.setMaxLength(256);
            this.setValue(readCustomIcon(configKey));
            this.setResponder(v -> writeCustomIcon(configKey, v));
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
                    btn -> TabsMenu.openGlobalSettings());
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
            if (!TabsMenu.isEditing(getScreenInternal())) return;
            int bg = isMouseOver(mouseX, mouseY) ? 0xCC2A3A30 : 0xC0101418;
            int border = 0xFF44FF66;
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            gui.fill(x, y, x + w, y + h, bg);
            gui.fill(x, y, x + w, y + 1, border);
            gui.fill(x, y + h - 1, x + w, y + h, border);
            gui.fill(x, y, x + 1, y + h, border);
            gui.fill(x + w - 1, y, x + w, y + h, border);
            int iconSize = Math.max(8, Math.min(w, h) - 4);
            int iconX = x + (w - iconSize) / 2;
            int iconY = y + (h - iconSize) / 2;
            gui.pose().pushPose();
            gui.pose().translate(iconX, iconY, 0);
            float s = iconSize / 25f;
            gui.pose().scale(s, s, 1f);
            gui.blit(TabsMenu.cogwheelTexture(), 0, 0, 0, 0, 25, 25, 25, 25);
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
