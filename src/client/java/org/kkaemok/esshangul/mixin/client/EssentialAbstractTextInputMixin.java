package org.kkaemok.esshangul.mixin.client;

import org.kkaemok.esshangul.client.EssentialImeBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "gg.essential.gui.common.input.AbstractTextInput", remap = false)
public abstract class EssentialAbstractTextInputMixin {
    @Inject(
            method = "setActive(Z)Lgg/essential/gui/common/input/AbstractTextInput;",
            at = @At("TAIL"),
            require = 0,
            remap = false
    )
    private void esshangul$syncImeFocus(boolean active, CallbackInfoReturnable<Object> cir) {
        EssentialImeBridge.onEssentialInputFocusChanged(active);
    }
}
