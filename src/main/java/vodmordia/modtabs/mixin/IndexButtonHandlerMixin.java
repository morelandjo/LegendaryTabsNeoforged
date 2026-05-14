package vodmordia.modtabs.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Suppress Completionist's Index's inventory button. The mod's
 * {@code IndexButtonHandler.onScreenInit$Post$1} is registered through puzzleslib's
 * {@code ScreenEvents.AFTER_INIT} and adds an {@code ImageButton} positioned next to
 * the recipe-book button on {@link net.minecraft.client.gui.screens.inventory.InventoryScreen}.
 * Forcing the method to return at HEAD skips both the {@code recipeBookButton} lookup
 * and the consumer.accept call, so no button gets attached to the inventory.
 *
 * <p>Only {@code $Post$1} (the InventoryScreen variant) is suppressed —
 * {@code $Post$2} (PauseScreen) is left alone since the pause-menu placement isn't
 * what the tab replaces.
 *
 * <p>Target is referenced by FQN string so this mixin is a no-op when the mod is
 * absent ({@code require = 0}).
 */
@Mixin(targets = "fuzs.completionistsindex.client.handler.IndexButtonHandler", remap = false)
public class IndexButtonHandlerMixin {

    @Inject(
        method = "onScreenInit$Post$1",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private static void modtabs$suppressInventoryButton(
            Minecraft minecraft, Screen screen, int width, int height,
            List<AbstractWidget> widgets,
            Consumer<AbstractWidget> addRenderableWidget,
            Consumer<AbstractWidget> addWidget,
            CallbackInfo ci) {
        ci.cancel();
    }
}
