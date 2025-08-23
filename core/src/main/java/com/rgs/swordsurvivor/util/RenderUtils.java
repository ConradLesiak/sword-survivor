package com.rgs.swordsurvivor.util;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class RenderUtils {
    private static final GlyphLayout LAYOUT = new GlyphLayout();
    private RenderUtils() {}

    public static void rectCentered(ShapeRenderer s, float cx, float cy, float w, float h) {
        s.rect(cx - w/2f, cy - h/2f, w, h);
    }

    public static void drawWrapped(BitmapFont font, SpriteBatch batch, String text,
                                   float x, float topY, float wrapWidth, float lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float y = topY;
        for (String w : words) {
            String test = (line.length() == 0) ? w : line + " " + w;
            LAYOUT.setText(font, test);
            if (LAYOUT.width > wrapWidth) {
                font.draw(batch, line.toString(), x, y);
                y -= lineHeight;
                line.setLength(0);
                line.append(w);
            } else {
                line.setLength(0);
                line.append(test);
            }
        }
        if (line.length() > 0) font.draw(batch, line.toString(), x, y);
    }
}
