package com.resonant.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * All tunable constants for RESONANT.
 * Single source of truth for game balance and visual style.
 */
public final class GameConfig {

    private GameConfig() {}

    // ---- World / viewport ----
    public static final float WORLD_WIDTH  = 854f;
    public static final float WORLD_HEIGHT = 480f;
    public static final float CENTER_X     = WORLD_WIDTH  * 0.5f;
    public static final float CENTER_Y     = WORLD_HEIGHT * 0.5f;

    // ---- Ring geometry ----
    public static final int   RING_COUNT_INITIAL = 3;
    public static final int   RING_COUNT_MAX     = 5;
    public static final int   SEGMENTS_PER_RING  = 8;
    public static final float SEGMENT_ANGLE      = MathUtils.PI2 / 8f; // 45 degrees in radians

    public static final float NEXUS_RADIUS    = 14f;
    public static final float RING_INNER_BASE = 38f;   // innermost ring inner edge
    public static final float RING_THICKNESS  = 26f;   // each ring's radial width
    public static final float RING_GAP        = 6f;    // visual gap between adjacent rings
    public static final float SEGMENT_INSET   = 0.025f; // radians; gap between adjacent segments

    // ---- Player ----
    public static final float PLAYER_RADIUS      = 6f;
    public static final float PLAYER_ORBIT_SPEED = 1.6f;  // rad/sec at multiplier 1.0
    public static final int   TRAIL_LENGTH       = 22;
    public static final float SWITCH_COOLDOWN    = 0.10f; // seconds

    // ---- Ring rotation (multiplier of player speed) ----
    // Inner CW (negative), alternating outward. Slightly varied magnitudes.
    public static final float[] RING_SPEED_MULT = {-0.55f, 0.42f, -0.48f, 0.36f, -0.50f};

    // ---- Colors ----
    public static final Color COLOR_BACKGROUND = new Color(0.020f, 0.012f, 0.063f, 1f);
    public static final Color COLOR_NEXUS      = new Color(0.30f, 0.85f, 1.00f, 1f);
    public static final Color COLOR_OBSTACLE   = new Color(0.90f, 0.30f, 0.20f, 1f);
    public static final Color COLOR_GAP        = new Color(0.10f, 0.10f, 0.15f, 1f);
    public static final Color COLOR_PLAYER     = new Color(0.50f, 1.00f, 0.80f, 1f);
    public static final Color COLOR_RING_EDGE  = new Color(0.30f, 0.30f, 0.50f, 0.55f);
    public static final Color COLOR_STAR       = new Color(0.65f, 0.70f, 0.90f, 0.6f);
    public static final Color COLOR_TEXT       = new Color(0.92f, 0.95f, 1.00f, 1f);
    public static final Color COLOR_TEXT_DIM   = new Color(0.55f, 0.60f, 0.75f, 1f);
    public static final Color COLOR_NEW_BEST   = new Color(1.00f, 0.85f, 0.40f, 1f);

    // ---- Difficulty ----
    public static final float SPEED_MULT_INITIAL  = 1.0f;
    public static final float SPEED_MULT_MAX      = 4.0f;
    public static final float SPEED_INCREASE_RATE = 0.04f; // mult per second
    public static final float NEW_RING_INTERVAL   = 30f;   // seconds between ring additions
    public static final float DENSITY_INITIAL     = 0.5f;  // 4 of 8 segments solid
    public static final float DENSITY_MAX         = 0.75f; // 6 of 8 segments solid
    public static final float DENSITY_STEP        = 0.05f;
    public static final float LAYOUT_REGEN_INTERVAL = 4.0f; // ring regen cadence (seconds)

    // ---- Scoring ----
    public static final float SCORE_TIME_WEIGHT = 1.0f;
    public static final String HIGH_SCORE_KEY   = "high_score";
    public static final String PREFS_NAME       = "resonant_prefs";
}
