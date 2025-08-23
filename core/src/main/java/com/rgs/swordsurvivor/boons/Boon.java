package com.rgs.swordsurvivor.boons;

import com.rgs.swordsurvivor.entities.Player;

public class Boon {
    public final BoonType type;
    public final String title;
    public final String desc;

    public Boon(BoonType type, String title, String desc) {
        this.type = type;
        this.title = title;
        this.desc = desc;
    }

    public static Boon fromType(BoonType t) {
        switch (t) {
            case ATTACK_SPEED:
                return new Boon(t, "Haste", "Reduce attack cooldown by 15% (faster auto-attacks).");
            case DAMAGE:
                return new Boon(t, "Sharpened Edge", "Increase sword damage by +1.");
            case MOVE_SPEED:
                return new Boon(t, "Boots of Wind", "Increase movement speed by +15%.");
            case AREA:
                return new Boon(t, "Whirlwind", "Increase sword area of effect (reach +15%, width +15%).");
            case XP_GAIN:
                return new Boon(t, "Scholar", "Gain +15% more experience from orbs.");
            case PICKUP_RANGE:
                return new Boon(t, "Magnet", "Increase orb pickup range by +15%.");
            case MAX_HEALTH:
                return new Boon(t, "Toughness", "Increase max HP by +3.");
            default:
                return new Boon(t, "Mysterious Power", "A strange surge of strength...");
        }
    }

    public void apply(Player p) {
        switch (type) {
            case ATTACK_SPEED:
                p.attackCooldown = Math.max(0.18f, p.attackCooldown * 0.85f);
                break;
            case DAMAGE:
                p.damage += 1;
                break;
            case MOVE_SPEED:
                p.speed *= 1.15f;
                break;
            case AREA:
                p.swordReach *= 1.15f;
                p.swordThickness *= 1.15f;
                break;
            case XP_GAIN:
                p.xpGain *= 1.15f;
                break;
            case PICKUP_RANGE:
                p.pickupRange *= 1.15f;
                break;
            case MAX_HEALTH:
                p.maxHp += 3; // was +2
                // no instant heal anymore
                break;
        }
    }
}
