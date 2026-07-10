package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;

import java.util.List;

/**
 * Modal "global settings" panel — per-tab visibility, order, and global icon-offset config.
 * Triggered from the cogwheel button in the layout-editor overlay.
 *
 * Extracted from {@link TabsMenu} so the tab-menu surface stays focused on the
 * tab-row registry and edit-mode interaction. The panel reads the registry via
 * {@link TabsMenu#screenInfos()} and signals the host via {@link TabsMenu#previewRendering}
 * and {@link TabsMenu#reinitCurrentScreen()} only.
 */
@OnlyIn(Dist.CLIENT)
public final class GlobalSettingsPanel {
    public enum GlobalSettingsTab { VISIBILITY, STICKY, ORDER, GENERAL }
    /** Which numeric input on the General tab currently has focus, if any. */
    private enum GsField { NONE, OFFSET_TOP, OFFSET_RIGHT, OFFSET_BOTTOM, OFFSET_LEFT }
    private static GsField gsFocusedField = GsField.NONE;
    /** String draft of each numeric input — committed to ModTabsConfig only on Save. */
    private static String gsDraftOffsetTop = "0";
    private static String gsDraftOffsetRight = "0";
    private static String gsDraftOffsetBottom = "0";
    private static String gsDraftOffsetLeft = "0";
    /** Draft toggle for the nearby-container tabs feature. Committed on Save. */
    private static boolean gsDraftNearbyContainersEnabled = true;
    private static boolean globalSettingsOpen = false;
    private static GlobalSettingsTab gsActiveTab = GlobalSettingsTab.VISIBILITY;
    /** Draft state — applied to {@link ModTabsConfig} only on Save. */
    private static java.util.Map<String, Boolean> gsDraftEnabled = null;
    /** Draft per-tab sticky state (pinned to the leading end of the bar). Committed on Save. */
    private static java.util.Map<String, Boolean> gsDraftSticky = null;
    private static java.util.List<String> gsDraftOrder = null;
    /** Snapshot of the configurable tabs at modal-open time, keyed by short configKey.
     *  Avoids re-probing every tab's {@code isEnabled} every render frame. */
    private static java.util.Map<String, TabBase> gsTabsCache = null;
    /** Vertical pixel scroll offsets applied to slot positions in each modal tab. */
    private static int gsScrollVisibility = 0;
    private static int gsScrollSticky = 0;
    private static int gsScrollOrder = 0;
    /** -1 when not dragging; otherwise index in {@link #gsDraftOrder}. */
    private static int gsDraggingIndex = -1;

    private static final int GS_NAV_W = 90;
    private static final int GS_HEADER_H = 18;
    private static final int GS_FOOTER_H = 24;
    private static final int GS_NAV_BTN_H = 18;
    private static final int GS_PAD = 8;
    private static final int GS_CELL = 28;
    private static final int GS_FOOTER_BTN_W = 56;
    private static final int GS_FOOTER_BTN_H = 16;

    /** Width × height of one numeric input cell on the General tab. */
    private static final int GS_INPUT_W = 44;
    private static final int GS_INPUT_H = 14;

    private static final ResourceLocation COGWHEEL_TEX =
            ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/cogwheel.png");

    private GlobalSettingsPanel() {}

    public static ResourceLocation cogwheelTexture() { return COGWHEEL_TEX; }
    public static boolean isOpen() { return globalSettingsOpen; }

    /** Short configKey from a tab's @TabConfig (strips trailing "Tab"). */
    private static String shortConfigKey(TabBase tab) {
        TabConfig tc = tab.getClass().getAnnotation(TabConfig.class);
        if (tc == null) return null;
        String k = tc.configKey();
        return k.endsWith("Tab") ? k.substring(0, k.length() - 3) : k;
    }

    /** All registered tabs that have a {@code @TabConfig}, deduped by class. */
    private static java.util.List<TabBase> collectConfigurableTabs() {
        java.util.LinkedHashMap<Class<?>, TabBase> seen = new java.util.LinkedHashMap<>();
        for (TabsMenu.ScreenInfo info : TabsMenu.screenInfos()) {
            for (List<TabBase> list : info.tabs.values()) {
                for (TabBase tab : list) {
                    if (tab.getClass().isAnnotationPresent(TabConfig.class) && isTabAvailable(tab)) {
                        seen.putIfAbsent(tab.getClass(), tab);
                    }
                }
            }
        }
        return new java.util.ArrayList<>(seen.values());
    }

    /**
     * "Available" = the tab's mod is loaded. Many integration tabs register their screens
     * unconditionally but their {@code isEnabled(player)} short-circuits via a
     * {@code ModIntegrationManager.isModLoaded(...)} check. We probe by temporarily
     * forcing the user-enabled flag on, calling {@code isEnabled}, and restoring the flag.
     * If it returns false, the mod isn't present and the tab can never appear in any row.
     */
    private static boolean isTabAvailable(TabBase tab) {
        String key = shortConfigKey(tab);
        if (key == null) return false;
        Player p = Minecraft.getInstance().player;
        if (p == null) return true; // assume available before player exists
        java.lang.reflect.Field f;
        try {
            f = Config.Baked.class.getField(key + "TabEnabled");
        } catch (NoSuchFieldException e) {
            return true; // no flag on Baked → no way to probe; let it through
        }
        try {
            boolean original = f.getBoolean(null);
            try {
                f.setBoolean(null, true);
                return tab.isEnabled(p);
            } finally {
                f.setBoolean(null, original);
            }
        } catch (IllegalAccessException e) {
            return true;
        }
    }

