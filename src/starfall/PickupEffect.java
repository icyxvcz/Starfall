package starfall;

/** Two rising notes for collecting a power-up. */
final class PickupEffect extends SoundEffect {
    @Override
    double duration() {
        return 0.18;
    }

    @Override
    double sample(double t) {
        double frequency = t < duration() * 0.45 ? 780 : 1180;
        return Math.sin(2 * Math.PI * frequency * t) * 0.30 * fadeOut(t, duration(), 0.5);
    }
}
