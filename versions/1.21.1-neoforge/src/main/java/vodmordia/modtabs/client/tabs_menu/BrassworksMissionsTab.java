package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabRenderer;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@TabConfig(configKey = "brassworksMissionsTab", defaultEnabled = true, defaultOrder = 0)
public class BrassworksMissionsTab extends TabBase {
    // Use the first icon from the mission_icons.png texture atlas (16x16 at position 0,0)
    private static final ResourceLocation BRASSWORKS_ICON = ResourceLocation.fromNamespaceAndPath("brassworksmissions", "textures/gui/mission_icons.png");
    private static final int ATLAS_SIZE = 256;

    public BrassworksMissionsTab() {
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        // Render the tab background and icon using the first icon from the atlas (at 0,0)
        TabRenderer.builder()
            .withBackground()
            .withTextureIcon(BRASSWORKS_ICON, 5, 4, 0, 0, 16, 16, ATLAS_SIZE, ATLAS_SIZE)
            .render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        // Render inverted tab (for top positioning)
        TabRenderer.builder()
            .withBackground()
            .withTextureIcon(BRASSWORKS_ICON, 5, 4, 0, 0, 16, 16, ATLAS_SIZE, ATLAS_SIZE)
            .render(gui, x, y, hover, true);
    }

    @Override
    public void openTargetScreen(Player player) {
        ModTabs.LOGGER.info("BrassworksMissionsTab: openTargetScreen called");

        if (!Config.Baked.brassworksMissionsTabEnabled) {
            ModTabs.LOGGER.warn("BrassworksMissionsTab: Tab is disabled in config");
            return;
        }

        if (!player.level().isClientSide) {
            ModTabs.LOGGER.warn("BrassworksMissionsTab: Not on client side");
            return;
        }

        ModTabs.LOGGER.info("BrassworksMissionsTab: Attempting to open Brassworks Missions screen");

        try {
            // Try to send the network packet like the keybind does
            Class<?> openMissionsUIMessageClass = Class.forName("net.swzo.brassworksmissions.network.OpenMissionsUIMessage");

            // Create the packet
            Object packet = openMissionsUIMessageClass.getDeclaredConstructor().newInstance();

            // Send to server using PacketDistributor.sendToServer
            net.neoforged.neoforge.network.PacketDistributor.sendToServer((net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);

            ModTabs.LOGGER.info("BrassworksMissionsTab: Successfully sent OpenMissionsUIMessage to server");

        } catch (Exception e) {
            ModTabs.LOGGER.error("BrassworksMissionsTab: Failed to send network packet: " + e.getMessage());

            // Fallback: try to consume the keybind click directly
            try {
                ModTabs.LOGGER.info("BrassworksMissionsTab: Trying keybind consumeClick fallback");
                Class<?> keybindingClass = Class.forName("net.swzo.brassworksmissions.init.KeybindingInit");
                java.lang.reflect.Field keyField = keybindingClass.getField("OPEN_MISSIONS_UI_KEY");
                net.minecraft.client.KeyMapping keyMapping = (net.minecraft.client.KeyMapping) keyField.get(null);

                if (keyMapping != null) {
                    ModTabs.LOGGER.info("BrassworksMissionsTab: Found keybind, calling consumeClick");

                    // Simulate a key press and consume it
                    keyMapping.setDown(true);
                    boolean consumed = keyMapping.consumeClick();
                    keyMapping.setDown(false);

                    if (consumed) {
                        ModTabs.LOGGER.info("BrassworksMissionsTab: Successfully consumed keybind click");

                        // Now try to send the packet directly as the keybind would
                        try {
                            Class<?> openMissionsUIMessageClass = Class.forName("net.swzo.brassworksmissions.network.OpenMissionsUIMessage");
                            Object packet = openMissionsUIMessageClass.getDeclaredConstructor().newInstance();
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer((net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
                            ModTabs.LOGGER.info("BrassworksMissionsTab: Sent packet after consumeClick");
                        } catch (Exception packetEx) {
                            ModTabs.LOGGER.error("BrassworksMissionsTab: Failed to send packet after consumeClick: " + packetEx.getMessage());
                        }
                    } else {
                        ModTabs.LOGGER.warn("BrassworksMissionsTab: consumeClick returned false");
                    }
                } else {
                    ModTabs.LOGGER.warn("BrassworksMissionsTab: Keybind is null");
                }
            } catch (Exception keyException) {
                ModTabs.LOGGER.error("BrassworksMissionsTab: Keybind fallback also failed: " + keyException.getMessage());
            }
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.brassworksMissionsTabEnabled
                && ModIntegrationManager.isModLoaded(ModIntegration.BRASSWORKS_MISSIONS);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> uiScreenClass = Class.forName("net.swzo.brassworksmissions.client.gui.UiScreen");
            return uiScreenClass.isInstance(currentScreen);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.brassworks_missions.description");
    }

    @Override
    public void initTabOnScreens() {
        // Register Brassworks Missions screen with inverted tabs at the top
        ScreenRegistry.registerInvertedScreens("net.swzo.brassworksmissions.client.gui.UiScreen");
    }
}