    private static boolean readEnabledField(String shortKey) {
        try { return ModTabsConfig.class.getField(shortKey + "TabEnabled").getBoolean(null); }
        catch (Exception ignored) { return true; }
    }
    private static int readOrderField(String shortKey) {
        try { return ModTabsConfig.class.getField(shortKey + "TabOrder").getInt(null); }
        catch (Exception ignored) { return 0; }
    }
    private static void writeEnabledField(String shortKey, boolean value) {
        try { ModTabsConfig.class.getField(shortKey + "TabEnabled").setBoolean(null, value); }
        catch (Exception ignored) {}
    }
    private static void writeOrderField(String shortKey, int value) {
        try { ModTabsConfig.class.getField(shortKey + "TabOrder").setInt(null, value); }
        catch (Exception ignored) {}
    }
    private static boolean readStickyField(String shortKey) {
        try { return ModTabsConfig.class.getField(shortKey + "TabSticky").getBoolean(null); }
        catch (Exception ignored) { return false; }
    }
    private static void writeStickyField(String shortKey, boolean value) {
        try { ModTabsConfig.class.getField(shortKey + "TabSticky").setBoolean(null, value); }
        catch (Exception ignored) {}
    }

    private static int parseSafeInt(String s, int fallback) {
        if (s == null || s.isEmpty() || s.equals("-")) return fallback;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    public static void open() {
        gsDraftEnabled = new java.util.LinkedHashMap<>();
        gsDraftSticky = new java.util.LinkedHashMap<>();
        gsDraftOrder = new java.util.ArrayList<>();
        gsTabsCache = new java.util.LinkedHashMap<>();
        java.util.List<TabBase> tabs = collectConfigurableTabs();
        // Build sorted view by current order, ties broken by class name for stability.
        tabs.sort(java.util.Comparator.<TabBase>comparingInt(t -> readOrderField(shortConfigKey(t)))
                .thenComparing(t -> t.getClass().getSimpleName()));
        for (TabBase t : tabs) {
            String k = shortConfigKey(t);
            if (k == null) continue;
            gsDraftEnabled.put(k, readEnabledField(k));
            gsDraftSticky.put(k, readStickyField(k));
            gsDraftOrder.add(k);
            gsTabsCache.put(k, t);
        }
        gsActiveTab = GlobalSettingsTab.VISIBILITY;
        gsDraggingIndex = -1;
        gsFocusedField = GsField.NONE;
        gsDraftOffsetTop = String.valueOf(Config.Baked.iconOffsetTop);
        gsDraftOffsetRight = String.valueOf(Config.Baked.iconOffsetRight);
        gsDraftOffsetBottom = String.valueOf(Config.Baked.iconOffsetBottom);
        gsDraftOffsetLeft = String.valueOf(Config.Baked.iconOffsetLeft);
        gsDraftNearbyContainersEnabled = Config.Baked.nearbyContainersTabEnabled;
        globalSettingsOpen = true;
    }

    public static void close(boolean save) {
        boolean configChanged = false;
        if (save && gsDraftEnabled != null && gsDraftOrder != null) {
            for (var entry : gsDraftEnabled.entrySet()) {
                writeEnabledField(entry.getKey(), entry.getValue());
            }
            if (gsDraftSticky != null) {
                for (var entry : gsDraftSticky.entrySet()) {
                    writeStickyField(entry.getKey(), entry.getValue());
                }
            }
            // Write 1-based indices: the existing sort comparator treats order==0 as
            // "no explicit override" and banishes the tab to an alphabetical bucket.
            for (int i = 0; i < gsDraftOrder.size(); i++) {
                writeOrderField(gsDraftOrder.get(i), i + 1);
            }
            // General tab settings
            ModTabsConfig.iconOffsetTop = parseSafeInt(gsDraftOffsetTop, 0);
            ModTabsConfig.iconOffsetRight = parseSafeInt(gsDraftOffsetRight, 0);
            ModTabsConfig.iconOffsetBottom = parseSafeInt(gsDraftOffsetBottom, 0);
            ModTabsConfig.iconOffsetLeft = parseSafeInt(gsDraftOffsetLeft, 0);
            ModTabsConfig.nearbyContainersTabEnabled = gsDraftNearbyContainersEnabled;
            ModTabsConfig.write("modtabs");
            Config.Baked.bakeClient();
            configChanged = true;
        }
        globalSettingsOpen = false;
        gsDraftEnabled = null;
        gsDraftSticky = null;
        gsDraftOrder = null;
        gsTabsCache = null;
        gsDraggingIndex = -1;
        gsScrollVisibility = 0;
        gsScrollSticky = 0;
        gsScrollOrder = 0;
        gsFocusedField = GsField.NONE;
        // Force a re-init of the current screen so its tab row picks up the new
        // enabled/order config — TabButton instances are added to screen.children
        // at Init.Post time and won't reflect later config writes otherwise.
        // Routes through reinitCurrentScreen so Screen.initialized is cleared via
        // reflection first, otherwise vanilla Screen.init short-circuits and only
        // calls repositionElements (skipping Init.Post and our tab rebuild).
        if (configChanged) {
            TabsMenu.reinitCurrentScreen();
        }
    }

    /** Looks up the tab instance for a short configKey. Used to render its icon. */
    private static TabBase tabForKey(String shortKey) {
        if (gsTabsCache != null) return gsTabsCache.get(shortKey);
        for (TabBase t : collectConfigurableTabs()) {
            if (shortKey.equals(shortConfigKey(t))) return t;
        }
        return null;
    }

    private static int[] gsWindowRect(Screen screen) {
        int sw = screen.width;
        int sh = screen.height;
        int w = (int)(sw * 0.75);
        int h = Math.min((int)(sh * 0.85), Math.max(160, sh - 40));
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;
        return new int[]{x, y, w, h};
    }

    private static int[] gsContentRect(Screen screen) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + GS_NAV_W;
        int y = win[1] + GS_HEADER_H;
        int w = win[2] - GS_NAV_W - 1;
        int h = win[3] - GS_HEADER_H - GS_FOOTER_H;
        return new int[]{x, y, w, h};
    }

