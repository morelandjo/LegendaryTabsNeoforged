package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.utils.ArsElixirumInspector;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;


@TabConfig(configKey = "arsElixirumTab", defaultEnabled = true, defaultOrder = 0)
public class ArsElixirumTab extends ConfigurableItemTab {

    // Enum class names that have held the page-enum across Ars Elixirum versions, newest first.
    // 0.12.0+ moved the enum to .alchemy.AlchemyPage; 0.10.2 had it at .widgets.pages.PageKind.
    private static final String[] PAGE_ENUM_CLASSES = {
        "dev.obscuria.elixirum.client.screen.alchemy.AlchemyPage",
        "dev.obscuria.elixirum.client.screen.widgets.pages.PageKind"
    };

    // Direct CollectionScreen class names — used as a last resort if the page enum is missing.
    private static final String[] COLLECTION_SCREEN_CLASSES = {
        "dev.obscuria.elixirum.client.screen.alchemy.pages.collection.CollectionScreen",
        "dev.obscuria.elixirum.client.screen.widgets.pages.collection.CollectionPage"
    };

    private static ItemStack cachedIcon;

    public ArsElixirumTab() {
        super(() -> getGlassCauldronItem(), Config.Baked.arsElixirumTabCustomIcon, "arsElixirum");
    }

    private static ItemStack getGlassCauldronItem() {
        if (cachedIcon != null) return cachedIcon;
        try {
            Item glassCauldronItem = ArsElixirumInspector.tryGetGlassCauldronItem();
            if (glassCauldronItem != null) {
                cachedIcon = new ItemStack(glassCauldronItem);
                return cachedIcon;
            }
        } catch (Throwable ignored) {
        }
        cachedIcon = new ItemStack(Items.BREWING_STAND);
        return cachedIcon;
    }

    @Override
    public void openTargetScreen(Player player) {
        // Ars Elixirum's keybind handler bails when Minecraft.screen != null, so we replicate
        // what the keybind ultimately does: invoke the COLLECTION page-enum's open() method.
        // Class name moved between versions, so we try each known location.
        for (String className : PAGE_ENUM_CLASSES) {
            try {
                Class<?> enumClass = Class.forName(className);
                Object collection = null;
                for (Object constant : enumClass.getEnumConstants()) {
                    if ("COLLECTION".equals(((Enum<?>) constant).name())) {
                        collection = constant;
                        break;
                    }
                }
                if (collection == null) continue;
                enumClass.getMethod("open").invoke(collection);
                return;
            } catch (Throwable ignored) {
            }
        }

        // Last resort: instantiate the CollectionScreen class directly via its no-arg constructor.
        for (String className : COLLECTION_SCREEN_CLASSES) {
            try {
                Class<?> screenClass = Class.forName(className);
                java.lang.reflect.Constructor<?> constructor = screenClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object screen = constructor.newInstance();
                Minecraft.getInstance().setScreen((Screen) screen);
                return;
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.arsElixirumTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.ARS_ELIXIRUM);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        // On 1.20.1 the active page is a generated PageKind screen, not a single ElixirumScreen
        // class — checking by package prefix covers both branches.
        return currentScreen != null
                && currentScreen.getClass().getName().startsWith("dev.obscuria.elixirum.");
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.ars_elixirum.description");
    }

    @Override
    public void initTabOnScreens() {
        // Forge 1.20.1 Ars Elixirum has no single root screen — each page is its own Screen.
        // 0.12.0 moved/renamed every page; we register both old and new class names so tabs
        // render regardless of which version is installed.
        ScreenRegistry.builder()
            .withStandardDimensions()
            .registerAllTabs(
                // 0.12.0+ class layout
                "dev.obscuria.elixirum.client.screen.alchemy.pages.compendium.CompendiumScreen",
                "dev.obscuria.elixirum.client.screen.alchemy.pages.collection.CollectionScreen",
                "dev.obscuria.elixirum.client.screen.alchemy.pages.discoveries.DiscoveriesScreen",
                "dev.obscuria.elixirum.client.screen.alchemy.pages.recent.RecentlyBrewedScreen",
                // 0.10.x class layout
                "dev.obscuria.elixirum.client.screen.widgets.pages.CompendiumPage",
                "dev.obscuria.elixirum.client.screen.widgets.pages.collection.CollectionPage",
                "dev.obscuria.elixirum.client.screen.widgets.pages.discoveries.DiscoveriesPage",
                "dev.obscuria.elixirum.client.screen.widgets.pages.recent.RecentlyBrewedPage"
            );
    }
}
