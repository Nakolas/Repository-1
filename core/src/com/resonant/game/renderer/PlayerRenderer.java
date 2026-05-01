package com.resonant.game.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.resonant.game.GameConfig;
import com.resonant.game.model.DeathParticles;
import com.resonant.game.model.Player;
import com.resonant.game.model.Ring;

/**
 * Renders the player's trail, glow, core particle, and the death VFX particles.
 * All drawn with additive blending for a luminous look.
 */
public class PlayerRenderer {

    public void render(ShapeRenderer sr, Player player, Ring[] rings, boolean alive,
                       DeathParticles deathFx) {
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        Color c = GameConfig.COLOR_PLAYER;

        if (alive) {
            // Trail (oldest -> newest)
            player.iterateTrail((tx, ty, age) -> {
                float alpha = age * 0.7f;
                float radius = GameConfig.PLAYER_RADIUS * (0.25f + 0.75f * age);
                sr.setColor(c.r, c.g, c.b, alpha);
                sr.circle(tx, ty, radius, 8);
            });

            float px = player.worldX(rings);
            float py = player.worldY(rings);

            // Glow halos
            sr.setColor(c.r, c.g, c.b, 0.10f);
            sr.circle(px, py, GameConfig.PLAYER_RADIUS * 4.0f, 18);
            sr.setColor(c.r, c.g, c.b, 0.30f);
            sr.circle(px, py, GameConfig.PLAYER_RADIUS * 2.0f, 14);

            // Switch flash (briefly white)
            if (player.flashTimer > 0f) {
                sr.setColor(1f, 1f, 1f, Math.min(1f, player.flashTimer * 12f));
                sr.circle(px, py, GameConfig.PLAYER_RADIUS * 2.4f, 14);
            }

            // Core
            sr.setColor(c.r, c.g, c.b, 1.0f);
            sr.circle(px, py, GameConfig.PLAYER_RADIUS, 12);
        }

        // Death VFX particles (drawn whether alive or not — life will be 0 if not active)
        for (int i = 0; i < DeathParticles.COUNT; i++) {
            if (deathFx.life[i] <= 0f) continue;
            float a = deathFx.life[i] / DeathParticles.LIFETIME;
            sr.setColor(c.r, c.g, c.b, a);
            sr.circle(deathFx.x[i], deathFx.y[i], GameConfig.PLAYER_RADIUS * (0.4f + 0.8f * a), 8);
        }

        sr.end();
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
