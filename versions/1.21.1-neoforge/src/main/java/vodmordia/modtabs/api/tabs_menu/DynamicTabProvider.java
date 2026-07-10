package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Source of transient tabs that don't fit the "register once at boot" model — e.g. one tab
 * per nearby chest. Providers run on every screen re-init after the static-tab list
 * has been assembled, and append directly to the same list, so dynamic tabs participate in
 * the existing sort, pagination, visibility, and tuck plumbing unchanged.
 */
@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface DynamicTabProvider {
    /**
     * Append zero or more {@link TabBase} instances to {@code out}. Called from
     * {@link TabsMenu#initScreenButtons} on the client thread. {@code player} and
     * {@code screen} are both non-null; the level is reachable via {@code player.level()}.
     */
    void contribute(Player player, Screen screen, List<TabBase> out);
}
