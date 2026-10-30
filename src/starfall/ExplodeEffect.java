package starfall;

import java.util.Random;

/** The noisy boom when an enemy is destroyed. */
final class ExplodeEffect extends SoundEffect {
    private final Random noise = new Random(7);

    @Override
    double duration() {
        return 0.42;
    }

    @Override
    double sample(double t) {
        double hiss = noise.nextDouble() * 2 - 1;
        double rumble = Math.sin(2 * Math.PI * (150 - 180 * t) * t);
        return (hiss * 0.55 + rumble * 0.45) * 0.42 * Math.pow(1 - t / duration(), 2.1);
    }
}
