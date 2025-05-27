package org.projectplatformer.lwjgl3;

import java.util.HashSet;
import java.util.Set;

/**
 * Дані збереження гри: стан рівня, позиція гравця, здоров’я, монети,
 * зібрані вороги і монети, предмети та зброя.
 */
public class SaveData {
    private int levelIndex;
    private float playerX;
    private float playerY;
    private int health;
    private int coins;

    private Set<String> killedEnemies;
    private Set<String> collectedCoins;
    private Set<String> collectedItems;
    private Set<String> purchasedItems;
    private Set<String> unlockedWeapons;
    private String equippedWeaponId;

    /** Початкові значення збереження */
    public SaveData() {
        levelIndex = 0;
        playerX = 0f;
        playerY = 0f;
        health = 100;
        coins = 0;
        killedEnemies = new HashSet<>();
        collectedCoins = new HashSet<>();
        collectedItems = new HashSet<>();
        purchasedItems = new HashSet<>();
        unlockedWeapons = new HashSet<>();
        equippedWeaponId = null;
    }

    // ===== Вбиті вороги =====
    public void markEnemyKilled(String enemyId) {
        killedEnemies.add(enemyId);
    }
    public boolean isEnemyKilled(String enemyId) {
        return killedEnemies.contains(enemyId);
    }
    public Set<String> getKilledEnemies() {
        return killedEnemies;
    }

    // ===== Зібрані монети =====
    public void markCoinCollected(String coinId) {
        collectedCoins.add(coinId);
    }
    public boolean isCoinCollected(String coinId) {
        return collectedCoins.contains(coinId);
    }
    public Set<String> getCollectedCoins() {
        return collectedCoins;
    }

    // ===== Рівень =====
    public int getLevelIndex() {
        return levelIndex;
    }
    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    // ===== Позиція =====
    public float getPlayerX() {
        return playerX;
    }
    public void setPlayerX(float playerX) {
        this.playerX = playerX;
    }
    public float getPlayerY() {
        return playerY;
    }
    public void setPlayerY(float playerY) {
        this.playerY = playerY;
    }

    // ===== Здоров’я =====
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }

    // ===== Монети =====
    public int getCoins() {
        return coins;
    }
    public void setCoins(int coins) {
        this.coins = coins;
    }
    public void addCoins(int amount) {
        coins += amount;
    }
    public void spendCoins(int amount) {
        coins = Math.max(0, coins - amount);
    }

    // ===== Зібрані предмети =====
    public void collectItem(String itemId) {
        collectedItems.add(itemId);
    }
    public boolean isItemCollected(String itemId) {
        return collectedItems.contains(itemId);
    }
    public Set<String> getCollectedItems() {
        return collectedItems;
    }

    // ===== Придбані предмети =====
    public void purchaseItem(String itemId) {
        purchasedItems.add(itemId);
    }
    public boolean isItemPurchased(String itemId) {
        return purchasedItems.contains(itemId);
    }
    public Set<String> getPurchasedItems() {
        return purchasedItems;
    }

    // ===== Зброя =====
    public void unlockWeapon(String weaponId) {
        unlockedWeapons.add(weaponId);
    }
    public boolean isWeaponUnlocked(String weaponId) {
        return unlockedWeapons.contains(weaponId);
    }
    public Set<String> getUnlockedWeapons() {
        return unlockedWeapons;
    }
    public void equipWeapon(String weaponId) {
        if (unlockedWeapons.contains(weaponId)) {
            equippedWeaponId = weaponId;
        }
    }
    public String getEquippedWeapon() {
        return equippedWeaponId;
    }

    // ===== Скидання збереження =====
    public void reset() {
        levelIndex = 0;
        playerX = 0f;
        playerY = 0f;
        health = 100;
        coins = 0;
        killedEnemies.clear();
        collectedCoins.clear();
        collectedItems.clear();
        purchasedItems.clear();
        unlockedWeapons.clear();
        equippedWeaponId = null;
    }

    public int getCurrentLevel() {
        return levelIndex;
    }
}
