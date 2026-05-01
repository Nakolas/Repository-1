package com.resonant.game.renderer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import com.resonant.game.GameConfig;

/**
 * Slow-drifting starfield. 80 procedural points generated once, with subtle parallax drift
 * on the X axis to give a feeling of motion.
 */
public class BackgroundRenderer {

    private static final int STAR_COUNT = 80;

    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starBrightness = new float[STAR_COUNT];
    private final float[] starDriftSpeed = new float[STAR_COUNT];

    public BackgroundRenderer() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]          = MathUtils.random() * GameConfig.WORLD_WIDTH;
            starY[i]          = MathUtils.random() * GameConfig.WORLD_HEIGHT;
            starBrightness[i] = MathUtils.random(0.25f, 0.85f);
            starDriftSpeed[i] = MathUtils.random(2f, 12f);
        }
    }

    public void render(ShapeRenderer sr, float delta) {
        // Drift stars left; wrap when off-screen
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] -= starDriftSpeed[i] * delta;
            if (starX[i] < 0f) {
                starX[i] += GameConfig.WORLD_WIDTH;
                starY[i] = MathUtils.random() * GameConfig.WORLD_HEIGHT;
            }
        }

        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < STAR_COUNT; i++) {
            float b = starBrightness[i];
            sr.setColor(b * 0.7f, b * 0.75f, b * 0.95f, 0.55f * b);
            // Tiny circles look better than points for visibility on high-DPI
            float size = b * 1.6f;
            sr.circle(starX[i], starY[i], size, 6);
        }
        sr.end();
    }
}
