package com.resonant.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;

import com.resonant.game.GameConfig;
import com.resonant.game.ResonantGame;
import com.resonant.game.model.Ring;
import com.resonant.game.renderer.BackgroundRenderer;
import com.resonant.game.renderer.NexusRenderer;
import com.resonant.game.renderer.RingRenderer;

/**
 * Title screen. Animated decorative rings, title text, "TAP TO BEGIN", high score.
 */
public class MenuScreen implements Screen {

    private final ResonantGame      game;
    private final BackgroundRenderer bg = new BackgroundRenderer();
    private final RingRenderer       rings = new RingRenderer();
    private final NexusRenderer      nexus = new NexusRenderer();
    private final GlyphLayout        layout = new GlyphLayout();

    private final Ring[] decorRings = new Ring[3];
    private float blinkTime = 0f;

    public MenuScreen(ResonantGame game) {
        this.game = game;

        // Build a few decorative rings for the menu (slow rotation, stable layouts)
        for (int i = 0; i < 3; i++) {
            float inner = GameConfig.RING_INNER_BASE + i * (GameConfig.RING_THICKNESS + GameConfig.RING_GAP);
            float outer = inner + GameConfig.RING_THICKNESS;
            decorRings[i] = new Ring(i, inner, outer, GameConfig.RING_SPEED_MULT[i] * 0.5f);
            decorRings[i].regenerate(0.45f);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int x, int y, int p, int b) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            @Override public boolean keyDown(int keycode) {
                game.setScreen(new GameScreen(game));
                return true;
            }
        });
        game.audio.resumeMusic();
    }

    @Override
    public void render(float delta) {
        blinkTime += delta;

        for (Ring r : decorRings) r.update(delta);

        Gdx.gl.glClearColor(GameConfig.COLOR_BACKGROUND.r, GameConfig.COLOR_BACKGROUND.g,
                GameConfig.COLOR_BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        game.viewport.apply();
        game.sr.setProjectionMatrix(game.camera.combined);
        game.batch.setProjectionMatrix(game.camera.combined);

        bg.render(game.sr, delta);
        rings.render(game.sr, decorRings, decorRings.length, game.audio.getBeatPhase());
        nexus.render(game.sr, delta, game.audio.getBeatPhase());

        // Title + prompts
        game.batch.begin();
        game.titleFont.setColor(GameConfig.COLOR_TEXT);
        layout.setText(game.titleFont, "RESONANT");
        game.titleFont.draw(game.batch, "RESONANT",
                GameConfig.CENTER_X - layout.width * 0.5f,
                GameConfig.WORLD_HEIGHT - 70f);

        // Subtitle
        game.font.getData().setScale(0.95f);
        game.font.setColor(GameConfig.COLOR_TEXT_DIM);
        layout.setText(game.font, "an orbital descent");
        game.font.draw(game.batch, "an orbital descent",
                GameConfig.CENTER_X - layout.width * 0.5f,
                GameConfig.WORLD_HEIGHT - 110f);

        // High score
        float hs = game.score.getHighScore();
        if (hs > 0) {
            String hsText = String.format("BEST  %06d", (int) hs);
            game.font.getData().setScale(1.05f);
            game.font.setColor(GameConfig.COLOR_NEW_BEST);
            layout.setText(game.font, hsText);
            game.font.draw(game.batch, hsText,
                    GameConfig.CENTER_X - layout.width * 0.5f, 90f);
        }

        // Blinking prompt
        float blinkAlpha = 0.55f + 0.45f * MathUtils.sin(blinkTime * 3.6f);
        game.font.setColor(1f, 1f, 1f, blinkAlpha);
        game.font.getData().setScale(1.15f);
        layout.setText(game.font, "TAP TO BEGIN");
        game.font.draw(game.batch, "TAP TO BEGIN",
                GameConfig.CENTER_X - layout.width * 0.5f, 50f);

        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {}
}
