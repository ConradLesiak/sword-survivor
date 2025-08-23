package com.rgs.swordsurvivor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Floating damage number that rises and fades, drawn in world space. */
public class DamageText extends Actor {
    private final BitmapFont font;
    private final String text;
    private final Color color = new Color();
    private final GlyphLayout layout = new GlyphLayout();

    private float lifetime = 0.8f;   // seconds before removal
    private float elapsed = 0f;
    private float riseSpeed = 40f;   // world units per second
    private float alpha = 1f;

    public DamageText(BitmapFont font, String text, float worldX, float worldY, Color tint) {
        this.font = font;
        this.text = text;
        color.set(tint);
        setPosition(worldX, worldY);
    }

    @Override
    public void act(float delta) {
        super.act(delta); // always call super
        elapsed += delta;
        moveBy(0f, riseSpeed * delta);
        alpha = Math.max(0f, 1f - (elapsed / lifetime));
        if (elapsed >= lifetime) remove();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color old = font.getColor();
        font.setColor(color.r, color.g, color.b, alpha * parentAlpha);

        layout.setText(font, text);
        font.draw(batch, layout, getX() - layout.width / 2f, getY());

        font.setColor(old);
    }
}
