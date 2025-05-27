package org.projectplatformer.lwjgl3.EnemiesAnimation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Array;

public class GoblinAnimationManager implements Disposable {
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

    public GoblinAnimationManager() {
        stateTime = 0f;
        currentState = State.WALK;
        frames = new Texture[10];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new Texture("Enemies/Goblin/Goblin" + (i + 1) + ".png");
        }
        walkAnim = createAnimation(0, 3, 0.15f, Animation.PlayMode.LOOP);
        attackAnim = createAnimation(4, 7, 0.12f, Animation.PlayMode.NORMAL);
        deathAnim = createAnimation(8, 9, 0.25f, Animation.PlayMode.NORMAL);
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

    /**
     * Оновлює анімаційний стан гобліна.
     * @param delta       час з минулого кадру
     * @param newState    новий стан
     * @param facingRight напрямок (flip не використовується)
     */
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

    public boolean isAttackAnimationFinished() {
        return attackAnim.isAnimationFinished(stateTime);
    }

    public void resetAttackAnim() {
        stateTime = 0f;
    }

    public boolean isDeathAnimationFinished() {
        return deathAnim.isAnimationFinished(stateTime);
    }

    public void resetDeathAnim() {
        stateTime = 0f;
    }

    @Override
    public void dispose() {
        for (Texture tex : frames) {
            tex.dispose();
        }
    }
}
