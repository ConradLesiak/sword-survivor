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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.rgs.swordsurvivor.SwordSurvivorGame;

public class MenuScreen implements Screen {
    private final SwordSurvivorGame game;
    private final ExtendViewport viewport;
    private final Stage stage;

    private Texture bgTex, btnUp, btnOver, btnDown, cardTex;

    public MenuScreen(SwordSurvivorGame game) {
        this.game = game;
        viewport = new ExtendViewport(SwordSurvivorGame.VIEW_WIDTH, SwordSurvivorGame.VIEW_HEIGHT);
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);
        buildUI();
    }

    private void buildUI() {
        // --- background panel ---
        bgTex = makeSolid(0.08f, 0.09f, 0.12f, 1f);
        TextureRegionDrawable bg = new TextureRegionDrawable(new TextureRegion(bgTex));

        // --- card behind title/highscore (optional polish) ---
        cardTex = makeSolid(0.12f, 0.14f, 0.18f, 1f);
        TextureRegionDrawable card = new TextureRegionDrawable(new TextureRegion(cardTex));

        // --- button visuals (no skin file needed) ---
        btnUp   = makeSolid(0.18f, 0.22f, 0.28f, 1f);
        btnOver = makeSolid(0.24f, 0.30f, 0.38f, 1f);
        btnDown = makeSolid(0.14f, 0.16f, 0.20f, 1f);
        TextButtonStyle btnStyle = new TextButtonStyle(
            new TextureRegionDrawable(new TextureRegion(btnUp)),
            new TextureRegionDrawable(new TextureRegion(btnDown)),
            new TextureRegionDrawable(new TextureRegion(btnOver)),
            game.font
        );

        LabelStyle labelStyle = new LabelStyle(game.font, Color.WHITE);

        // --- widgets ---
        Label title = new Label("Sword Survivor", labelStyle);
        title.setAlignment(Align.center);
        title.setFontScale(2.2f);

        Label highscore = new Label("High Score (kills): " + game.getHighscoreKills(), labelStyle);
        highscore.setAlignment(Align.center);
        highscore.setFontScale(1.1f);

        TextButton playBtn = new TextButton("Play", btnStyle);
        TextButton quitBtn = new TextButton("Quit", btnStyle);

        // --- layout ---
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(bg);
        stage.addActor(root);

        Table header = new Table();
        header.setBackground(card);
        header.pad(18f).defaults().pad(6f);
        header.add(title).row();
        header.add(highscore);

        root.add(header).expandX().top().padTop(40f).width(Math.min(520f, viewport.getWorldWidth() - 40f)).row();
        root.add().expand().row();

        Table buttons = new Table();
        buttons.defaults().width(220f).height(48f).pad(6f);
        buttons.add(playBtn).row();
        buttons.add(quitBtn).row();

        root.add(buttons).bottom().padBottom(60f);

        // --- input ---
        playBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });
        quitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    private Texture makeSolid(float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(r,g,b,a); pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override public void show() {
        // Music routing: menu music on
        if (!game.musicMenu.isPlaying()) game.musicMenu.play();
        if (game.musicGame.isPlaying()) game.musicGame.stop();
    }

    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
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

    @Override public void dispose() {
        stage.dispose();
        bgTex.dispose(); btnUp.dispose(); btnOver.dispose(); btnDown.dispose(); cardTex.dispose();
    }
}
