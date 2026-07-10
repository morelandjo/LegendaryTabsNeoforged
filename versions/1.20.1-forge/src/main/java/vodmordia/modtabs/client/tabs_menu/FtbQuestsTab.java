package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.FTBQuestsInspector;

@TabConfig(configKey = "ftbQuestsTab", defaultEnabled = true, defaultOrder = 0)
public class FtbQuestsTab extends ConfigurableItemTab {

    public FtbQuestsTab() {
        super(FtbQuestsTab::getQuestBookItem, Config.Baked.ftbQuestsTabCustomIcon, "ftbQuests");
    }

    private static ItemStack getQuestBookItem() {
        Item bookItem = FTBQuestsInspector.tryGetBookItem();
        return new ItemStack(bookItem != null ? bookItem : Items.BOOK);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.ftbQuestsTabEnabled && player.level().isClientSide) {
            try {
                Class<?> ftbQuestsClientClass = Class.forName("dev.ftb.mods.ftbquests.client.FTBQuestsClient");
                java.lang.reflect.Method openGuiMethod = ftbQuestsClientClass.getMethod("openGui");
                openGuiMethod.invoke(null);
            } catch (Exception e) {
                // FTB Quests not present or failed to open GUI
            }
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.ftbQuestsTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.FTB_QUESTS);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            // Always return false so this tab is never disabled - we want it visible on all screens including FTB Quests screens
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isHomeTab(Screen currentScreen) {
        // isCurrentlyUsed is forced to false to keep this tab clickable, so the default
        // isHomeTab (=isCurrentlyUsed) never matches — the long-press-to-edit gesture
        // would never arm on FTB Quests screens. Match the screen class directly here.
        if (currentScreen == null) return false;
        String name = currentScreen.getClass().getName();
        if (name.startsWith("dev.ftb.mods.ftbquests.")) return true;
        if (vodmordia.modtabs.utils.ScreenClasses.FTB_LIBRARY_WRAPPER.equals(name)) {
            String wrapped = FtbScreenWrapperUtil.getWrappedGuiClassName(currentScreen);
            return wrapped != null && wrapped.startsWith("dev.ftb.mods.ftbquests.");
        }
        return false;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.ftb_quests.description");
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.FTB_QUESTS)) {
            // FTB Quests not installed — let FtbTeamsTab claim the shared ScreenWrapper if needed.
            return;
        }
        // When FTB Quests is present we own the shared ftblibrary ScreenWrapper registration;
        // FtbTeamsTab detects this and skips its own ScreenWrapper registration to avoid a duplicate.
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .registerAllTabs(
                vodmordia.modtabs.utils.ScreenClasses.FTB_LIBRARY_WRAPPER,
                vodmordia.modtabs.utils.ScreenClasses.FTB_QUESTS_QUEST_SCREEN,
                vodmordia.modtabs.utils.ScreenClasses.FTB_QUESTS_QUESTION_SCREEN,
                vodmordia.modtabs.utils.ScreenClasses.FTB_QUESTS_QUESTS_SCREEN,
                vodmordia.modtabs.utils.ScreenClasses.FTB_QUESTS_QUEST_SCREEN_ALT1,
                vodmordia.modtabs.utils.ScreenClasses.FTB_QUESTS_QUEST_SCREEN_ALT2
            );
    }
}
