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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "arsElixirumTab", defaultEnabled = true, defaultOrder = 0)
public class ArsElixirumTab extends ConfigurableItemTab {

    public ArsElixirumTab() {
        super(() -> getGlassCauldronItem(), Config.Baked.arsElixirumTabCustomIcon, "arsElixirum");
    }

    private static ItemStack getGlassCauldronItem() {
        // Try to get Ars Elixirum glass cauldron item via reflection using inspector
        try {
            Item glassCauldronItem = ArsElixirumInspector.tryGetGlassCauldronItem();
            if (glassCauldronItem != null) {
                return new ItemStack(glassCauldronItem);
            }
        } catch (Exception e) {
            // Fall through to fallback
        }
        // Fallback to brewing stand
        return new ItemStack(Items.BREWING_STAND);
    }

    @Override
    public void openTargetScreen(Player player) {
        // 1.20.1 Ars Elixirum: the keybind handler KeyMappings.collectionPressed() bails early
        // when Minecraft.screen != null, so we can't go through it from inside the inventory tab.
        // Call PageKind.COLLECTION.open() directly — that's what the keybind ultimately invokes.
        try {
            Class<?> pageKindClass = Class.forName("dev.obscuria.elixirum.client.screen.widgets.pages.PageKind");
            Object collection = null;
            for (Object constant : pageKindClass.getEnumConstants()) {
                if ("COLLECTION".equals(((Enum<?>) constant).name())) {
                    collection = constant;
                    break;
                }
            }
            if (collection != null) {
                pageKindClass.getMethod("open").invoke(collection);
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            Class<?> screenClass = Class.forName("dev.obscuria.elixirum.client.screen.ElixirumScreen");
            java.lang.reflect.Constructor<?> constructor = screenClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object screen = constructor.newInstance();

            try {
                Class<?> sectionTypeClass = Class.forName("dev.obscuria.elixirum.client.screen.section.AbstractSection$Type");
                Field collectionField = sectionTypeClass.getField("COLLECTION");
                Object collectionSection = collectionField.get(null);

                Field selectedSectionField = screenClass.getDeclaredField("selectedSection");
                selectedSectionField.setAccessible(true);
                selectedSectionField.set(null, collectionSection);
            } catch (Exception ignored) {
            }

            Minecraft.getInstance().setScreen((Screen) screen);
        } catch (Exception ignored) {
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
        // Forge 1.20.1 build of Ars Elixirum has no single `ElixirumScreen` — each page is
        // its own Screen subclass under .widgets.pages. Register all four concrete pages so
        // tabs render on whichever page the user is viewing and Shift+Z opens the editor.
        ScreenRegistry.builder()
            .withStandardDimensions()
            .registerAllTabs(
                "dev.obscuria.elixirum.client.screen.widgets.pages.CompendiumPage",
                "dev.obscuria.elixirum.client.screen.widgets.pages.collection.CollectionPage",
                "dev.obscuria.elixirum.client.screen.widgets.pages.discoveries.DiscoveriesPage",
                "dev.obscuria.elixirum.client.screen.widgets.pages.recent.RecentlyBrewedPage"
            );
    }
}
