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
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/** Modal pause menu with dimmed background and a centered panel. */
public class PauseMenu extends Group {

    public interface Listener {
        void onResume();
        void onRestart();
        void onMainMenu();
    }

    private final Texture dimTex;
    private final Texture panelTex;

    public PauseMenu(BitmapFont font, final Listener listener) {
        // create 1x1 textures for drawables
        Pixmap pmDim = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pmDim.setColor(0f,0f,0f,1f); pmDim.fill();
        dimTex = new Texture(pmDim); pmDim.dispose();

        Pixmap pmPanel = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pmPanel.setColor(0.12f,0.14f,0.18f,1f); pmPanel.fill();
        panelTex = new Texture(pmPanel); pmPanel.dispose();

        TextureRegionDrawable dimDrawable = new TextureRegionDrawable(new TextureRegion(dimTex));
        TextureRegionDrawable panelDrawable = new TextureRegionDrawable(new TextureRegion(panelTex));

        // DIM LAYER (full-screen)
        Image dim = new Image(dimDrawable);
        dim.setColor(0f,0f,0f,0.65f);
        dim.setFillParent(true);
        addActor(dim);

        // PANEL (centered table)
        LabelStyle style = new LabelStyle(font, Color.WHITE);

        Label title = new Label("Paused", style);
        title.setAlignment(Align.center);
        title.setFontScale(1.6f);

        Label resume = new Label("Resume (P)", style);
        Label restart = new Label("Restart (R)", style);
        Label mainMenu = new Label("Main Menu (Esc)", style);

        ClickListener resumeClick = new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onResume(); }};
        ClickListener restartClick = new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onRestart(); }};
        ClickListener menuClick = new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onMainMenu(); }};

        resume.addListener(resumeClick);
        restart.addListener(restartClick);
        mainMenu.addListener(menuClick);

        Table panel = new Table();
        panel.setBackground(panelDrawable);
        panel.pad(24f).defaults().pad(8f);
        panel.add(title).padBottom(10f).row();
        panel.add(resume).row();
        panel.add(restart).row();
        panel.add(mainMenu).row();

        addActor(panel);
        // we'll size & center in centerOnCamera()
        setVisible(false);
    }

    /** Positions and sizes the menu to the current camera+viewport; returns this for chaining. */
    public PauseMenu centerOnCamera(Viewport viewport, Camera camera) {
        float vw = viewport.getWorldWidth();
        float vh = viewport.getWorldHeight();

        setSize(vw, vh);
        setPosition(camera.position.x - vw/2f, camera.position.y - vh/2f);

        if (getChildren().size >= 2) {
            Table panel = (Table)getChildren().get(1);
            float pw = Math.min(360f, vw - 40f);
            float ph = 200f;
            panel.setSize(pw, ph);
            // center relative to this group, not world
            panel.setPosition(getX() + vw/2f - pw/2f, getY() + vh/2f - ph/2f);
        }
        return this;
    }


    public void show() { setVisible(true); }
    public void hide() { setVisible(false); }

    public void dispose() {
        dimTex.dispose();
        panelTex.dispose();
    }
}
