package com.rgs.swordsurvivor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;

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
import com.rgs.swordsurvivor.ui.GameOverMenu;
import com.rgs.swordsurvivor.ui.WaveBanner;
import com.rgs.swordsurvivor.ui.VirtualJoystick;
import com.rgs.swordsurvivor.util.RenderUtils;

import java.util.EnumSet;
import java.util.Random;

public class GameScreen implements Screen {
    private final SwordSurvivorGame game;

    // World stage (follows player camera)
    private final ExtendViewport viewport;
    private final Stage stage;

    // UI stage (fixed to screen)
    private ScreenViewport uiViewport;
    private Stage uiStage;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch;

    // Textures
    private Texture texPlayer, texEnemy, texSword;

    // Entities / systems
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

    // Menus (world-space overlays that follow camera)
    private PauseMenu pauseMenu;
    private GameOverMenu gameOverMenu;

    // HUD text
    private final GlyphLayout layout = new GlyphLayout();

    // Pause button (UI-space, top-right)
    private ImageButton pauseBtn;
    private Texture btnUpTex, btnOverTex, btnDownTex, pauseIconTex;

    // Touch joysticks (UI-space)
    private VirtualJoystick leftJoy;
    private VirtualJoystick rightJoy;

    public GameScreen(SwordSurvivorGame game) {
        this.game = game;
        this.batch = game.batch;

        // World stage (extend viewport)
        viewport = new ExtendViewport(SwordSurvivorGame.VIEW_WIDTH, SwordSurvivorGame.VIEW_HEIGHT);
        stage = new Stage(viewport, batch);

        // UI stage (screen viewport)
        uiViewport = new ScreenViewport();
        uiStage = new Stage(uiViewport, batch);

        // Input: UI first, then world
        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, stage));

        // Menus (world stage)
        pauseMenu = new PauseMenu(game.font, new PauseMenu.Listener() {
            @Override public void onResume() { togglePause(false); }
            @Override public void onRestart() { init(); togglePause(false); }
            @Override public void onMainMenu() { game.setScreen(new MenuScreen(game)); }
        });
        stage.addActor(pauseMenu);

        gameOverMenu = new GameOverMenu(game.font, new GameOverMenu.Listener() {
            @Override public void onRestart() { init(); }
            @Override public void onMainMenu() { game.setScreen(new MenuScreen(game)); }
        });
        stage.addActor(gameOverMenu);

        // Pause button (UI stage)
        btnUpTex   = makeSolid(0.18f, 0.22f, 0.28f, 1f);
        btnOverTex = makeSolid(0.24f, 0.30f, 0.38f, 1f);
        btnDownTex = makeSolid(0.14f, 0.16f, 0.20f, 1f);
        pauseIconTex = makePauseIcon(32, 1f, 1f, 1f, 1f);

        ImageButtonStyle pStyle = new ImageButtonStyle();
        pStyle.up   = new TextureRegionDrawable(new TextureRegion(btnUpTex));
        pStyle.over = new TextureRegionDrawable(new TextureRegion(btnOverTex));
        pStyle.down = new TextureRegionDrawable(new TextureRegion(btnDownTex));
        TextureRegionDrawable icon = new TextureRegionDrawable(new TextureRegion(pauseIconTex));
        pStyle.imageUp = icon; pStyle.imageOver = icon; pStyle.imageDown = icon;

        pauseBtn = new ImageButton(pStyle);
        pauseBtn.setSize(44f, 44f);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!levelUpPending && !gameOver) togglePause(true);
            }
        });
        uiStage.addActor(pauseBtn);

        // Virtual joysticks (UI stage)
        leftJoy  = new VirtualJoystick(64f, 28f, 6f);
        rightJoy = new VirtualJoystick(64f, 28f, 6f);
        uiStage.addActor(leftJoy);
        uiStage.addActor(rightJoy);

        // Touch routing on UI stage: left half = movement, right half = aim
        uiStage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                // If an overlay/menu is up, let the event pass to the world stage buttons
                if (paused || gameOver || levelUpPending) return false;

                float cx = uiStage.getCamera().position.x;
                boolean handled = false;
                if (x <= cx) {
                    if (!leftJoy.isActive()) { leftJoy.activate(pointer, x, y); handled = true; }
                } else {
                    if (!rightJoy.isActive()) { rightJoy.activate(pointer, x, y); handled = true; }
                }
                return handled; // only consume when we actually spawned a joystick
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (leftJoy.isActive()  && pointer == leftJoy.getPointerId())  leftJoy.drag(pointer, x, y);
                if (rightJoy.isActive() && pointer == rightJoy.getPointerId()) rightJoy.drag(pointer, x, y);
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                leftJoy.release(pointer);
                rightJoy.release(pointer);
            }
        });


        // Load textures
        texPlayer = new Texture(Gdx.files.internal("player.png"));
        texEnemy  = new Texture(Gdx.files.internal("enemy.png"));
        texSword  = new Texture(Gdx.files.internal("sword.png"));

        init();
    }

    private Texture makeSolid(float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(r,g,b,a); pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Texture makePauseIcon(int size, float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0,0,0,0); pm.fill();
        pm.setColor(r,g,b,a);
        int margin = Math.max(2, size/6);
        int barW = Math.max(2, size/6);
        int gap  = Math.max(2, size/6);
        int totalW = barW + gap + barW;
        int leftX = (size - totalW) / 2;
        int topY = margin;
        int height = size - margin*2;
        pm.fillRectangle(leftX, topY, barW, height);
        pm.fillRectangle(leftX + barW + gap, topY, barW, height);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
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
        gameOverMenu.hide();

        // Show Wave 1 above player
        lastAnnouncedWave = 1;
        stage.addActor(new WaveBanner(game.font, "Wave 1!", player.pos, player.size + 32f, 1.25f));
    }

    @Override public void show() {
        // Music routing
        if (!game.musicGame.isPlaying()) game.musicGame.play();
        if (game.musicMenu.isPlaying()) game.musicMenu.stop();
    }

    @Override
    public void render(float delta) {
        handleGlobalInput();

        // Camera follow & clamp to world
        float halfW = viewport.getWorldWidth()/2f;
        float halfH = viewport.getWorldHeight()/2f;
        float camX = MathUtils.clamp(player.pos.x, halfW, SwordSurvivorGame.WORLD_WIDTH - halfW);
        float camY = MathUtils.clamp(player.pos.y, halfH, SwordSurvivorGame.WORLD_HEIGHT - halfH);
        stage.getCamera().position.set(camX, camY, 0f);
        stage.getCamera().update();

        // Keep world overlays centered
        pauseMenu.centerOnCamera(viewport, stage.getCamera());
        if (gameOver) gameOverMenu.centerOnCamera(viewport, stage.getCamera());

        // UI: position pause button in top-right of UI stage
        float uvw = uiViewport.getWorldWidth();
        float uvh = uiViewport.getWorldHeight();
        float ucx = uiStage.getCamera().position.x;
        float ucy = uiStage.getCamera().position.y;
        float left = ucx - uvw/2f, bottom = ucy - uvh/2f;
        pauseBtn.setPosition(left + uvw - pauseBtn.getWidth() - 10f, bottom + uvh - pauseBtn.getHeight() - 10f);

        // Show/hide HUD controls
        boolean hudVisible = !paused && !levelUpPending && !gameOver;
        pauseBtn.setVisible(hudVisible);
        leftJoy.setVisible(hudVisible && leftJoy.isActive());
        rightJoy.setVisible(hudVisible && rightJoy.isActive());
        if (!hudVisible) { leftJoy.release(leftJoy.getPointerId()); rightJoy.release(rightJoy.getPointerId()); }

        // Clear screen
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update world
        if (!gameOver && !levelUpPending && !paused) update(delta);

        // Draw world & HUD text
        shapes.setProjectionMatrix(stage.getCamera().combined);
        batch.setProjectionMatrix(stage.getCamera().combined);
        drawWorld();
        drawHUD();

        // Stages: world first, then UI (so buttons/joysticks draw on top)
        stage.act(delta);
        stage.draw();

        uiStage.act(delta);
        uiStage.draw();

        // Level-up overlay (world‑space)
        if (levelUpPending) drawLevelUpOverlay();
    }

    private void handleGlobalInput() {
        // GAME OVER
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) { init(); return; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { game.setScreen(new MenuScreen(game)); return; }
            return;
        }

        // Level-up overlay blocks pause toggles
        if (levelUpPending) return;

        // PAUSED
        if (paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) { togglePause(false); return; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) { init(); togglePause(false); return; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { game.setScreen(new MenuScreen(game)); return; }
            return;
        }

        // PLAYING: Esc or P -> open pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            togglePause(true);
        }
    }

    private void togglePause(boolean makePaused) {
        paused = makePaused;
        if (paused) pauseMenu.show(); else pauseMenu.hide();
    }

    private void update(float dt) {
        // ----- Movement -----
        // If the left joystick is active, drive movement here.
        // Otherwise, DO NOT move here—let Player.update() handle keyboard like before.
        if (leftJoy.isActive()) {
            Vector2 move = new Vector2(leftJoy.getValue());
            if (move.len2() > 1f) move.nor();
            if (!move.isZero()) {
                move.nor().scl(player.speed * dt);
                player.pos.add(move);
                // Optional: set facing based on joystick X
                if (move.x < 0) player.facingLeft = true;
                else if (move.x > 0) player.facingLeft = false;
            }
        }

        // Aim (right joystick overrides mouse)
        Vector2 aim = getAimWorld();

        // Player update (aim/swing, timers, etc.)
        player.update(dt, aim);

        // Keep player inside the playable world
        float half = player.size * 0.5f;
        player.pos.x = MathUtils.clamp(player.pos.x, half, SwordSurvivorGame.WORLD_WIDTH  - half);
        player.pos.y = MathUtils.clamp(player.pos.y, half, SwordSurvivorGame.WORLD_HEIGHT - half);

        // Spawner & wave banner
        spawner.update(dt);
        int currentWave = spawner.getWave();
        if (currentWave > lastAnnouncedWave) {
            stage.addActor(new WaveBanner(game.font, "Wave " + currentWave + "!", player.pos, player.size + 32f, 1.25f));
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

            // Facing flip
            if (player.pos.x < e.pos.x) e.facingLeft = true; else if (player.pos.x > e.pos.x) e.facingLeft = false;

            // Touch damage
            if (e.collidesWithPlayer(player)) {
                if (player.hurtCooldown <= 0f) {
                    player.hp -= e.touchDamage;
                    player.hurtCooldown = 0.6f;
                    player.hurtTimer = 0.2f;
                    game.sfxPlayerHit.play();
                    if (player.hp <= 0) {
                        gameOver = true;
                        game.maybeSetHighscore(kills);
                        gameOverMenu.setScores(kills, game.getHighscoreKills());
                        gameOverMenu.show();
                    }
                }
            }

            // Sword collision
            if (player.swing.active && CombatUtils.swordHitsEnemyThisFrame(player, e)) {
                if (e.lastHitSwingId != player.swing.id) {
                    e.hp -= player.damage;
                    e.lastHitSwingId = player.swing.id;
                    e.hurtTimer = 0.2f;
                    game.sfxEnemyHit.play();

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
                game.sfxPickup.play();
                int xpGain = Math.round(o.value * player.xpGain);
                if (player.gainXP(xpGain)) {
                    rollBoonChoices();
                    levelUpPending = true;
                    game.sfxLevel.play();
                }
                orbs.removeIndex(i);
            }
        }
    }

    private Vector2 getAimWorld() {
        // If right joystick is active, convert its direction to a world aim point
        if (rightJoy != null && rightJoy.isActive()) {
            Vector2 dir = new Vector2(rightJoy.getValue());
            if (dir.isZero()) return new Vector2(player.pos);
            dir.nor().scl(player.swordReach);
            return new Vector2(player.pos).add(dir);
        }
        // Mouse -> world
        Vector3 tmp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(tmp);
        return new Vector2(tmp.x, tmp.y);
    }

    private void drawWorld() {
        // Background play area
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.10f, 0.12f, 0.14f, 1f);
        shapes.rect(0, 0, SwordSurvivorGame.WORLD_WIDTH, SwordSurvivorGame.WORLD_HEIGHT);
        // XP orbs
        shapes.setColor(Color.YELLOW);
        for (Orb o : orbs) shapes.circle(o.pos.x, o.pos.y, o.radius);
        shapes.end();

        // Textured sprites
        batch.begin();

        // Sword
        if (player.swing.active) {
            Vector2 p0 = player.pos;
            Vector2 p1 = CombatUtils.endOfSword(player);
            float dx = p1.x - p0.x, dy = p1.y - p0.y;
            float length = (float)Math.sqrt(dx*dx + dy*dy);
            float angle = (float)Math.toDegrees(Math.atan2(dy, dx));
            batch.draw(texSword,
                p0.x, p0.y - player.swordThickness/2f,
                0, player.swordThickness/2f,
                length, player.swordThickness,
                1f, 1f, angle,
                0, 0, texSword.getWidth(), texSword.getHeight(),
                false, false);
        }

        // Player
        batch.setColor(player.hurtTimer > 0f ? Color.RED : Color.WHITE);
        if (!player.facingLeft) {
            batch.draw(texPlayer, player.pos.x - player.size/2f, player.pos.y - player.size/2f, player.size, player.size);
        } else {
            batch.draw(texPlayer, player.pos.x + player.size/2f, player.pos.y - player.size/2f, -player.size, player.size);
        }

        // Enemies
        for (Enemy e : enemies) {
            batch.setColor(e.hurtTimer > 0f ? Color.RED : Color.WHITE);
            if (!e.facingLeft) {
                batch.draw(texEnemy, e.pos.x - e.size/2f, e.pos.y - e.size/2f, e.size, e.size);
            } else {
                batch.draw(texEnemy, e.pos.x + e.size/2f, e.pos.y - e.size/2f, -e.size, e.size);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawHUD() {
        batch.begin();
        BitmapFont f = game.font;
        f.setColor(Color.WHITE);
        f.getData().setScale(1f);

        String stats = "HP: " + player.hp +
            "  |  Lvl: " + player.level +
            " (XP " + player.xp + "/" + player.xpToNext + ")" +
            "  |  Kills: " + kills +
            "  |  Wave: " + spawner.getWave();

        float left = stage.getCamera().position.x - viewport.getWorldWidth()/2f + 10f;
        float top  = stage.getCamera().position.y + viewport.getWorldHeight()/2f - 10f;
        f.draw(batch, stats, left, top);

        // Crosshair (mouse mode only)
        if (!(rightJoy != null && rightJoy.isActive())) {
            Vector2 m = getAimWorld();
            f.draw(batch, "+", m.x - 3, m.y + 4);
        }
        if (paused) {
            f.setColor(Color.LIGHT_GRAY);
            f.draw(batch, "[PAUSED]", left, top - 18f);
        }
        batch.end();
    }

    private void drawLevelUpOverlay() {
        // Ensure choices exist
        for (int i = 0; i < 3; i++) {
            if (boonChoices[i] == null || boonChoices[i].boon == null) {
                rollBoonChoices();
                break;
            }
        }

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

        // --- TEXT: reset state, then draw ---
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.setColor(1f,1f,1f,1f);
        game.font.setColor(1f,1f,1f,1f);

        batch.begin();
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
        // restore defaults
        game.font.getData().setScale(1f);
        game.font.setColor(Color.WHITE);
        batch.end();


        // input
        int pick = -1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) pick = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) pick = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) pick = 2;

        if (Gdx.input.justTouched()) {
            Vector3 mp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mp);
            Vector2 m = new Vector2(mp.x, mp.y);
            for (int i = 0; i < 3; i++) {
                float x = startX + i * (cw + gap);
                if (m.x >= x && m.x <= x + cw && m.y >= y && m.y <= y + ch) { pick = i; break; }
            }
        }

        if (pick != -1) {
            boonChoices[pick].boon.apply(player);
            player.hp = player.maxHp; // heal AFTER boon
            levelUpPending = false;
        }
    }

    private void rollBoonChoices() {
        EnumSet<BoonType> pool = EnumSet.allOf(BoonType.class);
        for (int i = 0; i < 3; i++) {
            if (pool.isEmpty()) pool = EnumSet.allOf(BoonType.class);
            int idx = rng.nextInt(pool.size());
            int c = 0; BoonType chosen = null;
            for (BoonType t : pool) { if (c == idx) { chosen = t; break; } c++; }
            pool.remove(chosen);
            boonChoices[i] = new BoonCard(Boon.fromType(chosen));
        }
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);    // world
        uiViewport.update(width, height, true);  // UI
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapes.dispose();
        stage.dispose();
        uiStage.dispose();

        texPlayer.dispose();
        texEnemy.dispose();
        texSword.dispose();

        btnUpTex.dispose();
        btnOverTex.dispose();
        btnDownTex.dispose();
        pauseIconTex.dispose();
    }
}
