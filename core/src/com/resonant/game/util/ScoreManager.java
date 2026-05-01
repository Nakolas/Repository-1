package com.resonant.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import com.resonant.game.GameConfig;

/**
 * Persists the high score across sessions via LibGDX {@link Preferences}.
 */
public class ScoreManager {

    private final Preferences prefs;

    public ScoreManager() {
        prefs = Gdx.app.getPreferences(GameConfig.PREFS_NAME);
    }

    public float getHighScore() {
        return prefs.getFloat(GameConfig.HIGH_SCORE_KEY, 0f);
    }

    /**
     * @return true if the submitted score was a new best.
     */
    public boolean submitScore(float score) {
        if (score > getHighScore()) {
            prefs.putFloat(GameConfig.HIGH_SCORE_KEY, score);
            prefs.flush();
            return true;
        }
        return false;
    }
}
