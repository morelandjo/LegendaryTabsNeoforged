package vodmordia.modtabs.mixins;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vodmordia.modtabs.client.events.FabricScreenEvents;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;

        if (FabricScreenEvents.onMouseClicked(screen, mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}