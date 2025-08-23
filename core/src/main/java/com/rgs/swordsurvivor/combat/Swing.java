package com.rgs.swordsurvivor.combat;

public class Swing {
    public boolean active = false;
    public int id = 0;
    public float elapsed = 0f;
    public float duration = 0.2f;

    public float startAngleDeg = 0f;   // top of the arc
    public float endAngleDeg = 0f;     // bottom of the arc
    public float currentAngleDeg = 0f; // animated each frame
}
