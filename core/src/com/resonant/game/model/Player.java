package com.resonant.game.model;

import com.badlogic.gdx.math.MathUtils;

import com.resonant.game.GameConfig;

/**
 * Player particle state: which ring it's on, its current angular position, and a trail buffer.
 * The player orbits automatically; the only player-controlled state is {@link #ringIndex}.
 */
public class Player {

    public int   ringIndex;
    public float orbitAngle;          // radians
    public float switchCooldown;      // seconds until next ring-switch is allowed
    public float flashTimer;          // seconds remaining on ring-switch flash effect

    private final float[] trailX = new float[GameConfig.TRAIL_LENGTH];
    private final float[] trailY = new float[GameConfig.TRAIL_LENGTH];
    private int trailHead = 0;
    private boolean trailInitialized = false;

    public Player() {
        reset();
    }

    public void reset() {
        ringIndex      = 0;
        orbitAngle     = 0f;
        switchCooldown = 0f;
        flashTimer     = 0f;
        trailHead      = 0;
        trailInitialized = false;
    }

    /** Records the player's current world position into the trail buffer. */
    public void recordTrailPosition(float x, float y) {
        if (!trailInitialized) {
            // Pre-fill so we don't see (0,0) artifacts in the first frames
            for (int i = 0; i < GameConfig.TRAIL_LENGTH; i++) {
                trailX[i] = x;
                trailY[i] = y;
            }
            trailInitialized = true;
            trailHead = 0;
            return;
        }
        trailX[trailHead] = x;
        trailY[trailHead] = y;
        trailHead = (trailHead + 1) % GameConfig.TRAIL_LENGTH;
    }

    /**
     * Iterates trail positions from oldest to newest, providing each with a normalizedAge
     * (0 = oldest, 1 = newest).
     */
    public void iterateTrail(TrailConsumer consumer) {
        int N = GameConfig.TRAIL_LENGTH;
        for (int i = 0; i < N; i++) {
            int idx = (trailHead + i) % N;
            float age = i / (float) N;
            consumer.accept(trailX[idx], trailY[idx], age);
        }
    }

    /** Computes the player's world position from its ringIndex + orbitAngle. */
    public float worldX(Ring[] rings) {
        Ring r = rings[ringIndex];
        float midR = (r.innerRadius + r.outerRadius) * 0.5f;
        return GameConfig.CENTER_X + midR * MathUtils.cos(orbitAngle);
    }

    public float worldY(Ring[] rings) {
        Ring r = rings[ringIndex];
        float midR = (r.innerRadius + r.outerRadius) * 0.5f;
        return GameConfig.CENTER_Y + midR * MathUtils.sin(orbitAngle);
    }

    public boolean canSwitch() {
        return switchCooldown <= 0f;
    }

    public void registerSwitch() {
        switchCooldown = GameConfig.SWITCH_COOLDOWN;
        flashTimer     = 0.08f;
    }

    public void tickTimers(float delta) {
        if (switchCooldown > 0f) switchCooldown -= delta;
        if (flashTimer > 0f) flashTimer -= delta;
    }

    public interface TrailConsumer {
        void accept(float x, float y, float normalizedAge);
    }
}
