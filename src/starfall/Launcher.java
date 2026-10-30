package starfall;

/**
 * Builds the window on Swing's event thread.
 *
 * Java's windowing library (Swing) wants its windows created on one specific
 * thread, so this tiny Runnable hands the work over to it.
 */
final class Launcher implements Runnable {
    @Override
    public void run() {
        new Game().launch();
    }
}
