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

    public Player(Vector2 start) {this.pos = start; }

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
            float t = MathUtils.clamp(swing.elapsed / swing.duration, 0f, 1f);
            swing.currentAngleDeg = swing.startAngleDeg + t * swing.totalSweepDeg;
            if (swing.elapsed >= swing.duration) swing.active = false;
        }
    }

    private void startSwing(float aimDeg) {
        swing.id++;
        swing.active = true;
        swing.elapsed = 0f;
        swing.duration = swingDuration;
        swing.totalSweepDeg = swingSweep;
        swing.startAngleDeg = aimDeg - swingSweep / 2f;
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
            // Recalculate next threshold
            xpToNext = Math.max(5, Math.round(xpToNext * 1.25f));
            // Fully heal on level-up
            hp = maxHp;
            leveled = true;
        }
        return leveled;
    }


}
