package com.resonant.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import com.resonant.game.GameConfig;
import com.resonant.game.ResonantGame;
import com.resonant.game.input.GameInputProcessor;
import com.resonant.game.model.GameWorld;
import com.resonant.game.renderer.BackgroundRenderer;
import com.resonant.game.renderer.HudRenderer;
import com.resonant.game.renderer.NexusRenderer;
import com.resonant.game.renderer.PlayerRenderer;
import com.resonant.game.renderer.RingRenderer;

/**
 * Main gameplay screen. Owns the world, all renderers, and the input processor.
 * Transitions to DeathScreen ~0.6s after player death (lets the death VFX play).
 */
public class GameScreen implements Screen {

    private static final float DEATH_TRANSITION_DELAY = 0.6f;

    private final ResonantGame       game;
    private final GameWorld          world = new GameWorld();
    private final BackgroundRenderer bg = new BackgroundRenderer();
    private final RingRenderer       rings = new RingRenderer();
    private final NexusRenderer      nexus = new NexusRenderer();
    private final PlayerRenderer     playerR = new PlayerRenderer();
    private final HudRenderer        hud = new HudRenderer();
    private final GameInputProcessor input;

    public GameScreen(ResonantGame game) {
        this.game = game;
        this.input = new GameInputProcessor(world);
        world.setAudio(game.audio);
        world.init();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
        game.audio.resumeMusic();
    }

    @Override
    public void render(float delta) {
        // Cap delta to avoid catastrophic simulation jumps after a hiccup
        float dt = Math.min(delta, 1f / 30f);
        world.update(dt);

        Gdx.gl.glClearColor(GameConfig.COLOR_BACKGROUND.r, GameConfig.COLOR_BACKGROUND.g,
                GameConfig.COLOR_BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        game.viewport.apply();
        game.sr.setProjectionMatrix(game.camera.combined);
        game.batch.setProjectionMatrix(game.camera.combined);

        float beatPulse = game.audio.getBeatPhase();
        // Convert beat phase (0..1) to a strong-on-1 envelope (peaks early in beat)
        float bp = (float) Math.exp(-beatPulse * 4.0);

        bg.render(game.sr, dt);
        rings.render(game.sr, world.getRings(), world.getActiveRings(), bp);
        nexus.render(game.sr, dt, bp);
        playerR.render(game.sr, world.getPlayer(), world.getRings(), world.isAlive(), world.getDeathFx());
        hud.render(game.batch, game.font, world, game.score.getHighScore());

        if (!world.isAlive() && world.getDeathTime() >= DEATH_TRANSITION_DELAY) {
            float finalScore = world.getScore();
            boolean newBest = game.score.submitScore(finalScore);
            game.setScreen(new DeathScreen(game, finalScore, newBest));
        }
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {}
}
