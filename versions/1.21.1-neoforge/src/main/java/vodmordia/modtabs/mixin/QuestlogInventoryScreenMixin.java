package vodmordia.modtabs.mixin;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Suppress Questlog's inventory open button. Questlog's own
 * {@code org.infernalstudios.questlog.mixin.client.InventoryScreenMixin} mixes into
 * {@link InventoryScreen#init()} at TAIL and adds a {@code QuestlogOpenButton}
 * via {@code addRenderableWidget}.
 *
 * We can't mix INTO Questlog's mixin (Mixin rejects mixin-targets-mixin with
 * "Cannot add target ... because the target is a mixin"), so instead we also inject at
 * the TAIL of {@code InventoryScreen.init()} — injection order is unspecified but every
 * injection runs after the original method body, so by the time *both* run the children
 * list already contains the button. We scan for it by FQN string and call the public
 * {@code Screen#removeWidget} (which removes from renderables / children / narratables
 * in one shot).
 *
 * Class-name matching by string keeps this mod-decoupled — if Questlog is absent the
 * loop iterates the inventory's normal widgets and finds nothing, then exits. No
 * {@code require = 0} needed because this mixin targets vanilla {@link InventoryScreen}
 * which is always present.
 */
@Mixin(InventoryScreen.class)
public abstract class QuestlogInventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    private static final String QUESTLOG_BUTTON_FQN =
            "org.infernalstudios.questlog.client.gui.components.QuestlogOpenButton";

    public QuestlogInventoryScreenMixin(InventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        throw new UnsupportedOperationException("Mixin constructor");
    }

    // Full descriptor "init()V" — bare "init" is ambiguous without a refmap and triggers
    // "Critical injection failure: could not find any targets matching 'init'" when the
    // mixin AP doesn't emit one. The fully-qualified form resolves the no-arg lifecycle
    // override directly. Matches AbstractContainerScreenMixin's descriptor style.
    @Inject(method = "init()V", at = @At("TAIL"))
    private void modtabs$removeQuestlogButton(CallbackInfo ci) {
        List<GuiEventListener> toRemove = null;
        for (GuiEventListener child : this.children()) {
            if (child != null && QUESTLOG_BUTTON_FQN.equals(child.getClass().getName())) {
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
}
