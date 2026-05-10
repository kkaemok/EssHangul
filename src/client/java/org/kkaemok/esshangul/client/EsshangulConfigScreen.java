package org.kkaemok.esshangul.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EsshangulConfigScreen extends Screen {
    private final Screen parent;

    public EsshangulConfigScreen(Screen parent) {
        super(Component.literal("EssHangul"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int baseY = this.height / 2 - 20;

        this.addRenderableWidget(Button.builder(toggleLabel(), button -> {
            EsshangulConfig.setEnabled(!EsshangulConfig.isEnabled());
            button.setMessage(toggleLabel());
        }).bounds(centerX - 100, baseY, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(centerX - 100, baseY + 28, 200, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static Component toggleLabel() {
        return Component.literal("Essential Social Hangul: " + (EsshangulConfig.isEnabled() ? "ON" : "OFF"));
    }
}
