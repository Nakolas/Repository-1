package com.resonant.game.model;

import com.badlogic.gdx.math.MathUtils;

import com.resonant.game.GameConfig;
import com.resonant.game.audio.AudioEngine;
import com.resonant.game.difficulty.DifficultyManager;

/**
 * The simulation. Owns the ring array, the player, and runs the per-frame update loop:
 * advances the player's orbit, rotates rings, regenerates layouts, checks collision, scores.
 *
 * Drives audio events via the optional AudioEngine reference.
 */
public class GameWorld {

    private final Ring[]            rings = new Ring[GameConfig.RING_COUNT_MAX];
    private final Player            player = new Player();
    private final DifficultyManager difficulty = new DifficultyManager();
    private final DeathParticles    deathFx = new DeathParticles();

    private int     activeRings;
    private float   elapsedTime;
    private float   speedMultiplier;
    private float   layoutRegenTimer;
    private boolean alive;
    private float   deathTime;     // seconds since death (for death-screen pacing)

    private AudioEngine audio;     // optional

    public GameWorld() {
        // Build all rings up front; only `activeRings` are used at any time.
        for (int i = 0; i < GameConfig.RING_COUNT_MAX; i++) {
            float inner = GameConfig.RING_INNER_BASE + i * (GameConfig.RING_THICKNESS + GameConfig.RING_GAP);
            float outer = inner + GameConfig.RING_THICKNESS;
            float speedMult = GameConfig.RING_SPEED_MULT[i];
            rings[i] = new Ring(i, inner, outer, speedMult * GameConfig.PLAYER_ORBIT_SPEED);
        }
    }

    public void setAudio(AudioEngine audio) { this.audio = audio; }

    public void init() {
        elapsedTime      = 0f;
        speedMultiplier  = GameConfig.SPEED_MULT_INITIAL;
        layoutRegenTimer = 0f;
        alive            = true;
        deathTime        = 0f;
        activeRings      = GameConfig.RING_COUNT_INITIAL;
        difficulty.reset();
        deathFx.clear();

        player.reset();
        // Place player on the middle active ring
        player.ringIndex = activeRings / 2;
        player.orbitAngle = 0f;

        regenerateAllLayouts(GameConfig.DENSITY_INITIAL);
        ensureSafeAtPlayerAngle();
    }

    public void update(float delta) {
        if (!alive) {
            deathTime += delta;
            deathFx.update(delta);
            return;
        }

        elapsedTime += delta;

        // 1) Difficulty escalation
        difficulty.update(delta, this);

        // 2) Player orbit
        float effSpeed = GameConfig.PLAYER_ORBIT_SPEED * speedMultiplier;
        player.orbitAngle += effSpeed * delta;
        player.orbitAngle = ((player.orbitAngle % MathUtils.PI2) + MathUtils.PI2) % MathUtils.PI2;
        player.tickTimers(delta);

        // 3) Trail
        player.recordTrailPosition(player.worldX(rings), player.worldY(rings));

        // 4) Rings rotate (also scale by speedMultiplier so they keep feeling related)
        float ringDelta = delta * (0.6f + 0.4f * speedMultiplier);
        for (int i = 0; i < activeRings; i++) rings[i].update(ringDelta);

        // 5) Periodic ring-layout regeneration (refreshes patterns over time)
        layoutRegenTimer += delta;
        if (layoutRegenTimer >= GameConfig.LAYOUT_REGEN_INTERVAL) {
            layoutRegenTimer = 0f;
            // Regenerate one ring at a time (the outermost one), stagger refresh
            int target = (int)(elapsedTime / GameConfig.LAYOUT_REGEN_INTERVAL) % activeRings;
            // Don't regenerate the player's current ring — would be unfair
            if (target == player.ringIndex) target = (target + 1) % activeRings;
            rings[target].regenerate(difficulty.getCurrentDensity());
            ensureSafeAtPlayerAngle();
        }

        // 6) Collision check
        if (rings[player.ringIndex].isObstacleAt(player.orbitAngle)) {
            triggerDeath();
        }
    }

    private void triggerDeath() {
        alive = false;
        deathTime = 0f;
        deathFx.spawn(player.worldX(rings), player.worldY(rings));
        if (audio != null) audio.triggerDeath();
    }

    public boolean playerMoveInward() {
        if (!alive || !player.canSwitch()) return false;
        if (player.ringIndex <= 0) return false;
        player.ringIndex--;
        player.registerSwitch();
        if (audio != null) audio.triggerSwitchSfx();
        return true;
    }

    public boolean playerMoveOutward() {
        if (!alive || !player.canSwitch()) return false;
        if (player.ringIndex >= activeRings - 1) return false;
        player.ringIndex++;
        player.registerSwitch();
        if (audio != null) audio.triggerSwitchSfx();
        return true;
    }

    /** Add one ring (called by DifficultyManager). */
    public void addOutermostRing() {
        if (activeRings >= GameConfig.RING_COUNT_MAX) return;
        rings[activeRings].regenerate(difficulty.getCurrentDensity());
        activeRings++;
        ensureSafeAtPlayerAngle();
    }

    public void increaseSpeed(float delta) {
        speedMultiplier = Math.min(speedMultiplier + delta, GameConfig.SPEED_MULT_MAX);
    }

    private void regenerateAllLayouts(float density) {
        for (int i = 0; i < activeRings; i++) rings[i].regenerate(density);
    }

    /**
     * Cross-ring solvability check: ensure the player's current ring has a gap at their angle.
     * If not, regenerate that ring with the current density until safe.
     */
    private void ensureSafeAtPlayerAngle() {
        Ring r = rings[player.ringIndex];
        int safetyAttempts = 8;
        while (r.isObstacleAt(player.orbitAngle) && safetyAttempts-- > 0) {
            r.regenerate(difficulty.getCurrentDensity());
        }
        // Last resort: forcibly clear the player's current segment
        if (r.isObstacleAt(player.orbitAngle)) {
            int segIdx = r.getSegmentIndexAt(player.orbitAngle);
            r.isObstacle[segIdx] = false;
        }
    }

    // ---- Getters ----
    public Ring[]        getRings()           { return rings; }
    public int           getActiveRings()     { return activeRings; }
    public Player        getPlayer()          { return player; }
    public float         getElapsedTime()     { return elapsedTime; }
    public float         getSpeedMultiplier() { return speedMultiplier; }
    public boolean       isAlive()            { return alive; }
    public float         getDeathTime()       { return deathTime; }
    public DeathParticles getDeathFx()        { return deathFx; }

    public float getScore() {
        return elapsedTime * speedMultiplier * GameConfig.SCORE_TIME_WEIGHT * 10f;
    }
}
