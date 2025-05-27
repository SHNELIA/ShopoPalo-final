package org.projectplatformer.lwjgl3.levellogic;

import org.projectplatformer.lwjgl3.objectslogic.GameObject;
import org.projectplatformer.lwjgl3.objectslogic.World;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовий клас для рівнів (створення та очищення).
 */
public abstract class Level {
    protected List<GameObject> objects = new ArrayList<>();
    protected float startX;
    protected float startY;

    /**
     * Створення рівня (додавання об'єктів до світу).
     */
    public abstract void createLevel(World world);

    public List<GameObject> getObjects() {
        return objects;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    /**
     * Очищення ресурсів рівня.
     */
    public abstract void dispose();
}

