package com.rgs.swordsurvivor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Wave banner that anchors above a target (e.g., player) and rises/fades. */
public class WaveBanner extends Actor {
    private final BitmapFont font;
    private final String text;
    private final GlyphLayout layout = new GlyphLayout();
    private final Color color = new Color(1f, 1f, 1f, 1f);

    private final Vector2 followPos;   // where to anchor (player.pos)
    private final float anchorYOffset; // how far above the target to start

    private float elapsed = 0f;
    private final float duration;      // total lifetime (sec)
    private final float rise;          // total rise distance
    private final float scale;

    /**
     * @param font shared font
     * @param text e.g. "Wave 3!"
     * @param followPos position to follow (pass player.pos)
     * @param anchorYOffset pixels above the followPos to draw at time 0
     * @param duration seconds to stay visible (e.g., 1.25f)
     */
    public WaveBanner(BitmapFont font, String text, Vector2 followPos, float anchorYOffset, float duration) {
        this(font, text, followPos, anchorYOffset, duration, 28f, 1.6f);
    }

    public WaveBanner(BitmapFont font, String text, Vector2 followPos, float anchorYOffset,
                      float duration, float rise, float scale) {
        this.font = font;
        this.text = text;
        this.followPos = followPos;
        this.anchorYOffset = anchorYOffset;
        this.duration = duration;
        this.rise = rise;
        this.scale = scale;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        elapsed += delta;
        if (elapsed >= duration) {
            remove();
            return;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // progress 0..1
        float t = Math.min(1f, elapsed / duration);

        // position: always above the target, plus a small rise over time
        float x = followPos.x;
        float y = followPos.y + anchorYOffset + (rise * t);

        // fade in first 20%, full, then fade out last 30%
        float alpha;
        if (t < 0.2f)       alpha = t / 0.2f;
        else if (t > 0.7f)  alpha = (1f - t) / 0.3f;
        else                alpha = 1f;

        // draw centered with soft shadow
        float oldScale = font.getData().scaleX;
        font.getData().setScale(scale);
        layout.setText(font, text);

        float cx = x - layout.width / 2f;
        float cy = y;

        Color old = font.getColor();
        font.setColor(0f, 0f, 0f, 0.6f * alpha * parentAlpha); // shadow
        font.draw(batch, layout, cx + 2f, cy - 2f);

        font.setColor(1f, 1f, 1f, alpha * parentAlpha);        // main
        font.draw(batch, layout, cx, cy);

        font.setColor(old);
        font.getData().setScale(oldScale);
    }
}
