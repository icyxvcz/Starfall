package starfall;

/** The quick "pew" played when the player fires. */
final class ShotEffect extends SoundEffect {
    @Override
    double duration() {
        return 0.07;
    }

    @Override
    double sample(double t) {
        return squareSweep(t, 1180, 5200) * 0.30 * fadeOut(t, duration(), 0.8);
    }
}