    private static int[] gsNavButtonRect(Screen screen, int index) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + 4;
        int y = win[1] + GS_HEADER_H + 4 + index * (GS_NAV_BTN_H + 2);
        return new int[]{x, y, GS_NAV_W - 8, GS_NAV_BTN_H};
    }

    private static int[] gsSaveButtonRect(Screen screen) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + win[2] - GS_FOOTER_BTN_W - 6;
        int y = win[1] + win[3] - GS_FOOTER_BTN_H - 5;
        return new int[]{x, y, GS_FOOTER_BTN_W, GS_FOOTER_BTN_H};
    }
    private static int[] gsCancelButtonRect(Screen screen) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + win[2] - GS_FOOTER_BTN_W * 2 - 12;
        int y = win[1] + win[3] - GS_FOOTER_BTN_H - 5;
        return new int[]{x, y, GS_FOOTER_BTN_W, GS_FOOTER_BTN_H};
    }

    /** Slot rect for icon at index within a two-column panel, offset by the given scroll. */
    private static int[] gsTwoColSlotRect(Screen screen, int column, int slotIndex, int scroll) {
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int colX = cr[0] + column * colW + GS_PAD;
        int colY = cr[1] + GS_HEADER_H;
        int cellsPerRow = Math.max(1, (colW - GS_PAD * 2) / GS_CELL);
        int row = slotIndex / cellsPerRow;
        int col = slotIndex % cellsPerRow;
        return new int[]{colX + col * GS_CELL, colY + row * GS_CELL - scroll, GS_CELL, GS_CELL};
    }

    /** Slot rect for icon at index within the visibility column (0=visible, 1=hidden). */
    private static int[] gsVisibilitySlotRect(Screen screen, int column, int slotIndex) {
        return gsTwoColSlotRect(screen, column, slotIndex, gsScrollVisibility);
    }

    /** Slot rect for icon at index within the sticky column (0=not sticky, 1=sticky). */
    private static int[] gsStickySlotRect(Screen screen, int column, int slotIndex) {
        return gsTwoColSlotRect(screen, column, slotIndex, gsScrollSticky);
    }

    private static int[] gsOrderSlotRect(Screen screen, int slotIndex) {
        int[] cr = gsContentRect(screen);
        int areaX = cr[0] + GS_PAD;
        // Drop below the header separator line (matches the visibility tab's column layout).
        int areaY = cr[1] + GS_HEADER_H;
        int areaW = cr[2] - GS_PAD * 2;
        int cellsPerRow = Math.max(1, areaW / GS_CELL);
        int row = slotIndex / cellsPerRow;
        int col = slotIndex % cellsPerRow;
        return new int[]{areaX + col * GS_CELL, areaY + row * GS_CELL - gsScrollOrder, GS_CELL, GS_CELL};
    }

    /** Maximum scroll-down value for the active tab; 0 means content fits in view. */
    private static int gsMaxScrollVisibility(Screen screen) {
        if (gsDraftOrder == null) return 0;
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int cellsPerRow = Math.max(1, (colW - GS_PAD * 2) / GS_CELL);
        int visCount = 0, hidCount = 0;
        for (String key : gsDraftOrder) {
            if (Boolean.TRUE.equals(gsDraftEnabled.get(key))) visCount++; else hidCount++;
        }
        int rows = Math.max(rowsFor(visCount, cellsPerRow), rowsFor(hidCount, cellsPerRow));
        int viewportH = cr[3] - GS_HEADER_H;
        return Math.max(0, rows * GS_CELL - viewportH);
    }

    private static int gsMaxScrollSticky(Screen screen) {
        if (gsDraftOrder == null || gsDraftSticky == null) return 0;
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int cellsPerRow = Math.max(1, (colW - GS_PAD * 2) / GS_CELL);
        int stickyCount = 0, notCount = 0;
        for (String key : gsDraftOrder) {
            if (Boolean.TRUE.equals(gsDraftSticky.get(key))) stickyCount++; else notCount++;
        }
        int rows = Math.max(rowsFor(stickyCount, cellsPerRow), rowsFor(notCount, cellsPerRow));
        int viewportH = cr[3] - GS_HEADER_H;
        return Math.max(0, rows * GS_CELL - viewportH);
    }

    private static int gsMaxScrollOrder(Screen screen) {
        if (gsDraftOrder == null) return 0;
        int[] cr = gsContentRect(screen);
        int areaW = cr[2] - GS_PAD * 2;
        int cellsPerRow = Math.max(1, areaW / GS_CELL);
        int rows = rowsFor(gsVisibleOrderKeys().size(), cellsPerRow);
        int viewportH = cr[3] - GS_HEADER_H;
        return Math.max(0, rows * GS_CELL - viewportH);
    }

    private static int rowsFor(int count, int cellsPerRow) {
        return count == 0 ? 0 : (count + cellsPerRow - 1) / cellsPerRow;
    }

    /** Order tab only shows tabs flagged enabled in gsDraftEnabled — disabled tabs are hidden
     *  there so the user only reorders what's actually visible in-game. The full gsDraftOrder
     *  list is still the canonical source of truth (we mutate it on drag); this helper is just
     *  the visible projection. */
    private static java.util.List<String> gsVisibleOrderKeys() {
        if (gsDraftOrder == null || gsDraftEnabled == null) return java.util.Collections.emptyList();
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String k : gsDraftOrder) {
            if (Boolean.TRUE.equals(gsDraftEnabled.get(k))) out.add(k);
        }
        return out;
    }

    private static int gsOrderHitTest(Screen screen, double mx, double my) {
        int n = gsVisibleOrderKeys().size();
        for (int i = 0; i < n; i++) {
            int[] r = gsOrderSlotRect(screen, i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) return i;
        }
        return -1;
    }

    /**
     * Hover hit-test for tab icons on the Visibility and Order panels — returns the configKey
     * of the icon under the cursor, or null if none. Used to drive the mod-name tooltip.
     * Skips while a drag is in progress: the dragged icon follows the cursor, so a tooltip
     * for whatever slot happens to be underneath would name a different tab than the one
     * being held.
     */
    private static String gsHoveredIconKey(Screen screen, double mx, double my) {
        if (gsDraftOrder == null || gsDraftEnabled == null) return null;
        if (gsActiveTab == GlobalSettingsTab.ORDER && gsDraggingIndex >= 0) return null;
        int[] cr = gsContentRect(screen);
        if (mx < cr[0] || mx >= cr[0] + cr[2] || my < cr[1] + GS_HEADER_H || my >= cr[1] + cr[3]) return null;

        if (gsActiveTab == GlobalSettingsTab.VISIBILITY) {
            int visIdx = 0, hidIdx = 0;
            for (String key : gsDraftOrder) {
                boolean enabled = Boolean.TRUE.equals(gsDraftEnabled.get(key));
                int[] slot = gsVisibilitySlotRect(screen, enabled ? 0 : 1, enabled ? visIdx++ : hidIdx++);
                if (mx >= slot[0] && mx < slot[0] + slot[2] && my >= slot[1] && my < slot[1] + slot[3]) {
                    return key;
                }
            }
        } else if (gsActiveTab == GlobalSettingsTab.STICKY) {
            int notIdx = 0, stickyIdx = 0;
            for (String key : gsDraftOrder) {
                boolean sticky = Boolean.TRUE.equals(gsDraftSticky.get(key));
                int[] slot = gsStickySlotRect(screen, sticky ? 1 : 0, sticky ? stickyIdx++ : notIdx++);
                if (mx >= slot[0] && mx < slot[0] + slot[2] && my >= slot[1] && my < slot[1] + slot[3]) {
                    return key;
                }
            }
        } else if (gsActiveTab == GlobalSettingsTab.ORDER) {
            int idx = gsOrderHitTest(screen, mx, my);
            if (idx >= 0) {
                java.util.List<String> visible = gsVisibleOrderKeys();
                if (idx < visible.size()) return visible.get(idx);
            }
        }
        return null;
    }

    /**
     * Tooltip text for a tab icon — prefers the integration mod's display name (matches what
     * the user asked for: "the mod name") when the tab carries a TabSpec, falls back to the
     * tab's own getTooltip() text otherwise. {@code spec} is package-protected on
     * {@link IntegrationIconTab} / {@link IntegrationItemTab}, so the cast-and-read works
     * here without a public getter.
     */
    private static String gsTabLabelFor(String key) {
        if (gsTabsCache == null) return null;
        TabBase tab = gsTabsCache.get(key);
        if (tab == null) return null;
        if (tab instanceof IntegrationIconTab integration
                && integration.spec != null && integration.spec.mod() != null) {
            return integration.spec.mod().getDisplayName();
        }
        if (tab instanceof IntegrationItemTab integration
                && integration.spec != null && integration.spec.mod() != null) {
            return integration.spec.mod().getDisplayName();
        }
        try {
            Component tip = tab.getTooltip();
            return tip == null ? null : tip.getString();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void render(GuiGraphics gui, Screen screen, int mouseX, int mouseY) {
        if (!globalSettingsOpen || gsDraftEnabled == null) return;
        int[] win = gsWindowRect(screen);
        int wx = win[0], wy = win[1], ww = win[2], wh = win[3];

        // Modal scrim — darkens the underlying edit overlay.
        gui.fill(0, 0, screen.width, screen.height, 0xC0000000);

        int bg = 0xF0101418;
        int border = 0xFF44FF66;
        gui.fill(wx, wy, wx + ww, wy + wh, bg);
        gui.fill(wx, wy, wx + ww, wy + 1, border);
        gui.fill(wx, wy + wh - 1, wx + ww, wy + wh, border);
        gui.fill(wx, wy, wx + 1, wy + wh, border);
        gui.fill(wx + ww - 1, wy, wx + ww, wy + wh, border);
        // Header bar
        gui.drawString(Minecraft.getInstance().font, "Global Settings", wx + GS_PAD, wy + 5, 0xFFCCFFCC, false);
        gui.fill(wx + 1, wy + GS_HEADER_H, wx + ww - 1, wy + GS_HEADER_H + 1, border);
        // Nav | content separator
        gui.fill(wx + GS_NAV_W, wy + GS_HEADER_H, wx + GS_NAV_W + 1, wy + wh, border);

        // Nav buttons
        String[] navLabels = { "Visibility", "Sticky", "Order", "General" };
        for (int i = 0; i < navLabels.length; i++) {
            int[] r = gsNavButtonRect(screen, i);
            boolean active = (gsActiveTab == GlobalSettingsTab.values()[i]);
            int btnBg = active ? 0xFF1F3322 : 0xFF1A1F23;
            gui.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], btnBg);
            gui.fill(r[0], r[1], r[0] + r[2], r[1] + 1, border);
            gui.fill(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], border);
            gui.fill(r[0], r[1], r[0] + 1, r[1] + r[3], border);
            gui.fill(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], border);
            int textColor = active ? 0xFFCCFFCC : 0xFFAAAAAA;
            gui.drawString(Minecraft.getInstance().font, navLabels[i], r[0] + 6, r[1] + 5, textColor, false);
        }

        // Content
        if (gsActiveTab == GlobalSettingsTab.VISIBILITY) {
            renderVisibilityTab(gui, screen);
        } else if (gsActiveTab == GlobalSettingsTab.STICKY) {
            renderStickyTab(gui, screen);
        } else if (gsActiveTab == GlobalSettingsTab.ORDER) {
            renderOrderTab(gui, screen, mouseX, mouseY);
        } else {
            renderGeneralTab(gui, screen);
        }

        // Footer buttons
        int[] sbr = gsSaveButtonRect(screen);
        int[] cbr = gsCancelButtonRect(screen);
        drawFooterButton(gui, sbr, "save", 0xFF1F3322);
        drawFooterButton(gui, cbr, "cancel", 0xFF331F1F);

        // Mod-name tooltip for icons on the Visibility / Order panels. Drawn last so it
        // floats above the panel chrome and footer buttons.
        String hoverKey = gsHoveredIconKey(screen, mouseX, mouseY);
        if (hoverKey != null) {
            String label = gsTabLabelFor(hoverKey);
            if (label != null && !label.isEmpty()) {
                drawIconTooltip(gui, screen, label, mouseX, mouseY);
            }
        }
    }

    /**
     * Manual tooltip render — we can't use {@code gui.renderTooltip} here because that path
     * queues into the active screen's tooltip renderer, which silently drops the tooltip
     * when called from inside our nested pose stack (Z=900) inside the host screen's render
     * event. Drawing a styled box ourselves matches the rest of the panel's chrome (dark fill
     * + green border) and guarantees the tooltip appears at the cursor immediately.
     */
    private static void drawIconTooltip(GuiGraphics gui, Screen screen, String label, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;
        int textW = font.width(label);
        int padX = 4, padY = 3;
        int boxW = textW + padX * 2;
        int boxH = font.lineHeight + padY * 2;
        // Offset like vanilla: a bit right and above the cursor. Clamp to keep the box on
        // screen even when the cursor is near the right edge or top of the panel area.
        int x = mouseX + 10;
        int y = mouseY - boxH - 2;
        if (x + boxW > screen.width - 2) x = screen.width - boxW - 2;
        if (y < 2) y = mouseY + 10;
        int bg = 0xF0101418;
        int border = 0xFF44FF66;
        gui.fill(x, y, x + boxW, y + boxH, bg);
        gui.fill(x, y, x + boxW, y + 1, border);
        gui.fill(x, y + boxH - 1, x + boxW, y + boxH, border);
        gui.fill(x, y, x + 1, y + boxH, border);
        gui.fill(x + boxW - 1, y, x + boxW, y + boxH, border);
        gui.drawString(font, label, x + padX, y + padY, 0xFFCCFFCC, false);
    }

    private static void drawFooterButton(GuiGraphics gui, int[] r, String label, int bg) {
        int border = 0xFF44FF66;
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], bg);
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + 1, border);
        gui.fill(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], border);
        gui.fill(r[0], r[1], r[0] + 1, r[1] + r[3], border);
        gui.fill(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], border);
        int tw = Minecraft.getInstance().font.width(label);
        gui.drawString(Minecraft.getInstance().font, label,
                r[0] + (r[2] - tw) / 2, r[1] + 4, 0xFFCCFFCC, false);
    }

    private static void renderVisibilityTab(GuiGraphics gui, Screen screen) {
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int leftColX = cr[0];
        int rightColX = cr[0] + colW;
        // Hidden column red tint background
        gui.fill(rightColX, cr[1], cr[0] + cr[2], cr[1] + cr[3], 0x30FF4444);
        // Headers (drawn outside the scroll area so they stay pinned)
        gui.drawString(Minecraft.getInstance().font, "Visible", leftColX + GS_PAD, cr[1] + 4, 0xFFAAFFAA, false);
        gui.drawString(Minecraft.getInstance().font, "Hidden", rightColX + GS_PAD, cr[1] + 4, 0xFFFFAAAA, false);
        gui.fill(leftColX, cr[1] + GS_HEADER_H - 4, cr[0] + cr[2], cr[1] + GS_HEADER_H - 3, 0xFF44FF66);

        // Clamp scroll to current bounds (recomputes when icons move between columns).
        gsScrollVisibility = Math.max(0, Math.min(gsScrollVisibility, gsMaxScrollVisibility(screen)));

        // Clip icons to the area below the headers so scrolling never bleeds over them.
        gui.enableScissor(cr[0], cr[1] + GS_HEADER_H, cr[0] + cr[2], cr[1] + cr[3]);
        int visIdx = 0, hidIdx = 0;
        for (String key : gsDraftOrder) {
            boolean enabled = Boolean.TRUE.equals(gsDraftEnabled.get(key));
            int[] slot = gsVisibilitySlotRect(screen, enabled ? 0 : 1, enabled ? visIdx++ : hidIdx++);
            renderTabIconAt(gui, key, slot[0], slot[1]);
        }
        gui.disableScissor();
    }

    private static void renderStickyTab(GuiGraphics gui, Screen screen) {
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int leftColX = cr[0];
        int rightColX = cr[0] + colW;
        // Sticky column (right) gets a gold tint to mark the pinned side.
        gui.fill(rightColX, cr[1], cr[0] + cr[2], cr[1] + cr[3], 0x30FFAA00);
        // Headers (drawn outside the scroll area so they stay pinned)
        gui.drawString(Minecraft.getInstance().font, "Not Sticky", leftColX + GS_PAD, cr[1] + 4, 0xFFAAAAAA, false);
        gui.drawString(Minecraft.getInstance().font, "Sticky", rightColX + GS_PAD, cr[1] + 4, 0xFFFFDD55, false);
        gui.fill(leftColX, cr[1] + GS_HEADER_H - 4, cr[0] + cr[2], cr[1] + GS_HEADER_H - 3, 0xFF44FF66);

        // Clamp scroll to current bounds (recomputes when icons move between columns).
        gsScrollSticky = Math.max(0, Math.min(gsScrollSticky, gsMaxScrollSticky(screen)));

        gui.enableScissor(cr[0], cr[1] + GS_HEADER_H, cr[0] + cr[2], cr[1] + cr[3]);
        int notIdx = 0, stickyIdx = 0;
        for (String key : gsDraftOrder) {
            boolean sticky = Boolean.TRUE.equals(gsDraftSticky.get(key));
            int[] slot = gsStickySlotRect(screen, sticky ? 1 : 0, sticky ? stickyIdx++ : notIdx++);
            renderTabIconAt(gui, key, slot[0], slot[1]);
        }
        gui.disableScissor();
    }

    private static void renderOrderTab(GuiGraphics gui, Screen screen, int mouseX, int mouseY) {
        int[] cr = gsContentRect(screen);
        gui.drawString(Minecraft.getInstance().font, "Drag to reorder",
                cr[0] + GS_PAD, cr[1] + 4, 0xFFAAAAAA, false);
        gui.fill(cr[0], cr[1] + GS_HEADER_H - 4, cr[0] + cr[2], cr[1] + GS_HEADER_H - 3, 0xFF44FF66);

        gsScrollOrder = Math.max(0, Math.min(gsScrollOrder, gsMaxScrollOrder(screen)));

        java.util.List<String> visible = gsVisibleOrderKeys();
        gui.enableScissor(cr[0], cr[1] + GS_HEADER_H, cr[0] + cr[2], cr[1] + cr[3]);
        for (int i = 0; i < visible.size(); i++) {
            if (i == gsDraggingIndex) continue; // dragged item rendered last at cursor
            int[] slot = gsOrderSlotRect(screen, i);
            renderTabIconAt(gui, visible.get(i), slot[0], slot[1]);
        }
        if (gsDraggingIndex >= 0 && gsDraggingIndex < visible.size()) {
            int[] slot = gsOrderSlotRect(screen, gsDraggingIndex);
            int dragX = (int) (mouseX - GS_CELL / 2);
            int dragY = (int) (mouseY - GS_CELL / 2);
            gui.fill(slot[0], slot[1], slot[0] + slot[2], slot[1] + 1, 0xFFFFAA00);
            gui.fill(slot[0], slot[1] + slot[3] - 1, slot[0] + slot[2], slot[1] + slot[3], 0xFFFFAA00);
            gui.fill(slot[0], slot[1], slot[0] + 1, slot[1] + slot[3], 0xFFFFAA00);
            gui.fill(slot[0] + slot[2] - 1, slot[1], slot[0] + slot[2], slot[1] + slot[3], 0xFFFFAA00);
            renderTabIconAt(gui, visible.get(gsDraggingIndex), dragX, dragY);
        }
        gui.disableScissor();
    }

    // ---- General tab ---------------------------------------------------------

    private static int[] gsOffsetInputRect(Screen screen, int index) {
        int[] cr = gsContentRect(screen);
        int gap = 6;
        int totalW = GS_INPUT_W * 4 + gap * 3;
        int rowX = cr[0] + (cr[2] - totalW) / 2;
        int rowY = cr[1] + GS_HEADER_H + 18; // labels above
        return new int[]{rowX + index * (GS_INPUT_W + gap), rowY, GS_INPUT_W, GS_INPUT_H};
    }

    private static int[] gsNearbyToggleRect(Screen screen) {
        int[] cr = gsContentRect(screen);
        int btnW = 72;
        int btnH = 14;
        // Sits below the 4 offset inputs (rowY = cr[1] + GS_HEADER_H + 18, h = GS_INPUT_H),
        // with a gap large enough to fit the section label.
        int rowY = cr[1] + GS_HEADER_H + 18 + GS_INPUT_H + 22;
        int x = cr[0] + (cr[2] - btnW) / 2;
        return new int[]{x, rowY, btnW, btnH};
    }

    private static GsField gsHitGeneralInput(Screen screen, double mx, double my) {
        GsField[] offsetFields = { GsField.OFFSET_TOP, GsField.OFFSET_RIGHT, GsField.OFFSET_BOTTOM, GsField.OFFSET_LEFT };
        for (int i = 0; i < 4; i++) {
            int[] r = gsOffsetInputRect(screen, i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) return offsetFields[i];
        }
        return null;
    }

    private static String gsDraftValue(GsField field) {
        return switch (field) {
            case OFFSET_TOP -> gsDraftOffsetTop;
            case OFFSET_RIGHT -> gsDraftOffsetRight;
            case OFFSET_BOTTOM -> gsDraftOffsetBottom;
            case OFFSET_LEFT -> gsDraftOffsetLeft;
            case NONE -> "";
        };
    }

    private static void gsSetDraftValue(GsField field, String value) {
        switch (field) {
            case OFFSET_TOP -> gsDraftOffsetTop = value;
            case OFFSET_RIGHT -> gsDraftOffsetRight = value;
            case OFFSET_BOTTOM -> gsDraftOffsetBottom = value;
            case OFFSET_LEFT -> gsDraftOffsetLeft = value;
            case NONE -> {}
        }
    }

    private static void renderGeneralTab(GuiGraphics gui, Screen screen) {
        int[] cr = gsContentRect(screen);
        gui.drawString(Minecraft.getInstance().font, "Icon offset (pixels)",
                cr[0] + GS_PAD, cr[1] + 4, 0xFFAAAAAA, false);
        gui.fill(cr[0], cr[1] + GS_HEADER_H - 4, cr[0] + cr[2], cr[1] + GS_HEADER_H - 3, 0xFF44FF66);

        String[] labels = { "top", "right", "bottom", "left" };
        GsField[] fields = { GsField.OFFSET_TOP, GsField.OFFSET_RIGHT, GsField.OFFSET_BOTTOM, GsField.OFFSET_LEFT };
        for (int i = 0; i < 4; i++) {
            int[] r = gsOffsetInputRect(screen, i);
            int lw = Minecraft.getInstance().font.width(labels[i]);
            gui.drawString(Minecraft.getInstance().font, labels[i],
                    r[0] + (r[2] - lw) / 2, r[1] - 10, 0xFFCCCCCC, false);
            gsRenderInput(gui, r, gsDraftValue(fields[i]), gsFocusedField == fields[i]);
        }

        // Section: nearby-container tabs toggle. The button colour mirrors the Save / Cancel
        // footer buttons (green = enabled, red = disabled) so the state is obvious at a glance.
        int[] toggleRect = gsNearbyToggleRect(screen);
        String sectionLabel = "Nearby inventory tabs";
        int slw = Minecraft.getInstance().font.width(sectionLabel);
        gui.drawString(Minecraft.getInstance().font, sectionLabel,
                cr[0] + (cr[2] - slw) / 2, toggleRect[1] - 12, 0xFFCCCCCC, false);
        String btnLabel = gsDraftNearbyContainersEnabled ? "Enabled" : "Disabled";
        int btnBg = gsDraftNearbyContainersEnabled ? 0xFF1F3322 : 0xFF331F1F;
        drawFooterButton(gui, toggleRect, btnLabel, btnBg);
    }

    private static void gsRenderInput(GuiGraphics gui, int[] r, String value, boolean focused) {
        int bg = 0xFF1A1F23;
        int border = focused ? 0xFFFFEE66 : 0xFF44FF66;
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], bg);
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + 1, border);
        gui.fill(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], border);
        gui.fill(r[0], r[1], r[0] + 1, r[1] + r[3], border);
        gui.fill(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], border);
        String display = value + (focused ? "_" : "");
        // Right-align numerically so growing values don't push the cursor offscreen.
        int tw = Minecraft.getInstance().font.width(display);
        int textX = r[0] + Math.max(4, r[2] - 4 - tw);
        gui.drawString(Minecraft.getInstance().font, display, textX, r[1] + 3, 0xFFFFFFFF, false);
    }

    /** Char-typed handler — appends digit/minus to the focused field. Returns true if consumed. */
    public static boolean handleCharTyped(char c) {
        if (!globalSettingsOpen || gsFocusedField == GsField.NONE) return false;
        String cur = gsDraftValue(gsFocusedField);
        if (c >= '0' && c <= '9') {
            if (cur.length() < 6) gsSetDraftValue(gsFocusedField, cur + c);
            return true;
        }
        if (c == '-' && cur.isEmpty()) {
            gsSetDraftValue(gsFocusedField, "-");
            return true;
        }
        return false;
    }

    /** Key handler for backspace / enter / escape on focused General-tab input.
     *  Returns true if the modal consumed the key. */
    public static boolean handleKey(int keyCode) {
        if (!globalSettingsOpen || gsFocusedField == GsField.NONE) return false;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
            String cur = gsDraftValue(gsFocusedField);
            if (!cur.isEmpty()) gsSetDraftValue(gsFocusedField, cur.substring(0, cur.length() - 1));
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            gsFocusedField = GsField.NONE;
            return true;
        }
        return false;
    }

    /** Renders a tab's bare icon (no background, no rotation, no vertical) at slot top-left. */
    private static void renderTabIconAt(GuiGraphics gui, String shortKey, int slotX, int slotY) {
        TabBase tab = tabForKey(shortKey);
        if (tab == null) return;
        // Centered: tab.render writes a 26×22 frame, with the icon centered inside.
        // Slot is GS_CELL × GS_CELL, so offset to center the tab inside the slot.
        int tabOffX = slotX + (GS_CELL - 26) / 2;
        int tabOffY = slotY + (GS_CELL - 22) / 2;
        TabsMenu.previewRendering = true;
        try {
            tab.render(gui, tabOffX, tabOffY, false);
        } finally {
            TabsMenu.previewRendering = false;
        }
    }

    /** Mouse-down on the modal. Returns true if the press should be consumed. */
    public static boolean handleMouseDown(Screen screen, double mx, double my) {
        if (!globalSettingsOpen) return false;
        // Footer buttons
        int[] sbr = gsSaveButtonRect(screen);
        if (mx >= sbr[0] && mx < sbr[0] + sbr[2] && my >= sbr[1] && my < sbr[1] + sbr[3]) {
            close(true);
            return true;
        }
        int[] cbr = gsCancelButtonRect(screen);
        if (mx >= cbr[0] && mx < cbr[0] + cbr[2] && my >= cbr[1] && my < cbr[1] + cbr[3]) {
            close(false);
            return true;
        }
        // Nav buttons
        for (int i = 0; i < GlobalSettingsTab.values().length; i++) {
            int[] r = gsNavButtonRect(screen, i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                gsActiveTab = GlobalSettingsTab.values()[i];
                gsDraggingIndex = -1;
                gsScrollVisibility = 0;
                gsScrollSticky = 0;
                gsScrollOrder = 0;
                gsFocusedField = GsField.NONE;
                return true;
            }
        }
        // Content-area interactions
        if (gsActiveTab == GlobalSettingsTab.GENERAL) {
            int[] toggleRect = gsNearbyToggleRect(screen);
            if (mx >= toggleRect[0] && mx < toggleRect[0] + toggleRect[2]
                    && my >= toggleRect[1] && my < toggleRect[1] + toggleRect[3]) {
                gsDraftNearbyContainersEnabled = !gsDraftNearbyContainersEnabled;
                gsFocusedField = GsField.NONE;
                return true;
            }
            GsField hit = gsHitGeneralInput(screen, mx, my);
            gsFocusedField = (hit != null) ? hit : GsField.NONE;
            return true;
        }
        if (gsActiveTab == GlobalSettingsTab.VISIBILITY) {
            int visIdx = 0, hidIdx = 0;
            for (String key : gsDraftOrder) {
                boolean enabled = Boolean.TRUE.equals(gsDraftEnabled.get(key));
                int[] slot = gsVisibilitySlotRect(screen, enabled ? 0 : 1, enabled ? visIdx++ : hidIdx++);
                if (mx >= slot[0] && mx < slot[0] + slot[2] && my >= slot[1] && my < slot[1] + slot[3]) {
                    gsDraftEnabled.put(key, !enabled);
                    return true;
                }
            }
        } else if (gsActiveTab == GlobalSettingsTab.STICKY) {
            int notIdx = 0, stickyIdx = 0;
            for (String key : gsDraftOrder) {
                boolean sticky = Boolean.TRUE.equals(gsDraftSticky.get(key));
                int[] slot = gsStickySlotRect(screen, sticky ? 1 : 0, sticky ? stickyIdx++ : notIdx++);
                if (mx >= slot[0] && mx < slot[0] + slot[2] && my >= slot[1] && my < slot[1] + slot[3]) {
                    gsDraftSticky.put(key, !sticky);
                    return true;
                }
            }
        } else if (gsActiveTab == GlobalSettingsTab.ORDER) {
            int idx = gsOrderHitTest(screen, mx, my);
            if (idx >= 0) {
                gsDraggingIndex = idx;
                return true;
            }
        }
        // Click outside any control inside the modal: still consume, prevent fall-through.
        return true;
    }

    public static boolean handleMouseDrag(Screen screen, double mx, double my) {
        if (!globalSettingsOpen) return false;
        if (gsActiveTab == GlobalSettingsTab.ORDER && gsDraggingIndex >= 0) {
            int targetIdx = gsOrderHitTest(screen, mx, my);
            // gsDraggingIndex / targetIdx are positions in the *visible* (enabled-only) list;
            // gsDraftOrder is the full list with disabled tabs interleaved. Translate by
            // looking up the moving + target keys in the full list and reinserting around
            // the target's position so disabled tabs stay where they were.
            java.util.List<String> visible = gsVisibleOrderKeys();
            if (targetIdx >= 0 && targetIdx != gsDraggingIndex
                    && gsDraggingIndex < visible.size() && targetIdx < visible.size()) {
                String moving = visible.get(gsDraggingIndex);
                String target = visible.get(targetIdx);
                int fromFull = gsDraftOrder.indexOf(moving);
                int toFull = gsDraftOrder.indexOf(target);
                if (fromFull >= 0 && toFull >= 0) {
                    gsDraftOrder.remove(fromFull);
                    if (fromFull < toFull) toFull--;
                    gsDraftOrder.add(toFull, moving);
                    gsDraggingIndex = targetIdx;
                }
            }
        }
        return true; // always consume drags inside the modal
    }

    public static boolean handleMouseUp(Screen screen, double mx, double my) {
        if (!globalSettingsOpen) return false;
        gsDraggingIndex = -1;
        return true;
    }

    /** Mouse-wheel scroll on the modal. {@code dy} > 0 means scroll up (content moves down). */
    public static boolean handleMouseScroll(Screen screen, double mx, double my, double dy) {
        if (!globalSettingsOpen) return false;
        // Only scroll when cursor is inside the content area; nav/footer ignore the wheel.
        int[] cr = gsContentRect(screen);
        if (mx < cr[0] || mx > cr[0] + cr[2] || my < cr[1] || my > cr[1] + cr[3]) return true;
        int step = GS_CELL; // one row per notch — matches a cell so partial rows aren't created
        if (gsActiveTab == GlobalSettingsTab.VISIBILITY) {
            int max = gsMaxScrollVisibility(screen);
            gsScrollVisibility = Math.max(0, Math.min(max, gsScrollVisibility - (int) (dy * step)));
        } else if (gsActiveTab == GlobalSettingsTab.STICKY) {
            int max = gsMaxScrollSticky(screen);
            gsScrollSticky = Math.max(0, Math.min(max, gsScrollSticky - (int) (dy * step)));
        } else {
            int max = gsMaxScrollOrder(screen);
            gsScrollOrder = Math.max(0, Math.min(max, gsScrollOrder - (int) (dy * step)));
        }
        return true;
    }
}
