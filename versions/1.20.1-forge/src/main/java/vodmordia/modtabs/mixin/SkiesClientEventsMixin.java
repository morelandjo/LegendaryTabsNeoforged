package vodmordia.modtabs.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Suppress Blue Skies' built-in inventory tabs (Vanilla / Arcs / Perks / Journal). The
 * mod's {@code SkiesClientEvents.openGuiEvent} adds those buttons via
 * {@code ScreenEvent.Init.Post.addListener}, but the whole block is gated on
 * {@code areInventoryTabsVisible(Screen)} returning true — forcing it to return false
 * at HEAD skips all four button registrations in one shot, without us having to mixin
 * each addListener call site.
 *
 * The Arcs and Journal tabs are replaced by {@code BlueSkiesArcsTab} /
 * {@code BlueSkiesJournalTab}; the vanilla-toggle and perks tabs are dropped (the
 * tab strip already has an Inventory tab, and Perks is supporter-only cosmetic).
 *
 * Target is referenced by FQN string so this mixin is a no-op when Blue Skies is
 * absent ({@code require = 0}).
 */
@Mixin(targets = "com.legacy.blue_skies.client.events.SkiesClientEvents", remap = false)
public class SkiesClientEventsMixin {

    @Inject(
        method = "areInventoryTabsVisible",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private static void modtabs$suppressNativeInventoryTabs(Screen screen, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
