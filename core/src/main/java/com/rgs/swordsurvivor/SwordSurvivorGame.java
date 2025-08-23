package com.rgs.swordsurvivor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rgs.swordsurvivor.screens.MenuScreen;

public class SwordSurvivorGame extends Game {
    // Viewport resolution
    public static final int VIEW_WIDTH = 800;
    public static final int VIEW_HEIGHT = 600;

    // Actual playable world size
    public static final int WORLD_WIDTH = 1600;
    public static final int WORLD_HEIGHT = 1200;

    public SpriteBatch batch;
    public BitmapFont font;
    private Preferences prefs;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        prefs = Gdx.app.getPreferences("SwordSurvivorPreferences");
        if (!prefs.contains("highscoreKills")) {
            prefs.putInteger("highscoreKills", 0).flush();
        }
        setScreen(new MenuScreen(this));
    }

    public int getHighscoreKills() {
        return prefs.getInteger("highscoreKills", 0);
    }

    public void maybeSetHighscore(int kills) {
        int cur = getHighscoreKills();
        if (kills > cur) {
            prefs.putInteger("highscoreKills", kills).flush();
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        batch.dispose();
        font.dispose();
    }
}
