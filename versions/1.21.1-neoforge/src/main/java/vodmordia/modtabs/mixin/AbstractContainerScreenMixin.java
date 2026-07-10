package vodmordia.modtabs.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

/**
 * Two unrelated tweaks for container screens:
 *
 * 1. While the layout editor is active, force {@code isHovering(Slot, ...)} to return
 *    false so vanilla doesn't draw slot-hover highlights or set {@code hoveredSlot} (which
 *    would render item tooltips on top of our overlay).
 * 2. Inject between the dim and the panel image in {@code renderBackground} to draw
 *    our tabs under the GUI panel, so the panel visually sits on top of any tab
 *    that overlaps it. Edit mode skips this pass and renders tabs on top via the normal
 *    widget loop, since handles need to be reachable.
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void modtabs$suppressSlotHoverWhileEditing(Slot slot, double mouseX, double mouseY,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (TabsMenu.isEditing(Minecraft.getInstance().screen)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void modtabs$drawTabsBehindPanel(GuiGraphics gui, int mouseX, int mouseY, float partialTick,
                                             CallbackInfo ci) {
        TabsMenu.renderTabsBehindPanel((AbstractContainerScreen<?>) (Object) this, gui, mouseX, mouseY, partialTick);
    }
}
