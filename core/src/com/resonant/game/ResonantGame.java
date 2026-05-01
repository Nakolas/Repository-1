package com.resonant.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.resonant.game.audio.AudioEngine;
import com.resonant.game.screen.MenuScreen;
import com.resonant.game.util.ScoreManager;

/**
 * Game entry point. Owns shared rendering resources and routes to the active Screen.
 */
public class ResonantGame extends Game {

    public SpriteBatch        batch;
    public ShapeRenderer      sr;
    public BitmapFont         font;
    public BitmapFont         titleFont;
    public OrthographicCamera camera;
    public Viewport           viewport;
    public AudioEngine        audio;
    public ScoreManager       score;

    @Override
    public void create() {
        camera   = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        camera.position.set(GameConfig.WORLD_WIDTH * 0.5f, GameConfig.WORLD_HEIGHT * 0.5f, 0f);
        camera.update();

        batch = new SpriteBatch();
        sr    = new ShapeRenderer();
        font  = new BitmapFont();
        font.getData().setScale(1.0f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.6f);

        score = new ScoreManager();
        audio = new AudioEngine();
        audio.start();

        setScreen(new MenuScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (batch     != null) batch.dispose();
        if (sr        != null) sr.dispose();
        if (font      != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (audio     != null) audio.dispose();
    }
}
