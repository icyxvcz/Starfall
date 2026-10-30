package starfall;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * The entry point. Double-clicking run.bat ends up here.
 *
 * Before starting the game it checks the Java version, because the game needs
 * Java 8 or newer. If your computer is too old or has no Java at all, you get a
 * plain window with instructions instead of a confusing error.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        String version = System.getProperty("java.version", "?");
        if (!isAtLeastJava8(version)) {
            SetupWindow.show("STARFALL needs Java 8 or newer.",
                    "This computer is running Java " + version + ".\n\n"
                            + "Download a free Java (JDK 21) from:\n"
                            + "https://adoptium.net/\n\n"
                            + "Then double-click run.bat again.");
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("STARFALL needs a screen to run. Launch it with run.bat instead.");
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default look and feel is fine too.
        }
        SwingUtilities.invokeLater(new Launcher());
    }

    /** True for "1.8.0_503", "21.0.1", "17-ea" and anything newer. */
    static boolean isAtLeastJava8(String version) {
        if (version == null || version.isEmpty()) {
            return false;
        }
        String v = version.startsWith("1.") ? version.substring(2) : version;
        int end = 0;
        while (end < v.length() && Character.isDigit(v.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return true;
        }
        try {
            return Integer.parseInt(v.substring(0, end)) >= 8;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
