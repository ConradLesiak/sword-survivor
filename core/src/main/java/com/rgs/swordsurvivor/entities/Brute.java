package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.math.Vector2;

/** A tougher, larger enemy that spawns infrequently from Wave 4+. */
public class Brute extends Enemy {
    public Brute(Vector2 pos) {
        super(pos);
        // 2x bigger body
        this.size *= 2f;
        // beefier stats
        this.hp = (int)Math.ceil(this.hp * 5f);          // ~5x HP of a normal enemy baseline
        this.touchDamage = Math.max(1, this.touchDamage * 3); // 3x contact damage
        this.speed *= 0.7f;                              // slower than normal enemies
    }
}
