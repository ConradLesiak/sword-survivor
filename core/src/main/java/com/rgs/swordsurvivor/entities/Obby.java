package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.math.Vector2;

/** Massive late-game enemy. 4x size & base stats of a regular enemy. Spawns from Wave 10+. */
public class Obby extends Enemy {
    public Obby(Vector2 pos) {
        super(pos);
        this.size *= 4f;                         // 4x bigger
        this.hp = (int)Math.ceil(this.hp * 27f);  // 27x base HP (before Spawner wave scaling)
        this.touchDamage = Math.max(1, this.touchDamage * 7); // 7x contact damage
        this.speed *= 0.45f;                     // big & slow
    }
}
