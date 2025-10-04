package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.config.Config;

public class AdvancementsTab extends SimpleTextureTab {
    // Use the advancements widgets texture sprite sheet
    private static final Identifier ADVANCEMENTS_ICON = new Identifier("minecraft", "textures/gui/advancements/widgets.png");

    public AdvancementsTab() {
        super(ADVANCEMENTS_ICON);
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Render using custom UV coordinates: icon at 27,129 with size 26x26 from 256x256 sprite sheet
        // Display as 16x16 (scaled down from 26x26 source)
        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withTextureIcon(getIconTexture(), 5, 4, 16, 16, 27, 129, 26, 26, 256, 256)
            .render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        // Render using custom UV coordinates for inverted tab
        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withTextureIcon(getIconTexture(), 5, 4, 16, 16, 27, 129, 26, 26, 256, 256)
            .render(gui, x, y, hover, true);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        AdvancementsScreen newGui = new AdvancementsScreen(minecraft.getNetworkHandler().getAdvancementHandler());
        minecraft.setScreen(newGui);
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return Config.Baked.advancementsTabEnabled;
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen instanceof AdvancementsScreen;
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip." + ModTabs.MOD_ID + ".tab.advancements.description");
    }

    @Override
    public void initTabOnScreens() {
        // Register the advancements screen with tabs displayed inverted at the top
        ScreenRegistry.registerInvertedScreens(AdvancementsScreen.class);
    }
}
