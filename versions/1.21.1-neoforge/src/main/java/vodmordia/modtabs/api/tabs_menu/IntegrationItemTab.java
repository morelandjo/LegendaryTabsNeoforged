package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import vodmordia.modtabs.utils.ClassCache;

import java.util.function.Supplier;

/**
 * Item-icon counterpart to {@link IntegrationIconTab}: same boilerplate elimination, but the
 * tab renders an item stack rather than a texture. Subclasses only implement
 * {@link #openTargetScreen(Player)}.
 */
@OnlyIn(Dist.CLIENT)
public abstract class IntegrationItemTab extends ConfigurableItemTab {

    protected final TabSpec spec;

    protected IntegrationItemTab(TabSpec spec, Supplier<ItemStack> iconSupplier, String customIconConfig) {
        super(iconSupplier, customIconConfig, spec.iconKey());
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
