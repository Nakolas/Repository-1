package com.resonant.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;

import com.resonant.game.GameConfig;
import com.resonant.game.ResonantGame;

/**
 * Shown after the player dies. Displays final score and best, with a brief input lockout
 * so accidental taps don't immediately restart.
 */
public class DeathScreen implements Screen {

    private static final float INPUT_LOCKOUT = 0.4f;

    private final ResonantGame game;
    private final float        finalScore;
    private final boolean      newBest;
    private final GlyphLayout  layout = new GlyphLayout();
    private float              elapsed = 0f;

    public DeathScreen(ResonantGame game, float finalScore, boolean newBest) {
        this.game = game;
        this.finalScore = finalScore;
        this.newBest = newBest;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int x, int y, int p, int b) {
                if (elapsed >= INPUT_LOCKOUT) {
                    game.setScreen(new GameScreen(game));
                }
                return true;
            }
            @Override public boolean keyDown(int keycode) {
                if (elapsed >= INPUT_LOCKOUT) {
                    game.setScreen(new GameScreen(game));
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        Gdx.gl.glClearColor(GameConfig.COLOR_BACKGROUND.r, GameConfig.COLOR_BACKGROUND.g,
                GameConfig.COLOR_BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();

        // "SIGNAL LOST"
        game.titleFont.getData().setScale(2.0f);
        game.titleFont.setColor(GameConfig.COLOR_OBSTACLE);
        layout.setText(game.titleFont, "SIGNAL LOST");
        game.titleFont.draw(game.batch, "SIGNAL LOST",
                GameConfig.CENTER_X - layout.width * 0.5f,
                GameConfig.WORLD_HEIGHT - 90f);

        // Final score
        String scoreText = String.format("%06d", (int) finalScore);
        game.font.getData().setScale(2.4f);
        game.font.setColor(GameConfig.COLOR_TEXT);
        layout.setText(game.font, scoreText);
        game.font.draw(game.batch, scoreText,
                GameConfig.CENTER_X - layout.width * 0.5f,
                GameConfig.WORLD_HEIGHT - 170f);

        // High score / new best
        float hs = game.score.getHighScore();
        if (newBest) {
            float pulse = 0.7f + 0.3f * MathUtils.sin(elapsed * 6f);
            game.font.getData().setScale(1.3f);
            game.font.setColor(GameConfig.COLOR_NEW_BEST.r, GameConfig.COLOR_NEW_BEST.g,
                    GameConfig.COLOR_NEW_BEST.b, pulse);
            layout.setText(game.font, "NEW BEST!");
            game.font.draw(game.batch, "NEW BEST!",
                    GameConfig.CENTER_X - layout.width * 0.5f,
                    GameConfig.WORLD_HEIGHT - 220f);
        } else if (hs > 0) {
            String hsText = String.format("BEST  %06d", (int) hs);
            game.font.getData().setScale(1.1f);
            game.font.setColor(GameConfig.COLOR_TEXT_DIM);
            layout.setText(game.font, hsText);
            game.font.draw(game.batch, hsText,
                    GameConfig.CENTER_X - layout.width * 0.5f,
                    GameConfig.WORLD_HEIGHT - 220f);
        }

        // Tap-to-retry prompt (after lockout)
        if (elapsed >= INPUT_LOCKOUT) {
            float blink = 0.45f + 0.55f * MathUtils.sin(elapsed * 4f);
            game.font.getData().setScale(1.15f);
            game.font.setColor(1f, 1f, 1f, blink);
            layout.setText(game.font, "TAP TO RETRY");
            game.font.draw(game.batch, "TAP TO RETRY",
                    GameConfig.CENTER_X - layout.width * 0.5f, 70f);
        }

        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {}
}
