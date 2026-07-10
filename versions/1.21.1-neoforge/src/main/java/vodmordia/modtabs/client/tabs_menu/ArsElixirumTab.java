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

    private static ItemStack cachedIcon;

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
            } catch (Throwable ignored) {
            }

            Minecraft.getInstance().setScreen((Screen) screen);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.arsElixirumTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.ARS_ELIXIRUM);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> screenClass = Class.forName("dev.obscuria.elixirum.client.screen.ElixirumScreen");
            return screenClass.isInstance(currentScreen);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.ars_elixirum.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .registerAllTabs("dev.obscuria.elixirum.client.screen.ElixirumScreen");
    }
}
