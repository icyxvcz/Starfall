package starfall;

/** The small click when a shot damages an enemy without destroying it. */
final class HitEffect extends SoundEffect {
    @Override
    double duration() {
        return 0.05;
    }

    @Override
    double sample(double t) {
        return squareSweep(t, 620, 3000) * 0.22 * fadeOut(t, duration(), 0.7);
    }
}
