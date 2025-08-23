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

public class PauseMenu extends Group {

    public interface Listener {
        void onResume();
        void onRestart();
        void onMainMenu();
    }

    private final Texture dimTex;
    private final Texture panelTex;

    private final Table panel;

    public PauseMenu(BitmapFont font, final Listener listener) {
        // solid textures
        Pixmap pmDim = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pmDim.setColor(0f,0f,0f,1f); pmDim.fill();
        dimTex = new Texture(pmDim); pmDim.dispose();

        Pixmap pmPanel = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pmPanel.setColor(0.12f,0.14f,0.18f,1f); pmPanel.fill();
        panelTex = new Texture(pmPanel); pmPanel.dispose();

        TextureRegionDrawable dimDrawable = new TextureRegionDrawable(new TextureRegion(dimTex));
        TextureRegionDrawable panelDrawable = new TextureRegionDrawable(new TextureRegion(panelTex));

        // Dim layer fills parent
        Image dim = new Image(dimDrawable);
        dim.setColor(0f,0f,0f,0.65f);
        dim.setFillParent(true);
        addActor(dim);

        LabelStyle style = new LabelStyle(font, Color.WHITE);

        Label title = new Label("Paused", style);
        title.setAlignment(Align.center);
        title.setFontScale(1.6f);

        Label resume = new Label("Resume (P)", style);
        Label restart = new Label("Restart (R)", style);
        Label mainMenu = new Label("Main Menu (Esc)", style);

        resume.addListener(new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onResume(); }});
        restart.addListener(new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onRestart(); }});
        mainMenu.addListener(new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ listener.onMainMenu(); }});

        panel = new Table();
        panel.setBackground(panelDrawable);
        panel.pad(24f).defaults().pad(8f);
        panel.add(title).padBottom(10f).row();
        panel.add(resume).row();
        panel.add(restart).row();
        panel.add(mainMenu).row();
        addActor(panel);

        setVisible(false);
    }

    /** Size to the view and center the panel. Call this each frame before drawing. */
    public PauseMenu centerOnCamera(Viewport viewport, Camera camera) {
        float vw = viewport.getWorldWidth();
        float vh = viewport.getWorldHeight();

        // Make this Group cover the current view, positioned to its top-left in world space
        setSize(vw, vh);
        setPosition(camera.position.x - vw/2f, camera.position.y - vh/2f);

        // Center the panel within this Group (local coords)
        float pw = Math.min(360f, vw - 40f);
        float ph = 200f;
        panel.setSize(pw, ph);
        panel.setPosition((vw - pw)/2f, (vh - ph)/2f);

        return this;
    }

    public void show() { setVisible(true); }
    public void hide() { setVisible(false); }

    public void dispose() { dimTex.dispose(); panelTex.dispose(); }
}
