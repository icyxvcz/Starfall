package starfall;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/* One background star. Stars drift downwards and wrap back to the top. */
final class Star {
    private double x;
    private double y;
    private final double speed;
    private final double size;
    private final int brightness;

    Star(double x, double y) {
        this.x = x;
        this.y = y;
        this.speed = 14 + Math.random() * 95;
        this.size = 0.7 + Math.random() * 1.8;
        this.brightness = 55 + (int) (Math.random() * 150);
    }

    void update(double dt) {
        y += speed * dt;
        if (y > Game.HEIGHT + 4) {
            y = -4;
            x = Math.random() * Game.WIDTH;
        }
    }

    void draw(Graphics2D g) {  
        g.setColor(new Color(198, 226, 255, brightness));
        g.fill(new Ellipse2D.Double(x, y, size, size));
    }
}
