package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.player.Player;
import org.projectplatformer.lwjgl3.EnemiesAnimation.WitchAnimationManager;

import java.util.ArrayList;
import java.util.List;

public class Witch extends BaseEnemy {
    private static final float CIRCLE_RADIUS = 100f;
    private static final float ANGULAR_SPEED = 1.0f;
    private static final float AGGRO_RANGE = 400f;
    private static final float FIREBALL_COOLDOWN = 1f;
    private static final int   FIREBALL_DAMAGE  = 10;

    private final float spawnCenterX;
    private final float spawnCenterY;
    private float angle = 0f;

    private float fireballTimer = 0f;
    private final List<Fireball> fireballs = new ArrayList<>();

    private final WitchAnimationManager anim;
    private WitchAnimationManager.State nextAnimState;

    public Witch(float x, float y) {
        super(x, y, 60, 100, null,
            200, 0, 0, 0, 0, 0);
        this.spawnCenterX = x + getBounds().width  / 2f;
        this.spawnCenterY = y + getBounds().height / 2f;
        this.attackRange    = 48f;
        this.attackDamage   = 24;
        this.attackCooldown = 1.2f;

        anim = new WitchAnimationManager();
        nextAnimState = WitchAnimationManager.State.FLY;
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        if (!alive) return;

        Rectangle b = physics.getBounds();
        float cx = b.x + b.width  / 2f;
        float cy = b.y + b.height / 2f;

        // агро чи вільне кружляння?
        float px = player.getBounds().x + player.getBounds().width  / 2f;
        float py = player.getBounds().y + player.getBounds().height / 2f;
        float dx = px - cx, dy = py - cy;

        if (dx*dx + dy*dy < AGGRO_RANGE*AGGRO_RANGE) {
            nextAnimState = WitchAnimationManager.State.ATTACK;
            fireballTimer -= delta;
            if (fireballTimer <= 0f) {
                fireballs.add(new Fireball(cx, cy, dx, dy));
                fireballTimer = FIREBALL_COOLDOWN;
                anim.resetAttackAnim();
            }
        } else {
            nextAnimState = WitchAnimationManager.State.FLY;
        }

        // обчислюємо бажану нову позицію по колу
        angle += ANGULAR_SPEED * delta;
        float targetX = spawnCenterX + CIRCLE_RADIUS * (float)Math.cos(angle) - b.width/2f;
        float targetY = spawnCenterY + CIRCLE_RADIUS * (float)Math.sin(angle) - b.height/2f;

        // спроба руху по X
        Rectangle testX = new Rectangle(targetX, b.y, b.width, b.height);
        boolean collideX = false;
        for (Rectangle p : platforms) {
            if (testX.overlaps(p)) { collideX = true; break; }
        }
        if (!collideX) {
            b.x = targetX;
        }

        // спроба руху по Y
        Rectangle testY = new Rectangle(b.x, targetY, b.width, b.height);
        boolean collideY = false;
        for (Rectangle p : platforms) {
            if (testY.overlaps(p)) { collideY = true; break; }
        }
        if (!collideY) {
            b.y = targetY;
        }

        // оновлюємо анімацію
        anim.update(delta, nextAnimState);

        // оновлення фаєрболів
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
        TextureRegion frame = anim.getCurrentFrame();
        Rectangle b = physics.getBounds();
        batch.draw(frame, b.x, b.y, b.width, b.height);
        for (Fireball fb : fireballs) fb.render(batch);
    }

    @Override
    public void renderHitbox(ShapeRenderer r) {
        super.renderHitbox(r);
        // (хітбокс фаєрболів вже малюється всередині Fireball.renderHitbox)
    }

    @Override
    public void dispose() {
        super.dispose();
        anim.dispose();
        for (Fireball fb : fireballs) fb.dispose();
    }

    private static class Fireball {
        private static final float SPEED = 260f;
        private static final float SIZE  = 14f;
        private float x, y, vx, vy;
        private Rectangle bounds;
        private static final Texture fireballTex =
            new Texture("Enemies/Witch/Attack/Fireball.png");

        public Fireball(float startX, float startY, float dx, float dy) {
            this.x = startX - SIZE/2f;
            this.y = startY - SIZE/2f;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            vx = (dx / dist) * SPEED;
            vy = (dy / dist) * SPEED;
            bounds = new Rectangle(x, y, SIZE, SIZE);
        }

        public void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            bounds.setPosition(x, y);
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public boolean isOffscreen() {
            return x < -SIZE || x > 2000 || y < -SIZE || y > 2000;
        }

        public void render(SpriteBatch batch) {
            batch.draw(fireballTex, x, y, SIZE, SIZE);
        }

        public void dispose() {
            fireballTex.dispose();
        }
    }
}
