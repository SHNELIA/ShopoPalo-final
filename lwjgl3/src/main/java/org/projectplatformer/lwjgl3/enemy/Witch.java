package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Witch extends BaseEnemy {
    private static final float CIRCLE_RADIUS      = 100f;   // радіус кола
    private static final float ANGULAR_SPEED      = 1.0f;   // радіан за секунду

    // параметри стрільби
    private static final float AGGRO_RANGE       = 400f;
    private static final float CHASE_SPEED       = 180f;
    private static final float FIREBALL_COOLDOWN = 2.0f;
    private static final int   FIREBALL_DAMAGE   = 20;

    private final float spawnCenterX;
    private final float spawnCenterY;
    private float angle = 0f;

    private float fireballTimer = 0f;
    private final List<Fireball> fireballs = new ArrayList<>();

    public Witch(float x, float y, Texture tex) {
        super(x, y, 48, 56, tex,
            /* health= */200,
            /* gravity= */0, /* maxFallSpeed= */0,
            /* drag= */0, /* maxStepHeight= */0, /* stepUpSpeed= */0);
        this.spawnCenterX = x + getBounds().width/2f;
        this.spawnCenterY = y + getBounds().height/2f;
        this.attackRange    = 48f;
        this.attackDamage   = 24;
        this.attackCooldown = 1.2f;
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        if (!alive) return;

        // 1) Стріляємо, якщо гравець близько
        Rectangle b = physics.getBounds();
        float cx = b.x + b.width/2f;
        float cy = b.y + b.height/2f;

        float px = player.getBounds().x + player.getBounds().width/2f;
        float py = player.getBounds().y + player.getBounds().height/2f;
        float dx = px - cx, dy = py - cy;
        if (dx*dx + dy*dy < AGGRO_RANGE*AGGRO_RANGE) {
            fireballTimer -= delta;
            if (fireballTimer <= 0f) {
                fireballs.add(new Fireball(cx, cy, dx, dy));
                fireballTimer = FIREBALL_COOLDOWN;
            }
        }

        // 2) Політ по колу
        angle += ANGULAR_SPEED * delta;
        float newCx = spawnCenterX + CIRCLE_RADIUS * (float)Math.cos(angle);
        float newCy = spawnCenterY + CIRCLE_RADIUS * (float)Math.sin(angle);

        b.x = newCx - b.width/2f;
        b.y = newCy - b.height/2f;

        // 3) Оновлюємо фаєрболи
        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball fb = fireballs.get(i);
            fb.update(delta);
            if (fb.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(FIREBALL_DAMAGE);
                fireballs.remove(i);
            } else if (fb.isOffscreen()) {
                fireballs.remove(i);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        for (Fireball fb : fireballs) fb.render(batch);
    }

    @Override
    public void renderHitbox(ShapeRenderer r) {
        super.renderHitbox(r);
        for (Fireball fb : fireballs) fb.renderHitbox(r);
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Fireball fb : fireballs) fb.dispose();
    }

    // --- Внутрішній клас Fireball (те саме, що і раніше) ---
    private static class Fireball {
        private static final float SPEED = 260f;
        private static final float SIZE  = 14f;
        private float x, y, vx, vy;
        private Rectangle bounds;
        private static final Texture fireballTex = new Texture("Enemies/Witch/Attack/Fireball.png");

        public Fireball(float startX, float startY, float dx, float dy) {
            this.x = startX - SIZE/2f;
            this.y = startY - SIZE/2f;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            vx = (dx/dist)*SPEED;
            vy = (dy/dist)*SPEED;
            bounds = new Rectangle(x, y, SIZE, SIZE);
        }

        public void update(float delta) {
            x += vx*delta;
            y += vy*delta;
            bounds.setPosition(x, y);
        }

        public Rectangle getBounds() { return bounds; }

        public boolean isOffscreen() {
            return x < -SIZE || x > 2000 || y < -SIZE || y > 2000;
        }

        public void render(SpriteBatch batch) {
            batch.draw(fireballTex, x, y, SIZE, SIZE);
        }

        public void renderHitbox(ShapeRenderer r) {
            r.setColor(1f,0.5f,0f,1f);
            r.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        public void dispose() {
            fireballTex.dispose();
        }
    }
}
