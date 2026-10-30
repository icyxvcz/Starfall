package starfall;


abstract class SoundEffect {
    static final float SAMPLE_RATE = 22050f;

    private javax.sound.sampled.Clip clip;

    /* How long the sound lasts, in seconds. */
    abstract double duration();

    abstract double sample(double t);

    javax.sound.sampled.Clip clip() {
        return clip;
    }

    final void prepare() throws Exception {
        int frames = (int) (duration() * SAMPLE_RATE);
        double[] raw = new double[frames];
        double peak = 0;
        for (int i = 0; i < frames; i++) {
            raw[i] = sample(i / (double) SAMPLE_RATE);
            peak = Math.max(peak, Math.abs(raw[i]));
        }

        double scale = peak > 0.0001 ? Math.min(1.0, 0.85 / peak) : 1.0;
        byte[] data = new byte[frames * 2];
        for (int i = 0; i < frames; i++) {
            short value = (short) Math.max(-32767, Math.min(32767, raw[i] * scale * 32767));
            data[i * 2] = (byte) (value & 0xff);
            data[i * 2 + 1] = (byte) ((value >> 8) & 0xff);
        }

        javax.sound.sampled.AudioFormat format =
                new javax.sound.sampled.AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        clip = javax.sound.sampled.AudioSystem.getClip();
        clip.open(format, data, 0, data.length);
    }

    /** A square wave whose pitch slides from startHz downwards. */
    static double squareSweep(double t, double startHz, double fallPerSecond) {
        double phase = startHz * t - fallPerSecond * t * t / 2.0;
        return Math.sin(2 * Math.PI * phase) >= 0 ? 1 : -1;
    }

    /** Fades the sound out so it ends cleanly instead of clicking. */
    static double fadeOut(double t, double duration, double power) {
        double x = Math.min(1.0, t / duration);
        return Math.pow(1 - x, power);
    }
}
