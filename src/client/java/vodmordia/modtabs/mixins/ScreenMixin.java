package vodmordia.modtabs.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.events.FabricScreenEvents;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@Mixin(Screen.class)
public class ScreenMixin {


    @Inject(method = "init", at = @At("TAIL"))
    private void onScreenInit(CallbackInfo ci) {
        Screen screen = (Screen)(Object)this;
        FabricScreenEvents.onScreenInit(screen);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onScreenClose(CallbackInfo ci) {
        Screen screen = (Screen)(Object)this;
        FabricScreenEvents.onScreenClose(screen);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        Screen screen = (Screen)(Object)this;

        if (FabricScreenEvents.onKeyPressed(screen, keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderPre(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen screen = (Screen)(Object)this;
        FabricScreenEvents.onScreenRenderPre(screen, guiGraphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderPost(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen screen = (Screen)(Object)this;
        FabricScreenEvents.onScreenRenderPost(screen, guiGraphics, mouseX, mouseY, partialTick);
    }


}