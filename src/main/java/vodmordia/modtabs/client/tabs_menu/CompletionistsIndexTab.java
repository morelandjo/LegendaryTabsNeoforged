package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
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

import java.lang.reflect.Constructor;

/**
 * Tab for Fuzs' Completionist's Index. Mirrors the mod's own inventory-button path in
 * {@code IndexButtonHandler}: {@code Minecraft.setScreen(new ModsIndexViewScreen(prev))}.
 * The mod's native inventory button is suppressed via {@code IndexButtonHandlerMixin}
 * so the tab is the only entry point into the index from the inventory.
 *
 * {@code IndexViewScreen} is a plain {@link Screen} subclass (not a container screen)
 * whose {@code render()} draws the book background then calls {@code super.render()} —
 * so tabs added as children DO render naturally, and this FQN does not need to be added
 * to the manual-renderables list in {@code ClientNeoForgeEvents.onScreenRenderPost}.
 *
 * Both {@code ModsIndexViewScreen} (top-level mod list) and {@code ItemsIndexViewScreen}
 * (per-mod drill-down) are in the current-screen FQN list so cycling skips this tab when
 * the player is anywhere inside the index.
 */
@TabConfig(configKey = "completionistsIndexTab", defaultEnabled = true, defaultOrder = 0)
public class CompletionistsIndexTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "completionistsIndexTab",
            ModIntegration.COMPLETIONISTS_INDEX,
            () -> Config.Baked.completionistsIndexTabEnabled,
            "completionistsIndex",
            "completionists_index",
            TabSpec.Layout.guiRelative(),
            new String[] {
                    ScreenClasses.COMPLETIONISTS_INDEX_MODS_SCREEN,
                    ScreenClasses.COMPLETIONISTS_INDEX_ITEMS_SCREEN
            },
            new String[] {
                    ScreenClasses.COMPLETIONISTS_INDEX_MODS_SCREEN,
                    ScreenClasses.COMPLETIONISTS_INDEX_ITEMS_SCREEN
            }
    );

    public CompletionistsIndexTab() {
        super(SPEC, CompletionistsIndexTab::getIcon, Config.Baked.completionistsIndexTabCustomIcon);
    }

    private static ItemStack getIcon() {
        return new ItemStack(Items.KNOWLEDGE_BOOK);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.completionistsIndexTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> screenClass = ClassCache.resolve(ScreenClasses.COMPLETIONISTS_INDEX_MODS_SCREEN);
            if (screenClass == null) return;
            Constructor<?> ctor = screenClass.getConstructor(Screen.class);
            Screen current = Minecraft.getInstance().screen;
            Screen screen = (Screen) ctor.newInstance(current);
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Completionist's Index: " + e.getMessage());
        }
    }
}
