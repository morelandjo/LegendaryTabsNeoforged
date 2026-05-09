package vodmordia.modtabs.client.tabs_menu;

// import dev.xkmc.l2tabs.tabs.contents.AttributeScreen; // Available at runtime only
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScaledItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@TabConfig(configKey = "l2AttributesTab", defaultEnabled = true, defaultOrder = 0)
public class L2AttributeTab extends ScaledItemTab {

    public L2AttributeTab() {
        super(() -> new ItemStack(Items.IRON_SWORD), 5, 4, 0.9f);
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            // Use reflection to access the constructor
            Class<?> screenClass = Class.forName("dev.xkmc.l2tabs.tabs.contents.AttributeScreen");
            
            // Try no-arg constructor first
            try {
                java.lang.reflect.Constructor<?> constructor = screenClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object screen = constructor.newInstance();
                Minecraft.getInstance().setScreen((Screen) screen);
                return;
            } catch (NoSuchMethodException e) {
                // Fall through to try other constructors
            }
            
            // If no-arg fails, try all available constructors
            java.lang.reflect.Constructor<?>[] constructors = screenClass.getDeclaredConstructors();
            for (java.lang.reflect.Constructor<?> constructor : constructors) {
                constructor.setAccessible(true);
                Class<?>[] paramTypes = constructor.getParameterTypes();
                
                if (paramTypes.length == 0) {
                    Object screen = constructor.newInstance();
                    Minecraft.getInstance().setScreen((Screen) screen);
                    return;
                } else if (paramTypes.length == 1 && paramTypes[0].equals(Component.class)) {
                    Object screen = constructor.newInstance(Component.translatable("l2library.attributes.title"));
                    Minecraft.getInstance().setScreen((Screen) screen);
                    return;
                } else if (paramTypes.length == 2 && paramTypes[0].equals(Component.class) && paramTypes[1].equals(int.class)) {
                    // Constructor(Component, int) - try with 0 as the int parameter
                    Object screen = constructor.newInstance(Component.translatable("l2library.attributes.title"), 0);
                    Minecraft.getInstance().setScreen((Screen) screen);
                    return;
                }
            }
            
            
        } catch (Exception e) {
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.l2AttributesTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.L2_ATTRIBUTES);
    }


    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> screenClass = Class.forName("dev.xkmc.l2tabs.tabs.contents.AttributeScreen");
            return screenClass.isInstance(currentScreen);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.l2_attributes.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .registerAllTabs("dev.xkmc.l2tabs.tabs.contents.AttributeScreen");
    }
}