package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "mapAtlasesTab", defaultEnabled = true, defaultOrder = 0)
public class MapAtlasesTab extends IntegrationIconTab {
    private static final ResourceLocation MAP_ATLAS_ICON =
            new ResourceLocation("map_atlases", "textures/item/atlas_generic.png");

    private static final TabSpec SPEC = TabSpec.withoutCurrentScreen(
            "mapAtlasesTab",
            ModIntegration.MAP_ATLASES,
            () -> Config.Baked.mapAtlasesTabEnabled,
            "mapAtlases",
            "map_atlases",
            TabSpec.Layout.invertedTop(),
            ScreenClasses.MAP_ATLASES_OVERVIEW
    );

    public MapAtlasesTab() {
        super(SPEC, MAP_ATLAS_ICON, Config.Baked.mapAtlasesTabCustomIcon);
    }

    @Override
    public boolean isEnabled(Player player) {
        // Tab only shows when an atlas is actually present in the player's inventory.
        return super.isEnabled(player) && hasAtlas(player);
    }

    private static boolean hasAtlas(Player player) {
        try {
            Class<?> accessUtils = ClassCache.resolve("pepjebs.mapatlases.utils.MapAtlasesAccessUtils");
            if (accessUtils == null) return false;
            ItemStack atlas = (ItemStack) accessUtils
                    .getMethod("getAtlasFromPlayerByConfig", Player.class)
                    .invoke(null, player);
            return ClassCache.isInstance(ScreenClasses.MAP_ATLASES_ITEM, atlas.getItem());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.mapAtlasesTabEnabled || !player.level().isClientSide) return;
        // The 1.21.1 NeoForge build went through MapAtlasesNetworking → CustomPacketPayload, but
        // 1.20.1 doesn't have CustomPacketPayload (it's a 1.20.5+ class). AtlasOverviewScreen has a
        // public no-arg constructor on 1.20.1, so we just open it client-side directly.
        try {
            Class<?> screenClass = Class.forName("pepjebs.mapatlases.client.screen.AtlasOverviewScreen");
            Object screen = screenClass.getDeclaredConstructor().newInstance();
            net.minecraft.client.Minecraft.getInstance().setScreen((net.minecraft.client.gui.screens.Screen) screen);
        } catch (Exception e) {
            // Map Atlases not present or screen failed to open.
        }
    }
}
