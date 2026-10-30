package starfall;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/* A spark from an explosion. Fades out as it flies. */
final class Particle {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private double life;
    private final double maxLife;
    private final double size;
    private final Color color;

    Particle(double x, double y, double vx, double vy, double life, double size, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.color = color;
    }

    boolean update(double dt) {
        x += vx * dt;
        y += vy * dt;
        vx *= 0.97;
        vy *= 0.97;
        life -= dt;
        return life > 0;
    }

    void draw(Graphics2D g) {
        double t = Math.max(0, life / maxLife);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (220 * t)));
        double d = Math.max(1, size * t);
        g.fill(new Ellipse2D.Double(x - d, y - d, d * 2, d * 2));
    }
}
