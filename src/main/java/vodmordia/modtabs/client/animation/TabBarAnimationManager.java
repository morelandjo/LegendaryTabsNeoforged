package vodmordia.modtabs.client.animation;

import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;

public class TabBarAnimationManager {
    public enum AnimationState {
        VISIBLE,       // Tabs fully visible
        TUCKED,        // Tabs partially hidden
        ANIMATING_IN,  // Animating from tucked to visible
        ANIMATING_OUT  // Animating from visible to tucked
    }

    // Start tucked: tabs are hidden by default and reveal on hover. Previous behavior
    // (start visible, auto-tuck after 800ms) made tabs slide AWAY just as the user
    // was moving their cursor toward them, which read as backwards.
    private AnimationState currentState = AnimationState.TUCKED;
    private long animationStartTime = 0;
    private static final long ANIMATION_DURATION_MS = 300; // 300ms for smooth animation
    private static final float TUCK_PERCENTAGE = 0.6f; // Tuck in by 60%, show 40% of tab height when tucked

    public TabBarAnimationManager() {
    }

    public void onMouseEnterHoverZone() {
        if (currentState == AnimationState.TUCKED) {
            startAnimation(AnimationState.ANIMATING_IN);
        }
    }

    public void onMouseExitHoverZone() {
        if (currentState == AnimationState.VISIBLE) {
            startAnimation(AnimationState.ANIMATING_OUT);
        }
    }

    private void startAnimation(AnimationState newState) {
        this.currentState = newState;
        this.animationStartTime = System.currentTimeMillis();
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Handle animation completion
        if (currentState == AnimationState.ANIMATING_IN || currentState == AnimationState.ANIMATING_OUT) {
            if (currentTime - animationStartTime >= ANIMATION_DURATION_MS) {
                // Animation completed
                currentState = (currentState == AnimationState.ANIMATING_IN) ?
                    AnimationState.VISIBLE : AnimationState.TUCKED;
            }
        }
    }

    public float getAnimationProgress() {
        if (currentState != AnimationState.ANIMATING_IN && currentState != AnimationState.ANIMATING_OUT) {
            return 1.0f; // No animation in progress
        }

        long elapsed = System.currentTimeMillis() - animationStartTime;
        float progress = Math.min(1.0f, (float) elapsed / ANIMATION_DURATION_MS);

        // Apply easing function (cubic ease-in-out)
        return easeInOut(progress);
    }

    private float easeInOut(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    /** Current 0..TUCK_PERCENTAGE multiplier for the active animation state. */
    public float getOffsetFactor() {
        update();
        return currentOffsetFactor();
    }

    /** @deprecated TabsMenu now computes the tuck offset as a rotation-aware 2D vector
     *  via {@link #getOffsetFactor()}. Retained for older call sites. */
    @Deprecated
    public int getYOffset(int tabHeight, TabDisplayMode displayMode) {
        update();
        float offsetFactor = currentOffsetFactor();
        int offset = (int) (tabHeight * offsetFactor);
        return displayMode == TabDisplayMode.INVERTED ? -offset : offset;
    }

    /** @deprecated See {@link #getOffsetFactor()}. */
    @Deprecated
    public int getXOffset(int tabWidth) {
        update();
        return (int) (tabWidth * currentOffsetFactor());
    }

    private float currentOffsetFactor() {
        switch (currentState) {
            case VISIBLE:
                return 0.0f;
            case TUCKED:
                return TUCK_PERCENTAGE;
            case ANIMATING_IN:
                return TUCK_PERCENTAGE * (1.0f - getAnimationProgress());
            case ANIMATING_OUT:
                return TUCK_PERCENTAGE * getAnimationProgress();
        }
        return 0.0f;
    }

    public boolean isInHoverState() {
        return currentState == AnimationState.VISIBLE || currentState == AnimationState.ANIMATING_IN;
    }

    public boolean shouldShowTabs() {
        // Always show tabs in tuck mode, just with different positioning
        return true;
    }

    public AnimationState getCurrentState() {
        update();
        return currentState;
    }
}