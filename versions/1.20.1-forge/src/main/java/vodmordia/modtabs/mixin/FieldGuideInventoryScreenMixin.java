package vodmordia.modtabs.mixin;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Suppress Field Guide's inventory open button. The mod's
 * {@code com.evandev.fieldguide.mixin.client.InventoryScreenMixin} mixes into
 * {@link InventoryScreen#init()} at RETURN and adds a plain {@link ImageButton} whose
 * texture is {@code fieldguide:textures/gui/fieldguide_inventory_button.png}.
 *
 * We can't mix INTO a mixin class (Mixin rejects {@code targets} pointing at a mixin), so
 * we also inject at TAIL of {@code InventoryScreen.init()}: by that point both inject runs
 * have completed and the button is on {@code children()}. We identify it by reflecting on
 * the ImageButton's {@code ResourceLocation} field and checking the namespace — matching
 * by class FQN would catch any vanilla {@link ImageButton}, including the recipe-book
 * button, which would break the inventory.
 *
 * If Field Guide is absent the loop iterates the inventory's normal widgets, finds no
 * ImageButton with a "fieldguide" texture namespace, and exits.
 */
@Mixin(InventoryScreen.class)
public abstract class FieldGuideInventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    private static final String FIELDGUIDE_NAMESPACE = "fieldguide";

    public FieldGuideInventoryScreenMixin(InventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        throw new UnsupportedOperationException("Mixin constructor");
    }

    // Full descriptor "init()V" — bare "init" is ambiguous on the SRG/Mojang boundary when
    // no refmap is generated (which currently happens after a clean build; the AP silently
    // skips writing one). The fully-qualified form lets Mixin find the no-arg lifecycle
    // override directly without needing a name translation.
    @Inject(method = "init()V", at = @At("TAIL"))
    private void modtabs$removeFieldGuideButton(CallbackInfo ci) {
        List<GuiEventListener> toRemove = null;
        for (GuiEventListener child : this.children()) {
            if (child instanceof ImageButton btn && isFieldGuideButton(btn)) {
                if (toRemove == null) toRemove = new ArrayList<>(1);
                toRemove.add(child);
            }
        }
        if (toRemove != null) {
            for (GuiEventListener w : toRemove) {
                this.removeWidget(w);
            }
        }
    }

    /**
     * Returns true when the ImageButton has at least one {@link ResourceLocation} field
     * whose namespace is {@code fieldguide}. Iterates declared fields rather than naming
     * {@code "resourceLocation"} directly so the check survives any future ImageButton
     * field rename — and so a Yarn-mapped or otherwise-remapped runtime still works.
     */
    private static boolean isFieldGuideButton(ImageButton btn) {
        try {
            for (Field f : ImageButton.class.getDeclaredFields()) {
                if (!ResourceLocation.class.isAssignableFrom(f.getType())) continue;
                f.setAccessible(true);
                Object val = f.get(btn);
                if (val instanceof ResourceLocation rl && FIELDGUIDE_NAMESPACE.equals(rl.getNamespace())) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
