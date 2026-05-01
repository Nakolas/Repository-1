package com.resonant.game.audio;

/**
 * Stateless waveform generators. All return values in roughly [-1, 1].
 * Time {@code t} is in seconds; frequency {@code freq} in Hz.
 */
public final class Synth {

    private Synth() {}

    private static final double TWO_PI = Math.PI * 2.0;

    public static double sine(double freq, double t) {
        return Math.sin(TWO_PI * freq * t);
    }

    public static double square(double freq, double t) {
        return (Math.sin(TWO_PI * freq * t) >= 0) ? 1.0 : -1.0;
    }

    public static double saw(double freq, double t) {
        double phase = (freq * t) % 1.0;
        return 2.0 * phase - 1.0;
    }

    public static double triangle(double freq, double t) {
        double phase = (freq * t) % 1.0;
        return (phase < 0.5) ? (4.0 * phase - 1.0) : (3.0 - 4.0 * phase);
    }

    /**
     * Synthesized kick drum: descending sine sweep with click attack.
     * @param tFromKick seconds since kick onset (0..0.25 typical)
     */
    public static double kick(double tFromKick) {
        if (tFromKick < 0 || tFromKick > 0.25) return 0;
        double freq = 110.0 * Math.exp(-tFromKick * 18.0) + 40.0;
        double env  = Math.exp(-tFromKick * 11.0);
        // Add a click in the first 8ms
        double click = (tFromKick < 0.008) ? 0.6 : 0;
        return Math.sin(TWO_PI * freq * tFromKick) * env + click;
    }

    /**
     * Hi-hat: filtered noise burst with fast decay.
     * @param tFromHat seconds since hat onset
     */
    public static double hat(double tFromHat) {
        if (tFromHat < 0 || tFromHat > 0.10) return 0;
        double env = Math.exp(-tFromHat * 50.0);
        // White-ish noise (deterministic-ish, doesn't matter)
        double noise = (Math.random() * 2.0 - 1.0);
        return noise * env;
    }

    /** Simple ADSR envelope evaluated at time {@code t} since note onset. */
    public static double adsr(double t, double attack, double decay, double sustain,
                              double release, double duration) {
        if (t < 0) return 0;
        if (t < attack) return t / attack;
        if (t < attack + decay) {
            double k = (t - attack) / decay;
            return 1.0 - (1.0 - sustain) * k;
        }
        if (t < duration - release) return sustain;
        if (t < duration) {
            double k = (duration - t) / release;
            return sustain * Math.max(0, k);
        }
        return 0;
    }
}
