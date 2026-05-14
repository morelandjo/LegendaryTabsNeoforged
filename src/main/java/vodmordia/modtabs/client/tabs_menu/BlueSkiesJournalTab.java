package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Method;

/**
 * Tab for Blue Skies' Blue Journal. Mirrors the mod's own journal button in
 * {@code SkiesClientEvents.openGuiEvent} — invokes the static
 * {@code BlueJournalScreen.open(boolean)} entry point which constructs a
 * {@code BlueJournalStartScreen} (or restores the player's last-viewed page) and pushes
 * it onto {@link net.minecraft.client.Minecraft#setScreen}.
 *
 * <p>Passing {@code false} for the back-button arg means closing the journal returns to
 * the previous screen rather than always opening InventoryScreen — that fits this mod's
 * tab-based navigation better than the native button's {@code true}.
 *
 * <p>Blue Skies' native button is gated on {@code ISkiesPlayer.hasUsedBlueLore()}; this
 * tab is not, so the journal is always reachable from the tab strip when Blue Skies
 * is loaded.
 *
 * <p>{@code BlueJournalScreen} is a plain {@link net.minecraft.client.gui.screens.Screen}
 * whose {@code render()} iterates {@code renderables} manually (so tabs added as
 * children do draw), but does not invoke {@code super.render()}.
 */
@TabConfig(configKey = "blueSkiesJournalTab", defaultEnabled = true, defaultOrder = 0)
public class BlueSkiesJournalTab extends IntegrationItemTab {
    private static final ResourceLocation JOURNAL_ID =
            new ResourceLocation("blue_skies", "blue_journal");

    private static final TabSpec SPEC = new TabSpec(
            "blueSkiesJournalTab",
            ModIntegration.BLUE_SKIES,
            () -> Config.Baked.blueSkiesJournalTabEnabled,
            "blueSkiesJournal",
            "blue_skies_journal",
            TabSpec.Layout.guiRelative(),
            new String[] {
                    ScreenClasses.BLUE_SKIES_JOURNAL_SCREEN,
                    ScreenClasses.BLUE_SKIES_JOURNAL_START_SCREEN,
                    ScreenClasses.BLUE_SKIES_JOURNAL_SECTION_SCREEN,
                    ScreenClasses.BLUE_SKIES_JOURNAL_ENTRY_SCREEN,
                    ScreenClasses.BLUE_SKIES_JOURNAL_SEARCH_SCREEN
            },
            new String[] {
                    ScreenClasses.BLUE_SKIES_JOURNAL_START_SCREEN,
                    ScreenClasses.BLUE_SKIES_JOURNAL_SECTION_SCREEN,
                    ScreenClasses.BLUE_SKIES_JOURNAL_ENTRY_SCREEN,
                    ScreenClasses.BLUE_SKIES_JOURNAL_SEARCH_SCREEN
            }
    );

    public BlueSkiesJournalTab() {
        super(SPEC, BlueSkiesJournalTab::getJournalIcon, Config.Baked.blueSkiesJournalTabCustomIcon);
    }

    private static ItemStack getJournalIcon() {
        Item item = BuiltInRegistries.ITEM.get(JOURNAL_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.WRITTEN_BOOK);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.blueSkiesJournalTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> journalClass = ClassCache.resolve(ScreenClasses.BLUE_SKIES_JOURNAL_SCREEN);
            if (journalClass == null) return;
            Method open = journalClass.getMethod("open", boolean.class);
            open.invoke(null, false);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Blue Skies journal: " + e.getMessage());
        }
    }
}
