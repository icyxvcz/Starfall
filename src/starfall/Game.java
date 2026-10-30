package starfall;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * STARFALL - a small but complete arcade shooter.
 *
 * Run it with: java -cp out starfall.Main   (or double-click run.bat on Windows)
 *
 * How the game works, in one paragraph: the player's ship sits near the bottom
 * of the screen. Enemies fall from the top, the player shoots them for points,
 * and touching an enemy or an enemy bullet costs a life. Shooting enough of
 * them raises the difficulty and, every 500 points, the wave number. Some
 * enemies drop pickups that upgrade the gun, add a shield, or give a life back.
 */
public final class Game implements Runnable {

    // Screen layout

    static final int WIDTH = 960;
    static final int HEIGHT = 640;

    /** The playing field starts below this line so the HUD has room. */
    static final int TOP = 64;

    // The engine's moving parts

    private final Input input = new Input();
    private final Random random = new Random();
    private final Sound sound = new Sound();
    private final Player player = new Player();
    private final List<Star> stars = new ArrayList<Star>();
    private final List<Bullet> bullets = new ArrayList<Bullet>();
    private final List<Enemy> enemies = new ArrayList<Enemy>();
    private final List<Particle> particles = new ArrayList<Particle>();
    private final List<Pickup> pickups = new ArrayList<Pickup>();

    private JFrame window;
    private GameView view;

    // Score and progression

    private GameState state = GameState.TITLE;
    private int score;
    private int bestScore;
    private int lives;
    private int wave;
    private double playTime;
    private double spawnTimer;
    private double shake;
    private double flash;
    private double hitStop;
    private double bannerTimer;
    private String banner = "";
    private Font fontSmall;
    private Font fontMedium;
    private Font fontBig;
    private Font fontTitle;
    private Font fontPickup;

    /** Launching the class starts a new game. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Launcher());
    }

    private void start() {
        createFonts();

        view = new GameView(this);
        Keyboard.install(input, view);

        window = new JFrame("STARFALL");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setContentPane(view);
        window.pack();
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        view.requestFocusInWindow();

        player.reset();
        for (int i = 0; i < 130; i++) {
            stars.add(new Star(random.nextDouble() * WIDTH, random.nextDouble() * HEIGHT));
        }

        // The game loop runs on its own thread so the window stays responsive.
        Thread loop = new Thread(this, "starfall-loop");
        loop.start();
    }

    /** Creates the window and starts the game. */
    public void launch() {
        start();
    }

    private void createFonts() {
        fontSmall = new Font("Consolas", Font.BOLD, 14);
        fontMedium = new Font("Consolas", Font.BOLD, 18);
        fontBig = new Font("Consolas", Font.BOLD, 34);
        fontTitle = new Font("Consolas", Font.BOLD, 92);
        fontPickup = new Font("Consolas", Font.BOLD, 16);
        if (!fontTitle.canDisplay('A')) {
            fontSmall = new Font(Font.SANS_SERIF, Font.BOLD, 14);
            fontMedium = new Font(Font.SANS_SERIF, Font.BOLD, 18);
            fontBig = new Font(Font.SANS_SERIF, Font.BOLD, 34);
            fontTitle = new Font(Font.SANS_SERIF, Font.BOLD, 92);
            fontPickup = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        }
    }

    // The main loop

