package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.math.Vector2;

/** Huge late-game FireOrb. 5x size & stats of a regular enemy. Spawns from Wave 15+. */
public class FireOrb extends Enemy {
    public FireOrb(Vector2 pos) {
        super(pos);
        this.size *= 5f;
        this.hp = (int)Math.ceil(this.hp * 100.0f);
        this.touchDamage = Math.max(1, this.touchDamage * 5);
        this.speed *= 0.1f; // giant, very slow
    }
}
