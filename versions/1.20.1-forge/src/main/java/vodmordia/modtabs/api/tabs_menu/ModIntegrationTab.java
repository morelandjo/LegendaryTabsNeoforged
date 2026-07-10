package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

/**
 * Base class for tabs that depend on mod integrations
 */
public abstract class ModIntegrationTab extends TabBase {

    private final ModIntegration requiredMod;

    protected ModIntegrationTab(ModIntegration requiredMod) {
        this.requiredMod = requiredMod;
    }

    @Override
    public boolean isEnabled(Player player) {
        return ModIntegrationManager.isModLoaded(requiredMod) && isTabConfigurationEnabled();
    }

    /**
     * Check if this tab is enabled in the configuration
     * Subclasses should override this to check their specific config
     */
    protected abstract boolean isTabConfigurationEnabled();

    /**
     * Get the required mod for this tab
     */
    protected ModIntegration getRequiredMod() {
        return requiredMod;
    }
}