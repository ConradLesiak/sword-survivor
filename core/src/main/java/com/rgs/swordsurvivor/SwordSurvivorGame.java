package com.rgs.swordsurvivor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.rgs.swordsurvivor.screens.MenuScreen;

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

    // Global mute state (persisted)
    public boolean muted = false;

    private int highscoreKills = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
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

        // Load persisted mute setting
        Preferences prefs = Gdx.app.getPreferences("sword_survivor_settings");
        muted = prefs.getBoolean("muted", false);
        updateMusicVolumes();

        setScreen(new MenuScreen(this));
    }

    public int getHighscoreKills() { return highscoreKills; }
    public void maybeSetHighscore(int kills) { highscoreKills = Math.max(highscoreKills, kills); }

    /** Centralized SFX playback that respects global mute. */
    public void playSfx(Sound sfx) {
        if (sfx == null) return;
        if (muted) return;
        sfx.play(1f);
    }

    /** Apply mute state to music volumes. */
    public void updateMusicVolumes() {
        float vol = muted ? 0f : 1f;
        if (musicMenu != null) musicMenu.setVolume(vol);
        if (musicGame != null) musicGame.setVolume(vol);
    }

    /** Set + persist mute state, and immediately apply to currently playing music. */
    public void setMuted(boolean m) {
        muted = m;
        updateMusicVolumes();
        Preferences prefs = Gdx.app.getPreferences("sword_survivor_settings");
        prefs.putBoolean("muted", muted);
        prefs.flush();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        font.dispose();

        if (musicMenu != null) musicMenu.dispose();
        if (musicGame != null) musicGame.dispose();
        if (sfxPlayerHit != null) sfxPlayerHit.dispose();
        if (sfxEnemyHit  != null) sfxEnemyHit.dispose();
        if (sfxPickup    != null) sfxPickup.dispose();
        if (sfxLevel     != null) sfxLevel.dispose();
    }
}
