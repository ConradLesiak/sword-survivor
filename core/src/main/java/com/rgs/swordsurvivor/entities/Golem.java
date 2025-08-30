package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.math.Vector2;

/** Very tanky, very big enemy. 3x size & base stats of a regular enemy. Spawns from Wave 5+. */
public class Golem extends Enemy {
    public Golem(Vector2 pos) {
        super(pos);
        // 3x size
        this.size *= 3f;
        // 3x tougher & harder-hitting baseline (before wave scaling in Spawner)
        this.hp = (int)Math.ceil(this.hp * 12f);    // ~12x HP of a normal enemy baseline
        this.touchDamage = Math.max(1, this.touchDamage * 5);
        // Usually slower than normals
        this.speed *= 0.55f;
    }
}
