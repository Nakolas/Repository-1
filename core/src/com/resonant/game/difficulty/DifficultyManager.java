package com.resonant.game.difficulty;

import com.resonant.game.GameConfig;
import com.resonant.game.model.GameWorld;

/**
 * Drives difficulty escalation: speed multiplier grows continuously, rings are added
 * at fixed intervals, obstacle density increases stepwise.
 */
public class DifficultyManager {

    private float timeSinceLastRingAdd;
    private float currentDensity;

    public DifficultyManager() { reset(); }

    public void reset() {
        timeSinceLastRingAdd = 0f;
        currentDensity       = GameConfig.DENSITY_INITIAL;
    }

    public float getCurrentDensity() { return currentDensity; }

    public void update(float delta, GameWorld world) {
        timeSinceLastRingAdd += delta;

        // Continuous speed escalation
        world.increaseSpeed(GameConfig.SPEED_INCREASE_RATE * delta);

        // Add a ring every NEW_RING_INTERVAL seconds (until at max)
        if (timeSinceLastRingAdd >= GameConfig.NEW_RING_INTERVAL) {
            timeSinceLastRingAdd = 0f;
            if (world.getActiveRings() < GameConfig.RING_COUNT_MAX) {
                currentDensity = Math.min(currentDensity + GameConfig.DENSITY_STEP,
                        GameConfig.DENSITY_MAX);
                world.addOutermostRing();
            } else {
                // Already at max rings — bump density only
                currentDensity = Math.min(currentDensity + GameConfig.DENSITY_STEP,
                        GameConfig.DENSITY_MAX);
            }
        }
    }
}
