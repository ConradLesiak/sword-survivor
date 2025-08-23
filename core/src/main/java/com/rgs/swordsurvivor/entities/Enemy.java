package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public final Vector2 pos;
    public float size = 18f;
    public int hp = 3;
    public int touchDamage = 1;
    public float speed = 85f;
    public int lastHitSwingId = -1;

    public Enemy(Vector2 pos) { this.pos = pos; }

    public void update(float dt, Vector2 target) {
        Vector2 dir = new Vector2(target).sub(pos);
        if (dir.len2() > 1e-4f) {
            dir.nor().scl(speed * dt);
            pos.add(dir);
        }
    }

    public boolean collidesWithPlayer(Player p) {
        float r = size/2f + p.size/2f;
        return pos.dst2(p.pos) <= r*r;
    }
}
