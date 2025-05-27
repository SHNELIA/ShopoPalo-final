package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.EnemiesAnimation.GoblinAnimationManager;
import org.projectplatformer.lwjgl3.player.Player;
import org.projectplatformer.lwjgl3.weapon.SwordWeapon;

import java.util.List;

public class Goblin extends BaseEnemy {
    private static final float PATROL_RADIUS = 80f;
    private static final float PATROL_SPEED = 50f;
    private static final float DETECTION_RANGE = 150f;
    private static final float CHASE_SPEED = 120f;
    private static final float JUMP_SPEED  = 400f;
    private static final float STEP_UP_SPEED = 400f;

    private static final float ATTACK_DURATION = 0.45f;
    private static final float ATTACK_COOLDOWN = 1.20f;
    private static final int   ATTACK_DAMAGE   = 16;
    private static final float MELEE_RANGE     = 70f;

    private final SwordWeapon slashWeapon;
    private final float patrolCenterX;
    private float patrolDir = 1f;         // Напрямок патрулювання
    private boolean facingRight = true;   // Орієнтація спрайта

    // Менеджер анімацій гобліна
    private final GoblinAnimationManager animationManager;
    private GoblinAnimationManager.State currentState;
    private boolean isAttackAnimationPlaying = false; // Атака зараз виконується?
    private boolean deathAnimationStarted = false;    // Чи почалась анімація смерті?

    // Початкова орієнтація спрайта
    private static final boolean SPRITE_LOOKS_RIGHT = false;

    public Goblin(float x, float y) {
        super(x, y, 32f, 48f,
            null,  // текстуру задаємо пізніше
            50, -2000f, -1000f, 0.9f,
            16f, 400f);
        slashWeapon = new SwordWeapon(ATTACK_DURATION, ATTACK_COOLDOWN, ATTACK_DAMAGE);
        patrolCenterX = x;
        animationManager = new GoblinAnimationManager();
        currentState = GoblinAnimationManager.State.WALK;
    }

    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        // Якщо вже зник — нічого не робимо
        if (isDeadAndGone()) return;

        Rectangle b = getBounds();
        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + b.height * 0.7f;

        // Якщо гоблін мертвий — відтворюємо анімацію смерті
        if (!isAlive()) {
            currentState = GoblinAnimationManager.State.DEATH;
            if (!deathAnimationStarted) {
                animationManager.resetDeathAnim();
                deathAnimationStarted = true;
            }
            animationManager.update(delta, currentState, facingRight);
            if (animationManager.isDeathAnimationFinished()) {
                setDeadAndGone();
            }
            return;
        }

        // Оновлюємо зброю
        slashWeapon.update(delta, pivotX, pivotY, facingRight);

        // Перевірка на дистанцію для атаки
        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        float dx = playerCX - pivotX;
        boolean inAttackRange = dx * dx <= MELEE_RANGE * MELEE_RANGE;
        boolean canAttack = slashWeapon.getCooldownRemaining() <= 0f;

        // Логіка переходу між станами атаки і ходу
        if (isAttackAnimationPlaying) {
            if (animationManager.isAttackAnimationFinished()) {
                isAttackAnimationPlaying = false;
                currentState = GoblinAnimationManager.State.WALK;
            } else {
                currentState = GoblinAnimationManager.State.ATTACK;
            }
        } else if (inAttackRange && canAttack) {
            isAttackAnimationPlaying = true;
            animationManager.resetAttackAnim();
            currentState = GoblinAnimationManager.State.ATTACK;
            slashWeapon.startAttack(pivotX, pivotY, facingRight);
        } else {
            currentState = GoblinAnimationManager.State.WALK;
        }

        animationManager.update(delta, currentState, facingRight);
        slashWeapon.applyDamage(player);

        // Виклик фізики або рух під час атаки
        if (!isAttackAnimationPlaying) {
            super.update(delta, player, platforms);
        } else {
            physics.update(delta, platforms);
        }
    }

    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();
        float belowX = b.x + b.width / 2f;
        // Перевіряємо, чи є земля під гобліном
        boolean hasGround = platforms.stream().anyMatch(p ->
            belowX >= p.x && belowX <= p.x + p.width && p.y + p.height <= b.y
        );
        if (!hasGround) {
            patrolDir = -patrolDir;
            facingRight = patrolDir > 0;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            return;
        }

        // Обчислюємо відстань до гравця
        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        float cx = b.x + b.width / 2f;
        float dx = playerCX - cx;
        float dist2 = dx * dx;

        // Якщо гравець занадто близько — зупиняємося
        if (dist2 <= MELEE_RANGE * MELEE_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        float moveDir;
        float speed;
        boolean onGround = physics.getVelocityY() == 0f;

        // Якщо гравець виявлений — переслідуємо
        if (dist2 <= DETECTION_RANGE * DETECTION_RANGE) {
            moveDir = Math.signum(dx);
            speed = CHASE_SPEED;
            facingRight = moveDir > 0;
        } else {
            // Інакше — патрулювання навколо центру
            if (b.x > patrolCenterX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolCenterX - PATROL_RADIUS) patrolDir = 1f;
            moveDir = patrolDir;
            speed = PATROL_SPEED;
            facingRight = patrolDir > 0;
        }

        // Перевірка стіни перед собою та можливість підйому
        float aheadX = facingRight ? b.x + b.width + 2f : b.x - 2f;
        Rectangle probe = new Rectangle(aheadX, b.y, 2f, b.height);
        boolean wallAhead = platforms.stream().anyMatch(p -> probe.overlaps(p));

        float footX = b.x + b.width / 2f + (facingRight ? 6f : -6f);
        Rectangle footProbe = new Rectangle(footX, b.y - 4f, 4f, 4f);
        boolean groundAhead = platforms.stream().anyMatch(p -> footProbe.overlaps(p));

        if (wallAhead && onGround && groundAhead) {
            Rectangle hitP = platforms.stream()
                .filter(p -> probe.overlaps(p))
                .findFirst().orElse(null);
            if (hitP != null) {
                float stepH = hitP.y + hitP.height - b.y;
                if (stepH <= physics.getMaxStepHeight()) {
                    physics.setVelocityY(STEP_UP_SPEED);
                } else {
                    physics.setVelocityY(JUMP_SPEED);
                }
            }
        }

        physics.setVelocityX(moveDir * speed);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (isDeadAndGone()) return;
        Rectangle b = getBounds();
        TextureRegion frame = animationManager.getCurrentFrame();
        boolean flip = facingRight != SPRITE_LOOKS_RIGHT;
        if (flip) {
            // Малюємо перевернутий спрайт
            batch.draw(frame, b.x + b.width, b.y, -b.width, b.height);
        } else {
            batch.draw(frame, b.x, b.y, b.width, b.height);
        }
    }

    @Override
    public void renderHitbox(ShapeRenderer r) {
        if (isDeadAndGone()) return;
        super.renderHitbox(r);
        Rectangle hb = slashWeapon.getHitbox();
        if (hb != null) {
            r.setColor(1f, 0.5f, 0f, 1f);
            r.rect(hb.x, hb.y, hb.width, hb.height);
        }
        slashWeapon.renderTrajectory(r);  // малюємо траєкторію атаки
    }

    @Override
    public void dispose() {
        animationManager.dispose();  // звільняємо ресурси анімації
    }
}
