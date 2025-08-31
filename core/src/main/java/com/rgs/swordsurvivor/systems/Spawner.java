package com.rgs.swordsurvivor.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rgs.swordsurvivor.entities.Enemy;

public class Spawner {
    private float spawnTimer = 0f;
    private float spawnInterval = 1.2f;
    private int wave = 1;
    private float waveTimer = 0f;
    private float waveInterval = 20f;
    private boolean spawnFlag = false;

    // --- tuning knobs ---
    private static final float SPEED_SCALE_PER_WAVE = 0.12f;  // caps at ~+80%
    private static final float HP_SCALE_PER_WAVE    = 0.28f;  // faster HP ramp
    private static final float DMG_SCALE_PER_WAVE   = 0.18f;  // faster touch-dmg ramp
    private static final float MAX_SPEED_SCALE      = 1.8f;   // safety cap

    public void reset() {
        spawnTimer = 0f;
        spawnInterval = 1.2f;
        waveTimer = 0f;
        wave = 1;
        spawnFlag = false;
    }

    public void update(float dt) {
        waveTimer += dt;
        if (waveTimer >= waveInterval) {
            waveTimer = 0f;
            wave++;
            // Faster spawns each wave (unchanged)
            spawnInterval = Math.max(0.35f, spawnInterval * 0.9f);
        }

        spawnTimer += dt;
        if (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval;
            spawnFlag = true;
        }
    }

    public boolean shouldSpawn() {
        if (spawnFlag) {
            spawnFlag = false;
            return true;
        }
        return false;
    }

    /** Spawns just OUTSIDE the current camera view rectangle. */
    public Enemy spawnEnemyAroundView(float camX, float camY, float viewW, float viewH) {
        float halfW = viewW / 2f;
        float halfH = viewH / 2f;

        // pick an edge: 0=left,1=right,2=bottom,3=top
        int edge = MathUtils.random(3);
        float margin = 30f;
        float x, y;
        if (edge == 0) { // left
            x = camX - halfW - margin;
            y = MathUtils.random(camY - halfH, camY + halfH);
        } else if (edge == 1) { // right
            x = camX + halfW + margin;
            y = MathUtils.random(camY - halfH, camY + halfH);
        } else if (edge == 2) { // bottom
            x = MathUtils.random(camX - halfW, camX + halfW);
            y = camY - halfH - margin;
        } else { // top
            x = MathUtils.random(camX - halfW, camX + halfW);
            y = camY + halfH + margin;
        }

        Enemy e = new Enemy(new Vector2(x, y));

        // --- scale stats with wave ---
        float speedScale = Math.min(MAX_SPEED_SCALE, 1f + (wave - 1) * SPEED_SCALE_PER_WAVE);
        float hpScale    = 1f + (wave - 1) * HP_SCALE_PER_WAVE;
        float dmgScale   = 1f + (wave - 1) * DMG_SCALE_PER_WAVE;

        e.speed       *= speedScale;
        e.hp           = Math.max(1, Math.round(e.hp * hpScale));
        e.touchDamage  = Math.max(1, Math.round(e.touchDamage * dmgScale));

        return e;
    }

    public int getWave() {
        return wave;
    }
}
