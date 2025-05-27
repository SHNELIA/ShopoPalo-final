package org.projectplatformer.lwjgl3.physics;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;

/**
 * Загальна фізика: гравітація, step-up, горизонтальні переміщення та колізії.
 */
public class PhysicsComponent {
    private final Rectangle bounds;
    private float velX;
    private float velY;
    private final float gravity;
    private final float maxFallSpeed;
    private final float drag;
    private final float maxStepHeight;
    private final float stepUpSpeed;
    private boolean isClimbing;
    private float climbSpeed;

    public PhysicsComponent(Rectangle bounds,
                            float gravity,
                            float maxFallSpeed,
                            float drag,
                            float maxStepHeight,
                            float stepUpSpeed) {
        this.bounds = bounds;
        this.gravity = gravity;
        this.maxFallSpeed = maxFallSpeed;
        this.drag = drag;
        this.maxStepHeight = maxStepHeight;
        this.stepUpSpeed = stepUpSpeed;
    }

    /** Почати карабкання по стіні */
    public void startClimbing(float speed) {
        isClimbing = true;
        climbSpeed = speed;
    }

    /** Припинити карабкання */
    public void stopClimbing() {
        isClimbing = false;
    }

    /** Оновлення позиції та обробка колізій */
    public void update(float delta, List<Rectangle> platforms) {
        if (isClimbing) {
            velY = climbSpeed;
        } else {
            velY += gravity * delta;
            if (velY < maxFallSpeed) {
                velY = maxFallSpeed;
            }
        }
        bounds.y += velY * delta;

        for (Rectangle p : platforms) {
            if (bounds.overlaps(p) && velY <= 0f) {
                bounds.y = p.y + p.height;
                velY = 0f;
                break;
            }
        }

        bounds.x += velX * delta;
        velX *= drag;

        for (Rectangle p : platforms) {
            if (bounds.overlaps(p)) {
                if (velX > 0f) {
                    bounds.x = p.x - bounds.width;
                } else {
                    bounds.x = p.x + p.width;
                }
                velX = 0f;
                break;
            }
        }
    }

    /** Спроба піднятися на невеликі пороги */
    public void tryStepUp(List<Rectangle> platforms, boolean facingRight) {
        float probeX = facingRight
            ? bounds.x + bounds.width + 1f
            : bounds.x - 1f;
        Rectangle probe = new Rectangle(probeX, bounds.y, 1f, bounds.height);
        for (Rectangle p : platforms) {
            if (probe.overlaps(p)) {
                float heightDiff = p.y + p.height - bounds.y;
                if (heightDiff > 0f && heightDiff <= maxStepHeight && velY == 0f) {
                    velY = stepUpSpeed;
                }
                break;
            }
        }
    }

    public float getMaxStepHeight() {
        return maxStepHeight;
    }

    public void setVelocityX(float vx) {
        velX = vx;
    }

    public void setVelocityY(float vy) {
        velY = vy;
    }

    public float getVelocityX() {
        return velX;
    }

    public float getVelocityY() {
        return velY;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getStepUpSpeed() {
        return stepUpSpeed;
    }
}
