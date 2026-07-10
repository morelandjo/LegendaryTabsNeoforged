package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "bodyDamageTab", defaultEnabled = true, defaultOrder = 0)
public class BodyDamageTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "bodyDamageTab",
            ModIntegration.LEGENDARY_SURVIVAL_OVERHAUL,
            () -> Config.Baked.bodyDamageTabEnabled,
            "bodyDamage",
            "body_damage",
            new TabSpec.Layout(false, TabSpec.Layout.Dimensions.BODY_HEALTH),
            new String[] { ScreenClasses.LSO_BODY_HEALTH_SCREENS },
            new String[] { ScreenClasses.LSO_BODY_HEALTH_SCREENS }
    );

    public BodyDamageTab() {
        super(SPEC, BodyDamageTab::getFirstAidItem, Config.Baked.bodyDamageTabCustomIcon);
    }

    private static ItemStack getFirstAidItem() {
        try {
            Class<?> itemsClass = Class.forName("sfiomn.legendarysurvivaloverhaul.registry.ItemRegistry");
            Field firstAidField = itemsClass.getField("FIRST_AID_SUPPLIES");
            Object registryObject = firstAidField.get(null);
            Method getMethod = registryObject.getClass().getMethod("get");
            return new ItemStack((Item) getMethod.invoke(registryObject));
        } catch (Exception e) {
            return new ItemStack(Items.GLISTERING_MELON_SLICE);
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        // LSO has its own per-feature toggle; the body-damage screen only exists when that flag is on.
        return super.isEnabled(player) && isLocalizedBodyDamageEnabled();
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.LEGENDARY_SURVIVAL_OVERHAUL)
                || !isLocalizedBodyDamageEnabled()) {
            return;
        }
        try {
            Class<?> hooksClass = Class.forName("sfiomn.legendarysurvivaloverhaul.client.ClientHooks");
            Method openMethod = hooksClass.getMethod("openBodyHealthScreen", Player.class);
            openMethod.invoke(null, player);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Failed to open LSO body health screen via reflection", e);
        }
    }

    /**
     * Read LSO's {@code Config.Baked.localizedBodyDamageEnabled} via reflection so this
     * class doesn't carry a compile-time dependency on LSO. Returns false if LSO is
     * absent or the field is missing.
     */
    private static boolean isLocalizedBodyDamageEnabled() {
        Class<?> bakedClass = ClassCache.resolve("sfiomn.legendarysurvivaloverhaul.config.Config$Baked");
        if (bakedClass == null) return false;
        try {
            return bakedClass.getField("localizedBodyDamageEnabled").getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }
}
