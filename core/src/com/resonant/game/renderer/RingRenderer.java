package com.resonant.game.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.resonant.game.GameConfig;
import com.resonant.game.model.Ring;
import com.resonant.game.util.RingSegmentDrawer;

/**
 * Renders all active rings: solid obstacle segments, dim gap segments, and ring boundary lines.
 * Solid segments get an additive 3-layer pseudo-bloom glow.
 */
public class RingRenderer {

    public void render(ShapeRenderer sr, Ring[] rings, int activeCount, float beatPulse) {
        float cx = GameConfig.CENTER_X;
        float cy = GameConfig.CENTER_Y;

        // ---- Pass 1: Gap segments (dim background "guide") ----
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(GameConfig.COLOR_GAP);
        for (int ri = 0; ri < activeCount; ri++) {
            Ring ring = rings[ri];
            for (int seg = 0; seg < GameConfig.SEGMENTS_PER_RING; seg++) {
                if (ring.isObstacle[seg]) continue;
                float a0 = ring.getSegmentStartAngle(seg) + GameConfig.SEGMENT_INSET;
                float a1 = ring.getSegmentStartAngle(seg) + GameConfig.SEGMENT_ANGLE - GameConfig.SEGMENT_INSET;
                RingSegmentDrawer.drawFilledAnnularSector(sr, cx, cy,
                        ring.innerRadius, ring.outerRadius, a0, a1);
            }
        }
        sr.end();

        // ---- Pass 2: Solid segments with additive glow ----
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // Outer (most diffuse) glow layer
        sr.begin(ShapeRenderer.ShapeType.Filled);
        Color obs = GameConfig.COLOR_OBSTACLE;
        sr.setColor(obs.r, obs.g, obs.b, 0.10f * (0.85f + 0.15f * beatPulse));
        for (int ri = 0; ri < activeCount; ri++) {
            Ring ring = rings[ri];
            for (int seg = 0; seg < GameConfig.SEGMENTS_PER_RING; seg++) {
                if (!ring.isObstacle[seg]) continue;
                float a0 = ring.getSegmentStartAngle(seg) - GameConfig.SEGMENT_INSET;
                float a1 = ring.getSegmentStartAngle(seg) + GameConfig.SEGMENT_ANGLE + GameConfig.SEGMENT_INSET;
                RingSegmentDrawer.drawFilledAnnularSector(sr, cx, cy,
                        ring.innerRadius - 4f, ring.outerRadius + 4f, a0, a1);
            }
        }
        sr.end();

        // Mid glow
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(obs.r, obs.g, obs.b, 0.30f * (0.85f + 0.15f * beatPulse));
        for (int ri = 0; ri < activeCount; ri++) {
            Ring ring = rings[ri];
            for (int seg = 0; seg < GameConfig.SEGMENTS_PER_RING; seg++) {
                if (!ring.isObstacle[seg]) continue;
                float a0 = ring.getSegmentStartAngle(seg) + GameConfig.SEGMENT_INSET * 0.5f;
                float a1 = ring.getSegmentStartAngle(seg) + GameConfig.SEGMENT_ANGLE - GameConfig.SEGMENT_INSET * 0.5f;
                RingSegmentDrawer.drawFilledAnnularSector(sr, cx, cy,
                        ring.innerRadius - 1.5f, ring.outerRadius + 1.5f, a0, a1);
            }
        }
        sr.end();

        // Restore normal blending for the core fill
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(obs);
        for (int ri = 0; ri < activeCount; ri++) {
            Ring ring = rings[ri];
            for (int seg = 0; seg < GameConfig.SEGMENTS_PER_RING; seg++) {
                if (!ring.isObstacle[seg]) continue;
                float a0 = ring.getSegmentStartAngle(seg) + GameConfig.SEGMENT_INSET;
                float a1 = ring.getSegmentStartAngle(seg) + GameConfig.SEGMENT_ANGLE - GameConfig.SEGMENT_INSET;
                RingSegmentDrawer.drawFilledAnnularSector(sr, cx, cy,
                        ring.innerRadius, ring.outerRadius, a0, a1);
            }
        }
        sr.end();

        // ---- Pass 3: Ring boundary lines ----
        Gdx.gl.glLineWidth(1.5f);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(GameConfig.COLOR_RING_EDGE);
        for (int ri = 0; ri < activeCount; ri++) {
            Ring ring = rings[ri];
            sr.circle(cx, cy, ring.innerRadius, 64);
            sr.circle(cx, cy, ring.outerRadius, 64);
        }
        sr.end();
    }
}
