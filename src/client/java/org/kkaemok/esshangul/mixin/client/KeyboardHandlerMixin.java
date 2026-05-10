package org.kkaemok.esshangul.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.PreeditEvent;
import org.kkaemok.esshangul.client.EssentialPreeditOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "preeditCallback(JLnet/minecraft/client/input/PreeditEvent;)V", at = @At("TAIL"), require = 0)
    private void esshangul$onPreedit(long windowHandle, PreeditEvent event, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        EssentialPreeditOverlay.onPreedit(client.screen, event);
    }

    @Inject(method = "charTyped(JLnet/minecraft/client/input/CharacterEvent;)V", at = @At("HEAD"), require = 0)
    private void esshangul$beforeCharTyped(long windowHandle, CharacterEvent event, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        EssentialPreeditOverlay.onBeforeCharTyped(client.screen);
    }

    @Inject(method = "charTyped(JLnet/minecraft/client/input/CharacterEvent;)V", at = @At("TAIL"), require = 0)
    private void esshangul$afterCharTyped(long windowHandle, CharacterEvent event, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        EssentialPreeditOverlay.onAfterCharTyped(client.screen);
    }
}
