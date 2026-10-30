package starfall;

/** A four-note fanfare for starting a run or reaching a new wave. */
final class StartEffect extends SoundEffect {
    private static final double[] NOTES = { 523.25, 659.25, 783.99, 1046.5 };

    @Override
    double duration() {
        return 0.34;
    }

    @Override
    double sample(double t) {
        int step = (int) (t / 0.085);
        double frequency = NOTES[Math.min(NOTES.length - 1, step)];
        return Math.sin(2 * Math.PI * frequency * t) * 0.26 * fadeOut(t, duration(), 0.35);
    }
}
