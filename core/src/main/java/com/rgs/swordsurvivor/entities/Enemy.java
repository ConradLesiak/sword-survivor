package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public final Vector2 pos;
    public float size = 48f;

    public float radius;
    public int hp = 4;
    public int touchDamage = 1;
    public float speed = 85f;
    public int lastHitSwingId = -1;

    public boolean facingLeft = false;

    public float hurtTimer = 0f;

    public Enemy(Vector2 pos) {
        this.pos = pos;
        this.size = 48f; // used for sprite
        this.radius = size * 0.5f; // for collision
        this.hp = 4;
        this.touchDamage = 1;
    }

    public void update(float dt, Vector2 target) {
        Vector2 dir = new Vector2(target).sub(pos);
        if (dir.len2() > 1e-4f) {
            dir.nor().scl(speed * dt);
            pos.add(dir);
        }
        if (target.x < pos.x) facingLeft = true;
        else if (target.x > pos.x) facingLeft = false;

        if (hurtTimer > 0f) hurtTimer -= dt;
    }

    public boolean collidesWithPlayer(Player p) {
        float dx = p.pos.x - this.pos.x;
        float dy = p.pos.y - this.pos.y;
        float dist2 = dx*dx + dy*dy;
        float radSum = p.radius + this.radius;
        return dist2 < radSum * radSum;
    }
}
