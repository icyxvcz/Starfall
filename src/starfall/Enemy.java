package starfall;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Something to shoot. Three kinds, each moving and looking a little different. */
final class Enemy {
    final EnemyType type;
    double x;
    double y;
    double radius;
    int hp;
    int maxHp;
    int score;
    Color color;
    double hitFlash;
    private double vy;
    private double baseX;
    private double phase;
    private double fireTimer;

    Enemy(EnemyType type, double x, double y, double difficulty) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.baseX = x;
        this.phase = Math.random() * Math.PI * 2;

        int bonusHp = (int) Math.min(6, difficulty / 22);
        if (type == EnemyType.DRIFTER) {
            radius = 16;
            hp = 1 + bonusHp / 2;
            score = 10;
            color = new Color(255, 108, 180);
            vy = Math.min(260, 96 + difficulty * 3.5);
        } else if (type == EnemyType.ZIGZAG) {
            radius = 15;
            hp = 2 + bonusHp;
            score = 25;
            color = new Color(255, 186, 88);
            vy = Math.min(260, 122 + difficulty * 4);
        } else {
            radius = 25;
            hp = 5 + bonusHp * 2;
            score = 60;
            color = new Color(186, 130, 255);
            vy = 54;
        }
        maxHp = hp;
        fireTimer = 0.9 + Math.random();
    }

    /** Moves the enemy and returns false once it should be removed. */
    boolean update(double dt) {
        phase += dt;
        hitFlash = Math.max(0, hitFlash - dt * 5);
        fireTimer -= dt;

        if (type == EnemyType.ZIGZAG) {
            x = baseX + Math.sin(phase * 2.3) * 105;
        } else if (type == EnemyType.DRIFTER) {
            x = baseX + Math.sin(phase * 0.8) * 34;
        } else {
            x = baseX + Math.sin(phase * 0.55) * 70;
        }
        y += vy * dt;
        return y < Game.HEIGHT + 70;
    }

    boolean wantsToFire() {
        if (type != EnemyType.TANK || fireTimer > 0 || y < Game.TOP + 60 || y > Game.HEIGHT * 0.62) {
            return false;
        }
        fireTimer = 1.7 + Math.random() * 0.9;
        return true;
    }

    private Shape body() {
        Path2D.Double s = new Path2D.Double();
        double r = radius;
        if (type == EnemyType.DRIFTER) {
            s.moveTo(0, r);
            s.lineTo(-r * 0.95, -r * 0.7);
            s.lineTo(0, -r * 0.2);
            s.lineTo(r * 0.95, -r * 0.7);
            s.closePath();
        } else if (type == EnemyType.ZIGZAG) {
            s.moveTo(0, r);
            s.lineTo(r * 0.8, 0);
            s.lineTo(0, -r);
            s.lineTo(-r * 0.8, 0);
            s.closePath();
        } else {
            for (int i = 0; i < 6; i++) {
                double a = Math.PI / 6 + i * Math.PI / 3;
                double px = Math.cos(a) * r;
                double py = Math.sin(a) * r;
                if (i == 0) {
                    s.moveTo(px, py);
                } else {
                    s.lineTo(px, py);
                }
            }
            s.closePath();
        }
        double spin = type == EnemyType.ZIGZAG ? phase * 1.6 : Math.sin(phase * 1.4) * 0.16;
        AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
        transform.rotate(spin);
        return transform.createTransformedShape(s);
    }

    void draw(Graphics2D g) {
        Shape body = body();
        Fx.glow(g, body, color, 1.5f);

        g.setColor(hitFlash > 0 ? Fx.mix(color, Color.WHITE, 0.75 * hitFlash) : Fx.alpha(color, 60));
        g.fill(body);
        g.setColor(hitFlash > 0 ? Color.WHITE : color);
        g.setStroke(new BasicStroke(2f));
        g.draw(body);

        if (type == EnemyType.TANK) {
            g.setColor(Fx.alpha(color, 200));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new Ellipse2D.Double(x - radius * 0.45, y - radius * 0.45, radius * 0.9, radius * 0.9));
        }

        if (maxHp > 1 && hp < maxHp) {
            double w = radius * 1.8;
            double frac = Math.max(0, hp / (double) maxHp);
            g.setColor(new Color(255, 255, 255, 40));
            g.fill(new RoundRectangle2D.Double(x - w / 2, y - radius - 10, w, 4, 4, 4));
            g.setColor(frac > 0.4 ? new Color(120, 250, 200) : new Color(255, 120, 140));
            g.fill(new RoundRectangle2D.Double(x - w / 2, y - radius - 10, w * frac, 4, 4, 4));
        }
    }
}
