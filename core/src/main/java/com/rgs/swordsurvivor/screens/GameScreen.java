package com.rgs.swordsurvivor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.rgs.swordsurvivor.SwordSurvivorGame;
import com.rgs.swordsurvivor.boons.Boon;
import com.rgs.swordsurvivor.boons.BoonCard;
import com.rgs.swordsurvivor.boons.BoonType;
import com.rgs.swordsurvivor.combat.CombatUtils;
import com.rgs.swordsurvivor.entities.Enemy;
import com.rgs.swordsurvivor.entities.Orb;
import com.rgs.swordsurvivor.entities.Player;
import com.rgs.swordsurvivor.systems.Spawner;
import com.rgs.swordsurvivor.ui.DamageText;
import com.rgs.swordsurvivor.ui.PauseMenu;
import com.rgs.swordsurvivor.ui.WaveBanner;
import com.rgs.swordsurvivor.util.RenderUtils;

import java.util.EnumSet;
import java.util.Random;

public class GameScreen implements Screen {
    private final SwordSurvivorGame game;

    private final FitViewport viewport;
    private final Stage stage;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch;

    private final Array<Enemy> enemies = new Array<>();
    private final Array<Orb> orbs = new Array<>();
    private final Spawner spawner = new Spawner();
    private final Random rng = new Random();

    private Player player;
    private int kills = 0;
    private boolean gameOver = false;
    private boolean paused = false;

    // Level up UI
    private boolean levelUpPending = false;
    private BoonCard[] boonChoices = new BoonCard[3];

    // Wave banner
    private int lastAnnouncedWave = 0;

    // Pause menu
    private PauseMenu pauseMenu;

    private final GlyphLayout layout = new GlyphLayout();

    public GameScreen(SwordSurvivorGame game) {
        this.game = game;
        this.batch = game.batch;
        viewport = new FitViewport(SwordSurvivorGame.WORLD_WIDTH, SwordSurvivorGame.WORLD_HEIGHT);
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        // Pause menu
        pauseMenu = new PauseMenu(game.font, new PauseMenu.Listener() {
            @Override public void onResume() { togglePause(false); }
            @Override public void onRestart() { init(); togglePause(false); }
            @Override public void onMainMenu() { game.setScreen(new MenuScreen(game)); }
        });
        stage.addActor(pauseMenu); // keep attached; control visibility

        init();
    }

    private void init() {
        player = new Player(new Vector2(SwordSurvivorGame.WORLD_WIDTH/2f, SwordSurvivorGame.WORLD_HEIGHT/2f));
        enemies.clear();
        orbs.clear();
        spawner.reset();
        kills = 0;
        gameOver = false;
        levelUpPending = false;
        paused = false;
        pauseMenu.hide();

        lastAnnouncedWave = 0;
        // announce Wave 1
        stage.addActor(new WaveBanner(game.font, "Wave 1!",
            stage.getCamera().position.x, stage.getCamera().position.y, 1.25f));
        lastAnnouncedWave = 1;
    }

    @Override public void show() {}

    @Override
    public void render(float delta) {
        handleGlobalInput();

        // Camera follow (lock to player)
        stage.getCamera().position.set(player.pos.x, player.pos.y, 0f);
        stage.getCamera().update();
        pauseMenu.centerOnCamera(viewport, stage.getCamera());

        // Clear outside to black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update world unless paused or in modal state
        if (!gameOver && !levelUpPending && !paused) {
            update(delta);
        }

        // Draw world
        shapes.setProjectionMatrix(stage.getCamera().combined);
        batch.setProjectionMatrix(stage.getCamera().combined);
        drawWorld();
        drawHUD();

        // Stage actors (damage numbers, banners, pause menu)
        stage.act(delta);
        stage.draw();

        // Overlays
        if (levelUpPending) drawLevelUpOverlay();
        if (gameOver) drawGameOverOverlay();
    }

    private void handleGlobalInput() {
        // ESCAPE key handling
        if (!levelUpPending && !gameOver && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!paused) {
                // playing → open pause menu
                togglePause(true);
            } else {
                // already paused → go back to main menu
                game.setScreen(new MenuScreen(game));
            }
            return; // consume ESCAPE completely
        }

        // P toggles pause (only when not paused)
        if (!levelUpPending && !gameOver && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            togglePause(!paused);
        }

