package vodmordia.modtabs.layout;

/**
 * User-editable layout overrides for a single screen class.
 *
 * Phase 1 only carries a translation offset; the schema will grow in later phases
 * (scale, tab spacing, rotation, independent next-button transform). Gson tolerates
 * missing fields, so older JSON files keep loading as new fields are added.
 */
public class ScreenLayout {
    public int offsetX = 0;
    public int offsetY = 0;
    /** Uniform scale applied to tab dimensions. 1.0 = default. */
    public float scale = 1.0f;
    /** Pixel gap between adjacent tabs along the primary axis. Default 1 matches the legacy (TAB_WIDTH + 1) layout. */
    public int tabSpacing = 1;
    /** Rotation around bar center in degrees, clockwise. 0 = unrotated. */
    public float rotation = 0.0f;

    /** Independent translation for the next-page chevron (screen coords, additive after bar rotation). */
    public int nextButtonOffsetX = 0;
    public int nextButtonOffsetY = 0;
    /** Additional rotation for the next-page chevron, on top of the bar's rotation. */
    public float nextButtonRotation = 0.0f;
    /** Rotation applied to each icon around its own center (0, 90, 180, 270). */
    public int iconRotation = 0;

    /** Direction (in screen-space) tabs slide when tucked. Default DOWN matches legacy. */
    public TuckDirection tuckDirection = TuckDirection.DOWN;

    /**
     * Reference frame for {@link #offsetX}/{@link #offsetY}. Default
     * {@link Anchor#GUI_RELATIVE} matches today's centered-on-GUI behavior.
     */
    public Anchor anchor = Anchor.GUI_RELATIVE;

    /**
     * Visual order of tabs in the bar. {@link TabOrder#RIGHT_TO_LEFT} flips the
     * indices so the first tab renders on the right end of the unrotated bar —
     * useful when the bar is rotated 180° and the user wants inventory to stay
     * on the visible left after rotation.
     */
    public TabOrder tabOrder = TabOrder.LEFT_TO_RIGHT;

    /**
     * Maximum tabs visible on one page for this screen. {@code 0} means unlimited
     * (all enabled tabs render on a single page). Pagination kicks in when this is
     * positive and less than the total enabled tab count.
     */
    public int maxTabsPerPage = 6;

    public ScreenLayout() {}

    public ScreenLayout(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public ScreenLayout(int offsetX, int offsetY, float scale, int tabSpacing) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
        this.tabSpacing = tabSpacing;
    }

    public ScreenLayout(int offsetX, int offsetY, float scale, int tabSpacing, float rotation) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
        this.tabSpacing = tabSpacing;
        this.rotation = rotation;
    }

    public ScreenLayout(int offsetX, int offsetY, float scale, int tabSpacing, float rotation,
                        int nextButtonOffsetX, int nextButtonOffsetY, float nextButtonRotation) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
        this.tabSpacing = tabSpacing;
        this.rotation = rotation;
        this.nextButtonOffsetX = nextButtonOffsetX;
        this.nextButtonOffsetY = nextButtonOffsetY;
        this.nextButtonRotation = nextButtonRotation;
    }

    public ScreenLayout(int offsetX, int offsetY, float scale, int tabSpacing, float rotation,
                        int nextButtonOffsetX, int nextButtonOffsetY, float nextButtonRotation,
                        int iconRotation) {
        this(offsetX, offsetY, scale, tabSpacing, rotation,
             nextButtonOffsetX, nextButtonOffsetY, nextButtonRotation);
        this.iconRotation = iconRotation;
    }

    public boolean isDefault() {
        return offsetX == 0 && offsetY == 0 && scale == 1.0f && tabSpacing == 1 && rotation == 0.0f
            && nextButtonOffsetX == 0 && nextButtonOffsetY == 0 && nextButtonRotation == 0.0f
            && iconRotation == 0 && tuckDirection == TuckDirection.DOWN
            && anchor == Anchor.GUI_RELATIVE && tabOrder == TabOrder.LEFT_TO_RIGHT
            && maxTabsPerPage == 6;
    }

    public ScreenLayout copy() {
        ScreenLayout c = new ScreenLayout(offsetX, offsetY, scale, tabSpacing, rotation,
            nextButtonOffsetX, nextButtonOffsetY, nextButtonRotation, iconRotation);
        c.tuckDirection = tuckDirection;
        c.anchor = anchor;
        c.tabOrder = tabOrder;
        c.maxTabsPerPage = maxTabsPerPage;
        return c;
    }
}
