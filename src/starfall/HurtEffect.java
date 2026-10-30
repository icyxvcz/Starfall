package starfall;

import java.util.Random;

/** The harsh buzz when the player is hit. */
final class HurtEffect extends SoundEffect {
    private final Random noise = new Random(11);

    @Override
    double duration() {
        return 0.45;
    }

    @Override
    double sample(double t) {
        double pitch = t * (420 - 620 * t);
        double buzz = 2 * (pitch % 1.0) - 1;
        double hiss = noise.nextDouble() * 2 - 1;
        return (buzz * 0.55 + hiss * 0.45) * 0.38 * Math.pow(1 - t / duration(), 1.6);
    }
}
