package com.example.virtualhand;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class VirtualHandManager {
    private static final String MOD_ID = "virtualhand";
    private static final int MARGIN = 6;
    private static final int ENTRY_GAP = 6;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int ENTRY_COLOR = 0xFFC9A0FF;

    private static final List<String> activeHand = new ArrayList<>();
    private static String stringTarget = null;

    private static KeyMapping selectAlphaKey;
    private static KeyMapping selectBetaKey;
    private static KeyMapping clearHandKey;

    private VirtualHandManager() {
    }

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "keys"));

        selectAlphaKey = registerKey("key.virtualhand.select_alpha", InputConstants.KEY_Z, category);
        selectBetaKey = registerKey("key.virtualhand.select_beta", InputConstants.KEY_X, category);
        clearHandKey = registerKey("key.virtualhand.clear_hand", InputConstants.KEY_BACKSPACE, category);

        ClientTickEvents.END_CLIENT_TICK.register(VirtualHandManager::handleKeys);
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "hand_overlay"),
                (graphics, deltaTracker) -> extract(graphics));
    }

    /** Adds the current selection to the hand. Does nothing until Z or X has been pressed. */
    public static void addTargetToHand() {
        if (stringTarget != null) {
            activeHand.add(stringTarget);
        }
    }

    private static KeyMapping registerKey(String translationKey, int keyCode, KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping(
                new KeyMapping(translationKey, InputConstants.Type.KEYSYM, keyCode, category));
    }

    private static void handleKeys(Minecraft client) {
        while (selectAlphaKey.consumeClick()) {
            stringTarget = "Alpha";
        }
        while (selectBetaKey.consumeClick()) {
            stringTarget = "Beta";
        }
        while (clearHandKey.consumeClick()) {
            activeHand.clear();
        }
    }

    private static void extract(GuiGraphicsExtractor graphics) {
        Font font = Minecraft.getInstance().font;

        String selection = "Target: " + (stringTarget == null ? "None" : stringTarget);
        graphics.text(font, selection, MARGIN, MARGIN, LABEL_COLOR, true);

        int y = MARGIN + font.lineHeight + 3;
        int x = MARGIN;
        for (String entry : activeHand) {
            graphics.text(font, entry, x, y, ENTRY_COLOR, true);
            x += font.width(entry) + ENTRY_GAP;
        }
    }
}
