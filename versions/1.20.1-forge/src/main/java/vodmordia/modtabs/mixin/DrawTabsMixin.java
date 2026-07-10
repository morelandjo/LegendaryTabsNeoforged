package vodmordia.modtabs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppress Runic Skills' native tab strip — it draws horizontally above the inventory at the
 * exact position our tab strip occupies, causing visual overlap. Their MixInventoryScreen
 * already gates this on {@code l2tabs}/{@code legendarytabs} being loaded; cancelling
 * DrawTabs.render at HEAD has the same effect for ModTabs users without us having to spoof
 * a mod ID.
 *
 * Targets are referenced by FQN string so this mixin is a no-op when Runic Skills is absent.
 */
@Mixin(targets = "com.otectus.runicskills.client.gui.DrawTabs", remap = false)
public class DrawTabsMixin {

    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private static void modtabs$cancelDrawTabs(CallbackInfo ci) {
        ci.cancel();
    }
}
