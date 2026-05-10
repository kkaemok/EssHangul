package org.kkaemok.esshangul.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class EsshangulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EsshangulConfig.load();
        ClientTickEvents.START_CLIENT_TICK.register(EssentialImeBridge::onClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            EssentialImeBridge.onClientTick(client);
            EssentialPreeditOverlay.onClientTick(client);
        });
    }
}
