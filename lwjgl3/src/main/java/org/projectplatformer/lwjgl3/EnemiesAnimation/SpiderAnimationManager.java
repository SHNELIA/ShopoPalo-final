package org.projectplatformer.lwjgl3.EnemiesAnimation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class SpiderAnimationManager implements Disposable {
    public enum State {
        WALK,
        ATTACK
    }

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> attackAnim;
    private final Animation<TextureRegion> deathAnim;

    private float stateTime;
    private State currentState;
    private TextureRegion currentFrame;

    private final Texture[] frames;

    public SpiderAnimationManager() {
        stateTime = 0f;
        currentState = State.WALK;

        frames = new Texture[12]; // 3 walk + 3 attack

        for (int i = 0; i < 12; i++) {
            frames[i] = new Texture("Enemies/Spider/Spider" + (i + 1) + ".png");
        }

        walkAnim   = createAnimation(0, 5, 0.15f);
        attackAnim = createAnimation(6, 9, 0.12f);
        deathAnim  = createAnimation(9, 11, 0.25f);
    }

    private Animation<TextureRegion> createAnimation(int from, int to, float duration) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = from; i < to; i++) {
            regions.add(new TextureRegion(frames[i]));
        }
        return new Animation<>(duration, regions, Animation.PlayMode.LOOP);
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
                currentFrame = walkAnim.getKeyFrame(stateTime);
                break;
            case ATTACK:
                currentFrame = attackAnim.getKeyFrame(stateTime);
                break;
        }
    }

    public void resetAttackAnim() {
        stateTime = 0f;
        currentState = State.ATTACK;
    }

    public boolean isAttackAnimationFinished() {
        return attackAnim.isAnimationFinished(stateTime);
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void dispose() {
        for (Texture t : frames) {
            t.dispose();
        }
    }
}
