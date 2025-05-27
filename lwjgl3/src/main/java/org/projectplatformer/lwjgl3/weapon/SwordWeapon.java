package org.projectplatformer.lwjgl3.weapon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.List;

public class SwordWeapon implements Weapon {
    private final float duration, cooldown;
    private final int damage;

    private float timer = 0f;
    private float cooldownTimer = 0f;
    private boolean damageDone = false;
    private final Rectangle hitbox = new Rectangle();

    private float pivotX, pivotY;
    private boolean facingRight;

    private static final float START_ANGLE = 90f;
    private static final float SWEEP_DELTA = -135f;
    private static final float RADIUS = 40f;
    private static final float SIZE = 20f;

    public SwordWeapon(float duration, float cooldown, int damage) {
        this.duration = duration;
        this.cooldown = cooldown;
        this.damage = damage;
    }

    public SwordWeapon() {
        this(0.35f, 0.2f, 20);
    }

    @Override
    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        this.timer = duration;
        this.cooldownTimer = cooldown;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.facingRight = facingRight;
        this.damageDone = false;
        hitbox.set(0, 0, 0, 0);
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.facingRight = facingRight;
        timer = Math.max(0f, timer - delta);
        cooldownTimer = Math.max(0f, cooldownTimer - delta);
        if (timer > 0f) {
            float t = 1f - (timer / duration);
            float angle = START_ANGLE + SWEEP_DELTA * t;
            if (!facingRight) angle = 180f - angle;
            float rad = angle * MathUtils.degRad;
            float cx = pivotX + MathUtils.cos(rad) * RADIUS;
            float cy = pivotY + MathUtils.sin(rad) * RADIUS;
            hitbox.set(cx - SIZE/2f, cy - SIZE/2f, SIZE, SIZE);
        } else {
            hitbox.set(0, 0, 0, 0);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return timer > 0f ? hitbox : null;
    }

    @Override
    public void applyDamage(List<BaseEnemy> enemies) {
        if (timer <= 0f || damageDone) return;
        for (BaseEnemy e : enemies) {
            if (e.isAlive() && hitbox.overlaps(e.getBounds())) {
                e.takeDamage(damage);
                damageDone = true;
                break;
            }
        }
    }

    @Override
    public void applyDamage(Player player) {
        if (timer <= 0f || damageDone) return;
        if (hitbox.overlaps(player.getBounds())) {
            player.takeDamage(damage);
            damageDone = true;
        }
    }

    @Override
    public float getCooldownRemaining() {
        return cooldownTimer;
    }

    public void renderTrajectory(ShapeRenderer r) {
        if (timer <= 0f) return;
        if (facingRight) {
            r.arc(pivotX, pivotY, RADIUS, START_ANGLE, SWEEP_DELTA);
        } else {
            r.arc(pivotX, pivotY, RADIUS, START_ANGLE, -SWEEP_DELTA);
        }
    }

    public boolean isAttacking() {
        return timer > 0f;
    }
}
