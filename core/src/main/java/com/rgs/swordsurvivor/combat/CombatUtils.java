package com.rgs.swordsurvivor.combat;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rgs.swordsurvivor.entities.Enemy;
import com.rgs.swordsurvivor.entities.Player;

public final class CombatUtils {
    private CombatUtils() {}

    public static boolean swordHitsEnemyThisFrame(Player p, Enemy e) {
        // Sword represented by a thick line from player.pos to end point in swing direction.
        Vector2 dir = angleToDir(p.swing.currentAngleDeg);
        Vector2 p0 = p.pos;
        Vector2 p1 = new Vector2(p.pos).mulAdd(dir, p.swordReach);

        // Closest point from enemy center to segment p0-p1
        Vector2 v = new Vector2(p1).sub(p0);
        float len = v.len();
        if (len == 0) return false;
        Vector2 u = new Vector2(v).scl(1f / len);

        Vector2 pe = e.pos;
        float t = (pe.x - p0.x) * u.x + (pe.y - p0.y) * u.y;
        t = MathUtils.clamp(t, 0f, len);
        Vector2 closest = new Vector2(p0).mulAdd(u, t);

        float dist = pe.dst(closest);
        float hitRadius = p.swordThickness * 0.5f + e.size * 0.5f;
        return dist <= hitRadius;
    }

    public static Vector2 endOfSword(Player p) {
        Vector2 dir = angleToDir(p.swing.currentAngleDeg);
        return new Vector2(p.pos).mulAdd(dir, p.swordReach);
    }

    private static Vector2 angleToDir(float deg) {
        float rad = deg * MathUtils.degRad;
        return new Vector2(MathUtils.cos(rad), MathUtils.sin(rad));
    }
}
