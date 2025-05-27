package org.projectplatformer.lwjgl3.EnemiesAnimation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class SkeletonAnimationManager implements Disposable {
    public enum State {
        WALK,
        ATTACK,
        DEATH
    }

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> attackAnim;
    private final Animation<TextureRegion> deathAnim;

    private float stateTime;
    private State currentState;
    private TextureRegion currentFrame;

    private final Texture[] frames;

    public SkeletonAnimationManager() {
        stateTime = 0f;
        currentState = State.WALK;

        // Завантажуємо 16 кадрів з папки Enemies/Skeleton/
        frames = new Texture[14];
        for (int i = 0; i < 14; i++) {
            String path = "Enemies/Skeleton/Skeleton" + (i + 1) + ".png";
            if (!Gdx.files.internal(path).exists()) {
                throw new RuntimeException("❌ Missing file: " + path);
            }
            frames[i] = new Texture(path);
        }

        walkAnim   = createAnimation(0, 5, 0.15f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(6, 8, 0.12f, Animation.PlayMode.NORMAL);
        deathAnim  = createAnimation(9, 13, 0.25f, Animation.PlayMode.NORMAL);
    }

    private Animation<TextureRegion> createAnimation(int from, int to, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = from; i < to; i++) {
            regions.add(new TextureRegion(frames[i]));
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, regions);
        anim.setPlayMode(playMode);
        return anim;
    }

    public void update(float delta, State newState, boolean facingRight) {
        if (currentState != newState) {
            currentState = newState;
            stateTime = 0f;
        } else {
            stateTime += delta;
        }

        switch (currentState) {
            case WALK:
                currentFrame = walkAnim.getKeyFrame(stateTime, true);
                break;
            case ATTACK:
                currentFrame = attackAnim.getKeyFrame(stateTime, false);
                break;
            case DEATH:
                currentFrame = deathAnim.getKeyFrame(stateTime, false);
                break;
        }
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    // --- Методи для атаки та смерті (як у гобліна) ---

    public void resetAttackAnim() {
        stateTime = 0f;
    }
    public void resetDeathAnim() {
        stateTime = 0f;
    }
    public boolean isAttackAnimationFinished() {
        return attackAnim.isAnimationFinished(stateTime);
    }
    public boolean isDeathAnimationFinished() {
        return deathAnim.isAnimationFinished(stateTime);
    }

    @Override
    public void dispose() {
        for (Texture tex : frames) {
            tex.dispose();
        }
    }
}
