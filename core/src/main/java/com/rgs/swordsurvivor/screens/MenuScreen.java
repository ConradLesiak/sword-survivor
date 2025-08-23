package com.rgs.swordsurvivor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rgs.swordsurvivor.SwordSurvivorGame;

public class MenuScreen implements Screen {
    private final SwordSurvivorGame game;
    private final FitViewport viewport;
    private final Stage stage;

    private Texture bgTexture;

    public MenuScreen(SwordSurvivorGame game) {
        this.game = game;
        // IMPORTANT: use VIEW_* here (not WORLD_*)
        viewport = new FitViewport(SwordSurvivorGame.VIEW_WIDTH, SwordSurvivorGame.VIEW_HEIGHT);
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);
        buildUI();
    }

    private void buildUI() {
        LabelStyle style = new LabelStyle(game.font, Color.WHITE);

        Label titleLbl = new Label("Sword Survivor", style);
        titleLbl.setAlignment(Align.center);
        titleLbl.setFontScale(2.2f);

        Label highscoreLbl = new Label("High Score (kills): " + game.getHighscoreKills(), style);
        highscoreLbl.setAlignment(Align.center);
        highscoreLbl.setFontScale(1.1f);

        Label controls1 = new Label("Move: WASD / Arrow Keys", style);
        Label controls2 = new Label("Aim: Mouse  •  Auto-attacks toward cursor", style);
        Label controls3 = new Label("Collect yellow orbs to level up; choose 1 of 3 boons.", style);
        controls1.setAlignment(Align.center);
        controls2.setAlignment(Align.center);
        controls3.setAlignment(Align.center);

        Label startLbl = new Label("Click / Enter / Space to Start", style);
        startLbl.setAlignment(Align.center);
        startLbl.setFontScale(1.05f);

        // Background color panel
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0.08f, 0.09f, 0.12f, 1f);
        pm.fill();
        bgTexture = new Texture(pm);
        pm.dispose();
        TextureRegionDrawable bgDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(bgDrawable);
        stage.addActor(root);

        // Layout
        Table top = new Table();
        top.add(titleLbl).padTop(30f).row();
        top.add(highscoreLbl).padTop(16f);
        root.add(top).expandX().top().padTop(20f).row();

        root.add().expand().row();

        Table controls = new Table();
        controls.add(controls1).row();
        controls.add(controls2).padTop(6f).row();
        controls.add(controls3).padTop(6f);
        root.add(controls).expandX().bottom().padBottom(90f).row();

        root.add(startLbl).expandX().bottom().padBottom(40f);

        // Click anywhere to start
        root.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });
    }

    @Override public void show() {
        // start/ensure menu music
        if (!game.musicMenu.isPlaying()) {
            game.musicMenu.play();
        }
        // make sure gameplay music is off in menu
        if (game.musicGame.isPlaying()) game.musicGame.stop();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            return;
        }
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); bgTexture.dispose(); }
}
