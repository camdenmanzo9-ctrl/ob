package com.example.virtualhand;

import net.fabricmc.api.ClientModInitializer;

public final class VirtualHandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VirtualHandManager.register();
        MechanicalInterfaceDetector.register();
    }
}
