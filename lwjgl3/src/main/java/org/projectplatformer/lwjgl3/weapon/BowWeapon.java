package org.projectplatformer.lwjgl3.weapon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BowWeapon implements Weapon {
    private static final float GRAVITY = 800f;
    private static final Texture arrowTexture = new Texture("arrow.png");

    private final float cooldown;
    private final float initialSpeed;
    private final float launchAngleDeg;
    private final float arrowW, arrowH;
    private final float maxRange;
    private final int damage;

    private float cooldownTimer = 0f;
    private List<Rectangle> worldPlatforms;
    private final List<Projectile> arrows = new ArrayList<>();

    private static class Projectile {
        Rectangle hitbox;
        float vx, vy;
        float travelled = 0f;
        boolean facingRight;
        Projectile(Rectangle hb, float vx, float vy, boolean facingRight) {
            this.hitbox = hb;
            this.vx = vx;
            this.vy = vy;
            this.facingRight = facingRight;
        }
    }

    public BowWeapon(float cooldown, float speed, float angleDeg,
                     float w, float h, float maxRange, int damage) {
        this.cooldown       = cooldown;
        this.initialSpeed   = speed;
        this.launchAngleDeg = angleDeg;
        this.arrowW         = w;
        this.arrowH         = h;
        this.maxRange       = maxRange;
        this.damage         = damage;
    }

    public BowWeapon() {
        this(0.8f, 500f, 40f, 20f, 6f, 800f, 25);
    }

    @Override
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        // не використовується
    }

    public void setPlatforms(List<Rectangle> platforms) {
        this.worldPlatforms = platforms;
    }

    public void releaseArrow(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        cooldownTimer = cooldown;
        double ang = Math.toRadians(launchAngleDeg);
        float vx = (float)(Math.cos(ang) * initialSpeed) * (facingRight ? 1 : -1);
        float vy = (float)(Math.sin(ang) * initialSpeed);
        float x = facingRight ? pivotX : pivotX - arrowW;
        float y = pivotY - arrowH / 2f;
        Rectangle hb = new Rectangle(x, y, arrowW, arrowH);
        arrows.add(new Projectile(hb, vx, vy, facingRight));
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        cooldownTimer = Math.max(0f, cooldownTimer - delta);
        Iterator<Projectile> it = arrows.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.vy -= GRAVITY * delta;
            float dx = p.vx * delta;
            float dy = p.vy * delta;
            p.hitbox.x += dx;
            p.hitbox.y += dy;
            p.travelled += Math.hypot(dx, dy);

            boolean hitPlatform = worldPlatforms != null && worldPlatforms.stream()
                .anyMatch(pl -> p.hitbox.overlaps(pl));
            if (hitPlatform || p.travelled >= maxRange || p.hitbox.y + p.hitbox.height < 0) {
                it.remove();
            }
        }
    }

    @Override
    public Rectangle getHitbox() {
        return arrows.isEmpty() ? null : arrows.get(0).hitbox;
    }

    @Override
    public void applyDamage(List<BaseEnemy> enemies) {
        Iterator<Projectile> it = arrows.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            for (BaseEnemy e : enemies) {
                if (e.isAlive() && p.hitbox.overlaps(e.getBounds())) {
                    e.takeDamage(damage);
                    it.remove();
                    break;
                }
            }
        }
    }

    @Override
    public void applyDamage(Player player) {
        Iterator<Projectile> it = arrows.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (player.isAlive() && p.hitbox.overlaps(player.getBounds())) {
                player.takeDamage(damage);
                it.remove();
                break;
            }
        }
    }

    @Override
    public float getCooldownRemaining() {
        return cooldownTimer;
    }

    public void renderProjectiles(SpriteBatch batch) {
        for (Projectile p : arrows) {
            float x = p.hitbox.x;
            float y = p.hitbox.y;
            float w = p.hitbox.width;
            float h = p.hitbox.height;
            if (p.facingRight) {
                batch.draw(arrowTexture, x, y, w, h);
            } else {
                batch.draw(arrowTexture, x + w, y, -w, h);
            }
        }
    }
}
