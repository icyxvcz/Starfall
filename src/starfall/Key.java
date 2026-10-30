package starfall;

/*
 The keyboard keys the game cares about.
 Each number is a "virtual key code" that Java uses to identify a key.
 For example 32 is the space bar and 37 is the left arrow.
 */
final class Key {
    private Key() {
    }

    static final int LEFT = 37;
    static final int UP = 38;
    static final int RIGHT = 39;
    static final int DOWN = 40;
    static final int SPACE = 32;
    static final int ENTER = 10;
    static final int ESCAPE = 27;
    static final int A = 65;
    static final int D = 68;
    static final int S = 83;
    static final int W = 87;
    static final int M = 77;
    static final int P = 80;
}
