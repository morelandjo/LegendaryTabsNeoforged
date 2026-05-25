package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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
 * Tab for klikli-dev's Modonomicon — a Patchouli-style guidebook framework. Books are
 * item-driven (each downstream mod registers its own {@code ModonomiconItem} subclass with
 * a book id stored in NBT), so this tab mirrors the right-click open path: find the first
 * {@code ModonomiconItem} in the player's inventory, resolve its {@code Book} via the
 * static helper, and call {@code BookGuiManager.openBook(book.getId())} — the same call
 * the item's {@code use()} makes on the client side.
 *
 * Note: the 1.20.1 API differs from 1.21.1 — {@code openBook} takes a {@code ResourceLocation}
 * directly here (1.21.1 wraps it in {@code BookAddress}). {@code ModonomiconItem.getBookId}
 * is private on 1.20.1 (public on 1.21.1), so we always go through
 * {@code getBook(stack).getId()} instead of trying the shorter path.
 *
 * Tab visibility is gated on actually having a Modonomicon book in inventory: with no
 * book present there's nothing to open, and a static "Modonomicon" tab would just error
 * out. Match {@link EccentricTomeTab}'s approach there.
 *
 * Modpacks typically install several Modonomicon books at once (Theurgy, Occultism,
 * Forbidden and Arcanus, etc.). v1 of this tab opens whichever book is first in the
 * player's inventory.
 *
 * {@code BookOverviewScreen.render()} calls {@code super.render()} so tabs added as
 * children draw naturally; no manual-render entry needed.
 */
@TabConfig(configKey = "modonomiconTab", defaultEnabled = true, defaultOrder = 0)
public class ModonomiconTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "modonomiconTab",
            ModIntegration.MODONOMICON,
            () -> Config.Baked.modonomiconTabEnabled,
            "modonomicon",
            "modonomicon",
            TabSpec.Layout.guiRelative(),
            new String[] {
                    ScreenClasses.MODONOMICON_BOOK_OVERVIEW_SCREEN,
                    ScreenClasses.MODONOMICON_BOOK_ERROR_SCREEN
            },
            new String[] {
                    ScreenClasses.MODONOMICON_BOOK_OVERVIEW_SCREEN,
                    ScreenClasses.MODONOMICON_BOOK_ERROR_SCREEN
            }
    );

    public ModonomiconTab() {
        super(SPEC, ModonomiconTab::getIcon, Config.Baked.modonomiconTabCustomIcon);
    }

    private static ItemStack getIcon() {
        Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null) {
            ItemStack book = findBookInInventory(player);
            if (!book.isEmpty()) {
                return book;
            }
        }
        return new ItemStack(Items.KNOWLEDGE_BOOK);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && !findBookInInventory(player).isEmpty();
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.modonomiconTabEnabled || !player.level().isClientSide) return;
        ItemStack stack = findBookInInventory(player);
        if (stack.isEmpty()) return;
        try {
            Class<?> itemClass = ClassCache.resolve(ScreenClasses.MODONOMICON_ITEM);
            Class<?> mgrClass = ClassCache.resolve(ScreenClasses.MODONOMICON_BOOK_GUI_MANAGER);
            if (itemClass == null || mgrClass == null) return;

            Method getBook = itemClass.getMethod("getBook", ItemStack.class);
            Object book = getBook.invoke(null, stack);
            if (book == null) return;

            Method getId = book.getClass().getMethod("getId");
            Object id = getId.invoke(book);
            if (!(id instanceof ResourceLocation rl)) return;

            Method get = mgrClass.getMethod("get");
            Object mgr = get.invoke(null);
            if (mgr == null) return;

            Method openBook = mgrClass.getMethod("openBook", ResourceLocation.class);
            openBook.invoke(mgr, rl);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Modonomicon book: " + e.getMessage());
        }
    }

    private static ItemStack findBookInInventory(Player player) {
        Class<?> itemClass = ClassCache.resolve(ScreenClasses.MODONOMICON_ITEM);
        if (itemClass == null) return ItemStack.EMPTY;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
