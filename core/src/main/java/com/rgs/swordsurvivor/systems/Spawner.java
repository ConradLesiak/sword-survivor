package com.rgs.swordsurvivor.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.rgs.swordsurvivor.entities.Enemy;
import com.rgs.swordsurvivor.entities.Brute;
import com.rgs.swordsurvivor.entities.Golem;
import com.rgs.swordsurvivor.entities.Obby;
import com.rgs.swordsurvivor.entities.FireOrb;

public class Spawner {

    // ---- Wave timing & scaling ----
    private int wave = 1;
    private float waveTimer = 0f;

    // We accumulate fractional “credits” over time; each whole credit = 1 spawn
    private float spawnCredits = 0f;

    // Tunables (tweak freely)
    private static final float BASE_WAVE_DURATION   = 30f;  // seconds per wave at wave 1
    private static final float MIN_WAVE_DURATION    = 18f;  // don’t get shorter than this

    private static final float BASE_SPAWNS_PER_SEC  = 0.9f; // ~1 enemy/sec at wave 1
    private static final float SPAWN_RATE_GROWTH    = 0.08f; // +8% rate per wave (multiplicative)
    private static final float MAX_SPAWNS_PER_SEC   = 4.0f;  // cap the insanity

    // Safety cap so we don’t queue unlimited spawns if the game lags
    private static final int   MAX_PENDING_PER_FRAME = 8;

    // Pending spawns that GameScreen will consume with shouldSpawn()
    private int pendingSpawns = 0;

    public void reset() {
        wave = 1;
        waveTimer = 0f;
        spawnCredits = 0f;
        pendingSpawns = 0;
    }

    public int getWave() { return wave; }

    public void update(float dt) {
        // ---- Wave timer & progression ----
        waveTimer += dt;
        float waveDuration = Math.max(MIN_WAVE_DURATION, BASE_WAVE_DURATION - 0.5f * (wave - 1));
        if (waveTimer >= waveDuration) {
            wave++;
            waveTimer -= waveDuration;
        }

        // ---- Spawn credit accrual (continuous trickle that ramps with waves) ----
        float spawnsPerSec = (float)(BASE_SPAWNS_PER_SEC * Math.pow(1.0 + SPAWN_RATE_GROWTH, wave - 1));
        spawnsPerSec = Math.min(spawnsPerSec, MAX_SPAWNS_PER_SEC);

        spawnCredits += spawnsPerSec * dt;

        // Convert whole credits into pending spawns (don’t over-queue)
        while (spawnCredits >= 1f && pendingSpawns < MAX_PENDING_PER_FRAME) {
            pendingSpawns++;
            spawnCredits -= 1f;
        }
    }

    /** GameScreen calls this in a while-loop; we only return true when a spawn is queued. */
    public boolean shouldSpawn() {
        if (pendingSpawns > 0) { pendingSpawns--; return true; }
        return false;
    }

    /** Create a new enemy just outside the current camera view. */
    public Enemy spawnEnemyAroundView(float cx, float cy, float vw, float vh) {
        // --- Choose a spawn edge around the view and offset slightly outward ---
        int edge = MathUtils.random(3);
        float x = cx, y = cy;
        if (edge == 0) { // left
            x = cx - vw / 2f - 64f;
            y = cy + MathUtils.random(-vh / 2f, vh / 2f);
        } else if (edge == 1) { // right
            x = cx + vw / 2f + 64f;
            y = cy + MathUtils.random(-vh / 2f, vh / 2f);
        } else if (edge == 2) { // top
            x = cx + MathUtils.random(-vw / 2f, vw / 2f);
            y = cy + vh / 2f + 64f;
        } else { // bottom
            x = cx + MathUtils.random(-vw / 2f, vw / 2f);
            y = cy - vh / 2f - 64f;
        }

        Vector2 pos = new Vector2(x, y);

        // --- Type selection with wave gates (ordered buckets) ---
        // Brute: starts wave 2, up to 35%
        float bruteChance = (wave >= 2)  ? Math.min(0.35f, 0.06f + 0.025f * (wave - 2)) : 0f;
        // Golem: starts wave 5, up to 20%
        float golemChance = (wave >= 5)  ? Math.min(0.20f, 0.04f + 0.02f  * (wave - 5)) : 0f;
        // Obby:  starts wave 10, up to 15%
        float obbyChance  = (wave >= 10) ? Math.min(0.15f, 0.03f + 0.015f * (wave - 10)) : 0f;
        // FireOrb: starts wave 15, up to 10%
        float fireChance  = (wave >= 50) ? Math.min(0.10f, 0.02f + 0.01f  * (wave - 50)) : 0f;

        float r = MathUtils.random();
        Enemy e;
        if (r < fireChance) {
            e = new FireOrb(pos);
        } else if (r < fireChance + obbyChance) {
            e = new Obby(pos);
        } else if (r < fireChance + obbyChance + golemChance) {
            e = new Golem(pos);
        } else if (r < fireChance + obbyChance + golemChance + bruteChance) {
            e = new Brute(pos);
        } else {
            e = new Enemy(pos);
        }

        // --- Per-wave difficulty scaling (applies to all enemy types) ---
        // HP scaling: +5% per wave
        float hpScale = (float) Math.pow(1.05, Math.max(0, wave - 1));
        e.hp = Math.round(e.hp * hpScale);

        // Contact dmg scales gently: +2% per wave
        float dmgScale = (float) Math.pow(1.02, Math.max(0, wave - 1));
        e.touchDamage = Math.max(1, Math.round(e.touchDamage * dmgScale));

        return e;
    }
}
