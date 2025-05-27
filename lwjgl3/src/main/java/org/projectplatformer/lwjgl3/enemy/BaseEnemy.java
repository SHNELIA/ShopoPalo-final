package org.projectplatformer.lwjgl3.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import org.projectplatformer.lwjgl3.SaveData;
import org.projectplatformer.lwjgl3.SaveManager;
import org.projectplatformer.lwjgl3.StartupHelper;
import org.projectplatformer.lwjgl3.physics.PhysicsComponent;
import org.projectplatformer.lwjgl3.player.Player;
import org.projectplatformer.lwjgl3.physics.PhysicsComponent;

import java.util.List;

public abstract class BaseEnemy {
    protected final PhysicsComponent physics;
    protected final Texture          texture;
    protected int                    health;
    protected boolean                alive = true;
    protected boolean                deadAndGone = false; // новий флаг для видалення ворога

    // --- Атака ворога ---
    protected float   attackRange;
    protected int     attackDamage;
    protected float   attackCooldown;
    private   float   attackTimer = 0f;
    protected Rectangle attackHitbox = new Rectangle();
    private String id;
    public void setId(String id) { this.id = id; }

    public String getId() { return id; }

    public BaseEnemy(float x, float y, float width, float height,
                     Texture tex, int initialHp,
                     float gravity, float maxFallSpeed,
                     float drag, float maxStepHeight,
                     float stepUpSpeed) {
        Rectangle bounds = new Rectangle(x, y, width, height);
        this.physics = new PhysicsComponent(
            bounds, gravity, maxFallSpeed, drag,
            maxStepHeight, stepUpSpeed);
        this.texture = tex;
        this.health  = initialHp;
    }

    public void update(float delta, Player player, List<Rectangle> platforms) {

        if (!alive || deadAndGone) return;


        // Запам'ятаємо, чи був живий на початку кадру
        boolean wasAlive = alive;

        // 1) AI-логіка руху
        aiMove(delta, player, platforms);

        // 2) Спроба "step-up" якщо потрібно
        physics.tryStepUp(platforms, physics.getVelocityX() >= 0);

        // 3) Оновлення фізики (гравітація + колізії)
        physics.update(delta, platforms);

        // 4) Оновлення хитбоксу
        Rectangle bounds = physics.getBounds();

        // 5) Обробка атаки гравця (як у вас було)
        attackTimer = Math.max(0f, attackTimer - delta);
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;
        Rectangle pb = player.getBounds();
        float px = pb.x + pb.width / 2f;
        float py = pb.y + pb.height / 2f;
        float dx2 = (px - cx) * (px - cx), dy2 = (py - cy) * (py - cy);
        if (dx2 + dy2 <= attackRange * attackRange && attackTimer == 0f) {
            computeAttackHitbox(player);
            if (attackHitbox.overlaps(pb)) {
                // Якщо takeDamage викликає alive=false, ми зловимо це нижче
                takeDamage(attackDamage);
            }
            attackTimer = attackCooldown;
        }

    }


    protected abstract void aiMove(float delta, Player player, List<Rectangle> platforms);

    protected void computeAttackHitbox(Player player) {
        float w = 20f, h = 10f;
        boolean faceRight = player.getBounds().x + player.getBounds().width/2f
            > physics.getBounds().x + physics.getBounds().width/2f;
        float x = faceRight ? physics.getBounds().x + physics.getBounds().width : physics.getBounds().x - w;
        float y = physics.getBounds().y + physics.getBounds().height/2f - h/2f;
        attackHitbox.set(x, y, w, h);
    }

    public void render(SpriteBatch batch) {
        if (!alive || deadAndGone) return;
        Rectangle b = physics.getBounds();
        batch.draw(texture, b.x, b.y, b.width, b.height);
    }

    public void renderHitbox(ShapeRenderer r) {
        if (!alive || deadAndGone) return;
        Rectangle b = physics.getBounds();
        r.setColor(1f, 0f, 1f, 1f);
        r.rect(b.x, b.y, b.width, b.height);
        if (attackTimer > 0f) {
            r.setColor(1f, 1f, 0f, 1f);
            r.rect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        }
    }

    public Rectangle getBounds() { return physics.getBounds(); }
    public boolean   isAlive()   { return alive; }

    public void takeDamage(int dmg) {
        if (!alive) return;

        health -= dmg;
        if (health <= 0) {
            alive = false;

            int slot = StartupHelper.getSelectedSlot();
            SaveData data = SaveManager.load(slot);

            data.markEnemyKilled(id);

            SaveManager.save(slot, data);

            SaveData reloaded = SaveManager.load(slot);

        }
    }


    /** Позначає ворога як "зниклого" для прибирання з гри */
    public void setDeadAndGone() { deadAndGone = true; }
    public boolean isDeadAndGone() { return deadAndGone; }


    public void dispose() {}
}
