package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vodmordia.modtabs.utils.ClassCache;

/**
 * Abstract base for icon-style integration tabs that are entirely described by a {@link TabSpec}.
 * Subclasses only implement {@link #openTargetScreen(Player)} (the click action).
 *
 * Replaces the boilerplate that previously appeared verbatim in every tab class:
 * isEnabled / isCurrentlyUsed / getTooltip / initTabOnScreens.
 */
@OnlyIn(Dist.CLIENT)
public abstract class IntegrationIconTab extends ConfigurableIconTab {

    protected final TabSpec spec;

    protected IntegrationIconTab(TabSpec spec, ResourceLocation defaultIcon, String customIconConfig) {
        super(defaultIcon, customIconConfig, spec.iconKey());
        this.spec = spec;
    }

    protected IntegrationIconTab(TabSpec spec, ResourceLocation defaultIcon, String customIconConfig,
                                 int u, int v, int width, int height, int textureWidth, int textureHeight) {
        super(defaultIcon, customIconConfig, spec.iconKey(), u, v, width, height, textureWidth, textureHeight);
        this.spec = spec;
    }

    @Override
    public boolean isEnabled(Player player) {
        return spec.enabledFlag().getAsBoolean() && spec.modLoaded();
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        for (String fqn : spec.currentScreenFqns()) {
            if (ClassCache.isInstance(fqn, currentScreen)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Component getTooltip() {
        return spec.buildTooltip();
    }

    @Override
    public void initTabOnScreens() {
        String[] fqns = spec.screenFqns();
        if (fqns == null || fqns.length == 0) {
            return;
        }
        TabSpec.Layout layout = spec.layout();
        ScreenRegistry.builder()
                .withDimensions(layout.dimensions().width, layout.dimensions().height)
                .withDisplayMode(layout.displayMode())
                .registerAllTabs(fqns);
    }
}