    @Override
    public void run() {
        long previous = System.nanoTime();
        while (true) {
            long now = System.nanoTime();
            double dt = (now - previous) / 1_000_000_000.0;
            previous = now;
            // After a window drag or a slow frame, ignore the huge jump.
            dt = Math.min(dt, 0.05);

            update(dt);
            input.endFrame();
            view.repaint();

            try {
                long elapsed = (System.nanoTime() - now) / 1_000_000L;
                long wait = 1000L / 60L - elapsed;
                Thread.sleep(Math.max(1, wait));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // Update

    private void update(double dt) {
        for (Star star : stars) {
            star.update(dt);
        }
        flash = Math.max(0, flash - dt * 3.5);
        bannerTimer = Math.max(0, bannerTimer - dt);

        if (input.wasPressed(Key.M)) {
            sound.setEnabled(!sound.isEnabled());
            if (!sound.isEnabled()) {
                sound.stopAll();
            }
        }

        if (state == GameState.TITLE) {
            if (input.wasPressed(Key.SPACE) || input.wasPressed(Key.ENTER)) {
                beginRun();
            }
            return;
        }

        if (state == GameState.GAME_OVER) {
            updateParticles(dt);
            if (input.wasPressed(Key.SPACE) || input.wasPressed(Key.ENTER)) {
                beginRun();
            }
            return;
        }

        if (state == GameState.PAUSED) {
            if (input.wasPressed(Key.P) || input.wasPressed(Key.SPACE) || input.wasPressed(Key.ENTER)) {
                state = GameState.PLAYING;
            }
            if (input.wasPressed(Key.ESCAPE)) {
                state = GameState.TITLE;
            }
            return;
        }

        if (input.wasPressed(Key.P) || input.wasPressed(Key.ESCAPE)) {
            state = GameState.PAUSED;
            return;
        }

        if (hitStop > 0) {
            hitStop -= dt;
            return;
        }

        playTime += dt;
        shake = Math.max(0, shake - dt * 34);
        player.update(dt, input);
        player.shield = Math.max(0, player.shield - dt);

        if (input.isHeld(Key.SPACE) && player.canFire()) {
            firePlayerWeapon();
        }

        updateBullets(dt);
        updateEnemies(dt);
        updatePickups(dt);
        updateParticles(dt);
        spawnEnemies(dt);

        if (score >= wave * 500) {
            wave++;
            banner = "WAVE " + wave;
            bannerTimer = 1.8;
            sound.play(SoundType.START);
        }
        if (score > bestScore) {
            bestScore = score;
        }
    }

    private double difficulty() {
        return Math.min(210, score / 42.0 + playTime * 0.32);
    }

    private void beginRun() {
        state = GameState.PLAYING;
        score = 0;
        lives = 3;
        wave = 1;
        playTime = 0;
        spawnTimer = 0.6;
        shake = 0;
        flash = 0;
        hitStop = 0;
        bullets.clear();
        enemies.clear();
        particles.clear();
        pickups.clear();
        player.reset();
        banner = "WAVE 1";
        bannerTimer = 1.8;
        sound.play(SoundType.START);
    }

    // Player weapon

    private void firePlayerWeapon() {
        player.noteFired();
        sound.play(SoundType.SHOOT);
        double x = player.x;
        double y = player.y - Player.RADIUS;
        Color shot = new Color(150, 244, 255);
        double speed = 720;

        switch (player.weaponLevel) {
            case 1:
                bullets.add(new Bullet(x, y, 0, -speed, 3, 1, true, shot));
                break;
            case 2:
                bullets.add(new Bullet(x - 7, y, 0, -speed, 2.6, 1, true, shot));
                bullets.add(new Bullet(x + 7, y, 0, -speed, 2.6, 1, true, shot));
                break;
            case 3:
                bullets.add(new Bullet(x, y - 4, 0, -speed, 3, 1, true, shot));
                bullets.add(new Bullet(x - 10, y, -150, -speed * 0.97, 2.4, 1, true, shot));
                bullets.add(new Bullet(x + 10, y, 150, -speed * 0.97, 2.4, 1, true, shot));
                break;
            default:
                bullets.add(new Bullet(x - 8, y, 0, -speed, 2.8, 1, true, shot));
                bullets.add(new Bullet(x + 8, y, 0, -speed, 2.8, 1, true, shot));
                bullets.add(new Bullet(x - 13, y + 4, -230, -speed * 0.92, 2.2, 1, true, shot));
                bullets.add(new Bullet(x + 13, y + 4, 230, -speed * 0.92, 2.2, 1, true, shot));
                break;
        }
    }

    // Spawning

    private void spawnEnemies(double dt) {
        spawnTimer -= dt;
        if (spawnTimer > 0) {
            return;
        }
        double diff = difficulty();
        spawnTimer = Math.max(0.26, 0.98 - diff * 0.013) * (0.72 + random.nextDouble() * 0.56);

        double margin = 70;
        double x = margin + random.nextDouble() * (WIDTH - margin * 2);
        double roll = random.nextDouble();
        double tankChance = Math.min(0.24, 0.02 + diff * 0.004);
        double zigChance = Math.min(0.45, 0.14 + diff * 0.006);

        EnemyType type;
        if (roll < tankChance) {
            type = EnemyType.TANK;
        } else if (roll < tankChance + zigChance) {
            type = EnemyType.ZIGZAG;
        } else {
            type = EnemyType.DRIFTER;
        }
        enemies.add(new Enemy(type, x, TOP - 40, diff));
    }

    // Bullets

    private void updateBullets(double dt) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            boolean alive = bullet.update(dt);

            if (alive && bullet.friendly) {
                for (int j = enemies.size() - 1; j >= 0; j--) {
                    Enemy enemy = enemies.get(j);
                    if (!bullet.hits(enemy.x, enemy.y, enemy.radius)) {
                        continue;
                    }
                    enemy.hp -= (int) bullet.damage;
                    enemy.hitFlash = 1;
                    alive = false;
                    spark(enemy.x, enemy.y, enemy.color, 5);
                    if (enemy.hp <= 0) {
                        destroyEnemy(j, true);
                    } else {
                        sound.play(SoundType.HIT);
                    }
                    break;
                }
            } else if (alive && !bullet.friendly) {
                if (bullet.hits(player.x, player.y, Player.RADIUS * 0.85)) {
                    alive = false;
                    if (player.invuln > 0) {
                        spark(bullet.x, bullet.y, bullet.color, 6);
                    } else {
                        damagePlayer();
                    }
                }
            }

            if (!alive) {
                bullets.remove(i);
            }
        }
    }

    // Enemies

    private void updateEnemies(double dt) {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (!enemy.update(dt)) {
                enemies.remove(i);
                continue;
            }
            if (enemy.wantsToFire()) {
                fireEnemyBullet(enemy);
            }
            double dx = enemy.x - player.x;
            double dy = enemy.y - player.y;
            double reach = enemy.radius + Player.RADIUS * 0.8;
            if (dx * dx + dy * dy <= reach * reach) {
                if (player.invuln > 0) {
                    continue;
                }
                destroyEnemy(i, false);
                damagePlayer();
            }
        }
    }

