package org.kkaemok.esshangul.client;

import com.mojang.blaze3d.platform.TextInputManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public final class EssentialImeBridge {
    private static final String SOCIAL_MENU_CLASS = "gg.essential.gui.friends.SocialMenu";
    private static final String SOCIAL_PACKAGE_PREFIX = "gg.essential.gui.friends.";
    private static final String ESSENTIAL_INPUT_PACKAGE = "gg.essential.gui.common.input.";
    private static final String ELEMENTA_INPUT_PACKAGE = "gg.essential.elementa.components.input.";

    private static boolean keepImeOpenInSocial;

    private EssentialImeBridge() {
    }

    public static void onConfigChanged(boolean enabled) {
        if (enabled) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            keepImeOpenInSocial = false;
            return;
        }

        TextInputManager textInputManager = client.textInputManager();
        if (textInputManager != null) {
            textInputManager.onTextInputFocusChange(false);
        }
        forceNativeImeMode(client, false);
        keepImeOpenInSocial = false;
    }

    public static void onEssentialInputFocusChanged(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }

        TextInputManager textInputManager = client.textInputManager();
        if (textInputManager == null) {
            return;
        }

        if (!EsshangulConfig.isEnabled()) {
            textInputManager.onTextInputFocusChange(false);
            forceNativeImeMode(client, false);
            keepImeOpenInSocial = false;
            return;
        }

        Screen screen = client.screen;
        boolean socialScreen = isSocialScreen(screen);

        if (active) {
            textInputManager.onTextInputFocusChange(true);
            forceNativeImeMode(client, true);
            if (socialScreen) {
                keepImeOpenInSocial = true;
            }
            return;
        }

        if (socialScreen) {
            // Essential's text inputs can briefly lose focus while still on Social UI.
            // Closing native text input here makes IME language switch unstable.
            keepImeOpenInSocial = true;
            forceNativeImeMode(client, true);
            return;
        }

        textInputManager.onTextInputFocusChange(false);
        forceNativeImeMode(client, false);
        keepImeOpenInSocial = false;
    }

    public static void onClientTick(Minecraft client) {
        if (client == null) {
            return;
        }

        TextInputManager textInputManager = client.textInputManager();
        if (textInputManager == null) {
            return;
        }

        if (!EsshangulConfig.isEnabled()) {
            if (keepImeOpenInSocial) {
                textInputManager.onTextInputFocusChange(false);
                forceNativeImeMode(client, false);
                keepImeOpenInSocial = false;
            }
            return;
        }

        if (!isSocialScreen(client.screen)) {
            if (keepImeOpenInSocial) {
                textInputManager.onTextInputFocusChange(false);
                forceNativeImeMode(client, false);
                keepImeOpenInSocial = false;
            }
            return;
        }

        if (isEssentialTextInputFocused(client.screen)) {
            keepImeOpenInSocial = true;
        }

        if (keepImeOpenInSocial) {
            textInputManager.onTextInputFocusChange(true);
            forceNativeImeMode(client, true);
        }
    }

    static boolean isSocialScreen(Screen screen) {
        if (screen == null) {
            return false;
        }

        String className = screen.getClass().getName();
        return SOCIAL_MENU_CLASS.equals(className) || className.startsWith(SOCIAL_PACKAGE_PREFIX);
    }

    private static boolean isEssentialTextInputFocused(Screen screen) {
        try {
            Object window = screen.getClass().getMethod("getWindow").invoke(screen);
            if (window == null) {
                return false;
            }

            Object focusedComponent = window.getClass().getMethod("getFocusedComponent").invoke(window);
            return isInputComponentOrChildOfInput(focusedComponent);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isInputComponentOrChildOfInput(Object component) {
        Object cursor = component;
        while (cursor != null) {
            String className = cursor.getClass().getName();
            if (className.startsWith(ESSENTIAL_INPUT_PACKAGE) || className.startsWith(ELEMENTA_INPUT_PACKAGE)) {
                return true;
            }
            try {
                cursor = cursor.getClass().getMethod("getParent").invoke(cursor);
            } catch (Throwable ignored) {
                return false;
            }
        }
        return false;
    }

    private static void forceNativeImeMode(Minecraft client, boolean enabled) {
        try {
            long handle = client.getWindow().handle();
            GLFW.glfwSetInputMode(handle, GLFW.GLFW_IME, enabled ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
            TextInputManager textInputManager = client.textInputManager();
            if (textInputManager != null) {
                textInputManager.notifyIMEChanged();
            }
        } catch (Throwable ignored) {
        }
    }
}
