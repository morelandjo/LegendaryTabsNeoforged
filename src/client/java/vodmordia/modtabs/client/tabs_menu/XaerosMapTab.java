package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

public class XaerosMapTab extends TabBase {
    private static net.minecraft.util.Identifier cachedTexture = null;
    private static boolean textureSearchCompleted = false;

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Use the same approach as working NeoForge version
            Class<?> worldMapSessionClass = Class.forName("xaero.map.WorldMapSession");
            java.lang.reflect.Method getCurrentSessionMethod = worldMapSessionClass.getMethod("getCurrentSession");
            Object currentSession = getCurrentSessionMethod.invoke(null);

            if (currentSession != null) {
                java.lang.reflect.Method getMapProcessorMethod = currentSession.getClass().getMethod("getMapProcessor");
                Object mapProcessor = getMapProcessorMethod.invoke(currentSession);

                if (mapProcessor != null) {
                    Class<?> guiMapClass = Class.forName("xaero.map.gui.GuiMap");

                    Screen guiMap = (Screen) guiMapClass.getDeclaredConstructor(
                        Screen.class,
                        Screen.class,
                        mapProcessor.getClass(),
                        net.minecraft.entity.Entity.class
                    ).newInstance(null, null, mapProcessor, minecraft.getCameraEntity());

                    minecraft.setScreen(guiMap);
                    return;
                }
            }

        } catch (Exception e) {
            // Xaero's World Map not present or failed to open map
        }
    }


    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.XAEROS_WORLDMAP);
    }

    @Override
    public void initTabOnScreens() {
        // Register Xaero's World Map screen with custom bottom-left positioning
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Screen> guiMapClass = (Class<? extends Screen>) Class.forName("xaero.map.gui.GuiMap");

            // Register with custom positioning - bottom-left positioning
            TabsMenu.registerScreenWithCustomPosition(guiMapClass,
                (player) -> 176, // Standard GUI width
                (player) -> 166, // Standard GUI height
                TabDisplayMode.NORMAL, // Normal display mode (not inverted)
                (screen) -> 16, // Left position - 16px offset from left edge
                (screen) -> screen.height); // Bottom position

        } catch (ClassNotFoundException e) {
            // Xaero's World Map mod not available
        }
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Try to get Xaero's map texture/icon only once, then use compass fallback
        if (!textureSearchCompleted) {
            cachedTexture = getXaerosMapTexture();
            textureSearchCompleted = true;
        }

        if (cachedTexture != null) {
            // Use renderWithIcon helper to support inverted mode
            renderWithIcon(gui, x, y, hover, cachedTexture);
        } else {
            // Fallback to compass item (texture not found or doesn't exist)
            renderWithItem(gui, x, y, hover, new ItemStack(Items.COMPASS));
        }
    }

    private net.minecraft.util.Identifier getXaerosMapTexture() {
        try {
            // Use the actual Xaero's World Map icon texture
            return new net.minecraft.util.Identifier("xaeroworldmap", "icon.png");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Xaero's World Map screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("xaero") && screenName.contains("map") && screenName.contains("gui");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Xaero's World Map");
    }
}
