package vodmordia.modtabs.layout;

/**
 * Per-screen visual order of tabs in the bar. Useful when the bar is rotated 180°
 * and the user wants the "first" tab (e.g. inventory) to still appear on the
 * visual left after rotation — flip to {@link #RIGHT_TO_LEFT}.
 */
public enum TabOrder {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT;

    public TabOrder next() {
        return this == LEFT_TO_RIGHT ? RIGHT_TO_LEFT : LEFT_TO_RIGHT;
    }
}
