package com.resonant.game.model;

import com.badlogic.gdx.math.MathUtils;

/**
 * Lightweight particle burst for the death VFX.
 * Allocated once and reused via spawn()/clear() to avoid per-death GC.
 */
public class DeathParticles {

    public static final int COUNT = 14;
    public static final float LIFETIME = 0.6f;

    public final float[] x = new float[COUNT];
    public final float[] y = new float[COUNT];
    public final float[] vx = new float[COUNT];
    public final float[] vy = new float[COUNT];
    public final float[] life = new float[COUNT];

    public void clear() {
        for (int i = 0; i < COUNT; i++) life[i] = 0f;
    }

    public void spawn(float originX, float originY) {
        for (int i = 0; i < COUNT; i++) {
            float angle = (i / (float) COUNT) * MathUtils.PI2 + MathUtils.random(-0.3f, 0.3f);
            float speed = MathUtils.random(110f, 220f);
            x[i] = originX;
            y[i] = originY;
            vx[i] = MathUtils.cos(angle) * speed;
            vy[i] = MathUtils.sin(angle) * speed;
            life[i] = LIFETIME;
        }
    }

    public void update(float delta) {
        for (int i = 0; i < COUNT; i++) {
            if (life[i] <= 0f) continue;
            x[i] += vx[i] * delta;
            y[i] += vy[i] * delta;
            // Damping
            vx[i] *= 0.94f;
            vy[i] *= 0.94f;
            life[i] -= delta;
        }
    }
}
