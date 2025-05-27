package org.projectplatformer.lwjgl3.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public abstract class Enemy {
    protected Rectangle bounds;
    protected int health;
    protected int maxHealth;
    protected boolean isAlive = true;

    public Enemy(float x, float y, float width, float height, int maxHealth) {
        bounds = new Rectangle(x, y, width, height);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    // Оновлення логіки ворога
    public abstract void update(float delta);

    // Відмалювання ворога
    public abstract void render(SpriteBatch batch);

    // Відмалювання хитбоксу ворога
    public abstract void renderHitbox(ShapeRenderer renderer);

    public boolean isAlive() {
        return isAlive;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    // Обробка отримання ушкоджень
    public void takeDamage(int amount) {
        if (!isAlive) return;
        health -= amount;
        if (health <= 0) {
            health = 0;
            isAlive = false;
            onDeath();
        }
    }

    // Викликається при смерті ворога
    protected void onDeath() {
    }

    // Очищення ресурсів ворога
    public void dispose() {
    }
}
