package org.projectplatformer.lwjgl3.weapon;

import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.enemy.BaseEnemy;
import org.projectplatformer.lwjgl3.player.Player;

import java.util.List;

public class SpearWeapon implements Weapon {
    private final float maxLength;
    private final float width;
    private final float duration;
    private final float cooldown;
    private final int damage;

    private float timer = 0f;
    private float cooldownTimer = 0f;
    private boolean damageDone = false;

    private final Rectangle hitbox = new Rectangle();

    private float pivotX, pivotY;
    private boolean facingRight;

    public SpearWeapon(float maxLength, float width, float duration, float cooldown, int damage) {
        this.maxLength = maxLength;
        this.width     = width;
        this.duration  = duration;
        this.cooldown  = cooldown;
        this.damage    = damage;
    }

    @Override
    public void update(float delta, float pivotX, float pivotY, boolean facingRight) {
        this.pivotX      = pivotX;
        this.pivotY      = pivotY;
        this.facingRight = facingRight;

        timer         = Math.max(0f, timer - delta);
        cooldownTimer = Math.max(0f, cooldownTimer - delta);

        if (timer > 0f) {
            float t = 1f - (timer / duration);
            float lengthNow = t * maxLength;
            float cx = pivotX + (facingRight ? lengthNow : -lengthNow);
            float cy = pivotY;
            hitbox.set(
                cx - width / 2f,
                cy - width / 2f,
                width,
                width
            );
        } else {
            hitbox.set(0, 0, 0, 0);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return timer > 0f ? hitbox : null;
    }

    public void startAttack(float pivotX, float pivotY, boolean facingRight) {
        if (cooldownTimer > 0f) return;
        this.pivotX        = pivotX;
        this.pivotY        = pivotY;
        this.facingRight   = facingRight;
        this.timer         = duration;
        this.cooldownTimer = cooldown;
        this.damageDone    = false;
        hitbox.set(0, 0, 0, 0);
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

    public float getCooldownRemaining() {
        return cooldownTimer;
    }

    public boolean isAttacking() {
        return timer > 0f;
    }
}
