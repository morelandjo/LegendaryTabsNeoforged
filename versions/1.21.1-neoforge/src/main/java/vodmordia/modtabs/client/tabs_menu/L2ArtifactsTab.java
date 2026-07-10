package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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

@TabConfig(configKey = "l2ArtifactsTab", defaultEnabled = true, defaultOrder = 0)
public class L2ArtifactsTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "l2ArtifactsTab",
            ModIntegration.L2_ARTIFACTS,
            () -> Config.Baked.l2ArtifactsTabEnabled,
            "l2Artifacts",
            "l2_artifacts",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.L2_ARTIFACTS_SET_EFFECT },
            new String[] { ScreenClasses.L2_ARTIFACTS_SET_EFFECT }
    );

    public L2ArtifactsTab() {
        super(SPEC, L2ArtifactsTab::getSelectItem, Config.Baked.l2ArtifactsTabCustomIcon);
    }

    private static ItemStack getSelectItem() {
        try {
            Class<?> itemsClass = Class.forName("dev.xkmc.l2artifacts.init.registrate.items.ArtifactItems");
            Field selectField = itemsClass.getField("SELECT");
            Object registryObject = selectField.get(null);
            Method getMethod = registryObject.getClass().getMethod("get");
            return new ItemStack((Item) getMethod.invoke(registryObject));
        } catch (Exception e) {
            return new ItemStack(Items.NETHER_STAR);
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> screenClass = Class.forName(ScreenClasses.L2_ARTIFACTS_SET_EFFECT);

            // Try no-arg constructor first
            try {
                java.lang.reflect.Constructor<?> constructor = screenClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Minecraft.getInstance().setScreen((Screen) constructor.newInstance());
                return;
            } catch (NoSuchMethodException ignored) {}

            // If no-arg fails, try other shapes
            for (java.lang.reflect.Constructor<?> constructor : screenClass.getDeclaredConstructors()) {
                constructor.setAccessible(true);
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == 0) {
                    Minecraft.getInstance().setScreen((Screen) constructor.newInstance());
                    return;
                } else if (paramTypes.length == 1 && paramTypes[0].equals(Component.class)) {
                    Minecraft.getInstance().setScreen((Screen) constructor.newInstance(
                            Component.translatable("l2artifacts.gui.set_effects")));
                    return;
                } else if (paramTypes.length == 2 && paramTypes[0].equals(Component.class) && paramTypes[1].equals(int.class)) {
                    Minecraft.getInstance().setScreen((Screen) constructor.newInstance(
                            Component.translatable("l2artifacts.gui.set_effects"), 0));
                    return;
                }
            }
        } catch (Exception ignored) {}
    }
}
