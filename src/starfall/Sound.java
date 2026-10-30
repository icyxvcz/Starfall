package starfall;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Plays the game's sound effects.
 *
 * The shooting and pick-up sounds come from the SFX folder (shoot.wav and
 * pickup.wav). Everything else is generated in code when the game starts, so the
 * project still runs with no audio files at all.
 *
 * A long sound (like the three-second shot) is played through a small pool of
 * identical clips. One gunshot can still be ringing out while the next few
 * start, which sounds natural; without the pool, firing fast would either chop
 * the sound off or stop it completely.
 *
 * If a file is missing, or the computer has no working audio device, the game
 * quietly falls back to a generated sound instead of crashing.
 */
final class Sound {
    private static final String SHOOT_FILE = "shoot.wav";
    private static final String PICKUP_FILE = "pickup.wav";

    /** How many copies of a long sound may overlap. */
    private static final int VOICES = 4;

    private final SoundEffect[] generated = new SoundEffect[SoundType.values().length];
    private final Clip[][] voices = new Clip[SoundType.values().length][];
    private final boolean[] fromFile = new boolean[SoundType.values().length];
    private boolean ready;
    private boolean enabled = true;

    Sound() {
        // A generated version of every sound exists first, so something always
        // plays even if the SFX folder is empty.
        generated[SoundType.SHOOT.ordinal()] = new ShotEffect();
        generated[SoundType.HIT.ordinal()] = new HitEffect();
        generated[SoundType.EXPLODE.ordinal()] = new ExplodeEffect();
        generated[SoundType.PICKUP.ordinal()] = new PickupEffect();
        generated[SoundType.HURT.ordinal()] = new HurtEffect();
        generated[SoundType.START.ordinal()] = new StartEffect();

        try {
            for (SoundEffect effect : generated) {
                effect.prepare();
            }
            ready = true;
        } catch (Throwable ignored) {
            ready = false;
        }

        // Then prefer the real audio files where we have them.
        loadFile(SoundType.SHOOT, SHOOT_FILE);
        loadFile(SoundType.PICKUP, PICKUP_FILE);
    }

    private void loadFile(SoundType type, String fileName) {
        File file = new File("SFX", fileName);
        if (!file.isFile()) {
            return;
        }

        Clip[] pool = new Clip[VOICES];
        int loadedCount = 0;
        for (int i = 0; i < VOICES; i++) {
            Clip clip = open(file);
            if (clip == null) {
                break;
            }
            pool[i] = clip;
            loadedCount++;
        }
        if (loadedCount > 0) {
            voices[type.ordinal()] = pool;
            fromFile[type.ordinal()] = true;
        }
    }

    private static Clip open(File file) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                return clip;
            } finally {
                audio.close();
            }
        } catch (UnsupportedAudioFileException | IOException
                 | LineUnavailableException | IllegalArgumentException e) {
            return null;
        }
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    boolean isEnabled() {
        return enabled;
    }

    /** True when this sound came from a file in the SFX folder. */
    boolean comesFromFile(SoundType type) {
        return type != null && fromFile[type.ordinal()];
    }

    void play(SoundType type) {
        if (!enabled || type == null) {
            return;
        }

        Clip[] pool = voices[type.ordinal()];
        if (pool != null) {
            startFromPool(pool);
            return;
        }

        SoundEffect effect = generated[type.ordinal()];
        if (!ready || effect == null || effect.clip() == null) {
            return;
        }
        try {
            Clip clip = effect.clip();
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch (Throwable ignored) {
            // A glitch in audio must never break the game.
        }
    }

    /** Starts the first idle clip, or recycles the oldest one if all are busy. */
    private static void startFromPool(Clip[] pool) {
        Clip chosen = null;
        for (Clip clip : pool) {
            if (clip != null && !clip.isRunning()) {
                chosen = clip;
                break;
            }
        }
        if (chosen == null) {
            chosen = pool[0];
        }
        try {
            chosen.stop();
            chosen.setFramePosition(0);
            chosen.start();
        } catch (Throwable ignored) {
            // A glitch in audio must never break the game.
        }
    }

    /** Silences everything, for when the player mutes the game. */
    void stopAll() {
        for (Clip[] pool : voices) {
            if (pool != null) {
                for (Clip clip : pool) {
                    stop(clip);
                }
            }
        }
        for (SoundEffect effect : generated) {
            if (effect != null) {
                stop(effect.clip());
            }
        }
    }

    private static void stop(Clip clip) {
        if (clip == null) {
            return;
        }
        try {
            clip.stop();
        } catch (Throwable ignored) {
            // Nothing to do.
        }
    }
}
