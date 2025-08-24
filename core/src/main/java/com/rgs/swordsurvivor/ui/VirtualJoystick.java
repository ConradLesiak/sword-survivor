package com.rgs.swordsurvivor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * A simple on-demand virtual joystick:
 * - Appears where activated (touchDown)
 * - Tracks a single pointer until released
 * - Provides normalized value vector (-1..1 per axis), with magnitude clamped to 1
 */
public class VirtualJoystick extends Actor {
    private final float baseRadius;
    private final float knobRadius;
    private final float deadzone;

    private boolean active = false;
    private int pointerId = -1;

    private final Vector2 center = new Vector2();
    private final Vector2 knob = new Vector2();
    private final Vector2 value = new Vector2(); // normalized

    private final Texture baseTex;
    private final Texture knobTex;
    private final TextureRegionDrawable baseDrawable;
    private final TextureRegionDrawable knobDrawable;

    public VirtualJoystick(float baseRadius, float knobRadius, float deadzone) {
        this.baseRadius = baseRadius;
        this.knobRadius = knobRadius;
        this.deadzone = deadzone;

        baseTex = makeCircle(Math.round(baseRadius * 2f), new Color(1f, 1f, 1f, 0.12f));
        knobTex = makeCircle(Math.round(knobRadius * 2f), new Color(1f, 1f, 1f, 0.35f));
        baseDrawable = new TextureRegionDrawable(new TextureRegion(baseTex));
        knobDrawable = new TextureRegionDrawable(new TextureRegion(knobTex));

        setVisible(false);
        setTouchable(Touchable.disabled); // draws only; input is handled by GameScreen listener
    }

    private Texture makeCircle(int size, Color color) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.SourceOver);
        pm.setColor(0,0,0,0); pm.fill();
        pm.setColor(color);
        int r = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int dx = x - r, dy = y - r;
                if (dx*dx + dy*dy <= r*r) {
                    pm.drawPixel(x, y);
                }
            }
        }
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public boolean isActive() { return active; }
    public int getPointerId() { return pointerId; }
    /** Normalized vector in range [-1..1] per axis (length <= 1). */
    public Vector2 getValue() { return value; }

    /** Activate at a world/stage position for a given pointer. */
    public void activate(int pointer, float x, float y) {
        active = true;
        pointerId = pointer;
        center.set(x, y);
        knob.set(x, y);
        value.setZero();
        setVisible(true);
    }

    /** Deactivate if this joystick owns the pointer. */
    public void release(int pointer) {
        if (!active || pointer != pointerId) return;
        active = false;
        pointerId = -1;
        value.setZero();
        setVisible(false);
    }

    /** Update knob/value from a drag position (world/stage coords). */
    public void drag(int pointer, float x, float y) {
        if (!active || pointer != pointerId) return;

        Vector2 delta = new Vector2(x, y).sub(center);
        float len = delta.len();
        if (len < deadzone) {
            knob.set(center);
            value.setZero();
            return;
        }

        float max = baseRadius - knobRadius; // keep knob within ring
        float clamped = Math.min(len, max);
        Vector2 dir = (len == 0f) ? new Vector2() : delta.scl(1f / len);

        knob.set(center).mulAdd(dir, clamped);
        // Normalize to [-1..1], mapping max travel to 1.0
        float norm = clamped / max;
        value.set(dir.x * norm, dir.y * norm);
        value.x = MathUtils.clamp(value.x, -1f, 1f);
        value.y = MathUtils.clamp(value.y, -1f, 1f);
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        if (!active) return;

        float bx = center.x - baseRadius;
        float by = center.y - baseRadius;
        float kx = knob.x - knobRadius;
        float ky = knob.y - knobRadius;

        // draw base
        baseDrawable.draw(batch, bx, by, baseRadius * 2f, baseRadius * 2f);
        // draw knob
        knobDrawable.draw(batch, kx, ky, knobRadius * 2f, knobRadius * 2f);
    }

    public void dispose() {
        baseTex.dispose();
        knobTex.dispose();
    }
}
