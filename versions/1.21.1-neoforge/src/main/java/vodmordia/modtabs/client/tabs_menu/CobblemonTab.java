package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

@TabConfig(configKey = "cobblemonTab", defaultEnabled = true, defaultOrder = 0)
public class CobblemonTab extends IntegrationIconTab {
    private static final ResourceLocation POKEBALL_ICON =
            ResourceLocation.fromNamespaceAndPath("cobblemon", "textures/item/poke_balls/poke_ball.png");

    private static final String FQN_COBBLEMON_CLIENT = "com.cobblemon.mod.common.client.CobblemonClient";
    private static final String FQN_SUMMARY = "com.cobblemon.mod.common.client.gui.summary.Summary";
    private static final String FQN_NETWORK = "com.cobblemon.mod.common.CobblemonNetwork";
    private static final String FQN_NETWORK_PACKET = "com.cobblemon.mod.common.api.net.NetworkPacket";
    private static final String FQN_REQUEST_STARTER_PACKET =
            "com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket";

    private static final TabSpec SPEC = new TabSpec(
            "cobblemonTab",
            ModIntegration.COBBLEMON,
            () -> Config.Baked.cobblemonTabEnabled,
            "cobblemon",
            "cobblemon",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.COBBLEMON_SUMMARY, ScreenClasses.COBBLEMON_STARTER_SELECTION },
            new String[] { ScreenClasses.COBBLEMON_SUMMARY, ScreenClasses.COBBLEMON_STARTER_SELECTION }
    );

    public CobblemonTab() {
        super(SPEC, POKEBALL_ICON, Config.Baked.cobblemonTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.cobblemonTabEnabled || !player.level().isClientSide) return;

        try {
            Object storage = getClientStorage();
            if (storage == null) return;

            Object party = getPartyFromStorage(storage);
            if (party == null) return;

            Collection<?> slots = getSlotsAsCollection(party);
            if (slots == null) return;

            int selectedSlot = readSelectedSlot(storage);

            if (slots.stream().anyMatch(p -> p != null)) {
                openSummaryScreen(slots, selectedSlot);
            } else {
                requestStarterScreen();
            }
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Cobblemon screen: " + e.getMessage());
        }
    }

    private static Object getClientStorage() throws Exception {
        Class<?> cobblemonClient = ClassCache.resolve(FQN_COBBLEMON_CLIENT);
        if (cobblemonClient == null) return null;
        Field storageField = cobblemonClient.getDeclaredField("storage");
        storageField.setAccessible(true);
        return storageField.get(null);
    }

    private static Object getPartyFromStorage(Object storage) {
        // 1.7.x uses `party`; older builds used `myParty`.
        for (String name : new String[] { "party", "myParty" }) {
            try {
                Field f = storage.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return f.get(storage);
            } catch (NoSuchFieldException ignored) {
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * {@code slots} lives on a base class (PartyStore / PokemonStore), so walk up the
     * hierarchy. Returns a Collection regardless of whether the underlying impl is
     * List or Array-backed.
     */
    private static Collection<?> getSlotsAsCollection(Object party) {
        Class<?> c = party.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField("slots");
                f.setAccessible(true);
                Object value = f.get(party);
                if (value instanceof Collection<?> coll) return coll;
                if (value != null && value.getClass().isArray()) {
                    return java.util.Arrays.asList((Object[]) value);
                }
                return null;
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static int readSelectedSlot(Object storage) {
        try {
            Field f = storage.getClass().getDeclaredField("selectedSlot");
            f.setAccessible(true);
            Object value = f.get(storage);
            // 1.7.x defaults to -1 (no selection); treat as slot 0.
            if (value instanceof Integer i && i >= 0) return i;
        } catch (Exception ignored) {}
        return 0;
    }

    private static void openSummaryScreen(Collection<?> slots, int selectedSlot) throws Exception {
        Class<?> summaryClass = ClassCache.resolve(FQN_SUMMARY);
        Class<?> companionClass = ClassCache.resolve(FQN_SUMMARY + "$Companion");
        if (summaryClass == null || companionClass == null) return;
        Object companionInstance = summaryClass.getField("Companion").get(null);
        Method openMethod = companionClass.getMethod("open", Collection.class, boolean.class, int.class);
        openMethod.invoke(companionInstance, slots, true, selectedSlot);
    }

    /**
     * Send {@code RequestStarterScreenPacket} via {@code CobblemonNetwork.sendToServer}.
     * Falls back to invoking on the {@code INSTANCE} singleton if the method isn't
     * @JvmStatic in the loaded build.
     */
    private static void requestStarterScreen() throws Exception {
        Class<?> packetClass = ClassCache.resolve(FQN_REQUEST_STARTER_PACKET);
        Class<?> networkClass = ClassCache.resolve(FQN_NETWORK);
        Class<?> networkPacketClass = ClassCache.resolve(FQN_NETWORK_PACKET);
        if (packetClass == null || networkClass == null || networkPacketClass == null) return;

        Object packet = packetClass.getDeclaredConstructor().newInstance();
        Method sendToServer = networkClass.getMethod("sendToServer", networkPacketClass);
        try {
            sendToServer.invoke(null, packet);
        } catch (NullPointerException npe) {
            Object instance = networkClass.getField("INSTANCE").get(null);
            sendToServer.invoke(instance, packet);
        }
    }
}
