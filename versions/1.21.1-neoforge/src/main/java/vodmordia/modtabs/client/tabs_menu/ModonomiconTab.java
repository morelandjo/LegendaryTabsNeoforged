package vodmordia.modtabs.client.tabs_menu;

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
 * a book id stored on a data component), so this tab mirrors the right-click open path:
 * find the first {@code ModonomiconItem} in the player's inventory/offhand, resolve its
 * {@code Book} via the static helper, and call
 * {@code BookGuiManager.openBook(BookAddress.defaultFor(book))} — the same call the item's
 * {@code use()} makes on the client side.
 *
 * Tab visibility is gated on actually having a Modonomicon book in inventory: with no
 * book present there's nothing to open, and a static "Modonomicon" tab would just error
 * out. Match {@link EccentricTomeTab}'s approach there.
 *
 * Modpacks typically install several Modonomicon books at once (Theurgy, Occultism,
 * Forbidden and Arcanus, etc.). v1 of this tab opens whichever book is first in the
 * player's inventory — the per-book "one tab each" dynamic-provider approach (cf.
 * NearbyContainersProvider) can be added later if multiple books in a pack make this
 * confusing.
 *
 * Parent screens ({@code BookParentNodeScreen}, {@code BookParentIndexScreen}) override
 * {@code render()} and manually iterate {@code this.renderables} instead of calling
 * {@code super.render()}, but our tab buttons are added through {@code addRenderableWidget}
 * and land in that same list — so tabs DO draw without needing an entry in
 * {@code ClientNeoForgeEvents.onScreenRenderPost}.
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
                    ScreenClasses.MODONOMICON_BOOK_PARENT_NODE_SCREEN,
                    ScreenClasses.MODONOMICON_BOOK_PARENT_INDEX_SCREEN,
                    ScreenClasses.MODONOMICON_BOOK_ERROR_SCREEN
            },
            new String[] {
                    ScreenClasses.MODONOMICON_BOOK_PARENT_NODE_SCREEN,
                    ScreenClasses.MODONOMICON_BOOK_PARENT_INDEX_SCREEN,
                    ScreenClasses.MODONOMICON_BOOK_ERROR_SCREEN
            }
    );

    public ModonomiconTab() {
        super(SPEC, ModonomiconTab::getIcon, Config.Baked.modonomiconTabCustomIcon);
    }

    /**
     * Icon is the first Modonomicon book item we find in inventory — that way the tab
     * displays the actual book the click will open (e.g. Theurgy's grimoire vs. Occultism's
     * dictionary). Falls back to a knowledge book when there's no player or no
     * ModonomiconItem in inventory; the tab is hidden in those cases anyway via
     * {@link #isEnabled(Player)}.
     */
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
            Class<?> addressClass = ClassCache.resolve(ScreenClasses.MODONOMICON_BOOK_ADDRESS);
            if (itemClass == null || mgrClass == null || addressClass == null) return;

            Method getBook = itemClass.getMethod("getBook", ItemStack.class);
            Object book = getBook.invoke(null, stack);
            if (book == null) return;

            // BookAddress.defaultFor takes Book (interface). Look it up by name + param
            // count rather than Class.getMethod("defaultFor", book.getClass()), since
            // downstream mods register Book subclasses and getMethod doesn't match a
            // superinterface parameter.
            Method defaultFor = findStaticMethodByName(addressClass, "defaultFor", 1);
            if (defaultFor == null) return;
            Object address = defaultFor.invoke(null, book);

            Method get = mgrClass.getMethod("get");
            Object mgr = get.invoke(null);
            if (mgr == null) return;

            Method openBook = mgrClass.getMethod("openBook", addressClass);
            openBook.invoke(mgr, address);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Modonomicon book: " + e.getMessage());
        }
    }

    private static Method findStaticMethodByName(Class<?> owner, String name, int paramCount) {
        for (Method m : owner.getMethods()) {
            if (name.equals(m.getName()) && m.getParameterCount() == paramCount) {
                return m;
            }
        }
        return null;
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
