package starfall;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

/*
  Small drawing helpers that give the game its neon glow look.
  Everything here is geometry and colour maths. No game rules live in Fx.
*/
final class Fx {
    private Fx() {
    }

    static Color alpha(Color color, int a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clamp255(a));
    }

    static Color mix(Color a, Color b, double t) {
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int b2 = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(clamp255(r), clamp255(g), clamp255(b2), clamp255(al));
    }

    static void glow(Graphics2D g, Shape shape, Color color, float baseWidth) {
        float[] widths = { baseWidth * 3.4f, baseWidth * 2.0f, baseWidth * 1.2f };
        int[] alphas = { 16, 30, 54 };
        for (int i = 0; i < widths.length; i++) {
            g.setColor(alpha(color, alphas[i]));
            g.setStroke(new BasicStroke(widths[i], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(shape);
        }
    }

    /* Draws text centred horizontally on cx. */
    static void text(Graphics2D g, String text, Font font, Color color, double cx, double baselineY) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (float) (cx - fm.stringWidth(text) / 2.0), (float) baselineY);
    }

    /** Big title text with a glow and a white-to-colour gradient fill. */
    static void glowText(Graphics2D g, String text, Font font, Color color, double cx, double baselineY, int glowSize) {
        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text);
        Rectangle2D bounds = gv.getVisualBounds();
        float x = (float) (cx - bounds.getWidth() / 2.0 - bounds.getX());
        Shape outline = gv.getOutline(x, (float) baselineY);

        g.setColor(alpha(color, 20));
        g.setStroke(new BasicStroke(glowSize * 2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(outline);

        g.setColor(alpha(color, 34));
        g.setStroke(new BasicStroke(glowSize * 1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(outline);

        Rectangle2D b = outline.getBounds2D();
        g.setPaint(new GradientPaint(
                0f, (float) b.getMinY(), mix(color, Color.WHITE, 0.72),
                0f, (float) b.getMaxY(), color));
        g.fill(outline);
    }

    static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
