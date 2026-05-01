package com.resonant.game.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import com.resonant.game.GameConfig;

/**
 * The central pulsing nexus. Multiple additive-blended layers create a soft glow.
 */
public class NexusRenderer {

    private float pulseTime = 0f;

    public void render(ShapeRenderer sr, float delta, float beatPulse) {
        pulseTime += delta;
        float pulse = 0.85f + 0.15f * MathUtils.sin(pulseTime * 4.5f);
        // Beat sync gives an extra punch on each beat
        pulse *= (0.85f + 0.20f * beatPulse);

        float cx = GameConfig.CENTER_X;
        float cy = GameConfig.CENTER_Y;
        float r  = GameConfig.NEXUS_RADIUS * pulse;
        Color nc = GameConfig.COLOR_NEXUS;

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(nc.r, nc.g, nc.b, 0.05f * pulse); sr.circle(cx, cy, r * 4.0f, 32);
        sr.setColor(nc.r, nc.g, nc.b, 0.10f * pulse); sr.circle(cx, cy, r * 2.5f, 24);
        sr.setColor(nc.r, nc.g, nc.b, 0.30f * pulse); sr.circle(cx, cy, r * 1.5f, 18);
        sr.setColor(nc.r, nc.g, nc.b, 1.00f);         sr.circle(cx, cy, r,         16);
        sr.end();

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
