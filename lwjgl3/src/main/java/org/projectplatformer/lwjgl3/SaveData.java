package org.projectplatformer.lwjgl3;

import java.util.HashSet;
import java.util.Set;


/**
 * Дані збереження гри: стан рівня, позиція гравця, здоров’я, монети,
 * зібрані та придбані предмети, розблокована зброя та обладнана зброя.
 */
public class SaveData {
    // Стан рівня та позиції гравця
    private int levelIndex;
    private float playerX;
    private float playerY;
    private int health;

    // Монети
    private int coins;

    /** Ідентифікатори вбитих ворогів */
    private Set<String> killedEnemies = new HashSet<>();
    /** Ідентифікатори зібраних монет */
    private Set<String> collectedCoins = new HashSet<>();

    // Предмети та зброя
    private Set<String> collectedItems;
    private Set<String> purchasedItems;
    private Set<String> unlockedWeapons;
    private String equippedWeaponId;

    public SaveData() {
        // Початкові значення
        this.levelIndex = 0;
        this.playerX = 0f;
        this.playerY = 0f;
        this.health = 100;
        this.coins = 0;
        this.collectedItems   = new HashSet<>();
        this.purchasedItems   = new HashSet<>();
        this.unlockedWeapons  = new HashSet<>();
        this.equippedWeaponId = null;
    }

    // ===== KILLED ENEMIES =====
    public void markEnemyKilled(String enemyId) {
        killedEnemies.add(enemyId);
    }
    public boolean isEnemyKilled(String enemyId) {
        return killedEnemies.contains(enemyId);
    }
    public Set<String> getKilledEnemies() {
        return killedEnemies;
    }

    // ===== COLLECTED COINS =====
    public void markCoinCollected(String coinId) {
        collectedCoins.add(coinId);
    }
    public boolean isCoinCollected(String coinId) {
        return collectedCoins.contains(coinId);
    }
    public Set<String> getCollectedCoins() {
        return collectedCoins;
    }

    // ===== Level Index =====
    public int getLevelIndex() {
        return levelIndex;
    }
    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    // ===== Position =====
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

    // ===== Health =====
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }

    // ===== Coins =====
    public int getCoins() {
        return coins;
    }
    public void setCoins(int coins) {
        this.coins = coins;
    }
    public void addCoins(int amount) {
        this.coins += amount;
    }
    public void spendCoins(int amount) {
        this.coins = Math.max(0, this.coins - amount);
    }

    // ===== Collected Items =====
    public void collectItem(String itemId) {
        collectedItems.add(itemId);
    }
    public boolean isItemCollected(String itemId) {
        return collectedItems.contains(itemId);
    }
    public Set<String> getCollectedItems() {
        return collectedItems;
    }

    // ===== Purchased Items =====
    public void purchaseItem(String itemId) {
        purchasedItems.add(itemId);
    }
    public boolean isItemPurchased(String itemId) {
        return purchasedItems.contains(itemId);
    }
    public Set<String> getPurchasedItems() {
        return purchasedItems;
    }

    // ===== Weapons =====
    /** Розблокувати (купити) зброю */
    public void unlockWeapon(String weaponId) {
        unlockedWeapons.add(weaponId);
    }
    /** Чи розблокована зброя */
    public boolean isWeaponUnlocked(String weaponId) {
        return unlockedWeapons.contains(weaponId);
    }
    /** Повертає набір усіх розблокованих зброї */
    public Set<String> getUnlockedWeapons() {
        return unlockedWeapons;
    }
    /** Обрати поточну зброю (лише з уже розблокованих) */
    public void equipWeapon(String weaponId) {
        if (isWeaponUnlocked(weaponId)) {
            this.equippedWeaponId = weaponId;
        }
    }
    /** Яка зброя зараз обрана? */
    public String getEquippedWeapon() {
        return equippedWeaponId;
    }

    // ===== Reset Helpers =====
    /** Повністю очистити всі дані збереження */
    public void reset() {
        this.levelIndex = 0;
        this.playerX = 0f;
        this.playerY = 0f;
        this.health = 100;
        this.coins = 0;
        this.collectedItems.clear();
        this.purchasedItems.clear();
        this.unlockedWeapons.clear();
        this.equippedWeaponId = null;
    }
}
