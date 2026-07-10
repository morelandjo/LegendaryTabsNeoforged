package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Tab for Mapwright. Mirrors the mod's no-scoping / no-pins keybind path in
 * {@code InputListener.tick}: snap {@code MapwrightClient.targetPanningPosition} to the
 * player's current {@code (x, z)} (int-truncated, matching the keybind) and then
 * {@code Minecraft.setScreen(new MapScreen(playerPosition))}.
 *
 * The keybind also handles two situational branches we intentionally don't replicate from
 * the tab: spyglass scoping (adds a pin at the raycast hit and centers there) and the
 * "average all existing pins" centering when pins already exist. Both require the player to
 * be holding/using a spyglass or to have placed pins — those are workflows the keybind owns;
 * a tab click is just "open the map at me," which maps to the third (else) branch.
 *
 * Also skips the keybind's defensive {@code reloadPageIO} call when the path ends with
 * {@code _0}. The reload primarily covers a stale state right after world load before
 * {@code ClientEvents.loadLevel} has fired — adding reflection for it (PAGE_MANAGER →
 * pageIO → getPagePath → reloadPageIO) doubles the brittleness for an edge case that
 * resolves on its own after one keybind press or one world tick.
 *
 * Everything past the field grab is reflection: Mapwright is compile-only ({@code
 * curse.maven:mapwright-1361711}) so we never link its types. The whole chain is wrapped in
 * a try/catch — a missing field/constructor (renamed by a future Mapwright build) silently
 * no-ops instead of throwing into the tab click handler.
 */
@TabConfig(configKey = "mapwrightTab", defaultEnabled = true, defaultOrder = 0)
public class MapwrightTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "mapwrightTab",
            ModIntegration.MAPWRIGHT,
            () -> Config.Baked.mapwrightTabEnabled,
            "mapwright",
            "mapwright",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.MAPWRIGHT_SCREEN },
            new String[] { ScreenClasses.MAPWRIGHT_SCREEN }
    );

    public MapwrightTab() {
        super(SPEC, MapwrightTab::getIcon, Config.Baked.mapwrightTabCustomIcon);
    }

    /** Filled-map item — thematic for a map-drawing mod, vanilla item so no PNG asset
     *  needed. Users can swap via {@code mapwrightTabCustomIcon} config. */
    private static ItemStack getIcon() {
        return new ItemStack(Items.FILLED_MAP);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.mapwrightTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> screenClass = ClassCache.resolve(ScreenClasses.MAPWRIGHT_SCREEN);
            Class<?> clientClass = ClassCache.resolve(ScreenClasses.MAPWRIGHT_CLIENT);
            Class<?> vec2dClass = ClassCache.resolve("org.joml.Vector2d");
            if (screenClass == null || clientClass == null || vec2dClass == null) return;

            // Keybind truncates to int — match exactly so the map opens at the same gridded
            // coordinate the keybind would, not an off-by-fractional-block offset.
            double px = (int) player.getX();
            double pz = (int) player.getZ();

            // MapScreen's constructor reads MapwrightClient.targetPanningPosition and lerps
            // toward it from openingPos. If we leave the target at whatever value the last
            // session/keybind set, the map animates away from the player's location on open.
            // Snap both ends of the lerp to the player's position so the open is stationary.
            Field targetField = clientClass.getField("targetPanningPosition");
            Object target = targetField.get(null);
            if (target != null) {
                Method set = vec2dClass.getMethod("set", double.class, double.class);
                set.invoke(target, px, pz);
            }

            Constructor<?> vec2dCtor = vec2dClass.getConstructor(double.class, double.class);
            Object openingPos = vec2dCtor.newInstance(px, pz);

            Constructor<?> screenCtor = screenClass.getConstructor(vec2dClass);
            Screen screen = (Screen) screenCtor.newInstance(openingPos);

            // Match the keybind's open-feedback sound so opening via tab feels identical.
            if (player.level() != null) {
                player.level().playLocalSound(
                        Minecraft.getInstance().player,
                        SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.MASTER, 0.5f, 1.0f);
            }

            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Mapwright: " + e.getMessage());
        }
    }
}
