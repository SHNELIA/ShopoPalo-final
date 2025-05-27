package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.EnemiesAnimation.SkeletonAnimationManager;
import org.projectplatformer.lwjgl3.player.Player;
import org.projectplatformer.lwjgl3.weapon.SwordWeapon;

import java.util.List;

public class Skeleton extends BaseEnemy {
    // Параметри патрулювання та руху
    private static final float PATROL_RADIUS = 80f;
    private static final float PATROL_SPEED = 50f;
    private static final float DETECTION_RANGE = 150f;
    private static final float CHASE_SPEED = 120f;
    private static final float JUMP_SPEED = 400f;
    private static final float STEP_UP_SPEED = 400f;

    // Параметри атаки
    private static final float ATTACK_DURATION = 0.30f;
    private static final float ATTACK_COOLDOWN = 1.5f;
    private static final int ATTACK_DAMAGE = 30;
    private static final float MELEE_RANGE = 70f;

    // Поля керування станом та анімаціями
    private final SwordWeapon slashWeapon;
    private final float patrolCenterX;
    private float patrolDir = 1f;
    private boolean facingRight = true;
    private final SkeletonAnimationManager animationManager;
    private SkeletonAnimationManager.State currentState;
    private boolean isAttackAnimationPlaying = false;
    private boolean deathAnimationStarted = false;
    private static final boolean SPRITE_LOOKS_RIGHT = false;

    // Ініціалізація
    public Skeleton(float x, float y) {
        super(x, y, 32f, 48f, null, 50, -2000f, -1000f, 0.9f, 16f, 400f);
        slashWeapon = new SwordWeapon(ATTACK_DURATION, ATTACK_COOLDOWN, ATTACK_DAMAGE);
        patrolCenterX = x;
        animationManager = new SkeletonAnimationManager();
        currentState = SkeletonAnimationManager.State.WALK;
    }

    // Оновлення логіки станів та анімацій
    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        if (isDeadAndGone()) return;

        Rectangle b = getBounds();
        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + b.height * 0.7f;

        // Анімація смерті
        if (!isAlive()) {
            if (!deathAnimationStarted) {
                animationManager.resetDeathAnim();
                deathAnimationStarted = true;
            }
            animationManager.update(delta, SkeletonAnimationManager.State.DEATH, facingRight);
            if (animationManager.isDeathAnimationFinished()) setDeadAndGone();
            return;
        }
        deathAnimationStarted = false;

        slashWeapon.update(delta, pivotX, pivotY, facingRight);
        float dx = (player.getBounds().x + player.getBounds().width / 2f) - pivotX;
        boolean inMelee = dx * dx <= MELEE_RANGE * MELEE_RANGE;
        boolean canAttack = slashWeapon.getCooldownRemaining() <= 0f;

        // Обробка атаки
        if (isAttackAnimationPlaying) {
            if (animationManager.isAttackAnimationFinished()) {
                isAttackAnimationPlaying = false;
                currentState = SkeletonAnimationManager.State.WALK;
            } else {
                currentState = SkeletonAnimationManager.State.ATTACK;
            }
            animationManager.update(delta, currentState, facingRight);
            slashWeapon.applyDamage(player);
            physics.update(delta, platforms);
            return;
        }

        if (inMelee && canAttack) {
            isAttackAnimationPlaying = true;
            animationManager.resetAttackAnim();
            currentState = SkeletonAnimationManager.State.ATTACK;
            slashWeapon.startAttack(pivotX, pivotY, facingRight);
            animationManager.update(delta, currentState, facingRight);
            slashWeapon.applyDamage(player);
            physics.update(delta, platforms);
            return;
        }

        // Рух у стані WALK
        currentState = SkeletonAnimationManager.State.WALK;
        animationManager.update(delta, currentState, facingRight);
        slashWeapon.applyDamage(player);
        super.update(delta, player, platforms);
    }

    // Логіка штучного інтелекту руху
    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();
        float belowX = b.x + b.width / 2f;
        boolean hasGround = platforms.stream().anyMatch(p ->
            belowX >= p.x && belowX <= p.x + p.width && p.y + p.height <= b.y);
        if (!hasGround) {
            patrolDir = -patrolDir;
            facingRight = patrolDir > 0;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            return;
        }

        float dx = (player.getBounds().x + player.getBounds().width / 2f) - (b.x + b.width / 2f);
        float dist2 = dx * dx;
        if (dist2 <= MELEE_RANGE * MELEE_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        float moveDir;
        float speed;
        if (dist2 <= DETECTION_RANGE * DETECTION_RANGE) {
            moveDir = Math.signum(dx);
            speed = CHASE_SPEED;
            facingRight = moveDir > 0;
        } else {
            if (b.x > patrolCenterX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolCenterX - PATROL_RADIUS) patrolDir = 1f;
            moveDir = patrolDir;
            speed = PATROL_SPEED;
            facingRight = patrolDir > 0;
        }

        // Обхід перешкод та підйом
        float aheadX = facingRight ? b.x + b.width + 2f : b.x - 2f;
        Rectangle probe = new Rectangle(aheadX, b.y, 2f, b.height);
        boolean wallAhead = platforms.stream().anyMatch(p -> probe.overlaps(p));
        Rectangle footProbe = new Rectangle(b.x + b.width / 2f + (facingRight ? 6f : -6f), b.y - 4f, 4f, 4f);
        boolean groundAhead = platforms.stream().anyMatch(p -> footProbe.overlaps(p));
        if (wallAhead && physics.getVelocityY() == 0f && groundAhead) {
            Rectangle hitP = platforms.stream().filter(p -> probe.overlaps(p)).findFirst().orElse(null);
            if (hitP != null) {
                float stepH = hitP.y + hitP.height - b.y;
                physics.setVelocityY(stepH <= physics.getMaxStepHeight() ? STEP_UP_SPEED : JUMP_SPEED);
            }
        }
        physics.setVelocityX(moveDir * speed);
    }

    // Відмальовка спрайта та хитбоксів
    @Override
    public void render(SpriteBatch batch) {
        if (isDeadAndGone()) return;
        Rectangle b = getBounds();
        TextureRegion frame = animationManager.getCurrentFrame();
        boolean flip = facingRight != SPRITE_LOOKS_RIGHT;
        batch.draw(frame, flip ? b.x + b.width : b.x, b.y, flip ? -b.width : b.width, b.height);
    }

    @Override
    public void renderHitbox(ShapeRenderer r) {
        if (isDeadAndGone()) return;
        super.renderHitbox(r);
        Rectangle hb = slashWeapon.getHitbox();
        if (hb != null) r.rect(hb.x, hb.y, hb.width, hb.height);
        slashWeapon.renderTrajectory(r);
    }

    // Звільнення ресурсів
    @Override
    public void dispose() {
        animationManager.dispose();
    }
}
