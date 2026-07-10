package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Tab for evanbones' Field Guide. Mirrors the mod's inventory-button branch in
 * {@code InventoryScreenMixin.addFieldGuideButton}:
 * <pre>
 *   if ("last_opened_screen".equals(defaultMode) &amp;&amp; BookScreen.lastOpenedScreen != null) {
 *       setScreen(BookScreen.lastOpenedScreen);
 *   } else {
 *       setScreen(new FieldGuideCategoryScreen());
 *   }
 * </pre>
 * The mod's own inventory button is suppressed via {@code FieldGuideInventoryScreenMixin}
 * so the tab is the only inventory entry point.
 *
 * The auto-open "recent unlock → entry screen" branch of {@code FieldGuideClient.openGuide}
 * is intentionally not mirrored — that's a keybind-only flow (it short-circuits on a 5-second
 * window after a guide unlock) and the inventory button doesn't honor it either.
 *
 * {@code FieldGuideCategoryScreen} (and its siblings) extend {@code BookScreen extends Screen}
 * — plain Screen subclasses, not container screens. Their {@code render()} blits the 300x200
 * book texture and then calls {@code super.render()}, so tabs added as children draw without
 * needing the manual-renderables list in {@code ClientNeoForgeEvents.onScreenRenderPost}.
 */
@TabConfig(configKey = "fieldGuideTab", defaultEnabled = true, defaultOrder = 0)
public class FieldGuideTab extends IntegrationItemTab {

    private static final ResourceLocation FIELD_GUIDE_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("fieldguide", "field_guide");

    private static final TabSpec SPEC = new TabSpec(
            "fieldGuideTab",
            ModIntegration.FIELD_GUIDE,
            () -> Config.Baked.fieldGuideTabEnabled,
            "fieldGuide",
            "field_guide",
            TabSpec.Layout.guiRelative(),
            new String[] {
                    ScreenClasses.FIELD_GUIDE_CATEGORY_SCREEN,
                    ScreenClasses.FIELD_GUIDE_ENTRY_SCREEN,
                    ScreenClasses.FIELD_GUIDE_JOURNAL_SCREEN
            },
            new String[] {
                    ScreenClasses.FIELD_GUIDE_CATEGORY_SCREEN,
                    ScreenClasses.FIELD_GUIDE_ENTRY_SCREEN,
                    ScreenClasses.FIELD_GUIDE_JOURNAL_SCREEN
            }
    );

    public FieldGuideTab() {
        super(SPEC, FieldGuideTab::getBookIcon, Config.Baked.fieldGuideTabCustomIcon);
    }

    private static ItemStack getBookIcon() {
        Item item = BuiltInRegistries.ITEM.get(FIELD_GUIDE_ITEM_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.KNOWLEDGE_BOOK);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.fieldGuideTabEnabled || !player.level().isClientSide) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            Screen target = resolveLastOpenedScreen();
            if (target == null) {
                Class<?> categoryClass = ClassCache.resolve(ScreenClasses.FIELD_GUIDE_CATEGORY_SCREEN);
                if (categoryClass == null) return;
                target = (Screen) categoryClass.getConstructor().newInstance();
            }
            mc.setScreen(target);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Field Guide: " + e.getMessage());
        }
    }

    /**
     * If the player's Field Guide config has {@code defaultScreen == "last_opened_screen"}
     * and a prior {@code BookScreen.lastOpenedScreen} exists, return it — same branch the
     * native inventory button takes. Otherwise return null and the caller will construct a
     * fresh {@code FieldGuideCategoryScreen}.
     */
    private static Screen resolveLastOpenedScreen() {
        try {
            Class<?> configClass = ClassCache.resolve(ScreenClasses.FIELD_GUIDE_CLIENT_CONFIG);
            if (configClass == null) return null;
            Method get = configClass.getMethod("get");
            Object cfg = get.invoke(null);
            if (cfg == null) return null;
            Field defaultScreenField = cfg.getClass().getField("defaultScreen");
            Object value = defaultScreenField.get(cfg);
            if (!"last_opened_screen".equals(value)) return null;

            Class<?> bookScreenClass = ClassCache.resolve(ScreenClasses.FIELD_GUIDE_BOOK_SCREEN);
            if (bookScreenClass == null) return null;
            Field lastOpenedField = bookScreenClass.getField("lastOpenedScreen");
            Object screen = lastOpenedField.get(null);
            if (screen instanceof Screen s) return s;
        } catch (Exception ignored) {
        }
        return null;
    }
}
