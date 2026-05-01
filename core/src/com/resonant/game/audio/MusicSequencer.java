package com.resonant.game.audio;

/**
 * Generates a continuous PCM stream by summing a chord progression with bass/pad/arp/drums.
 * Stateless except for SFX queueing — sample value is purely a function of time {@code t}.
 *
 * 128 BPM, 4/4. 4-chord 16-beat loop: Am -> F -> C -> G.
 */
public class MusicSequencer {

    private static final float BPM = 128f;
    private static final double BEAT_DUR = 60.0 / BPM;  // seconds per beat

    // Chord pitches (Hz): root, third, fifth (octaves chosen for warm bass register)
    private static final double[][] CHORDS = {
            {110.00, 130.81, 164.81},  // Am: A2 C3 E3
            { 87.31, 110.00, 130.81},  // F:  F2 A2 C3
            { 65.41,  82.41,  98.00},  // C:  C2 E2 G2
            { 98.00, 123.47, 146.83},  // G:  G2 B2 D3
    };

    // Lead arpeggio uses the chord notes one octave up
    private double[] arpNotes(int chordIdx) {
        double[] c = CHORDS[chordIdx];
        return new double[] { c[0] * 2.0, c[1] * 2.0, c[2] * 2.0, c[1] * 2.0 };
    }

    // SFX state (volatile because the audio thread reads, the game thread writes)
    private volatile double sfxSwitchTime = -10.0;
    private volatile double sfxDeathTime  = -10.0;
    private volatile double musicGain     = 1.0;
    private volatile double pitchShift    = 1.0;  // for death effect: temporarily 0..1

    public void queueSwitchSfx(double t) { sfxSwitchTime = t; }
    public void queueDeathSfx(double t)  { sfxDeathTime  = t; pitchShift = 1.0; }
    public void setMusicGain(double g)   { musicGain = g; }

    /**
     * Returns the current beat phase in [0,1) for visual sync.
     */
    public float getBeatPhase(double t) {
        double beat = t / BEAT_DUR;
        return (float)(beat - Math.floor(beat));
    }

    /**
     * Produces one PCM sample at absolute time {@code t} (seconds).
     */
    public double sampleAt(double t) {
        // Death pitch shift: drag music time slower for 200ms after death
        double timeSinceDeath = t - sfxDeathTime;
        double localT = t;
        if (timeSinceDeath >= 0 && timeSinceDeath < 0.4) {
            // Pitch shifts down (slows time) and fades to silence
            pitchShift = Math.max(0.05, 1.0 - timeSinceDeath * 2.5);
            localT = sfxDeathTime + (t - sfxDeathTime) * pitchShift;
        } else if (timeSinceDeath >= 0.4 && timeSinceDeath < 0.6) {
            pitchShift = 0.0;
            return sfxDeathThump(timeSinceDeath - 0.4);
        }

        double beat   = localT / BEAT_DUR;
        int chordIdx  = ((int) Math.floor(beat / 4)) % 4;
        if (chordIdx < 0) chordIdx += 4;
        double[] chord = CHORDS[chordIdx];
        double[] arp   = arpNotes(chordIdx);

        double beatInChord = (beat % 4 + 4) % 4;
        double tOnBeat = beatInChord * BEAT_DUR;

        // ---- Bass: square wave on root, 8th-note pulse pattern ----
        double bassFreq = chord[0];
        double bassPhaseInBeat = (localT * 2.0 / BEAT_DUR) % 1.0; // 8th-note position
        double bassEnv = Math.exp(-bassPhaseInBeat * 4.0);
        double bass = Synth.square(bassFreq, localT) * bassEnv * 0.13;

        // ---- Pad: sustained sine triad with soft amplitude modulation ----
        double padEnv = 0.06 + 0.02 * Math.sin(localT * 0.6);
        double pad = (Synth.sine(chord[0] * 2, localT)
                    + Synth.sine(chord[1] * 2, localT)
                    + Synth.sine(chord[2] * 2, localT)) * padEnv * 0.25;

        // ---- Arpeggio: 16th-note saw waves, low-passed via simple smoothing ----
        int sixteenth = (int)(beat * 4) % 4;
        double arpFreq = arp[((sixteenth + 1) % 4)];
        double arpPhase = (localT * 4.0 / BEAT_DUR) % 1.0;
        double arpEnv = Math.exp(-arpPhase * 6.0);
        double arp_ = Synth.saw(arpFreq, localT) * arpEnv * 0.07;

        // ---- Kick: on beats 0 and 2 of each chord measure ----
        double kickPhase = (localT % BEAT_DUR);
        double kick = 0;
        int beatInt = ((int) Math.floor(beat)) % 4;
        if (beatInt == 0 || beatInt == 2) {
            kick = Synth.kick(kickPhase) * 0.30;
        }

        // ---- Hi-hat: on offbeats (8th notes between kicks) ----
        double hatPhase = ((localT * 2.0) % BEAT_DUR);
        double hatPos = (localT * 2.0) / BEAT_DUR;
        int hatInt = ((int) Math.floor(hatPos)) % 2;
        double hat = 0;
        if (hatInt == 1 && hatPhase < 0.10) {
            hat = Synth.hat(hatPhase) * 0.05;
        }

        double mix = (bass + pad + arp_ + kick + hat) * musicGain;

        // ---- Switch SFX: short noise click ----
        double timeSinceSwitch = t - sfxSwitchTime;
        if (timeSinceSwitch >= 0 && timeSinceSwitch < 0.04) {
            double env = Math.exp(-timeSinceSwitch * 60.0);
            double click = (Math.random() * 2.0 - 1.0) * env * 0.20;
            mix += click;
        }

        return clamp(mix, -1.0, 1.0);
    }

    private double sfxDeathThump(double t) {
        if (t < 0 || t > 0.2) return 0;
        double freq = 60.0;
        double env = Math.exp(-t * 8.0);
        return Math.sin(2 * Math.PI * freq * t) * env * 0.35;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
