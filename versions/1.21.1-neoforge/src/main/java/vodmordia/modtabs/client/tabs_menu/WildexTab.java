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
 * Tab for the Wildex Bestiary mod. Mirrors the same permission rule the mod's own
 * keybind uses ({@code WildexKeybinds#onClientTick}): when the synced/local
 * {@code requireBookForKeybind} config is on, the wildex_book must be in the player's
 * inventory; when it's off, the tab shows unconditionally so admins can disable the
 * book gate server-side and still expose the bestiary screen.
 *
 * The book item is {@code wildex:wildex_book}; opening goes through
 * {@code WildexScreenOpener.open()} so we share the mod's sound/setScreen flow.
 */
@TabConfig(configKey = "wildexTab", defaultEnabled = true, defaultOrder = 0)
public class WildexTab extends IntegrationItemTab {
    private static final ResourceLocation WILDEX_BOOK_ID =
            ResourceLocation.fromNamespaceAndPath("wildex", "wildex_book");

    private static final TabSpec SPEC = new TabSpec(
            "wildexTab",
            ModIntegration.WILDEX,
            () -> Config.Baked.wildexTabEnabled,
            "wildex",
            "wildex",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.WILDEX_SCREEN },
            new String[] { ScreenClasses.WILDEX_SCREEN }
    );

    public WildexTab() {
        super(SPEC, WildexTab::getBookIcon, Config.Baked.wildexTabCustomIcon);
    }

    private static ItemStack getBookIcon() {
        Item item = BuiltInRegistries.ITEM.get(WILDEX_BOOK_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.WRITABLE_BOOK);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && canOpenBook(player);
    }

    /**
     * Creative players bypass the book gate (matches mod intent). When the synced
     * server config has {@code requireBookForKeybind=false}, the tab shows for everyone;
     * otherwise the {@code wildex:wildex_book} item must be present in inventory or offhand.
     */
    private static boolean canOpenBook(Player player) {
        if (player.getAbilities().instabuild) return true;
        if (!requireBookForKeybind()) return true;
        return hasBook(player);
    }

    /**
     * Reads {@code WildexClientConfigView.requireBookForKeybind()} via reflection. That
     * helper picks up the synced server value when connected to a server, so the tab
     * respects multiplayer config. Defaults to {@code true} (require book) on any
     * failure so we don't accidentally show the tab to players who shouldn't see it.
     */
    private static boolean requireBookForKeybind() {
        try {
            Class<?> viewClass = ClassCache.resolve(ScreenClasses.WILDEX_CLIENT_CONFIG_VIEW);
            if (viewClass == null) return true;
            Object result = viewClass.getMethod("requireBookForKeybind").invoke(null);
            return !(result instanceof Boolean b) || b;
        } catch (Exception ignored) {
            return true;
        }
    }

    private static boolean hasBook(Player player) {
        Item book = BuiltInRegistries.ITEM.get(WILDEX_BOOK_ID);
        if (book == null || book == Items.AIR) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == book) return true;
        }
        ItemStack offhand = player.getOffhandItem();
        return !offhand.isEmpty() && offhand.getItem() == book;
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.wildexTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> openerClass = ClassCache.resolve(ScreenClasses.WILDEX_SCREEN_OPENER);
            if (openerClass == null) return;
            Method open = openerClass.getMethod("open");
            open.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Wildex screen: " + e.getMessage());
        }
    }
}
