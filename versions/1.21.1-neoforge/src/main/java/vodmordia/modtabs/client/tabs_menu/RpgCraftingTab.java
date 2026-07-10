package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "rpgCraftingTab", defaultEnabled = true, defaultOrder = 0)
public class RpgCraftingTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "rpgCraftingTab",
            ModIntegration.RPG_CRAFTING,
            () -> Config.Baked.rpgCraftingTabEnabled,
            "rpgCrafting",
            "rpgcrafting",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.RPG_CRAFTING_HAND },
            new String[] { ScreenClasses.RPG_CRAFTING_HAND }
    );

    public RpgCraftingTab() {
        super(SPEC, () -> new ItemStack(Items.CRAFTING_TABLE), Config.Baked.rpgCraftingTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> rpgCraftingClientClass = Class.forName("com.github.theredbrain.rpgcrafting.RPGCraftingClient");
            java.lang.reflect.Method openScreenMethod = rpgCraftingClientClass.getMethod(
                    "openHandCraftingScreen", net.minecraft.client.Minecraft.class);
            openScreenMethod.invoke(null, Minecraft.getInstance());
        } catch (Exception e) {
            ModTabs.LOGGER.error("Failed to open RPG Crafting screen", e);
        }
    }
}
