package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "l2HostilityTab", defaultEnabled = true, defaultOrder = 0)
public class L2HostilityDifficultyTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "l2HostilityTab",
            ModIntegration.L2_HOSTILITY,
            () -> Config.Baked.l2HostilityTabEnabled,
            "l2Hostility",
            "l2_hostility_difficulty",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.L2_HOSTILITY_DIFFICULTY },
            new String[] { ScreenClasses.L2_HOSTILITY_DIFFICULTY }
    );

    public L2HostilityDifficultyTab() {
        super(SPEC, () -> new ItemStack(Items.ZOMBIE_HEAD), Config.Baked.l2HostilityTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> screenClass = Class.forName(ScreenClasses.L2_HOSTILITY_DIFFICULTY);

            try {
                java.lang.reflect.Constructor<?> constructor = screenClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Minecraft.getInstance().setScreen((Screen) constructor.newInstance());
                return;
            } catch (NoSuchMethodException ignored) {}

            for (java.lang.reflect.Constructor<?> constructor : screenClass.getDeclaredConstructors()) {
                constructor.setAccessible(true);
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == 0) {
                    Minecraft.getInstance().setScreen((Screen) constructor.newInstance());
                    return;
                } else if (paramTypes.length == 1 && paramTypes[0].equals(Component.class)) {
                    Minecraft.getInstance().setScreen((Screen) constructor.newInstance(
                            Component.translatable("l2hostility.difficulty.title")));
                    return;
                }
            }
        } catch (Exception ignored) {}
    }
}
