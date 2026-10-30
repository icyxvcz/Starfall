package starfall;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/* The ship the player flies. */
final class Player {
    static final double RADIUS = 15;
    private static final Color BODY = new Color(104, 236, 255);
    private static final double SPEED = 450;

    double x;
    double y;
    double fireCooldown;
    double invuln;
    double shield;
    double engine;
    int weaponLevel;

    void reset() {
        x = Game.WIDTH / 2.0;
        y = Game.HEIGHT - 74;
        fireCooldown = 0;
        invuln = 1.5;
        shield = 0;
        weaponLevel = 1;
        engine = 1;
    }

    void update(double dt, Input input) {
        double dx = 0;
        double dy = 0;
        if (input.isHeld(Key.LEFT) || input.isHeld(Key.A)) {
            dx -= 1;
        }
        if (input.isHeld(Key.RIGHT) || input.isHeld(Key.D)) {
            dx += 1;
        }
        if (input.isHeld(Key.UP) || input.isHeld(Key.W)) {
            dy -= 1;
        }
        if (input.isHeld(Key.DOWN) || input.isHeld(Key.S)) {
            dy += 1;
        }
        if (dx != 0 && dy != 0) {
            dx *= 0.7071;
            dy *= 0.7071;
        }

        x += dx * SPEED * dt;
        y += dy * SPEED * dt;
        x = Math.max(RADIUS + 8, Math.min(Game.WIDTH - RADIUS - 8, x));
        y = Math.max(Game.TOP + RADIUS, Math.min(Game.HEIGHT - RADIUS - 8, y));

        fireCooldown -= dt;
        engine = 0.55 + Math.random() * 0.45;
    }

    boolean canFire() {
        return fireCooldown <= 0;
    }

    void noteFired() {
        fireCooldown = 0.15;
    }

    private Shape shape() {
        Path2D.Double s = new Path2D.Double();
        s.moveTo(0, -RADIUS * 1.25);
        s.lineTo(RADIUS * 0.92, RADIUS * 0.78);
        s.lineTo(0, RADIUS * 0.42);
        s.lineTo(-RADIUS * 0.92, RADIUS * 0.78);
        s.closePath();
        return AffineTransform.getTranslateInstance(x, y).createTransformedShape(s);
    }

    void draw(Graphics2D g) {
        double flame = 9 + engine * 13;
        Path2D.Double fire = new Path2D.Double();
        fire.moveTo(x - 6, y + RADIUS * 0.95);
        fire.lineTo(x, y + RADIUS * 0.95 + flame);
        fire.lineTo(x + 6, y + RADIUS * 0.95);
        fire.closePath();
        g.setColor(new Color(110, 210, 255, 55));
        g.fill(fire);

        Path2D.Double core = new Path2D.Double();
        core.moveTo(x - 3, y + RADIUS * 0.95);
        core.lineTo(x, y + RADIUS * 0.95 + flame * 0.62);
        core.lineTo(x + 3, y + RADIUS * 0.95);
        core.closePath();
        g.setColor(new Color(205, 248, 255, 160));
        g.fill(core);

        Shape body = shape();
        Fx.glow(g, body, BODY, 1.7f);

        // The rocketship picture if it is there, otherwise the drawn ship.
        boolean drewPicture = Art.draw(g, Art.SHIP, x, y, RADIUS * 3.35);
        if (!drewPicture) {
            Rectangle2D b = body.getBounds2D();
            g.setPaint(new GradientPaint(
                    0f, (float) b.getMinY(), new Color(238, 255, 255),
                    0f, (float) b.getMaxY(), new Color(44, 126, 214)));
            g.fill(body);
            g.setColor(new Color(9, 18, 38));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(body);

            g.setColor(new Color(196, 250, 255));
            g.fill(new Ellipse2D.Double(x - 3, y - 6, 6, 12));
        }

        if (shield > 0) {
            double r = RADIUS * 2.15;
            Path2D.Double ring = new Path2D.Double();
            for (int i = 0; i < 6; i++) {
                double a = Math.PI / 3 * i + shield * 0.9;
                double px = x + Math.cos(a) * r;
                double py = y + Math.sin(a) * r;
                if (i == 0) {
                    ring.moveTo(px, py);
                } else {
                    ring.lineTo(px, py);
                }
            }
            ring.closePath();
            Fx.glow(g, ring, new Color(110, 245, 176), 1.4f);
            g.setColor(new Color(150, 255, 205, shield < 2 ? 90 : 175));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(ring);
        }

        if (invuln > 0) {
            double r = RADIUS * 1.95;
            int alpha = (int) (95 + 55 * Math.sin(invuln * 18));
            g.setColor(new Color(140, 240, 255, alpha));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
    }
}
