package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.FTBQuestsInspector;

public class FtbQuestsTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Try to open FTB Quests main screen
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Try to use the quest book item first (more reliable)
            Item questBook = FTBQuestsInspector.tryGetBookItem();
            if (questBook != null) {
                try {
                    ItemStack bookStack = new ItemStack(questBook);
                    // Use item interaction to open the quest screen
                    bookStack.getItem().use(player.getWorld(), player, net.minecraft.util.Hand.MAIN_HAND);
                    return;
                } catch (Exception ex) {
                    // Continue to reflection fallback
                }
            }

            // Fallback: Use reflection to open the quest screen directly
            Class<?> questsScreenClass = Class.forName("dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen");
            Object questsScreen = questsScreenClass.getConstructor().newInstance();

            minecraft.setScreen((Screen) questsScreen);
        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open FTB Quests screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.FTB_QUESTS);
    }

    @Override
    public void initTabOnScreens() {
        // Register FTB Quests screen classes with inverted display at the top
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .atTop()
            .registerAllTabs(
                "dev.ftb.mods.ftblibrary.ui.ScreenWrapper", // Main FTB Quests screen
                "dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen",
                "dev.ftb.mods.ftbquests.client.gui.QuestionScreen",
                "dev.ftb.mods.ftbquests.client.gui.QuestsScreen",
                "dev.ftb.mods.ftbquests.client.screens.QuestScreen",
                "dev.ftb.mods.ftbquests.client.QuestScreen"
            );

        // Force register ScreenWrapper to override any existing registration from FTB Teams
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .atTop()
            .forceRegisterAllTabs("dev.ftb.mods.ftblibrary.ui.ScreenWrapper");
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Try to get the FTB Quests book item for rendering
        Item questBook = FTBQuestsInspector.tryGetBookItem();
        if (questBook != null) {
            renderWithItem(gui, x, y, hover, new ItemStack(questBook));
        } else {
            // Fallback to written book
            renderWithItem(gui, x, y, hover, new ItemStack(Items.WRITTEN_BOOK));
        }
    }

    @Override
    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        // Try to get the FTB Quests book item for rendering
        Item questBook = FTBQuestsInspector.tryGetBookItem();
        ItemStack stack = questBook != null ? new ItemStack(questBook) : new ItemStack(Items.WRITTEN_BOOK);

        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withItemIcon(stack, 5, 3)
            .render(gui, x, y, hover, true);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is an FTB Quests screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("ftbquests") && screenName.contains("gui");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("FTB Quests");
    }
}
