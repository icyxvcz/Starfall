package starfall;

import java.util.Arrays;


final class Input {
    private static final int KEY_COUNT = 512;

    private final boolean[] held = new boolean[KEY_COUNT];
    private final boolean[] pressed = new boolean[KEY_COUNT];

    void setHeld(int keyCode, boolean down) {
        if (keyCode < 0 || keyCode >= KEY_COUNT) {
            return;
        }
        if (down && !held[keyCode]) {
            pressed[keyCode] = true;
        }
        held[keyCode] = down;
    }

    boolean isHeld(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_COUNT && held[keyCode];
    }

    boolean wasPressed(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_COUNT && pressed[keyCode];
    }

    /**
     * Forgets every key. Used when the window loses focus, because a key that
     * is released while another window is active never sends us an event, and
     * would otherwise stay "held" forever and keep the ship drifting.
     */
    void clearAll() {
        Arrays.fill(held, false);
        Arrays.fill(pressed, false);
    }

    /** Called once per frame, after the game has read the input. */
    void endFrame() {
        Arrays.fill(pressed, false);
    }
}
