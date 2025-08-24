package com.rgs.swordsurvivor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class GameOverMenu extends Group {

    public interface Listener {
        void onRestart();
        void onMainMenu();
    }

    private final Texture dimTex, panelTex, btnUp, btnOver, btnDown;
    private final Table panel;
    private final Label scoreLabel;

    public GameOverMenu(BitmapFont font, final Listener listener) {
        dimTex = solid(0f,0f,0f,1f);
        panelTex = solid(0.12f,0.10f,0.10f,1f);
        btnUp   = solid(0.30f,0.15f,0.15f,1f);
        btnOver = solid(0.38f,0.18f,0.18f,1f);
        btnDown = solid(0.22f,0.10f,0.10f,1f);

        TextureRegionDrawable dim = new TextureRegionDrawable(new TextureRegion(dimTex));
        TextureRegionDrawable panelBg = new TextureRegionDrawable(new TextureRegion(panelTex));

        Image backdrop = new Image(dim);
        backdrop.setColor(0f,0f,0f,0.65f);
        backdrop.setFillParent(true);
        addActor(backdrop);

        LabelStyle ls = new LabelStyle(font, Color.WHITE);
        TextButtonStyle bs = new TextButtonStyle(
            new TextureRegionDrawable(new TextureRegion(btnUp)),
            new TextureRegionDrawable(new TextureRegion(btnDown)),
            new TextureRegionDrawable(new TextureRegion(btnOver)),
            font
        );

        Label title = new Label("GAME OVER", ls);
        title.setAlignment(Align.center);
        title.setFontScale(1.8f);

        scoreLabel = new Label("Kills: 0   •   High Score: 0", ls);
        scoreLabel.setAlignment(Align.center);

        TextButton retry = new TextButton("Retry (R)", bs);
        TextButton mainMenu = new TextButton("Main Menu (Esc)", bs);

        retry.addListener(new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onRestart(); }});
        mainMenu.addListener(new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onMainMenu(); }});

        panel = new Table();
        panel.setBackground(panelBg);
        panel.pad(24f).defaults().pad(8f).width(260f).height(48f);
        panel.add(title).padBottom(8f).colspan(1).height(36f).width(300f).row();
        panel.add(scoreLabel).padBottom(10f).height(24f).row();
        panel.add(retry).row();
        panel.add(mainMenu).row();

        addActor(panel);
        setVisible(false);
    }

    private Texture solid(float r,float g,float b,float a){
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(r,g,b,a); pm.fill();
        Texture t = new Texture(pm); pm.dispose(); return t;
    }

    /** For updating score text before showing. */
    public void setScores(int kills, int highscore) {
        scoreLabel.setText("Kills: " + kills + "   •   High Score: " + highscore);
    }

    /** Size to current view and center panel. Call once per frame when visible. */
    public GameOverMenu centerOnCamera(Viewport viewport, Camera camera) {
        float vw = viewport.getWorldWidth(), vh = viewport.getWorldHeight();
        setSize(vw, vh);
        setPosition(camera.position.x - vw/2f, camera.position.y - vh/2f);
        float pw = Math.min(360f, vw - 40f), ph = 260f;
        panel.setSize(pw, ph);
        panel.setPosition((vw - pw)/2f, (vh - ph)/2f);
        return this;
    }

    public void show(){ setVisible(true); }
    public void hide(){ setVisible(false); }

    public void dispose() {
        dimTex.dispose(); panelTex.dispose(); btnUp.dispose(); btnOver.dispose(); btnDown.dispose();
    }
}
