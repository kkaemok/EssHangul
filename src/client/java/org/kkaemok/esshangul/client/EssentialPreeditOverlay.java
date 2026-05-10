package org.kkaemok.esshangul.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.PreeditEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class EssentialPreeditOverlay {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ESSENTIAL_INPUT_PACKAGE = "gg.essential.gui.common.input.";
    private static final String ELEMENTA_INPUT_PACKAGE = "gg.essential.elementa.components.input.";

    private static final long COMMIT_WAIT_MS = 120L;

    private static final Map<Object, InputCompositionState> STATES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile String lastLoggedComposition = null;

    private EssentialPreeditOverlay() {
    }

    public static void onConfigChanged(boolean enabled) {
        if (enabled) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            Object focusedInput = findFocusedEssentialInput(client.screen);
            if (focusedInput != null) {
                InputCompositionState state = getState(focusedInput);
                if (state != null && state.hasInjected()) {
                    removeInjectedComposition(focusedInput, state);
                }
            }
        }

        synchronized (STATES) {
            STATES.clear();
        }
    }

    public static void onPreedit(Screen screen, PreeditEvent event) {
        if (!EsshangulConfig.isEnabled() || !isSocialScreen(screen)) {
            return;
        }

        Object focusedInput = findFocusedEssentialInput(screen);
        if (focusedInput == null) {
            return;
        }

        String composition = event == null ? "" : event.fullText();
        composition = composition == null ? "" : composition;
        maybeLogPreedit(screen, focusedInput, event, composition);

        InputCompositionState state = getOrCreateState(focusedInput);
        if (composition.isEmpty()) {
            if (state.hasInjected()) {
                state.awaitingCommit = true;
                state.awaitingCommitSince = System.currentTimeMillis();
            }
            return;
        }

        applyOrUpdateComposition(focusedInput, state, composition);
        state.awaitingCommit = false;
    }

    public static void onBeforeCharTyped(Screen screen) {
        if (!EsshangulConfig.isEnabled() || !isSocialScreen(screen)) {
            return;
        }

        Object focusedInput = findFocusedEssentialInput(screen);
        if (focusedInput == null) {
            return;
        }

        InputCompositionState state = getState(focusedInput);
        if (state == null || !state.hasInjected()) {
            return;
        }

        removeInjectedComposition(focusedInput, state);
        state.awaitingCommit = false;
    }

    public static void onAfterCharTyped(Screen screen) {
        if (!EsshangulConfig.isEnabled() || !isSocialScreen(screen)) {
            return;
        }

        Object focusedInput = findFocusedEssentialInput(screen);
        if (focusedInput == null) {
            return;
        }

        InputCompositionState state = getState(focusedInput);
        if (state != null) {
            state.awaitingCommit = false;
        }
    }

    public static void renderComposition(Object input) {
        // Kept for compatibility with old mixin class; draw-overlay path is no longer used.
    }

    public static void onClientTick(Minecraft client) {
        if (client == null || !EsshangulConfig.isEnabled() || !isSocialScreen(client.screen)) {
            return;
        }

        Object focusedInput = findFocusedEssentialInput(client.screen);
        if (focusedInput == null) {
            return;
        }

        InputCompositionState state = getState(focusedInput);
        if (state == null || !state.awaitingCommit || !state.hasInjected()) {
            return;
        }

        long elapsed = System.currentTimeMillis() - state.awaitingCommitSince;
        if (elapsed < COMMIT_WAIT_MS) {
            return;
        }

        removeInjectedComposition(focusedInput, state);
        state.awaitingCommit = false;
    }

    private static void applyOrUpdateComposition(Object input, InputCompositionState state, String composition) {
        String text = readInputText(input);

        if (!state.hasInjected()) {
            int insertIndex = resolveCursorTextIndex(input, text);
            String next = insertAt(text, insertIndex, composition);
            if (writeInputText(input, next)) {
                state.insertIndex = insertIndex;
                state.insertedComposition = composition;
            }
            return;
        }

        String replaced = replaceInjectedComposition(text, state.insertIndex, state.insertedComposition, composition);
        if (replaced != null && writeInputText(input, replaced)) {
            state.insertedComposition = composition;
            return;
        }

        state.clearInjected();
        int insertIndex = resolveCursorTextIndex(input, text);
        String next = insertAt(text, insertIndex, composition);
        if (writeInputText(input, next)) {
            state.insertIndex = insertIndex;
            state.insertedComposition = composition;
        }
    }

    private static void removeInjectedComposition(Object input, InputCompositionState state) {
        if (!state.hasInjected()) {
            return;
        }

        String text = readInputText(input);
        String removed = removeInjectedComposition(text, state.insertIndex, state.insertedComposition);
        if (removed != null && !removed.equals(text)) {
            writeInputText(input, removed);
        }
        state.clearInjected();
    }

    private static String replaceInjectedComposition(String text, int index, String oldComp, String newComp) {
        if (text == null || oldComp == null || newComp == null || oldComp.isEmpty()) {
            return null;
        }

        if (index >= 0 && index + oldComp.length() <= text.length()
                && text.regionMatches(index, oldComp, 0, oldComp.length())) {
            return text.substring(0, index) + newComp + text.substring(index + oldComp.length());
        }

        int found = text.indexOf(oldComp);
        if (found < 0) {
            return null;
        }

        return text.substring(0, found) + newComp + text.substring(found + oldComp.length());
    }

    private static String removeInjectedComposition(String text, int index, String composition) {
        if (text == null || composition == null || composition.isEmpty()) {
            return text;
        }

        if (index >= 0 && index + composition.length() <= text.length()
                && text.regionMatches(index, composition, 0, composition.length())) {
            return text.substring(0, index) + text.substring(index + composition.length());
        }

        int found = text.indexOf(composition);
        if (found < 0) {
            return text;
        }

        return text.substring(0, found) + text.substring(found + composition.length());
    }

    private static int resolveCursorTextIndex(Object input, String text) {
        int fallback = text == null ? 0 : text.length();

        try {
            Method getCursor = input.getClass().getMethod("getCursor");
            Object cursor = getCursor.invoke(input);
            if (cursor == null) {
                return fallback;
            }

            Method getLine = cursor.getClass().getMethod("getLine");
            Method getColumn = cursor.getClass().getMethod("getColumn");
            Object lineObj = getLine.invoke(cursor);
            Object colObj = getColumn.invoke(cursor);
            if (!(lineObj instanceof Number lineNum) || !(colObj instanceof Number colNum)) {
                return fallback;
            }

            return lineColumnToIndex(text, lineNum.intValue(), colNum.intValue());
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static int lineColumnToIndex(String text, int line, int column) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String[] lines = text.split("\n", -1);
        int clampedLine = Math.max(0, Math.min(line, lines.length - 1));

        int index = 0;
        for (int i = 0; i < clampedLine; i++) {
            index += lines[i].length() + 1;
        }

        int clampedColumn = Math.max(0, Math.min(column, lines[clampedLine].length()));
        index += clampedColumn;
        return Math.max(0, Math.min(index, text.length()));
    }

    private static String insertAt(String text, int index, String part) {
        if (text == null) {
            text = "";
        }
        if (part == null || part.isEmpty()) {
            return text;
        }

        int clamped = Math.max(0, Math.min(index, text.length()));
        return text.substring(0, clamped) + part + text.substring(clamped);
    }

    private static boolean writeInputText(Object input, String text) {
        try {
            Method setText = findMethodInHierarchy(input.getClass(), "setText", String.class);
            if (setText == null) {
                return false;
            }
            setText.setAccessible(true);
            setText.invoke(input, text);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String readInputText(Object input) {
        try {
            Method getText = findMethodInHierarchy(input.getClass(), "getText");
            if (getText == null) {
                return "";
            }
            getText.setAccessible(true);
            Object value = getText.invoke(input);
            return value instanceof String text ? text : "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static InputCompositionState getOrCreateState(Object input) {
        synchronized (STATES) {
            return STATES.computeIfAbsent(input, key -> new InputCompositionState());
        }
    }

    private static InputCompositionState getState(Object input) {
        synchronized (STATES) {
            return STATES.get(input);
        }
    }

    private static Object findFocusedEssentialInput(Screen screen) {
        if (screen == null) {
            return null;
        }

        try {
            Method getWindow = screen.getClass().getMethod("getWindow");
            Object window = getWindow.invoke(screen);
            if (window == null) {
                return null;
            }

            Method getFocusedComponent = window.getClass().getMethod("getFocusedComponent");
            Object focusedComponent = getFocusedComponent.invoke(window);
            if (focusedComponent == null) {
                return null;
            }

            return findInputComponentOrParent(focusedComponent);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object findInputComponentOrParent(Object component) {
        Object cursor = component;
        while (cursor != null) {
            String className = cursor.getClass().getName();
            if (className.startsWith(ESSENTIAL_INPUT_PACKAGE) || className.startsWith(ELEMENTA_INPUT_PACKAGE)) {
                return cursor;
            }

            try {
                Method getParent = cursor.getClass().getMethod("getParent");
                cursor = getParent.invoke(cursor);
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    private static Method findMethodInHierarchy(Class<?> startClass, String methodName, Class<?>... parameterTypes) {
        Class<?> cursor = startClass;
        while (cursor != null) {
            try {
                return cursor.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean isSocialScreen(Screen screen) {
        return EssentialImeBridge.isSocialScreen(screen);
    }

    private static void maybeLogPreedit(Screen screen, Object focusedInput, PreeditEvent event, String composition) {
        if (composition != null && composition.equals(lastLoggedComposition)) {
            return;
        }
        lastLoggedComposition = composition;
        try {
            String screenName = screen == null ? "null" : screen.getClass().getName();
            String inputName = focusedInput == null ? "null" : focusedInput.getClass().getName();
            int caret = event == null ? -1 : event.caretPosition();
            LOGGER.info("[ESSHANGUL] preedit='{}' caret={} input={} screen={}",
                    composition, caret, inputName, screenName);
        } catch (Throwable ignored) {
        }
    }

    private static final class InputCompositionState {
        private int insertIndex = -1;
        private String insertedComposition = "";
        private boolean awaitingCommit;
        private long awaitingCommitSince;

        private boolean hasInjected() {
            return insertedComposition != null && !insertedComposition.isEmpty() && insertIndex >= 0;
        }

        private void clearInjected() {
            insertIndex = -1;
            insertedComposition = "";
        }
    }
}
