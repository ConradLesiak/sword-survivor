package com.rgs.swordsurvivor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
    private Texture cbBoxOff, cbBoxOn;
    private CheckBox muteBox;

    public MenuScreen(SwordSurvivorGame game) {
        this.game = game;
        viewport = new ExtendViewport(SwordSurvivorGame.VIEW_WIDTH, SwordSurvivorGame.VIEW_HEIGHT);
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        buildUI();
    }

    private void buildUI() {
        // Background
        bgTex = makeSolid(0.08f, 0.09f, 0.12f, 1f);
        TextureRegionDrawable bg = new TextureRegionDrawable(new TextureRegion(bgTex));
        cardTex = makeSolid(0.12f, 0.14f, 0.18f, 1f);
        TextureRegionDrawable card = new TextureRegionDrawable(new TextureRegion(cardTex));

        // Buttons
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

        Label title = new Label("Sword Survivor", labelStyle);
        title.setAlignment(Align.center);
        title.setFontScale(2.2f);

        Label highscore = new Label("High Score (kills): " + game.getHighscoreKills(), labelStyle);
        highscore.setAlignment(Align.center);
        highscore.setFontScale(1.1f);

        TextButton playBtn = new TextButton("Play", btnStyle);
        TextButton quitBtn = new TextButton("Quit", btnStyle);

        // Mute checkbox
        cbBoxOff = makeCheckboxBox(22, Color.WHITE, 0.9f);
        cbBoxOn  = makeCheckboxChecked(22, Color.WHITE);
        CheckBoxStyle cbStyle = new CheckBoxStyle();
        cbStyle.checkboxOff = new TextureRegionDrawable(new TextureRegion(cbBoxOff));
        cbStyle.checkboxOn  = new TextureRegionDrawable(new TextureRegion(cbBoxOn));
        cbStyle.font = game.font;
        cbStyle.fontColor = Color.WHITE;

        muteBox = new CheckBox("Mute", cbStyle);
        muteBox.getLabel().setFontScale(1f);
        muteBox.setChecked(game.muted);

        muteBox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setMuted(muteBox.isChecked());
            }
        });

        // Layout
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
        buttons.add(muteBox).width(220f).height(36f).padTop(6f).center().row();

        root.add(buttons).bottom().padBottom(60f);

        // Input
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

    private Texture makeCheckboxBox(int size, Color color, float alpha) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0,0,0,0); pm.fill();
        pm.setColor(color.r, color.g, color.b, alpha);
        int t = Math.max(2, size / 12);
        for (int i = 0; i < t; i++) pm.drawRectangle(i, i, size - 2*i, size - 2*i);
        Texture tex = new Texture(pm); pm.dispose(); return tex;
    }

    private Texture makeCheckboxChecked(int size, Color color) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0,0,0,0); pm.fill();
        pm.setColor(color);
        int t = Math.max(2, size / 12);
        for (int i = 0; i < t; i++) pm.drawRectangle(i, i, size - 2*i, size - 2*i);
        int pad = Math.max(2, size / 6);
        int x1 = pad, y1 = size/2;
        int x2 = size/2, y2 = size - pad;
        int x3 = size - pad, y3 = pad;
        int thick = Math.max(2, size / 18);
        for (int i = -thick/2; i <= thick/2; i++) {
            pm.drawLine(x1, y1+i, x2, y2+i);
            pm.drawLine(x2, y2+i, x3, y3+i);
            pm.drawLine(x1+i, y1, x2+i, y2);
            pm.drawLine(x2+i, y2, x3+i, y3);
        }
        Texture tex = new Texture(pm); pm.dispose(); return tex;
    }

    @Override public void show() {
        muteBox.setChecked(game.muted);
        game.updateMusicVolumes();

        if (!game.musicMenu.isPlaying()) game.musicMenu.play();
        if (game.musicGame.isPlaying()) game.musicGame.stop();
    }

    @Override
    public void render(float delta) {
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

    @Override
    public void dispose() {
        stage.dispose();
        if (bgTex != null) bgTex.dispose();
        if (btnUp != null) btnUp.dispose();
        if (btnOver != null) btnOver.dispose();
        if (btnDown != null) btnDown.dispose();
        if (cardTex != null) cardTex.dispose();
        if (cbBoxOff != null) cbBoxOff.dispose();
        if (cbBoxOn  != null) cbBoxOn.dispose();
    }
}
