package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.math.Vector2;

public class Orb {
    public final Vector2 pos;
    public final int value;
    public float radius = 5f;
    public float magnetSpeed = 160f;

    public Orb(Vector2 pos, int value) {
        this.pos = pos;
        this.value = value;
    }

    public void update(float dt, Vector2 playerPos, float pickupRange) {
        float r = pickupRange + 30f;
        if (pos.dst2(playerPos) <= r*r) {
            Vector2 dir = new Vector2(playerPos).sub(pos);
            if (dir.len2() > 1e-4f) {
                dir.nor().scl(magnetSpeed * dt);
                pos.add(dir);
            }
        }
    }

    public boolean canPickup(Vector2 playerPos, float pickupRange) {
        float r = pickupRange + radius;
        return pos.dst2(playerPos) <= r*r;
    }
}
