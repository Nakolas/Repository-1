package com.resonant.game.renderer;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.resonant.game.GameConfig;
import com.resonant.game.model.GameWorld;

/**
 * In-game HUD: score (top center) and speed multiplier (top right).
 * Uses GlyphLayout to measure text for centering without per-frame allocations.
 */
public class HudRenderer {

    private final GlyphLayout layout = new GlyphLayout();

    public void render(SpriteBatch batch, BitmapFont font, GameWorld world, float highScore) {
        batch.begin();

        // Score, top center
        String scoreText = String.format("%06d", (int) world.getScore());
        font.setColor(GameConfig.COLOR_TEXT);
        font.getData().setScale(1.4f);
        layout.setText(font, scoreText);
        font.draw(batch, scoreText,
                GameConfig.CENTER_X - layout.width * 0.5f,
                GameConfig.WORLD_HEIGHT - 14f);

        // Speed multiplier, top right
        String multText = String.format("x%.1f", world.getSpeedMultiplier());
        font.setColor(GameConfig.COLOR_NEXUS);
        font.getData().setScale(1.0f);
        layout.setText(font, multText);
        font.draw(batch, multText,
                GameConfig.WORLD_WIDTH - layout.width - 16f,
                GameConfig.WORLD_HEIGHT - 16f);

        // High score, top left, dimmed
        String hsText = String.format("BEST %06d", (int) highScore);
        font.setColor(GameConfig.COLOR_TEXT_DIM);
        font.getData().setScale(0.9f);
        font.draw(batch, hsText, 16f, GameConfig.WORLD_HEIGHT - 16f);

        // Reset font scale (other code may depend on default)
        font.getData().setScale(1.0f);
        batch.end();
    }
}