    private void fireEnemyBullet(Enemy enemy) {
        double dx = player.x - enemy.x;
        double dy = player.y - enemy.y;
        double length = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        double speed = Math.min(320, 185 + difficulty() * 2.0);
        bullets.add(new Bullet(enemy.x, enemy.y + enemy.radius, dx / length * speed, dy / length * speed,
                5, 1, false, new Color(255, 96, 150)));
        sound.play(SoundType.HIT);
    }

    private void destroyEnemy(int index, boolean awardPoints) {
        Enemy enemy = enemies.remove(index);
        explosion(enemy.x, enemy.y, enemy.color, enemy.type == EnemyType.TANK ? 26 : 16);
        sound.play(SoundType.EXPLODE);
        shake = Math.max(shake, enemy.type == EnemyType.TANK ? 9 : 4);
        if (awardPoints) {
            score += enemy.score;
        }
        if (awardPoints && random.nextDouble() < dropChance(enemy.type)) {
            dropPickup(enemy.x, enemy.y);
        }
    }

    private double dropChance(EnemyType type) {
        if (type == EnemyType.TANK) {
            return 0.42;
        }
        return type == EnemyType.ZIGZAG ? 0.10 : 0.05;
    }

    private void dropPickup(double x, double y) {
        double roll = random.nextDouble();
        PickupType type;
        if (roll < 0.5) {
            type = PickupType.WEAPON;
        } else if (roll < 0.85) {
            type = PickupType.SHIELD;
        } else {
            type = PickupType.LIFE;
        }
        pickups.add(new Pickup(type, x, y));
    }

    // Pickups

    private void updatePickups(double dt) {
        for (int i = pickups.size() - 1; i >= 0; i--) {
            Pickup pickup = pickups.get(i);
            if (!pickup.update(dt)) {
                pickups.remove(i);
                continue;
            }
            double dx = pickup.x - player.x;
            double dy = pickup.y - player.y;
            double reach = pickup.radius + Player.RADIUS;
            if (dx * dx + dy * dy <= reach * reach) {
                pickups.remove(i);
                collect(pickup);
            }
        }
    }

    private void collect(Pickup pickup) {
        sound.play(SoundType.PICKUP);
        spark(pickup.x, pickup.y, pickup.color(), 12);
        if (pickup.type == PickupType.WEAPON) {
            if (player.weaponLevel < 4) {
                player.weaponLevel++;
                banner = "WEAPON UP  LV " + player.weaponLevel;
            } else {
                score += 50;
                banner = "GUN MAXED  +50";
            }
        } else if (pickup.type == PickupType.SHIELD) {
            player.shield = 7;
            banner = "SHIELD UP";
        } else {
            if (lives < 5) {
                lives++;
                banner = "EXTRA LIFE";
            } else {
                score += 100;
                banner = "FULL LIVES  +100";
            }
        }
        bannerTimer = 1.4;
        flash = Math.max(flash, 0.25);
    }

    // Player damage

