package com.rgs.swordsurvivor.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rgs.swordsurvivor.entities.Brute;
import com.rgs.swordsurvivor.entities.Enemy;
import com.rgs.swordsurvivor.entities.FireOrb;
import com.rgs.swordsurvivor.entities.Golem;
import com.rgs.swordsurvivor.entities.Obby;

public class Spawner {
    private float spawnTimer = 0f;
    private float spawnInterval = 1.2f;
    private int wave = 1;
    private float waveTimer = 0f;
    private float waveInterval = 20f;
    private boolean spawnFlag = false;

    // --- tuning knobs ---
    private static final float SPEED_SCALE_PER_WAVE = 0.12f;  // caps at ~+80%
    private static final float HP_SCALE_PER_WAVE    = 10.0f;  // faster HP ramp
    private static final float DMG_SCALE_PER_WAVE   = 2.0f;  // faster touch-dmg ramp
    private static final float MAX_SPEED_SCALE      = 3f;   // safety cap

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

        Vector2 pos = new Vector2(x, y);

        // --- Pick type: Brute spawns infrequently from Wave 4+, scales up over time ---
        int wave = getWave();
        float bruteChance = 0f;
        if (wave >= 2) {
            // starts at 6% chance on wave 2, increases +2.5% per wave, capped at 35%
            bruteChance = Math.min(0.35f, 0.06f + 0.025f * (wave - 2));
        }

        // Golem: starts wave 5, rarer, up to 20% max
        float golemChance = 0f;
        if (wave >= 5) golemChance = Math.min(0.20f, 0.04f + 0.02f * (wave - 5));


        float obbyChance = 0f;
        if (wave >= 10) obbyChance = Math.min(0.15f, 0.03f + 0.015f * (wave - 10));

        // FireOrb chance
        float fireOrbChance = 0f;
        if (wave >= 15) fireOrbChance = Math.min(0.10f, 0.02f + 0.01f * (wave - 15));

        // Single roll
        float r = MathUtils.random();
        if (r < fireOrbChance) return new FireOrb(pos);
        r -= fireOrbChance;
        if (r < obbyChance) return new Obby(pos);
        r -= obbyChance;
        if (r < golemChance) return new Golem(pos);
        r -= golemChance;
        if (r < bruteChance) return new Brute(pos);

        return new Enemy(pos);
    }

    public int getWave() {
        return wave;
    }
}
