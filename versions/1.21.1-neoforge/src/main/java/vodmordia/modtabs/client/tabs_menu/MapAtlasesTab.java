package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
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
            ResourceLocation.fromNamespaceAndPath("map_atlases", "textures/item/atlas_generic.png");

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

    @Override
    public boolean isHomeTab(Screen currentScreen) {
        // Spec uses withoutCurrentScreen so the tab stays clickable on the atlas screen
        // (acts as a refresh). The long-press-to-edit gesture keys off isHomeTab, so we
        // match the atlas screen directly here — otherwise holding the tab on its own
        // screen never enters the layout editor.
        return ClassCache.isInstance(ScreenClasses.MAP_ATLASES_OVERVIEW, currentScreen);
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
        if (Config.Baked.mapAtlasesTabEnabled && player.level().isClientSide) {
            try {
                Class<?> accessUtils = ClassCache.resolve("pepjebs.mapatlases.utils.MapAtlasesAccessUtils");
                if (accessUtils == null) return;
                ItemStack atlas = (ItemStack) accessUtils
                        .getMethod("getAtlasFromPlayerByConfig", Player.class)
                        .invoke(null, player);

                if (!ClassCache.isInstance(ScreenClasses.MAP_ATLASES_ITEM, atlas.getItem())) return;

                Class<?> networkHelper = ClassCache.resolve("net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper");
                Class<?> packetClass = ClassCache.resolve("pepjebs.mapatlases.networking.C2S2COpenAtlasScreenPacket");
                Class<?> customPacketPayload = ClassCache.resolve("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
                if (networkHelper == null || packetClass == null || customPacketPayload == null) return;

                Object packet = packetClass.getDeclaredConstructor().newInstance();
                networkHelper.getMethod("sendToServer", customPacketPayload).invoke(null, packet);
            } catch (Exception e) {
                // Map Atlases not present or failed to open atlas
            }
        }
    }
}