    private void damagePlayer() {
        if (player.shield > 0) {
            player.shield = 0;
            player.invuln = 0.9;
            sound.play(SoundType.HURT);
            spark(player.x, player.y, new Color(110, 245, 176), 16);
            shake = Math.max(shake, 8);
            banner = "SHIELD DOWN";
            bannerTimer = 1.2;
            return;
        }

        lives--;
        player.invuln = 2.0;
        player.weaponLevel = Math.max(1, player.weaponLevel - 1);
        sound.play(SoundType.HURT);
        explosion(player.x, player.y, new Color(120, 220, 255), 24);
        shake = 14;
        flash = 0.5;
        hitStop = 0.12;

        if (lives < 0) {
            lives = 0;
            state = GameState.GAME_OVER;
            if (score > bestScore) {
                bestScore = score;
            }
        }
    }

    // Particles

    private void updateParticles(double dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            if (!particles.get(i).update(dt)) {
                particles.remove(i);
            }
        }
    }

    private void spark(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 40 + random.nextDouble() * 150;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    0.18 + random.nextDouble() * 0.24, 1.0 + random.nextDouble() * 1.6, color));
        }
    }

    private void explosion(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 60 + random.nextDouble() * 280;
            Color tint = Fx.mix(color, Color.WHITE, random.nextDouble() * 0.6);
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    0.3 + random.nextDouble() * 0.5, 1.4 + random.nextDouble() * 2.4, tint));
        }
    }

    // Drawing

    void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        drawBackground(g);

        double sx = 0;
        double sy = 0;
        if (shake > 0.2) {
            sx = (random.nextDouble() - 0.5) * shake * 1.6;
            sy = (random.nextDouble() - 0.5) * shake * 1.6;
        }
        g.translate(sx, sy);

        Shape previousClip = g.getClip();
        g.clip(new Rectangle2D.Double(0, TOP, WIDTH, HEIGHT - TOP));
        for (Particle particle : particles) {
            particle.draw(g);
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
        for (Pickup pickup : pickups) {
            pickup.draw(g, fontPickup);
        }
        if (state != GameState.GAME_OVER && state != GameState.TITLE) {
            player.draw(g);
        }
        g.setClip(previousClip);

        g.translate(-sx, -sy);
        if (state != GameState.TITLE) {
            drawHud(g);
        }

        if (flash > 0) {
            g.setColor(new Color(255, 255, 255, (int) (150 * flash)));
            g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));
        }

        if (state == GameState.TITLE) {
            drawTitle(g);
        } else if (state == GameState.PAUSED) {
            drawPaused(g);
        } else if (state == GameState.GAME_OVER) {
            drawGameOver(g);
        }
        g.dispose();
    }

    private void drawBackground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(9, 11, 28), 0, HEIGHT, new Color(22, 8, 38)));
        g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));

        for (Star star : stars) {
            star.draw(g);
        }

        RadialGradientPaint vignette = new RadialGradientPaint(
                WIDTH / 2f, HEIGHT / 2f, WIDTH * 0.78f,
                new float[] { 0.45f, 1f },
                new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 150) });
        g.setPaint(vignette);
        g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(120, 200, 255, 34));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(0, TOP - 1, WIDTH, TOP - 1));

        g.setFont(fontSmall);
        g.setColor(new Color(120, 200, 255, 170));
        g.drawString("SCORE", 24, 26);
        g.setFont(fontMedium);
        g.setColor(new Color(232, 250, 255));
        g.drawString(String.format("%06d", score), 24, 50);

        g.setFont(fontSmall);
        g.setColor(new Color(120, 200, 255, 140));
        g.drawString("BEST " + String.format("%06d", bestScore), 24 + 108, 50);

        Fx.text(g, "WAVE " + wave, fontMedium, new Color(160, 220, 255), WIDTH / 2.0, 40);

        for (int i = 0; i < lives; i++) {
            double lx = WIDTH - 30 - i * 28;
            double ly = 36;
            boolean showedHeart = Art.draw(g, Art.HEART, lx, ly, 21);
            if (!showedHeart) {
                Path2D.Double ship = new Path2D.Double();
                ship.moveTo(lx, ly - 11);
                ship.lineTo(lx + 8, ly + 7);
                ship.lineTo(lx, ly + 3);
                ship.lineTo(lx - 8, ly + 7);
                ship.closePath();
                g.setColor(new Color(104, 236, 255, 210));
                g.fill(ship);
            }
        }

        if (player.shield > 0) {
            g.setColor(new Color(110, 245, 176, 200));
            g.setFont(fontSmall);
            g.drawString("SHIELD " + String.format("%.1f", player.shield), WIDTH - 116, 60);
        }

        if (!sound.isEnabled()) {
            Fx.text(g, "M  sound off", fontSmall, new Color(255, 255, 255, 110), WIDTH / 2.0, HEIGHT - 14);
        }

        if (bannerTimer > 0 && state == GameState.PLAYING) {
            double t = Math.min(1, bannerTimer * 1.6);
            Fx.text(g, banner, fontBig, new Color(150, 240, 255, (int) (255 * t)), WIDTH / 2.0, HEIGHT * 0.32);
        }
    }

    private void drawTitle(Graphics2D g) {
        g.setColor(new Color(4, 6, 18, 150));
        g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));

        // The logo picture if it is there, otherwise glowing text.
        boolean showedLogo = Art.draw(g, Art.LOGO, WIDTH / 2.0, HEIGHT * 0.35, 250);
        if (!showedLogo) {
            Fx.glowText(g, "STARFALL", fontTitle, new Color(104, 224, 255), WIDTH / 2.0, HEIGHT * 0.42, 7);
        }
        Fx.text(g, "a tiny arcade shooter", fontMedium, new Color(170, 205, 255, 200), WIDTH / 2.0, HEIGHT * 0.58);

        double pulse = 0.55 + 0.45 * Math.sin(System.currentTimeMillis() / 320.0);
        Fx.text(g, "PRESS  SPACE  TO PLAY", fontBig,
                new Color(255, 255, 255, (int) (120 + 135 * pulse)), WIDTH / 2.0, HEIGHT * 0.68);

        Fx.text(g, "ARROWS / WASD  move        SPACE  fire        P  pause        M  mute",
                fontSmall, new Color(150, 190, 235, 170), WIDTH / 2.0, HEIGHT * 0.82);
        Fx.text(g, "Collect W  weapon      S  shield      +  extra life",
                fontSmall, new Color(150, 190, 235, 150), WIDTH / 2.0, HEIGHT * 0.88);
        if (bestScore > 0) {
            Fx.text(g, "BEST  " + String.format("%06d", bestScore), fontMedium,
                    new Color(255, 214, 120, 220), WIDTH / 2.0, HEIGHT * 0.95);
        }
    }

    private void drawPaused(Graphics2D g) {
        g.setColor(new Color(4, 6, 18, 175));
        g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));
        Fx.glowText(g, "PAUSED", fontTitle, new Color(140, 200, 255), WIDTH / 2.0, HEIGHT * 0.48, 5);
        Fx.text(g, "P  resume        ESC  quit to title", fontMedium,
                new Color(190, 215, 245, 190), WIDTH / 2.0, HEIGHT * 0.60);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(6, 4, 16, 165));
        g.fill(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));

        Fx.glowText(g, "GAME OVER", fontTitle, new Color(255, 110, 165), WIDTH / 2.0, HEIGHT * 0.40, 6);

        double boxW = 380;
        double boxH = 150;
        double bx = WIDTH / 2.0 - boxW / 2;
        double by = HEIGHT * 0.48;
        RoundRectangle2D.Double panel = new RoundRectangle2D.Double(bx, by, boxW, boxH, 18, 18);
        g.setColor(new Color(14, 16, 34, 210));
        g.fill(panel);
        g.setColor(new Color(120, 200, 255, 70));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(panel);

        Fx.text(g, "Best score", fontSmall, new Color(140, 190, 235, 200), WIDTH / 2.0, by + 32);
        Fx.text(g, String.format("%06d", score), fontBig, new Color(235, 252, 255), WIDTH / 2.0, by + 72);

        boolean record = score > 0 && score >= bestScore;
        Fx.text(g, record ? "NEW BEST!" : "BEST  " + String.format("%06d", bestScore),
                fontMedium, new Color(255, 214, 120, 230), WIDTH / 2.0, by + 100);
        Fx.text(g, "WAVES SURVIVED  " + wave, fontSmall,
                new Color(160, 200, 240, 190), WIDTH / 2.0, by + 126);

        double pulse = 0.55 + 0.45 * Math.sin(System.currentTimeMillis() / 320.0);
        Fx.text(g, "PRESS  SPACE  TO PLAY AGAIN", fontMedium,
                new Color(255, 255, 255, (int) (110 + 145 * pulse)), WIDTH / 2.0, HEIGHT * 0.86);
    }
}
