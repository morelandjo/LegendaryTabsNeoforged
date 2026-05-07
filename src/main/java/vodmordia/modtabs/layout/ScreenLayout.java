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
            && iconRotation == 0;
    }

    public ScreenLayout copy() {
        return new ScreenLayout(offsetX, offsetY, scale, tabSpacing, rotation,
            nextButtonOffsetX, nextButtonOffsetY, nextButtonRotation, iconRotation);
    }
}
