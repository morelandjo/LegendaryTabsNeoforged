package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableItemTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Field;

/**
 * Tab for SDM Shop ({@code sdmshop} mod ID, built from the 3.x "sdmshopa" line — see
 * {@link ScreenClasses} for the master-vs-1.21.1-branch story). Mirrors the keybind in
 * {@code SDMShopClient.init}: reads {@code ConfigFile.CLIENT.style} reflectively and
 * constructs either {@code ShopPageModern} (when {@code style == true}) or {@code ShopPage}
 * (default), then calls its inherited {@code BaseScreen.openGui()} which sets the screen
 * to a fresh {@code ScreenWrapper} wrapping the page.
 *
 * There is no {@code SDMShopClient.openGui(...)} helper on this line of the mod — the
 * keybind constructs the screen directly. We do the same. If the config-style field can't
 * be read (renamed in a future build), the fallback is {@code ShopPage} which matches the
 * shipped default ({@code style = false}).
 *
 * The displayed screen is an FTB Library {@code BaseScreen} wrapped in
 * {@link ScreenClasses#FTB_LIBRARY_WRAPPER} — same host class as FTB Quests / FTB Teams.
 * {@link #isCurrentlyUsed} inspects the wrapper's {@code wrappedGui} field to distinguish
 * SDM Shop from those two so cycling skips this tab only when the shop is actually open.
 *
 * Tab-bar visibility on the shop screen relies on whichever tab owns the wrapper
 * registration: FtbQuestsTab claims it when FTB Quests is installed; FtbTeamsTab claims
 * it as a fallback when only FTB Teams is installed. This tab adds a final fallback for
 * the case where neither FTB mod is installed but SDM Shop is — same coordination pattern,
 * one rung lower in priority.
 */
@TabConfig(configKey = "sdmShopTab", defaultEnabled = true, defaultOrder = 0)
public class SdmShopTab extends ConfigurableItemTab {

    public SdmShopTab() {
        super(SdmShopTab::getIcon, Config.Baked.sdmShopTabCustomIcon, "sdmShop");
    }

    private static ItemStack getIcon() {
        return new ItemStack(Items.EMERALD);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.sdmShopTabEnabled || !player.level().isClientSide) return;
        try {
            boolean modern = readModernStyleFlag();
            String fqn = modern ? ScreenClasses.SDM_SHOP_MODERN_SCREEN : ScreenClasses.SDM_SHOP_DEFAULT_SCREEN;
            Class<?> screenClass = ClassCache.resolve(fqn);
            if (screenClass == null) {
                // Modern-screen FQN missing (mod variant without modern style) — fall back
                // to the default page so the tab still does something useful.
                screenClass = ClassCache.resolve(ScreenClasses.SDM_SHOP_DEFAULT_SCREEN);
                if (screenClass == null) return;
            }
            Object page = screenClass.getConstructor().newInstance();
            // BaseScreen.openGui() — inherited from FTB Library. Method lookup goes through
            // the screen's class hierarchy via getMethod (public-only).
            page.getClass().getMethod("openGui").invoke(page);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening SDM Shop: " + e.getMessage());
        }
    }

    /**
     * Read {@code ConfigFile.CLIENT.style} — same flag the keybind uses to pick modern vs.
     * default. Returns false on any failure (matches the shipped config default, so the
     * fallback opens the screen most users actually have configured).
     */
    private static boolean readModernStyleFlag() {
        try {
            Class<?> configClass = ClassCache.resolve(ScreenClasses.SDM_SHOP_CONFIG_FILE);
            if (configClass == null) return false;
            Field clientField = configClass.getField("CLIENT");
            Object client = clientField.get(null);
            if (client == null) return false;
            Field styleField = client.getClass().getField("style");
            return styleField.getBoolean(client);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.sdmShopTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.SDM_SHOP);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;
        if (!ScreenClasses.FTB_LIBRARY_WRAPPER.equals(currentScreen.getClass().getName())) return false;
        String wrapped = FtbScreenWrapperUtil.getWrappedGuiClassName(currentScreen);
        // The 3.x line uses `net.sixk` (typo) — distinct from 1.20.1's correctly-
        // spelled `net.sixik`. Match the 3.x typo here since this is the 1.21.1 build.
        return wrapped != null && wrapped.startsWith("net.sixk.sdmshop.");
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.sdm_shop.description");
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.SDM_SHOP)) return;
        // Only claim the FTB Library wrapper when no other FTB-using tab has — FtbQuestsTab
        // takes priority, then FtbTeamsTab, then us. Registering identical params with the
        // same screen class is harmless (finalize keeps the first), but the explicit gate
        // documents the chain and matches the FtbTeamsTab pattern.
        boolean ftbTabClaimingWrapper =
                ModIntegrationManager.isModLoaded(ModIntegration.FTB_QUESTS) ||
                ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS);
        if (ftbTabClaimingWrapper) return;
        ScreenRegistry.builder()
                .withStandardDimensions()
                .inverted()
                .registerAllTabs(ScreenClasses.FTB_LIBRARY_WRAPPER);
    }
}
