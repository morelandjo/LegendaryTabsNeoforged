package vodmordia.modtabs.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.events.FabricScreenEvents;

@Mixin(HandledScreen.class)
public class AbstractContainerScreenMixin {

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderPre(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;

        // Update mouse position for tuck mode hover detection
        TabsMenu.onMouseMove(mouseX, mouseY, screen);

        if (!TabsMenu.hasCustomPositioning(screen)) {
            TabsMenu.updateButtonsPosition(screen, x, y);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderPost(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;

        // Handle special rendering for problematic screens
        FabricScreenEvents.onScreenRenderPost(screen, guiGraphics, mouseX, mouseY, partialTick);
    }
}