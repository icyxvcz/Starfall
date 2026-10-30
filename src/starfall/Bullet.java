package starfall;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/* A single travelling shot. Friendly shots go up, enemy shots come down. */
final class Bullet {
    double x;
    double y;
    double vx;
    double vy;
    final double radius;
    final double damage;
    final boolean friendly;
    final Color color;
    private double life = 3.0;

    Bullet(double x, double y, double vx, double vy, double radius, double damage, boolean friendly, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.damage = damage;
        this.friendly = friendly;
        this.color = color;
    }

    boolean update(double dt) {
        x += vx * dt;
        y += vy * dt;
        life -= dt;
        return life > 0 && y > -40 && y < Game.HEIGHT + 40 && x > -50 && x < Game.WIDTH + 50;
    }

    boolean hits(double cx, double cy, double cr) {
        double dx = x - cx;
        double dy = y - cy;
        double r = radius + cr;
        return dx * dx + dy * dy <= r * r;
    }

    void draw(Graphics2D g) {
        if (friendly) {
            // The bullet picture, if there is one. It is a tall, narrow icon, so
            // it is drawn a little above the collision point to look like it is
            // flying nose-first.
            double length = radius * 7.0;
            if (Art.image(Art.BULLET) != null) {
                g.setColor(Fx.alpha(color, 60));
                g.fill(new Ellipse2D.Double(x - radius * 2.0, y - length * 0.62,
                        radius * 4.0, length * 0.78));
                Art.drawTinted(g, Art.BULLET, x, y - length * 0.20, length, color);
                Art.drawTinted(g, Art.BULLET, x, y - length * 0.20, length * 0.55, Color.WHITE);
                return;
            }

            double tail = 16;
            g.setStroke(new BasicStroke((float) radius * 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Fx.alpha(color, 55));
            g.draw(new Line2D.Double(x, y, x, y + tail));
            g.setColor(Fx.alpha(color, 120));
            g.draw(new Line2D.Double(x, y, x, y + tail * 0.5));
            g.setColor(new Color(240, 255, 255));
            g.fill(new Ellipse2D.Double(x - radius * 0.75, y - radius * 1.5, radius * 1.5, radius * 3));
            g.setColor(Fx.alpha(color, 90));
            g.fill(new Ellipse2D.Double(x - radius * 1.8, y - radius * 2.2, radius * 3.6, radius * 4.4));
        } else {
            g.setColor(Fx.alpha(color, 45));
            g.fill(new Ellipse2D.Double(x - radius * 2.4, y - radius * 2.4, radius * 4.8, radius * 4.8));
            g.setColor(color);
            g.fill(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
            g.setColor(new Color(255, 240, 250));
            g.fill(new Ellipse2D.Double(x - radius * 0.45, y - radius * 0.45, radius * 0.9, radius * 0.9));
        }
    }
}
