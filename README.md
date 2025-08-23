# Sword Survivor

A top-down 2D roguelike built with **LibGDX**. Survive endless waves of enemies, level up, and grow stronger with random boons!

---

## 🎮 Gameplay

- Control a **green warrior** who automatically swings their sword toward the mouse cursor.  
- Defeat waves of **red enemies** and collect **yellow XP orbs** to level up.  
- Choose from **3 random boons** each level to customize your run.  
- Enemies scale in health and damage with each wave — how long can you survive?  
- Your score is the number of **kills** before you die.

---

## 🕹️ Controls

- **Move** → WASD / Arrow Keys  
- **Aim** → Mouse  
- **Pause** → P / Esc  
- **Restart** → R (while paused or on game over)  
- **Confirm Boon** → 1 / 2 / 3 or mouse click  

---

## ✨ Features

- **Dynamic sword combat**: sweeping arc attacks that aim toward your cursor.  
- **Scaling waves**: enemies get faster, tankier, and deadlier over time.  
- **Random boons**: increase attack speed, damage, sword area, XP gain, pickup range, or max HP.  
- **Level-up full heal**: regain all health when you select a boon.  
- **UI polish**: pause menu, wave notifications, floating damage numbers, high score tracking.  
- **Audio**: background music + hit, pickup, and level-up sound effects.  
- **Custom art**: player, enemy, and sword sprites instead of simple shapes.  

---

## 📦 Installation / Build

This project uses **LibGDX (Gradle)**.

1. Clone the repo:
   ```sh
   git clone https://github.com/yourname/sword-survivor.git
   cd sword-survivor
   ```
2. Run desktop version:
   ```sh
   ./gradlew desktop:run
   ```
3. Export:
   - **Desktop JAR**: `./gradlew desktop:dist`
   - **HTML (itch.io/Web)**: `./gradlew html:dist`

---

## 🎵 Assets

- **Music**:  
  - `music1.mp3` → main menu  
  - `music2.mp3` → gameplay / pause  

- **Sound Effects**:  
  - `hit1.mp3` → player hit  
  - `hit2.mp3` → enemy hit  
  - `pickup.mp3` → XP orb pickup  
  - `level.mp3` → level-up  

- **Sprites**:  
  - `player.png`  
  - `enemy.png`  
  - `sword.png`

*(All placed in `android/assets/`)*

---

## 🛠️ Tech Stack

- **Java 17+**  
- **LibGDX** for rendering, audio, input, and scene2d UI  
- **Gradle** build system  

---

## 🚀 Future Ideas

- More enemy types (ranged, tanky, fast swarms)  
- Boss waves every 5 rounds  
- Meta progression (persistent upgrades)  
- Animated sprites and particle effects  
- Screen shake & polish effects  

---

## 📜 License

This project is for learning/demonstration purposes. Use and modify freely!  