        if (paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                init();
                togglePause(false);
            }
            return;
        }

        // while NOT paused and not in overlays
        if (gameOver && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            init();
        }
    }



    private void togglePause(boolean makePaused) {
        paused = makePaused;
        if (paused) pauseMenu.show(); else pauseMenu.hide();
    }

    private void update(float dt) {
        Vector2 mouse = getMouseWorld();
        player.update(dt, mouse);

        // wave progression & announcement
        spawner.update(dt);
        int currentWave = spawner.getWave();
        if (currentWave > lastAnnouncedWave) {
            stage.addActor(new WaveBanner(game.font, "Wave " + currentWave + "!",
                stage.getCamera().position.x, stage.getCamera().position.y, 1.25f));
            lastAnnouncedWave = currentWave;
        }
        while (spawner.shouldSpawn()) {
            float vw = viewport.getWorldWidth();
            float vh = viewport.getWorldHeight();
            float cx = stage.getCamera().position.x;
            float cy = stage.getCamera().position.y;
            enemies.add(spawner.spawnEnemyAroundView(cx, cy, vw, vh));
        }

        // Enemies
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(dt, player.pos);

            // Touch damage
            if (e.collidesWithPlayer(player)) {
                if (player.hurtCooldown <= 0f) {
                    player.hp -= e.touchDamage;
                    player.hurtCooldown = 0.6f;
                    if (player.hp <= 0) {
                        gameOver = true;
                        game.maybeSetHighscore(kills);
                    }
                }
            }

            // Sword collision
            if (player.swing.active && CombatUtils.swordHitsEnemyThisFrame(player, e)) {
                if (e.lastHitSwingId != player.swing.id) {
                    e.hp -= player.damage;
                    e.lastHitSwingId = player.swing.id;

                    // Damage number
                    stage.addActor(new DamageText(game.font, String.valueOf(player.damage),
                        e.pos.x, e.pos.y + e.size * 0.6f, Color.ORANGE));

                    if (e.hp <= 0) {
                        enemies.removeIndex(i);
                        kills++;
                        orbs.add(new Orb(e.pos.cpy(), 1 + spawner.getWave()/2));
                    }
                }
            }
        }

        // Orbs
        for (int i = orbs.size - 1; i >= 0; i--) {
            Orb o = orbs.get(i);
            o.update(dt, player.pos, player.pickupRange);
            if (o.canPickup(player.pos, player.pickupRange)) {
                int xpGain = Math.round(o.value * player.xpGain);
                if (player.gainXP(xpGain)) {
                    rollBoonChoices();
                    levelUpPending = true;
                }
                orbs.removeIndex(i);
            }
        }
    }

    private void drawWorld() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Play area panel (dark gray)
        shapes.setColor(0.10f, 0.12f, 0.14f, 1f);
        shapes.rect(0, 0, SwordSurvivorGame.WORLD_WIDTH, SwordSurvivorGame.WORLD_HEIGHT);

        // Player (green)
        shapes.setColor(Color.GREEN);
        RenderUtils.rectCentered(shapes, player.pos.x, player.pos.y, player.size, player.size);

        // Sword (gray) if swinging
        if (player.swing.active) {
            shapes.setColor(Color.GRAY);
            Vector2 p0 = player.pos;
            Vector2 p1 = com.rgs.swordsurvivor.combat.CombatUtils.endOfSword(player);
            shapes.rectLine(p0, p1, player.swordThickness);
        }

        // Enemies (red)
        shapes.setColor(Color.RED);
        for (Enemy e : enemies) {
            RenderUtils.rectCentered(shapes, e.pos.x, e.pos.y, e.size, e.size);
        }

        // Orbs (yellow)
        shapes.setColor(Color.YELLOW);
        for (Orb o : orbs) {
            shapes.circle(o.pos.x, o.pos.y, o.radius);
        }

        shapes.end();
    }

    private void drawHUD() {
        batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1f);
        String stats = String.format("HP: %d  |  Lvl: %d (XP %d/%d)  |  Kills: %d  |  Wave: %d",
            player.hp, player.level, player.xp, player.xpToNext, kills, spawner.getWave());
        float left = stage.getCamera().position.x - viewport.getWorldWidth()/2f + 10f;
        float top = stage.getCamera().position.y + viewport.getWorldHeight()/2f - 10f;
        game.font.draw(batch, stats, left, top);

        // Crosshair
        Vector2 m = getMouseWorld();
        game.font.draw(batch, "+", m.x - 3, m.y + 4);

        // Paused tag
        if (paused) {
            game.font.setColor(Color.LIGHT_GRAY);
            game.font.draw(batch, "[PAUSED]", left, top - 18f);
        }

        batch.end();
    }

    private void drawLevelUpOverlay() {
        // darken
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.6f);
        shapes.rect(
            stage.getCamera().position.x - viewport.getWorldWidth()/2f,
            stage.getCamera().position.y - viewport.getWorldHeight()/2f,
            viewport.getWorldWidth(), viewport.getWorldHeight());
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        float cw = 220, ch = 140, gap = 40;
        float totalW = cw * 3 + gap * 2;
        float startX = stage.getCamera().position.x - totalW/2f;
        float y = stage.getCamera().position.y - ch/2f;

        // cards
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 3; i++) {
            float x = startX + i * (cw + gap);
            shapes.setColor(0.15f, 0.17f, 0.22f, 1f);
            shapes.rect(x, y, cw, ch);
            shapes.setColor(0.45f, 0.75f, 1f, 1f);
            shapes.rect(x, y, cw, 5);
        }
        shapes.end();

        // text
        batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1.2f);
        String header = "Level Up! Choose a Boon (1 / 2 / 3)";
        layout.setText(game.font, header);
        game.font.draw(batch, header,
            stage.getCamera().position.x - layout.width/2f,
            stage.getCamera().position.y + viewport.getWorldHeight()/2f - 120);

        float cx = startX + 12;
        for (int i = 0; i < 3; i++) {
            Boon b = boonChoices[i].boon;
            float tx = cx + i * (cw + gap);
            float ty = y + ch - 12;

            game.font.setColor(Color.valueOf("B3E5FC"));
            game.font.getData().setScale(1.1f);
            game.font.draw(batch, "[" + (i+1) + "] " + b.title, tx, ty);

            game.font.setColor(Color.LIGHT_GRAY);
            game.font.getData().setScale(0.95f);
            RenderUtils.drawWrapped(game.font, batch, b.desc, tx, ty - 20, cw - 24, 18);
        }
        batch.end();

        // input
        int pick = -1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) pick = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) pick = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) pick = 2;

        if (Gdx.input.justTouched()) {
            Vector2 m = getMouseWorld();
            for (int i = 0; i < 3; i++) {
                float x = startX + i * (cw + gap);
                if (m.x >= x && m.x <= x + cw && m.y >= y && m.y <= y + ch) { pick = i; break; }
            }
        }

        if (pick != -1) {
            boonChoices[pick].boon.apply(player);
            levelUpPending = false;
        }
    }

    private void drawGameOverOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0,0,0,0.65f);
        shapes.rect(
            stage.getCamera().position.x - viewport.getWorldWidth()/2f,
            stage.getCamera().position.y - viewport.getWorldHeight()/2f,
            viewport.getWorldWidth(), viewport.getWorldHeight());
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        game.font.setColor(Color.SCARLET);
        game.font.getData().setScale(2f);
        layout.setText(game.font, "GAME OVER");
        game.font.draw(batch, layout,
            stage.getCamera().position.x - layout.width/2f,
            stage.getCamera().position.y + 40);

        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1f);
        String s = "Kills: " + kills + "   |   High Score: " + game.getHighscoreKills();
        layout.setText(game.font, s);
        game.font.draw(batch, s,
            stage.getCamera().position.x - layout.width/2f,
            stage.getCamera().position.y - 10);

        String hint = "Press R to Retry   •   ESC to Menu";
        layout.setText(game.font, hint);
        game.font.draw(batch, hint,
            stage.getCamera().position.x - layout.width/2f,
            stage.getCamera().position.y - 40);
        batch.end();
    }

    private void rollBoonChoices() {
        java.util.EnumSet<BoonType> pool = EnumSet.allOf(BoonType.class);
        for (int i = 0; i < 3; i++) {
            if (pool.isEmpty()) pool = EnumSet.allOf(BoonType.class);
            int idx = rng.nextInt(pool.size());
            int c = 0;
            BoonType chosen = null;
            for (BoonType t : pool) { if (c == idx) { chosen = t; break; } c++; }
            pool.remove(chosen);
            boonChoices[i] = new BoonCard(Boon.fromType(chosen));
        }
    }

    private Vector2 getMouseWorld() {
        Vector3 tmp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(tmp);
        return new Vector2(tmp.x, tmp.y);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        shapes.dispose();
        stage.dispose();
        pauseMenu.dispose();
    }
}
