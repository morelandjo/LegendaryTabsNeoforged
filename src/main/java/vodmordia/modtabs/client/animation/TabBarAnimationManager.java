package vodmordia.modtabs.client.animation;

import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;

public class TabBarAnimationManager {
    public enum AnimationState {
        VISIBLE,       // Tabs fully visible
        TUCKED,        // Tabs partially hidden
        ANIMATING_IN,  // Animating from tucked to visible
        ANIMATING_OUT  // Animating from visible to tucked
    }

    private AnimationState currentState = AnimationState.VISIBLE;
    private long animationStartTime = 0;
    private static final long ANIMATION_DURATION_MS = 300; // 300ms for smooth animation
    private static final long INITIAL_DISPLAY_TIME_MS = 800; // Show for 0.8 seconds initially
    private static final float TUCK_PERCENTAGE = 0.6f; // Tuck in by 60%, show 40% of tab height when tucked

    private long screenOpenTime = 0;
    private boolean hasInitiallyTucked = false;

    public TabBarAnimationManager() {
        this.screenOpenTime = System.currentTimeMillis();
    }

    public void onMouseEnterHoverZone() {
        if (currentState == AnimationState.TUCKED) {
            startAnimation(AnimationState.ANIMATING_IN);
        }
    }

    public void onMouseExitHoverZone() {
        if (currentState == AnimationState.VISIBLE && hasInitiallyTucked) {
            startAnimation(AnimationState.ANIMATING_OUT);
        }
    }

    private void startAnimation(AnimationState newState) {
        this.currentState = newState;
        this.animationStartTime = System.currentTimeMillis();
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Handle initial auto-tuck after screen opens
        if (!hasInitiallyTucked && currentTime - screenOpenTime > INITIAL_DISPLAY_TIME_MS) {
            hasInitiallyTucked = true;
            startAnimation(AnimationState.ANIMATING_OUT);
        }

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

    public int getYOffset(int tabHeight, TabDisplayMode displayMode) {
        update(); // Update state

        float offsetFactor = currentOffsetFactor();
        int offset = (int) (tabHeight * offsetFactor);

        // For inverted tabs, move upward (negative offset)
        // For normal tabs, move downward (positive offset)
        return displayMode == TabDisplayMode.INVERTED ? -offset : offset;
    }

    /**
     * Cross-axis offset for vertical (right-side) tab bars: positive moves right,
     * sliding the bar off-screen to tuck it.
     */
    public int getXOffset(int tabWidth, TabPositioning positioning) {
        update();
        float offsetFactor = currentOffsetFactor();
        return (int) (tabWidth * offsetFactor);
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