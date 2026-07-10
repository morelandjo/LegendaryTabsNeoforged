package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.lang.reflect.Method;

/**
 * Tab for SDM Shop (sdmshop, 7.x+ rebrand of the older sdmshoprework). Mirrors the
 * keybind path in {@code SDMShopClient.keyInput}: reflectively invokes
 * {@code SDMShopClient.openGui(ResourceLocation)} with the {@code "server:auto_shop_open"}
 * sentinel. That goes through an async server round-trip ({@code AsyncClientTasks.openShop})
 * which on response calls the no-arg {@code SDMShopClient.openGui()} → dispatches to
 * {@code ModernShopScreen} / {@code MainShopScreen} based on the {@code GUI_STYLE} config.
 *
 * Going through the round-trip (rather than the simpler no-arg {@code openGui()}) is
 * deliberate: the server populates {@code SDMShopClient.CurrentShop} as part of the
 * response, and the screens read it during {@code onConstruct} — skipping the round-trip
 * leaves {@code CurrentShop = null} and the screen opens empty.
 *
 * The shop screen is an FTB Library {@code BaseScreen} wrapped in
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
            Class<?> clientClass = ClassCache.resolve(ScreenClasses.SDM_SHOP_CLIENT);
            if (clientClass == null) return;
            // openGui(ResourceLocation) — same path the keybind takes. We construct the
            // "server:auto_shop_open" sentinel directly rather than reading
            // SDMShopConstants.AUTO_SHOP_OPEN, so the call works even if that constants
            // field gets renamed in a future update.
            ResourceLocation shopId = new ResourceLocation(
                    ScreenClasses.SDM_SHOP_AUTO_OPEN_NAMESPACE,
                    ScreenClasses.SDM_SHOP_AUTO_OPEN_PATH);
            Method openGui = clientClass.getMethod("openGui", ResourceLocation.class);
            openGui.invoke(null, shopId);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening SDM Shop: " + e.getMessage());
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
        return wrapped != null && wrapped.startsWith("net.sixik.sdmshop.");
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
