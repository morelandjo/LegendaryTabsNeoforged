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
 * {@code IndexButtonHandler.onAfterInventoryScreenInit}:
 * {@code Minecraft.setScreen(new ModsIndexViewScreen(prev, true))}. The mod's native
 * inventory button is suppressed via {@code IndexButtonHandlerMixin} so the tab is the
 * only entry point into the index from the inventory.
 *
 * Constructor signature is {@code (Screen lastScreen, boolean fromInventory)} on
 * NeoForge 1.21.1 — the boolean is new in this build and gates back-navigation behavior.
 * Passing {@code true} matches what the mod's own inventory button does.
 *
 * {@code IndexViewScreen} (on 1.21.1) extends vanilla {@code StatsScreen} via
 * {@code StatsUpdateListener}, and its {@code render()} calls {@code super.render()} —
 * tabs added as children render naturally without needing the manual-renderables list
 * in {@code ClientNeoForgeEvents.onScreenRenderPost}.
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
            Constructor<?> ctor = screenClass.getConstructor(Screen.class, boolean.class);
            Screen current = Minecraft.getInstance().screen;
            Screen screen = (Screen) ctor.newInstance(current, true);
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Completionist's Index: " + e.getMessage());
        }
    }
}
