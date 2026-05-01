package com.resonant.game.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Tessellates a filled annular sector ("donut slice") using ShapeRenderer.triangle().
 * ShapeRenderer.arc() only draws outlines; this fills the band between two radii.
 * <p>
 * Must be called between sr.begin(ShapeType.Filled) and sr.end().
 */
public final class RingSegmentDrawer {

    private static final int STEPS = 18;

    private RingSegmentDrawer() {}

    /**
     * Draws a filled annular sector centered at (cx, cy), spanning angles [startAngle, endAngle]
     * in radians, between inner radius r1 and outer radius r2.
     * Color must be set on the ShapeRenderer beforehand.
     */
    public static void drawFilledAnnularSector(
            ShapeRenderer sr, float cx, float cy,
            float r1, float r2, float startAngle, float endAngle) {

        float delta = (endAngle - startAngle) / STEPS;
        for (int i = 0; i < STEPS; i++) {
            float a0 = startAngle + i * delta;
            float a1 = a0 + delta;
            float c0 = MathUtils.cos(a0), s0 = MathUtils.sin(a0);
            float c1 = MathUtils.cos(a1), s1 = MathUtils.sin(a1);

            float ox0 = cx + r2 * c0, oy0 = cy + r2 * s0;
            float ox1 = cx + r2 * c1, oy1 = cy + r2 * s1;
            float ix0 = cx + r1 * c0, iy0 = cy + r1 * s0;
            float ix1 = cx + r1 * c1, iy1 = cy + r1 * s1;

            sr.triangle(ox0, oy0, ox1, oy1, ix0, iy0);
            sr.triangle(ox1, oy1, ix1, iy1, ix0, iy0);
        }
    }
}
