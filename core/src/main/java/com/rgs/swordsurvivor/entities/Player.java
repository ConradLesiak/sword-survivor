package com.rgs.swordsurvivor.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rgs.swordsurvivor.SwordSurvivorGame;
import com.rgs.swordsurvivor.combat.Swing;

public class Player {
    public final Vector2 pos;

    public float size = 64f;

    public float radius;
    public int hp = 10, maxHp = 10;
    public float speed = 180f;

    public int damage = 2;
    public float attackCooldown = 0.6f;
    public float swordReach = 180f;
    public float swordThickness = 28f;
    public float swingSweep = 120f;
    public float swingDuration = 0.20f;

    public float pickupRange = 40f;
    public float xpGain = 1.0f;

    public int level = 1;
    public int xp = 0;
    public int xpToNext = 7;

    public float attackTimer = 0f;
    public float hurtCooldown = 0f;
    public final Swing swing = new Swing();

    public boolean facingLeft = false;

    public float hurtTimer = 0f; // seconds remaining for red flash

    public float critChance = 0.15f; // 15% base chance

    public Player(Vector2 pos) {
        this.pos = pos;
        this.size = 64f; // if you still use it for sprite drawing
        this.radius = size * 0.5f; // circle collision radius
        this.hp = maxHp = 10;
    }

    public void update(float dt, Vector2 mouseWorld) {
        // movement
        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  moveX -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    moveY += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  moveY -= 1;

        if (moveX < 0) facingLeft = true;
        else if (moveX > 0) facingLeft = false;

        pos.add(moveX * speed * dt, moveY * speed * dt);

        if (hurtCooldown > 0) hurtCooldown -= dt;

        // aim and attack
        float aimDeg = MathUtils.atan2(mouseWorld.y - pos.y, mouseWorld.x - pos.x) * MathUtils.radiansToDegrees;
        attackTimer -= dt;

        if (!swing.active && attackTimer <= 0f) {
            startSwing(aimDeg);
            attackTimer = attackCooldown;
        }

        if (swing.active) {
            swing.elapsed += dt;
            float t = com.badlogic.gdx.math.MathUtils.clamp(swing.elapsed / swing.duration, 0f, 1f);
            // simple linear interpolation on already-unwrapped angles
            swing.currentAngleDeg = swing.startAngleDeg + (swing.endAngleDeg - swing.startAngleDeg) * t;
            if (swing.elapsed >= swing.duration) swing.active = false;
        }

        if (hurtTimer > 0f) hurtTimer -= dt;
    }

    private void startSwing(float aimDeg) {
        swing.id++;
        swing.active = true;
        swing.elapsed = 0f;
        swing.duration = swingDuration;

        float half = Math.abs(swingSweep) * 0.5f;

        if (!facingLeft) {
            // FACING RIGHT: clockwise (top -> bottom), strictly decreasing angles
            swing.startAngleDeg = aimDeg + half;  // top
            swing.endAngleDeg   = aimDeg - half;  // bottom

            // unwrap so end <= start (decreasing)
            while (swing.endAngleDeg > swing.startAngleDeg) {
                swing.endAngleDeg -= 360f;
            }
        } else {
            // FACING LEFT: counter-clockwise (bottom -> top), strictly increasing angles
            swing.startAngleDeg = aimDeg - half;  // bottom
            swing.endAngleDeg   = aimDeg + half;  // top

            // unwrap so end >= start (increasing)
            while (swing.endAngleDeg < swing.startAngleDeg) {
                swing.endAngleDeg += 360f;
            }
        }

        swing.currentAngleDeg = swing.startAngleDeg;
    }


    private void clampToWorld() {
        pos.x = MathUtils.clamp(pos.x, size/2f, SwordSurvivorGame.WORLD_WIDTH - size/2f);
        pos.y = MathUtils.clamp(pos.y, size/2f, SwordSurvivorGame.WORLD_HEIGHT - size/2f);
    }

    /** @return true if leveled up this call */
    public boolean gainXP(int amount) {
        xp += amount;
        boolean leveled = false;
        while (xp >= xpToNext) {
            xp -= xpToNext;
            level++;
            xpToNext = Math.max(5, Math.round(xpToNext * 1.25f));
            // REMOVE any hp = maxHp here
            leveled = true;
        }
        return leveled;
    }


}
