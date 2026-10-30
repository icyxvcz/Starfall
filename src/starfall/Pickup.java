package starfall;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** A bonus that drifts down and is collected by flying into it. */
final class Pickup {
    double x;
    double y;
    final PickupType type;
    final double radius = 14;
    private double spin;

    Pickup(PickupType type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.spin = Math.random() * Math.PI;
    }

    boolean update(double dt) {
        spin += dt * 2.2;
        y += 82 * dt;
        x += Math.sin(spin * 0.8) * 26 * dt;
        return y < Game.HEIGHT + 30;
    }

    Color color() {
        if (type == PickupType.WEAPON) {
            return new Color(104, 236, 255);
        }
        if (type == PickupType.SHIELD) {
            return new Color(110, 245, 176);
        }
        return new Color(255, 108, 180);
    }

    String letter() {
        if (type == PickupType.WEAPON) {
            return "W";
        }
        if (type == PickupType.SHIELD) {
            return "S";
        }
        return "+";
    }

    void draw(Graphics2D g, Font font) {
        Color c = color();
        AffineTransform spinNow = AffineTransform.getRotateInstance(spin, x, y);
        Shape box = spinNow.createTransformedShape(
                new RoundRectangle2D.Double(x - radius, y - radius, radius * 2, radius * 2, 8, 8));

        g.setColor(Fx.alpha(c, 40));
        g.fill(spinNow.createTransformedShape(new RoundRectangle2D.Double(
                x - radius * 1.8, y - radius * 1.8, radius * 3.6, radius * 3.6, 12, 12)));

        Fx.glow(g, box, c, 1.6f);
        g.setColor(new Color(12, 18, 36, 220));
        g.fill(box);
        g.setColor(c);
        g.setStroke(new BasicStroke(1.8f));
        g.draw(box);

        Rectangle2D bounds = font.getStringBounds(letter(), new FontRenderContext(null, true, true));
        g.setFont(font);
        g.setColor(c);
        g.drawString(letter(), (float) (x - bounds.getWidth() / 2),
                (float) (y - bounds.getHeight() / 2 - bounds.getY()));
    }
}
