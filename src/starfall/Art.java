package starfall;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Loads the pictures in the images folder and draws them at whatever size the
 * game asks for.
 *
 * Each picture is read from disk once and then reused, because reading a file
 * every frame would make the game stutter. If a picture is missing, the drawing
 * helpers return false so the caller can draw its built-in shape instead - the
 * game keeps working with an empty images folder.
 */
final class Art {
    static final String SHIP = "Rocketship.png";
    static final String BULLET = "bullet.png";
    static final String HEART = "heart.png";
    static final String LOGO = "Starfall.png";

    private static final Map<String, BufferedImage> CACHE = new HashMap<String, BufferedImage>();

    private Art() {
    }

    /** Returns the picture, or null if it is not there or cannot be read. */
    static BufferedImage image(String fileName) {
        if (CACHE.containsKey(fileName)) {
            return CACHE.get(fileName);
        }

        BufferedImage picture = null;
        File file = new File("images", fileName);
        if (file.isFile()) {
            try {
                picture = ImageIO.read(file);
            } catch (IOException e) {
                picture = null;
            }
        }

        CACHE.put(fileName, picture);
        return picture;
    }

    /**
     * Draws a picture centred on (cx, cy) at the given height, keeping its
     * proportions. Returns false when the picture is not available.
     */
    static boolean draw(Graphics2D g, String fileName, double cx, double cy, double height) {
        BufferedImage picture = image(fileName);
        if (picture == null) {
            return false;
        }

        double scale = height / picture.getHeight();
        int width = (int) Math.round(picture.getWidth() * scale);
        int drawHeight = (int) Math.round(height);
        int x = (int) Math.round(cx - width / 2.0);
        int y = (int) Math.round(cy - drawHeight / 2.0);

        Object previous = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(picture, x, y, width, drawHeight, null);
        if (previous != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previous);
        }
        return true;
    }

    /** As {@link #draw}, but sized by width instead of height. */
    static boolean drawByWidth(Graphics2D g, String fileName, double cx, double cy, double width) {
        BufferedImage picture = image(fileName);
        if (picture == null) {
            return false;
        }
        return draw(g, fileName, cx, cy, width * picture.getHeight() / picture.getWidth());
    }

    /**
     * Draws a picture recoloured to a single colour.
     *
     * Used for art that is black or grayscale (an outline icon, for example):
     * recolouring keeps its shape and shading but makes it glow against the dark
     * playfield, where plain black would be invisible.
     */
    static boolean drawTinted(Graphics2D g, String fileName, double cx, double cy,
                              double height, Color tint) {
        BufferedImage tinted = tintedImage(fileName, tint);
        if (tinted == null) {
            return false;
        }
        double scale = height / tinted.getHeight();
        int width = (int) Math.round(tinted.getWidth() * scale);
        int drawHeight = (int) Math.round(height);
        int x = (int) Math.round(cx - width / 2.0);
        int y = (int) Math.round(cy - drawHeight / 2.0);

        Object previous = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(tinted, x, y, width, drawHeight, null);
        if (previous != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previous);
        }
        return true;
    }

    /**
     * Builds (once) a copy of a picture recoloured to the tint colour.
     *
     * The shape comes from the picture's transparency, not its brightness. That
     * matters for line-art icons (a black outline on see-through background):
     * tinting by brightness would turn black outlines almost invisible, while
     * using the transparency keeps the outline crisp and lets it glow on the
     * dark playfield.
     */
    private static BufferedImage tintedImage(String fileName, Color tint) {
        String key = fileName + "#" + tint.getRGB();
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }

        BufferedImage source = image(fileName);
        if (source == null) {
            CACHE.put(key, null);
            return null;
        }

        int tintRgb = (tint.getRed() << 16) | (tint.getGreen() << 8) | tint.getBlue();
        BufferedImage copy = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                if (alpha == 0) {
                    continue;
                }
                copy.setRGB(x, y, (alpha << 24) | tintRgb);
            }
        }

        CACHE.put(key, copy);
        return copy;
    }
}
