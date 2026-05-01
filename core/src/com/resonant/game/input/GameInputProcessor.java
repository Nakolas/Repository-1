package com.resonant.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import com.resonant.game.model.GameWorld;

/**
 * Routes screen taps to the world: left half tap -> move inward, right half -> move outward.
 * Also supports keyboard arrows for desktop dev/testing.
 */
public class GameInputProcessor extends InputAdapter {

    private final GameWorld world;

    public GameInputProcessor(GameWorld world) {
        this.world = world;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (screenX < Gdx.graphics.getWidth() * 0.5f) {
            world.playerMoveInward();
        } else {
            world.playerMoveOutward();
        }
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
            case Input.Keys.A:
            case Input.Keys.DOWN:
            case Input.Keys.S:
                world.playerMoveInward();
                return true;
            case Input.Keys.RIGHT:
            case Input.Keys.D:
            case Input.Keys.UP:
            case Input.Keys.W:
                world.playerMoveOutward();
                return true;
            default:
                return false;
        }
    }
}
