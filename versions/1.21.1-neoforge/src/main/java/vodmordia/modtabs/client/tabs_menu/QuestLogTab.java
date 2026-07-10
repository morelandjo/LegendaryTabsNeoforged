package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Tab for Infernal Studios' Questlog. Mirrors the mod's own keybind path:
 * {@code Minecraft.setScreen(new QuestlogScreen(previousScreen))}. The mod's native inventory
 * button (added by {@code InventoryScreenMixin}) is suppressed via {@code QuestlogInventoryScreenMixin}
 * so the tab is the only inventory entry point.
 *
 * {@code QuestlogScreen} is a plain {@link Screen} subclass whose {@code render()} calls
 * {@code super.render()}, so tabs added as children render naturally — no entry in
 * {@code ClientNeoForgeEvents.onScreenRenderPost} needed.
 *
 * Notification badge. The native button overlays an exclamation-style badge whenever
 * {@code QuestlogClientEvents.mostRecentNotificationQuest} is non-null OR any quest is
 * {@code isCompleted() && !isRewarded()}. This tab replicates that exact check reflectively
 * (so we don't compile-link the mod), and overlays the same {@code DEFAULT_BADGE} sprite from
 * {@code questlog:textures/gui/quest_peripherals.png} (16×16 at UV (8,12) in a 256×256 atlas) —
 * with the same sine-wave bob the native button uses when
 * {@code config.button.bobbingBadge == true}.
 *
 * Per-quest custom badges ({@code quest.getDisplay().getBadge()}) are intentionally not
 * mirrored — they require invoking the mod's {@code Blittable} interface reflectively across
 * an unstable API surface. The default badge is what 99% of quests render anyway, and missing
 * a per-quest icon is preferable to crashing if the mod renames {@code Blittable.blit}.
 */
@TabConfig(configKey = "questLogTab", defaultEnabled = true, defaultOrder = 0)
public class QuestLogTab extends IntegrationIconTab {

    /** Icon for the tab — the book-spiral glyph extracted from the mod's own
     *  {@code questlog_button.png} so the tab matches the visual identity the native button
     *  established. Source PNG is 18×16, drawn into the standard 16×16 tab icon slot (the
     *  shared icon renderer stretches non-16×16 textures; 18→16 is a ~11% horizontal squash). */
    private static final ResourceLocation QUEST_LOG_ICON =
            ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/questlog.png");

    /** Source of the native button's default exclamation-mark sprite. Path/UVs lifted from
     *  {@code QuestlogOpenButton.DEFAULT_BADGE} on both Forge 1.20.1 and NeoForge 1.21.1 —
     *  the badge has been stable across both lines. */
    private static final ResourceLocation BADGE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("questlog", "textures/gui/quest_peripherals.png");
    private static final int BADGE_W = 16;
    private static final int BADGE_H = 16;
    private static final int BADGE_U = 8;
    private static final int BADGE_V = 12;
    private static final int BADGE_TEX_W = 256;
    private static final int BADGE_TEX_H = 256;

    private static final TabSpec SPEC = new TabSpec(
            "questLogTab",
            ModIntegration.QUEST_LOG,
            () -> Config.Baked.questLogTabEnabled,
            "questLog",
            "quest_log",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.QUEST_LOG_SCREEN },
            new String[] { ScreenClasses.QUEST_LOG_SCREEN }
    );

    public QuestLogTab() {
        super(SPEC, QUEST_LOG_ICON, Config.Baked.questLogTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.questLogTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> screenClass = ClassCache.resolve(ScreenClasses.QUEST_LOG_SCREEN);
            if (screenClass == null) return;
            Constructor<?> ctor = screenClass.getConstructor(Screen.class);
            Screen current = Minecraft.getInstance().screen;
            Screen screen = (Screen) ctor.newInstance(current);
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Quest Log: " + e.getMessage());
        }
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        super.render(gui, x, y, hover);
        if (shouldShowBadge()) {
            drawBadge(gui, x, y);
        }
    }

    @Override
    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        super.renderInverted(gui, x, y, hover);
        if (shouldShowBadge()) {
            drawBadge(gui, x, y);
        }
    }

    /**
     * Returns true when the native button would also show its badge: there's a recent
     * notification quest, or any quest is completed but its rewards haven't been claimed.
     * Both checks come from {@code QuestlogOpenButton.render}; mirroring them here keeps
     * the tab indicator in lockstep with what the mod itself decides is "notify-worthy".
     */
    private boolean shouldShowBadge() {
        // mostRecentNotificationQuest is the cheap path (most common cause of a badge),
        // so check it first and short-circuit before touching the per-quest scan.
        Object recent = readStaticField(ScreenClasses.QUEST_LOG_CLIENT_EVENTS, "mostRecentNotificationQuest");
        if (recent != null) return true;
        return hasCompletedUnrewardedQuest();
    }

    private static Object readStaticField(String classFqn, String fieldName) {
        try {
            Class<?> c = ClassCache.resolve(classFqn);
            if (c == null) return null;
            Field f = c.getField(fieldName);
            return f.get(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Replicates the secondary badge check in {@code QuestlogOpenButton.render}:
     * iterate {@code QuestlogClient.getLocal().getAllQuests()} and look for the first
     * quest with {@code isCompleted() == true && isRewarded() == false}. Returns false
     * if anything in the reflection chain fails (mod renamed a method, no QuestManager
     * yet) — a missing badge is fine; throwing into the render loop is not.
     */
    private static boolean hasCompletedUnrewardedQuest() {
        try {
            Class<?> clientClass = ClassCache.resolve(ScreenClasses.QUEST_LOG_CLIENT);
            if (clientClass == null) return false;
            // QuestlogClient.getLocal() throws if the player is null — match the mod's own
            // null guard so the badge never crashes the title screen if a tab somehow gets
            // rendered there.
            if (Minecraft.getInstance().player == null) return false;
            Method getLocal = clientClass.getMethod("getLocal");
            Object manager = getLocal.invoke(null);
            if (manager == null) return false;
            Method getAllQuests = manager.getClass().getMethod("getAllQuests");
            Object quests = getAllQuests.invoke(manager);
            if (!(quests instanceof Iterable<?> iter)) return false;
            for (Object quest : iter) {
                if (quest == null) continue;
                Method isCompleted = quest.getClass().getMethod("isCompleted");
                Method isRewarded = quest.getClass().getMethod("isRewarded");
                Object c = isCompleted.invoke(quest);
                Object r = isRewarded.invoke(quest);
                if (Boolean.TRUE.equals(c) && Boolean.FALSE.equals(r)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Overlay the default exclamation-mark sprite on the tab's top-right corner, with the
     * same sine-bob the native button does. The native button is 64×64 with the badge
     * offset by (24, 0) from the button's top-left; a tab is 26×22, so we anchor the badge
     * around the top-right corner instead of trying to scale those numbers down.
     */
    private static void drawBadge(GuiGraphics gui, int tabX, int tabY) {
        int bx = tabX + TabBase.TAB_WIDTH - BADGE_W + 4;
        int by = tabY - 6;
        float bob = (float) Math.sin(((System.currentTimeMillis() / 50.0F)) * 0.2F) * 1.5F;
        gui.pose().pushPose();
        gui.pose().translate(0, bob, 0);
        gui.blit(BADGE_TEXTURE, bx, by, BADGE_U, BADGE_V, BADGE_W, BADGE_H, BADGE_TEX_W, BADGE_TEX_H);
        gui.pose().popPose();
    }
}
