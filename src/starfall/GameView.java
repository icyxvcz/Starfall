package starfall;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * The game's drawing surface.
 *
 * Swing calls paintComponent whenever the window needs repainting; this simply
 * hands a fresh Graphics2D to the game. It is its own class (rather than an
 * anonymous one inside Game) so the compiled code is one tidy file.
 */
final class GameView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final transient Game game;

    GameView(Game game) {
        this.game = game;
        setPreferredSize(new java.awt.Dimension(Game.WIDTH, Game.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        game.render((Graphics2D) graphics.create());
    }
}
