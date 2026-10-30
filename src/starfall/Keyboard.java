package starfall;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;

/**
 * Feeds real keyboard and mouse events into the game's Input object.
 *
 * One small translation layer: Swing talks in KeyEvents, the game talks in
 * "is this key held / was it just pressed".
 *
 * It also releases every key when the window loses focus. Otherwise a key held
 * down while another window takes over would never send its release event, and
 * the ship would keep drifting on its own.
 */
final class Keyboard implements KeyListener, MouseListener, FocusListener {
    private final Input input;
    private final JComponent surface;

    private Keyboard(Input input, JComponent surface) {
        this.input = input;
        this.surface = surface;
    }

    /** Builds the game's input plumbing and attaches it to a component. */
    static void install(Input input, JComponent surface) {
        Keyboard keyboard = new Keyboard(input, surface);
        surface.addKeyListener(keyboard);
        surface.addMouseListener(keyboard);
        surface.addFocusListener(keyboard);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        input.setHeld(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        input.setHeld(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used: the game only cares about pressed and held keys.
    }

    @Override
    public void focusGained(FocusEvent e) {
        // Nothing to restore: a fresh key press is always a fresh press.
    }

    @Override
    public void focusLost(FocusEvent e) {
        input.clearAll();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        surface.requestFocusInWindow();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Not used.
    }
}
