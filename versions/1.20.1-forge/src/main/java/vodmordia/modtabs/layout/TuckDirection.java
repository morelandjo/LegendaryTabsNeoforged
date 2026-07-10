package vodmordia.modtabs.layout;

/**
 * Direction (in screen-space) the tabs slide when tucked. Default DOWN matches the
 * legacy hard-coded behavior, so existing layouts don't change.
 */
public enum TuckDirection {
    UP, DOWN, LEFT, RIGHT;

    public int dx() {
        switch (this) {
            case LEFT: return -1;
            case RIGHT: return 1;
            default: return 0;
        }
    }

    public int dy() {
        switch (this) {
            case UP: return -1;
            case DOWN: return 1;
            default: return 0;
        }
    }

    public TuckDirection next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
