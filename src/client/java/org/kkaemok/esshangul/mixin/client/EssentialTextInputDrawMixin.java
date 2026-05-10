package org.kkaemok.esshangul.mixin.client;

import org.kkaemok.esshangul.client.EssentialPreeditOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = {
                "gg.essential.gui.common.input.UITextInput",
                "gg.essential.gui.common.input.UIMultilineTextInput"
        },
        remap = false
)
public abstract class EssentialTextInputDrawMixin {
    @Inject(
            method = "draw(Lgg/essential/universal/UMatrixStack;)V",
            at = @At("TAIL"),
            require = 0,
            remap = false
    )
    private void esshangul$renderPreeditOverlay(Object matrixStack, CallbackInfo ci) {
        EssentialPreeditOverlay.renderComposition(this);
    }
}
