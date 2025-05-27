package org.projectplatformer.lwjgl3.EnemiesAnimation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Array;

public class WitchAnimationManager implements Disposable {
    public enum State {
        FLY,
        ATTACK,
        DEATH
    }

    private final Animation<TextureRegion> flyAnim;
    private final Animation<TextureRegion> attackAnim;
    private final Animation<TextureRegion> deathAnim;

    private float stateTime;
    private State currentState;
    private TextureRegion currentFrame;
    private final Texture[] frames;

    /**
     * Завантажує спрайти та створює три анімації: польоту, атаки та смерті.
     */
    public WitchAnimationManager() {
        stateTime    = 0f;
        currentState = State.FLY;
        // Припустимо, що всього 12 кадрів: 0-3 польоту, 4-7 атаки, 8-11 смерті
        frames = new Texture[16];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new Texture("Enemies/Witch/Witch" + (i + 1) + ".png");
        }
        flyAnim     = createAnimation(0, 5, 0.15f, Animation.PlayMode.LOOP);
        attackAnim  = createAnimation(6, 10, 0.1f, Animation.PlayMode.NORMAL);
        deathAnim   = createAnimation(11, 15, 0.2f, Animation.PlayMode.NORMAL);
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
     * Оновлює стан анімації відьми.
     * @param delta     час з минулого кадру
     * @param newState  новий стан анімації
     */
    public void update(float delta, State newState) {
        if (currentState != newState) {
            currentState = newState;
            stateTime    = 0f;
        } else {
            stateTime += delta;
        }
        switch (currentState) {
            case FLY:
                currentFrame = flyAnim.getKeyFrame(stateTime, true);
                break;
            case ATTACK:
                currentFrame = attackAnim.getKeyFrame(stateTime, false);
                break;
            case DEATH:
                currentFrame = deathAnim.getKeyFrame(stateTime, false);
                break;
        }
    }

    /**
     * Повертає поточний кадр для відображення.
     */
    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Перевіряє, чи завершилась анімація атаки.
     */
    public boolean isAttackAnimationFinished() {
        return attackAnim.isAnimationFinished(stateTime);
    }

    /**
     * Перезапускає анімацію атаки.
     */
    public void resetAttackAnim() {
        stateTime = 0f;
    }

    /**
     * Перевіряє, чи завершилась анімація смерті.
     */
    public boolean isDeathAnimationFinished() {
        return deathAnim.isAnimationFinished(stateTime);
    }

    /**
     * Перезапускає анімацію смерті.
     */
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
