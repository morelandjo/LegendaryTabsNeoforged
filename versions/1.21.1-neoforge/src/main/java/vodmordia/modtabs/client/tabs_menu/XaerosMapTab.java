package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableCustomIconTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@TabConfig(configKey = "xaerosMapTab", defaultEnabled = true, defaultOrder = 0)
public class XaerosMapTab extends ConfigurableCustomIconTab {

    public XaerosMapTab() {
        super((context) -> {
            try {
                context.gui.blit(ResourceLocation.fromNamespaceAndPath("xaeroworldmap", "icon.png"),
                    context.x + 6, context.y + 5, 0, 0, 14, 14, 16, 16);
            } catch (Exception e) {
                context.gui.fill(context.x + 7, context.y + 5, context.x + 19, context.y + 17, 0xFF8B4513);
                context.gui.fill(context.x + 9, context.y + 7, context.x + 17, context.y + 15, 0xFF90EE90);
            }
        }, Config.Baked.xaerosMapTabCustomIcon, "xaerosMap", 13, 12);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.xaerosMapTabEnabled && player.level().isClientSide) {
            try {
                Class<?> worldMapSessionClass = Class.forName("xaero.map.WorldMapSession");
                java.lang.reflect.Method getCurrentSessionMethod = worldMapSessionClass.getMethod("getCurrentSession");
                Object currentSession = getCurrentSessionMethod.invoke(null);

                java.lang.reflect.Method getMapProcessorMethod = currentSession.getClass().getMethod("getMapProcessor");
                Object mapProcessor = getMapProcessorMethod.invoke(currentSession);

                Class<?> guiMapClass = Class.forName("xaero.map.gui.GuiMap");
                Class<?> mapProcessorClass = Class.forName("xaero.map.MapProcessor");

                Screen guiMap = (Screen) guiMapClass.getDeclaredConstructor(Screen.class, Screen.class, mapProcessorClass, net.minecraft.world.entity.Entity.class)
                    .newInstance(null, null, mapProcessor, Minecraft.getInstance().getCameraEntity());

                Minecraft.getInstance().setScreen(guiMap);
            } catch (Exception e) {
                // Xaero's World Map not present or failed to open map
            }
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.xaerosMapTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.XAEROS_MAP);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> guiMapClass = Class.forName("xaero.map.gui.GuiMap");
            return guiMapClass.isInstance(currentScreen);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.xaeros_map.description");
    }

    @Override
    public void initTabOnScreens() {
        // Register Xaero's World Map screen as a standard GUI-relative tab. Anything
        // about its placement is now editable via the per-screen layout editor and
        // ships as a JSON in config/modtabs/screen-layouts/.
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Screen> guiMapClass = (Class<? extends Screen>) Class.forName("xaero.map.gui.GuiMap");
            ScreenRegistry.builder()
                .withStandardDimensions()
                .registerAllTabs(guiMapClass);
        } catch (ClassNotFoundException e) {
            // Xaero's World Map mod not available
        }
    }
}
