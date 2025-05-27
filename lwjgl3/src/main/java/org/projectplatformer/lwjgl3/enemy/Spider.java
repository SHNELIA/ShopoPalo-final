package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.projectplatformer.lwjgl3.EnemiesAnimation.SpiderAnimationManager;
import org.projectplatformer.lwjgl3.player.Player;
import org.projectplatformer.lwjgl3.weapon.SpearWeapon;

import java.util.List;

public class Spider extends BaseEnemy {
    // Налаштування руху та патрулювання
    private static final float PATROL_RADIUS = 80f;
    private static final float PATROL_SPEED = 60f;
    private static final float CHASE_RANGE = 100f;
    private static final float MOVE_SPEED = 80f;
    private static final float STEP_UP_SPEED = 400f;
    private static final float JUMP_SPEED = 400f;

    // Параметри атаки списом
    private static final float SPEAR_MAX_LENGTH = 40f;
    private static final float SPEAR_WIDTH = 8f;
    private static final float ATTACK_RANGE = 60f;
    private static final float ATTACK_DURATION = 0.3f;
    private static final float ATTACK_COOLDOWN = 0.8f;
    private static final int ATTACK_DAMAGE = 12;

    private final SpearWeapon spearWeapon;
    private final float patrolStartX;
    private float patrolDir = 1f;
    private boolean facingRight = true;
    private final SpiderAnimationManager animationManager;
    private SpiderAnimationManager.State currentState;
    private boolean isAttackAnimationPlaying = false;
    private static final boolean SPRITE_LOOKS_RIGHT = false;

    // Ініціалізація
    public Spider(float x, float y) {
        super(x, y, 32f, 32f, null, 45, -2000f, -1000f, 0.9f, 16f, STEP_UP_SPEED);
        spearWeapon = new SpearWeapon(
            SPEAR_MAX_LENGTH,
            SPEAR_WIDTH,
            ATTACK_DURATION,
            ATTACK_COOLDOWN,
            ATTACK_DAMAGE
        );
        patrolStartX = x;
        animationManager = new SpiderAnimationManager();
        currentState = SpiderAnimationManager.State.WALK;
    }

    // Оновлення станів та анімацій
    @Override
    public void update(float delta, Player player, List<Rectangle> platforms) {
        if (isDeadAndGone()) return;
        if (!isAlive()) {
            setDeadAndGone();
            return;
        }

        Rectangle b = getBounds();
        float pivotX = b.x + b.width / 2f;
        float pivotY = b.y + b.height / 2f;

        spearWeapon.update(delta, pivotX, pivotY, facingRight);
        float playerCX = player.getBounds().x + player.getBounds().width / 2f;
        float playerCY = player.getBounds().y + player.getBounds().height / 2f;
        float dx = playerCX - pivotX;
        float dy = playerCY - pivotY;
        float dist2 = dx * dx + dy * dy;
        boolean sameLevel = Math.abs(dy) < b.height * 1.2f;

        if (isAttackAnimationPlaying) {
            if (animationManager.isAttackAnimationFinished()) {
                isAttackAnimationPlaying = false;
                currentState = SpiderAnimationManager.State.WALK;
            } else {
                currentState = SpiderAnimationManager.State.ATTACK;
            }
            animationManager.update(delta, currentState, facingRight);
            spearWeapon.applyDamage(player);
            physics.update(delta, platforms);
            return;
        }

        // Атака
        if (dist2 <= ATTACK_RANGE * ATTACK_RANGE && sameLevel && spearWeapon.getCooldownRemaining() <= 0f) {
            isAttackAnimationPlaying = true;
            animationManager.resetAttackAnim();
            currentState = SpiderAnimationManager.State.ATTACK;
            spearWeapon.startAttack(pivotX, pivotY, dx > 0);
            facingRight = dx > 0;
            animationManager.update(delta, currentState, facingRight);
            spearWeapon.applyDamage(player);
            physics.update(delta, platforms);
            return;
        }

        // Рух (AI)
        if (dist2 <= CHASE_RANGE * CHASE_RANGE && sameLevel) {
            float dir = Math.signum(dx);
            physics.setVelocityX(dir * MOVE_SPEED);
            facingRight = dir > 0;
        } else {
            if (b.x > patrolStartX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolStartX - PATROL_RADIUS) patrolDir = 1f;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            facingRight = patrolDir > 0;
        }

        currentState = SpiderAnimationManager.State.WALK;
        animationManager.update(delta, currentState, facingRight);
        spearWeapon.applyDamage(player);

        if (physics.getVelocityX() != 0f) {
            physics.tryStepUp(platforms, physics.getVelocityX() > 0f);
        }

        super.update(delta, player, platforms);
    }

    // Логіка руху та обходу перешкод
    @Override
    protected void aiMove(float delta, Player player, List<Rectangle> platforms) {
        Rectangle b = getBounds();
        float cx = b.x + b.width / 2f;
        float dx = player.getBounds().x + player.getBounds().width / 2f - cx;
        float dist2 = dx * dx;

        if (dist2 <= ATTACK_RANGE * ATTACK_RANGE) {
            physics.setVelocityX(0f);
            return;
        }

        if (dist2 <= CHASE_RANGE * CHASE_RANGE) {
            float dir = Math.signum(dx);
            physics.setVelocityX(dir * MOVE_SPEED);
            facingRight = dir > 0;
        } else {
            if (b.x > patrolStartX + PATROL_RADIUS) patrolDir = -1f;
            if (b.x < patrolStartX - PATROL_RADIUS) patrolDir = 1f;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
            facingRight = patrolDir > 0;
        }

        boolean onGround = physics.getVelocityY() == 0f;
        float probeX = facingRight ? b.x + b.width + 2f : b.x - 2f;
        Rectangle probe = new Rectangle(probeX, b.y, 2f, b.height);
        boolean wallAhead = false;
        Rectangle hitP = null;
        for (Rectangle p : platforms) {
            if (probe.overlaps(p)) {
                wallAhead = true;
                hitP = p;
                break;
            }
        }

        float footX = b.x + b.width / 2f + (facingRight ? 6f : -6f);
        Rectangle footProbe = new Rectangle(footX, b.y - 4f, 4f, 4f);
        boolean groundAhead = false;
        for (Rectangle p : platforms) {
            if (footProbe.overlaps(p)) {
                groundAhead = true;
                break;
            }
        }

        if (wallAhead || !groundAhead) {
            patrolDir = -patrolDir;
            facingRight = patrolDir > 0;
            physics.setVelocityX(patrolDir * PATROL_SPEED);
        }
    }

    // Відмальовка спрайта
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
        Rectangle hb = spearWeapon.getHitbox();
        if (hb != null) r.rect(hb.x, hb.y, hb.width, hb.height);
    }

    // Звільнення ресурсів
    @Override
    public void dispose() {
        animationManager.dispose();
    }
}
