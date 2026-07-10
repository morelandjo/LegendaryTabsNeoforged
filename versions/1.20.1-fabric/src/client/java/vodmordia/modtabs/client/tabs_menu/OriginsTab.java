package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.config.ModTabsConfig;

/** Opens Origins' current-origin screen, matching its O keybind. */
@TabConfig(configKey = "originsTab", defaultEnabled = true, defaultOrder = 0)
public final class OriginsTab extends SimpleTextureTab {
    private static final String VIEW_SCREEN = "io.github.apace100.origins.screen.ViewOriginScreen";

    public OriginsTab() {
        super(new Identifier("origins", "icon.png"));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            Class<?> type = Class.forName(VIEW_SCREEN);
            MinecraftClient.getInstance().setScreen((Screen) type.getConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open Origins screen", e);
        }
    }

    @Override public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.originsTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.ORIGINS);
    }

    @Override public boolean isCurrentlyUsed(Screen screen) {
        return screen != null && screen.getClass().getName().equals(VIEW_SCREEN);
    }

    @Override public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.origins.description");
    }

    @Override public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().registerAllTabs(VIEW_SCREEN);
    }
}
