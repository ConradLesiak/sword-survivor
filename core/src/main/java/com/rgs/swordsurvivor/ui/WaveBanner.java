package com.rgs.swordsurvivor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Center-screen wave banner that fades in/out and floats slightly upward. */
public class WaveBanner extends Actor {
    private final BitmapFont font;
    private final String text;
    private final GlyphLayout layout = new GlyphLayout();
    private final Color color = new Color(1f, 1f, 1f, 1f);

    private float elapsed = 0f;
    private final float duration;    // total lifetime (sec)
    private final float rise;        // how much to rise (world units)
    private final float fontScale;   // temporary scale while drawing

    /**
     * @param font shared game font
     * @param text e.g. "Wave 3!"
     * @param x center X (world space)
     * @param y center Y (world space)
     * @param duration seconds on screen (e.g., 1.25f)
     */
    public WaveBanner(BitmapFont font, String text, float x, float y, float duration) {
        this(font, text, x, y, duration, 36f, 24f, 1.8f);
    }

    /** Advanced ctor with tuning knobs. */
    public WaveBanner(BitmapFont font, String text, float x, float y,
                      float duration, float startRise, float endRise, float fontScale) {
        this.font = font;
        this.text = text;
        this.duration = duration;
        this.rise = endRise - startRise; // total upward movement
        this.fontScale = fontScale;
        setPosition(x, y + startRise);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        elapsed += delta;
        if (elapsed >= duration) {
            remove();
            return;
        }
        // ease-out vertical rise
        float t = elapsed / duration;
        float y = getY();
        float baseY = getY() - rise * (t - (t - delta / duration)); // undo last frame component
        setY(baseY + rise * t);

        // fade in (first 20%) and fade out (last 30%)
        float alpha;
        if (t < 0.2f) {
            alpha = t / 0.2f;               // 0 -> 1
        } else if (t > 0.7f) {
            alpha = (1f - t) / 0.3f;        // 1 -> 0
        } else {
            alpha = 1f;
        }
        color.a = Math.max(0f, Math.min(1f, alpha));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float oldScale = font.getData().scaleX;
        font.getData().setScale(fontScale);

        // shadow
        layout.setText(font, text);
        float cx = getX() - layout.width / 2f;
        float cy = getY();

        Color old = font.getColor();
        font.setColor(0f, 0f, 0f, 0.6f * color.a * parentAlpha);
        font.draw(batch, layout, cx + 2f, cy - 2f);

        // main
        font.setColor(1f, 1f, 1f, color.a * parentAlpha);
        font.draw(batch, layout, cx, cy);

        font.setColor(old);
        font.getData().setScale(oldScale);
    }
}
