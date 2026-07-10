package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for Apothic Attributes. The mod doesn't add its own screen — its
 * {@code AttributesGui} is a {@link net.minecraft.client.gui.components.Renderable}
 * overlay attached to {@link InventoryScreen} via {@code ScreenEvent.Init.Post}, and
 * the panel is auto-shown whenever the static {@code AttributesGui.wasOpen} flag is
 * true at the moment the inventory initializes.
 *
 * So clicking this tab flips that flag via reflection and opens the inventory; the
 * mod's own listener does the rest. Same trick the mod uses internally for its
 * Curios-swap path ({@code swappedFromCurios}).
 *
 * The tab is hidden when the server has {@code ALConfig.enableAttributesGui = false},
 * matching the mod's own gating: if the panel itself can't open, neither should the tab.
 */
@TabConfig(configKey = "apothicAttributesTab", defaultEnabled = true, defaultOrder = 0)
public class ApothicAttributesTab extends IntegrationIconTab {

    // Static iron-sword PNG (extracted from vanilla assets) instead of a live ItemStack
    // icon. ItemRenderer's deferred batch flushed at end-of-frame puts item icons on top
    // of the GUI panel even though our pre-panel pass flushes early — using a plain blit
    // sidesteps that by drawing through the same texture pipeline as every other tab.
    private static final ResourceLocation IRON_SWORD_ICON =
            ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/iron_sword.png");

    private static final TabSpec SPEC = TabSpec.withoutCurrentScreen(
            "apothicAttributesTab",
            ModIntegration.APOTHIC_ATTRIBUTES,
            () -> Config.Baked.apothicAttributesTabEnabled,
            "apothicAttributes",
            "apothic_attributes",
            TabSpec.Layout.guiRelative()
    );

    public ApothicAttributesTab() {
        super(SPEC, IRON_SWORD_ICON, Config.Baked.apothicAttributesTabCustomIcon);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && isPanelEnabledServerSide();
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.apothicAttributesTabEnabled) return;
        // Pre-flip wasOpen so the mod's screen-init listener calls toggleVisibility() on the new GUI.
        writeWasOpen(true);
        Minecraft.getInstance().setScreen(new InventoryScreen(player));
    }

    /**
     * Reads {@code ALConfig.enableAttributesGui}. When the server sends false (or the field
     * isn't reachable), the tab hides — there's no point opening an inventory whose attributes
     * panel won't render.
     */
    private static boolean isPanelEnabledServerSide() {
        try {
            Class<?> cfg = ClassCache.resolve(ScreenClasses.APOTHIC_ATTRIBUTES_CONFIG);
            if (cfg == null) return true;
            Object value = cfg.getField("enableAttributesGui").get(null);
            return !(value instanceof Boolean b) || b;
        } catch (Exception ignored) {
            return true;
        }
    }

    private static void writeWasOpen(boolean value) {
        try {
            Class<?> gui = ClassCache.resolve(ScreenClasses.APOTHIC_ATTRIBUTES_GUI);
            if (gui == null) return;
            gui.getField("wasOpen").setBoolean(null, value);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error setting AttributesGui.wasOpen: " + e.getMessage());
        }
    }
}
