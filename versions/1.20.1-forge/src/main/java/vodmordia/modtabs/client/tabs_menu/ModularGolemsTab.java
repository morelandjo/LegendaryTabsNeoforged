package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "modularGolemsTab", defaultEnabled = true, defaultOrder = 0)
public class ModularGolemsTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "modularGolemsTab",
            ModIntegration.MODULAR_GOLEMS,
            () -> Config.Baked.modularGolemsTabEnabled,
            "modularGolems",
            "modular_golems",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.MODULAR_GOLEMS_INFO },
            new String[] { ScreenClasses.MODULAR_GOLEMS_INFO }
    );

    public ModularGolemsTab() {
        super(SPEC, ModularGolemsTab::getHolderGolemItem, Config.Baked.modularGolemsTabCustomIcon);
    }

    private static ItemStack getHolderGolemItem() {
        try {
            Class<?> itemsClass = Class.forName("dev.xkmc.modulargolems.init.registrate.GolemItems");
            Field holderGolemField = itemsClass.getField("HOLDER_GOLEM");
            Object registryObject = holderGolemField.get(null);
            Method getMethod = registryObject.getClass().getMethod("get");
            return new ItemStack((Item) getMethod.invoke(registryObject));
        } catch (Exception e) {
            return new ItemStack(Items.IRON_GOLEM_SPAWN_EGG);
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> typeEnum = Class.forName("dev.xkmc.modulargolems.content.client.tracker.TrackerTab$Type");
            Object aliveType = null;
            for (Object enumConstant : typeEnum.getEnumConstants()) {
                if (enumConstant.toString().equals("ALIVE")) {
                    aliveType = enumConstant;
                    break;
                }
            }
            if (aliveType != null) {
                Method createScreenMethod = typeEnum.getMethod("createScreen");
                Object screen = createScreenMethod.invoke(aliveType);
                Minecraft.getInstance().setScreen((Screen) screen);
            }
        } catch (Exception ignored) {}
    }
}
