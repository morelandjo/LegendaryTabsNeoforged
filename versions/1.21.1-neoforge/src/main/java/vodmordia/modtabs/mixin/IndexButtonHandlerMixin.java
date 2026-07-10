package vodmordia.modtabs.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Suppress Completionist's Index's inventory button. The mod's
 * {@code IndexButtonHandler.onAfterInventoryScreenInit} is registered through puzzleslib's
 * {@code ScreenEvents.AFTER_INIT} and adds an {@code ImageButton} positioned next to
 * the recipe-book button on {@link InventoryScreen}. Forcing the method to return at
 * HEAD skips both the {@code recipeBookButton} lookup and the {@code addRenderableWidget}
 * call, so no button gets attached.
 *
 * Only the inventory variant is suppressed — {@code onAfterPauseScreenInit} is left
 * alone since the pause-menu placement isn't what the tab replaces.
 *
 * Method signature on NeoForge 1.21.1: {@code (Minecraft, InventoryScreen, int, int,
 * List<AbstractWidget>, UnaryOperator<AbstractWidget>, Consumer<AbstractWidget>)} —
 * different from the 1.20.1 Forge build's {@code onScreenInit$Post$1} (two Consumers,
 * generic Screen target).
 *
 * Target is referenced by FQN string so this mixin is a no-op when the mod is
 * absent ({@code require = 0}).
 */
@Mixin(targets = "fuzs.completionistsindex.client.handler.IndexButtonHandler", remap = false)
public class IndexButtonHandlerMixin {

    @Inject(
        method = "onAfterInventoryScreenInit",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private static void modtabs$suppressInventoryButton(
            Minecraft minecraft, InventoryScreen screen, int width, int height,
            List<AbstractWidget> widgets,
            UnaryOperator<AbstractWidget> addRenderableWidget,
            Consumer<AbstractWidget> addWidget,
            CallbackInfo ci) {
        ci.cancel();
    }
}
