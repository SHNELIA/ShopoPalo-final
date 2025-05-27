package org.projectplatformer.lwjgl3.EnemiesAnimation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Animation manager for spider enemy: walk and attack states.
 */
public class SpiderAnimationManager implements Disposable {
    // Possible animation states
    public enum State {
        WALK,   // walking loop
        ATTACK  // attack loop
    }

    private final Animation<TextureRegion> walkAnim;    // walk animation
    private final Animation<TextureRegion> attackAnim;  // attack animation
    private final Animation<TextureRegion> deathAnim;   // unused here

    private float stateTime;            // time tracker for current animation
    private State currentState;         // current animation state
    private TextureRegion currentFrame; // frame to render

    private final Texture[] frames;     // loaded textures array

    public SpiderAnimationManager() {
        stateTime = 0f;
        currentState = State.WALK;

        // load 12 frames: indices 0–11
        frames = new Texture[12];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new Texture("Enemies/Spider/Spider" + (i + 1) + ".png");
        }

        // create animations (fromIndex, toIndex, frameDuration)
        walkAnim = createAnimation(0, 5, 0.15f);
        attackAnim = createAnimation(6, 9, 0.12f);
        deathAnim = createAnimation(9, 11, 0.25f);
    }

    /**
     * Build an animation from a frame range.
     */
    private Animation<TextureRegion> createAnimation(int from, int to, float duration) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = from; i < to; i++) {
            regions.add(new TextureRegion(frames[i]));
        }
        return new Animation<>(duration, regions, Animation.PlayMode.LOOP);
    }

    /**
     * Update spider state and select current frame.
     */
    public void update(float delta, State newState, boolean facingRight) {
        if (currentState != newState) {
            currentState = newState;
            stateTime = 0f;  // reset animation timer
        } else {
            stateTime += delta;
        }

        // choose frame based on state
        switch (currentState) {
            case WALK:
                currentFrame = walkAnim.getKeyFrame(stateTime);
                break;
            case ATTACK:
                currentFrame = attackAnim.getKeyFrame(stateTime);
                break;
        }
    }

    /**
     * Restart attack animation from beginning.
     */
    public void resetAttackAnim() {
        stateTime = 0f;
        currentState = State.ATTACK;
    }

    /**
     * Check if attack animation has completed its loop.
     */
    public boolean isAttackAnimationFinished() {
        return attackAnim.isAnimationFinished(stateTime);
    }

    /**
     * Get the frame to draw.
     */
    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Dispose all loaded textures to free memory.
     */
    @Override
    public void dispose() {
        for (Texture t : frames) {
            t.dispose();
        }
    }
}
