package org.projectplatformer.lwjgl3.levellogic;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.projectplatformer.lwjgl3.SaveData;
import org.projectplatformer.lwjgl3.SaveManager;
import org.projectplatformer.lwjgl3.StartupHelper;
import org.projectplatformer.lwjgl3.enemy.*;
import org.projectplatformer.lwjgl3.objectslogic.Coin;
import org.projectplatformer.lwjgl3.objectslogic.Platform;
import org.projectplatformer.lwjgl3.objectslogic.World;
import java.util.List;
import java.util.ArrayList;


public class TiledLevel extends Level {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Texture defaultTex, coinTex;
    private List<Rectangle> spikes = new ArrayList<>();


    public List<Rectangle> getSpikes() {
        return spikes;
    }




    /** Примірники ворогів підтягують свої власні текстури в конструкторі */
    // Зона виходу:
    private Rectangle exitZone;

    public TiledLevel(AssetManager am, SpriteBatch batch, String mapPath) {
        this.map = am.get(mapPath, TiledMap.class);
        this.renderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        this.defaultTex = am.get("Levels/Images/default.png", Texture.class);
        this.coinTex    = am.get("Levels/Images/coin.png",    Texture.class);
    }

    @Override
    public void createLevel(World world) {
        // --- базова логіка створення рівня ---

        // 1) Зона спавну гравця
        RectangleMapObject spawnObj = null;
        MapLayer spawnLayer = map.getLayers().get("Spawn");
        if (spawnLayer != null) {
            for (MapObject obj : spawnLayer.getObjects().getByType(RectangleMapObject.class)) {
                if ("PlayerSpawn".equals(obj.getName())) {
                    spawnObj = (RectangleMapObject) obj;
                    break;
                }
            }
        }
        if (spawnObj == null) {
            // пошук у всіх шарах
            for (MapLayer layer : map.getLayers()) {
                for (MapObject obj : layer.getObjects().getByType(RectangleMapObject.class)) {
                    if ("PlayerSpawn".equals(obj.getName())) {
                        spawnObj = (RectangleMapObject) obj;
                        break;
                    }
                }
                if (spawnObj != null) break;
            }
        }
        if (spawnObj != null) {
            Rectangle r = spawnObj.getRectangle();
            startX = r.x;
            startY = r.y;
        } else {
            startX = startY = 0;
            System.err.println("Warning: у карті не знайдено PlayerSpawn → старт (0,0)");
        }

        // 2) Платформи з шару "ground"
        TiledMapTileLayer ground = (TiledMapTileLayer) map.getLayers().get("ground");
        if (ground != null) {
            float tw = ground.getTileWidth();
            float th = ground.getTileHeight();
            for (int x = 0; x < ground.getWidth(); x++) {
                for (int y = 0; y < ground.getHeight(); y++) {
                    if (ground.getCell(x, y) != null) {
                        world.addObject(new Platform(x*tw, y*th, tw, th, defaultTex));
                    }
                }
            }
        }

        // 3) Монети з шару "Coins" із фільтром за збереженням
        int slot = StartupHelper.getSelectedSlot();
        SaveData save = SaveManager.load(slot);
        MapLayer coins = map.getLayers().get("Coins");
        if (coins != null) {
            int idx = 0;
            for (MapObject obj : coins.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = ((RectangleMapObject)obj).getRectangle();
                String coinId = "coin_" + (idx++);
                if (save.isCoinCollected(coinId)) continue;

                // проста анімація монети
                Array<TextureRegion> frames = new Array<>();
                frames.add(new TextureRegion(coinTex));
                Animation<TextureRegion> idle = new Animation<>(0.2f, frames, Animation.PlayMode.LOOP);
                Animation<TextureRegion> collect = new Animation<>(0.1f, frames, Animation.PlayMode.NORMAL);

                Coin c = new Coin(r.x, r.y, idle, collect);
                c.setId(coinId);
                world.addObject(c);
            }
        }

        // 4) Вороги з шару "Enemies" із фільтром за збереженням
        MapLayer enemies = map.getLayers().get("Enemies");
        if (enemies != null) {
            int idx = 0;
            for (MapObject obj : enemies.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = ((RectangleMapObject)obj).getRectangle();
                String enemyId = "enemy_" + (idx++);
                if (save.isEnemyKilled(enemyId)) continue;

                BaseEnemy e;
                String type = obj.getProperties().get("type", String.class);
                switch (type) {
                    case "Goblin":   e = new Goblin(r.x, r.y); break;
                    case "Spider":   e = new Spider(r.x, r.y); break;
                    case "Skeleton": e = new Skeleton(r.x, r.y); break;
                    default: continue;
                }
                e.setId(enemyId);
                world.addEnemy(e);
            }
        }

        // 5) Тривігери — тут обробляємо "exit" і додаємо його до exitZone
        MapLayer triggers = map.getLayers().get("triggers");
        if (triggers != null) {
            for (MapObject obj : triggers.getObjects().getByType(RectangleMapObject.class)) {
                String type = obj.getProperties().get("type", String.class);
                if ("exit".equals(type)) {
                    RectangleMapObject rectObj = (RectangleMapObject) obj;
                    exitZone = rectObj.getRectangle();
                    break; // якщо на шарі зразу один тригер
                }
            }
        }

        // 5) Шипи з шару "Spikes"
        MapLayer spikesLayer = map.getLayers().get("Spikes");
        if (spikesLayer != null) {
            for (MapObject obj : spikesLayer.getObjects().getByType(RectangleMapObject.class)) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                spikes.add(r);
            }
        }

    }

    public Rectangle getExitZone() {
        return exitZone;
    }
    /** Рендеримо тайлову мапу */
    public void renderMap(OrthographicCamera cam) {
        renderer.setView(cam);
        renderer.render();
    }

    public float getMapPixelWidth() {
        MapProperties p = map.getProperties();
        return p.get("width", Integer.class)  * p.get("tilewidth", Integer.class);
    }

    public float getMapPixelHeight() {
        MapProperties p = map.getProperties();
        return p.get("height", Integer.class) * p.get("tileheight", Integer.class);
    }


    @Override
    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
