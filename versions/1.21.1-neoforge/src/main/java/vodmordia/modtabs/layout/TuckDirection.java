package vodmordia.modtabs.layout;

/**
 * Direction (in screen-space) the tabs slide when tucked. Default {@link #DOWN} is
 * what the codebase always did before the per-screen toggle was added — keeping it as
 * the default means existing layouts behave the same.
 */
public enum TuckDirection {
    UP, DOWN, LEFT, RIGHT;

    /** Screen-space dx component (-1, 0, +1) for this direction. */
    public int dx() {
        return switch (this) {
            case LEFT -> -1;
            case RIGHT -> 1;
            default -> 0;
        };
    }

    /** Screen-space dy component (-1, 0, +1) for this direction. Down is +y. */
    public int dy() {
        return switch (this) {
            case UP -> -1;
            case DOWN -> 1;
            default -> 0;
        };
    }

    public TuckDirection next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
