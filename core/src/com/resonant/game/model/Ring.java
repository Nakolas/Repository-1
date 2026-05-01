package com.resonant.game.model;

import com.badlogic.gdx.math.MathUtils;

import com.resonant.game.GameConfig;

/**
 * One concentric ring of obstacles. Holds geometry, rotation state, and per-segment occupancy.
 * Provides O(1) collision queries via {@link #isObstacleAt(float)}.
 */
public class Ring {

    public final int       ringIndex;
    public final float     innerRadius;
    public final float     outerRadius;
    public       float     rotationSpeed;       // rad/sec, signed
    public       float     currentAngleOffset;  // accumulated rotation, mod 2π
    public final boolean[] isObstacle;

    public Ring(int ringIndex, float innerRadius, float outerRadius, float rotationSpeed) {
        this.ringIndex     = ringIndex;
        this.innerRadius   = innerRadius;
        this.outerRadius   = outerRadius;
        this.rotationSpeed = rotationSpeed;
        this.isObstacle    = new boolean[GameConfig.SEGMENTS_PER_RING];
    }

    public void update(float delta) {
        currentAngleOffset += rotationSpeed * delta;
        // Normalize to [0, 2π)
        currentAngleOffset = ((currentAngleOffset % MathUtils.PI2) + MathUtils.PI2) % MathUtils.PI2;
    }

    /**
     * @return The world-space angle (radians) at which segment {@code segmentIndex} starts.
     */
    public float getSegmentStartAngle(int segmentIndex) {
        return currentAngleOffset + segmentIndex * GameConfig.SEGMENT_ANGLE;
    }

    /**
     * Returns the segment index (0..7) that contains the given world-space angle, accounting
     * for the ring's current rotation offset.
     */
    public int getSegmentIndexAt(float worldAngle) {
        float rel = (worldAngle - currentAngleOffset) % MathUtils.PI2;
        if (rel < 0) rel += MathUtils.PI2;
        int seg = (int) (rel / GameConfig.SEGMENT_ANGLE);
        return MathUtils.clamp(seg, 0, GameConfig.SEGMENTS_PER_RING - 1);
    }

    /**
     * @return true if the segment at the given world-space angle is a solid obstacle.
     */
    public boolean isObstacleAt(float worldAngle) {
        return isObstacle[getSegmentIndexAt(worldAngle)];
    }

    /**
     * Generates a new layout. Each segment is marked obstacle with probability {@code density}.
     * Guarantees at least one consecutive 2-segment gap (passable corridor).
     */
    public void regenerate(float density) {
        for (int i = 0; i < isObstacle.length; i++) {
            isObstacle[i] = MathUtils.random() < density;
        }

        // Ensure at least one consecutive 2-segment gap: scan, and if absent, force-clear two.
        if (!hasConsecutiveGap(2)) {
            // Find the longest gap and extend it; or, if the ring is entirely solid, clear two.
            int bestGapStart = 0, bestGapLen = 0;
            int curGapStart = -1, curGapLen = 0;
            int N = isObstacle.length;
            for (int i = 0; i < N * 2; i++) {
                int idx = i % N;
                if (!isObstacle[idx]) {
                    if (curGapLen == 0) curGapStart = idx;
                    curGapLen++;
                    if (curGapLen > bestGapLen) {
                        bestGapLen  = curGapLen;
                        bestGapStart = curGapStart;
                    }
                } else {
                    curGapLen = 0;
                }
            }
            if (bestGapLen == 0) {
                // Entirely solid — clear two adjacent segments at random
                int s = MathUtils.random(N - 1);
                isObstacle[s]           = false;
                isObstacle[(s + 1) % N] = false;
            } else {
                // Extend the existing gap by one segment in either direction
                int extend = (bestGapStart + bestGapLen) % N;
                isObstacle[extend] = false;
            }
        }
    }

    private boolean hasConsecutiveGap(int minLen) {
        int N = isObstacle.length;
        // Wrap-aware scan: examine 2N indices to handle wraparound
        int run = 0;
        for (int i = 0; i < N * 2; i++) {
            if (!isObstacle[i % N]) {
                run++;
                if (run >= minLen) return true;
            } else {
                run = 0;
            }
        }
        return false;
    }
}
