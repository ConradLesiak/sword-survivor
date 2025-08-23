package com.rgs.swordsurvivor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SwordSurvivorGame extends Game {
    public static final int VIEW_WIDTH = 800;
    public static final int VIEW_HEIGHT = 600;
    public static final int WORLD_WIDTH = 1600;
    public static final int WORLD_HEIGHT = 1200;

    public SpriteBatch batch;
    public BitmapFont font;

    // --- Audio ---
    public Music musicMenu;     // music1.mp3
    public Music musicGame;     // music2.mp3
    public Sound sfxPlayerHit;  // hit1.mp3
    public Sound sfxEnemyHit;   // hit2.mp3
    public Sound sfxPickup;     // pickup.mp3
    public Sound sfxLevel;      // level.mp3

    private int highscoreKills = 0;

    @Override public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        // keep text sharp if you used this earlier
        font.getRegion().getTexture().setFilter(
            com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest);

        // Load audio once
        musicMenu = Gdx.audio.newMusic(Gdx.files.internal("music1.mp3"));
        musicGame = Gdx.audio.newMusic(Gdx.files.internal("music2.mp3"));
        musicMenu.setLooping(true);
        musicGame.setLooping(true);

        sfxPlayerHit = Gdx.audio.newSound(Gdx.files.internal("hit1.mp3"));
        sfxEnemyHit  = Gdx.audio.newSound(Gdx.files.internal("hit2.mp3"));
        sfxPickup    = Gdx.audio.newSound(Gdx.files.internal("pickup.mp3"));
        sfxLevel     = Gdx.audio.newSound(Gdx.files.internal("level.mp3"));

        setScreen(new com.rgs.swordsurvivor.screens.MenuScreen(this));
    }

    public int getHighscoreKills() { return highscoreKills; }
    public void maybeSetHighscore(int kills) { highscoreKills = Math.max(highscoreKills, kills); }

    @Override public void dispose() {
        super.dispose();
        batch.dispose();
        font.dispose();

        // Dispose audio
        musicMenu.dispose();
        musicGame.dispose();
        sfxPlayerHit.dispose();
        sfxEnemyHit.dispose();
        sfxPickup.dispose();
        sfxLevel.dispose();
    }
}
