package starfall;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * A simple window that explains a setup problem in friendly language.
 *
 * Used when Java is too old to run the game, so the player sees instructions
 * instead of a wall of error text.
 */
final class SetupWindow {
    private SetupWindow() {
    }

    static void show(String title, String body) {
        JTextArea area = new JTextArea(body);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(area, BorderLayout.CENTER);

        try {
            JFrame frame = new JFrame("STARFALL - setup needed");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.setSize(520, 290);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Throwable noWindowPossible) {
            System.out.println(title);
            System.out.println(body);
        }
    }
}
