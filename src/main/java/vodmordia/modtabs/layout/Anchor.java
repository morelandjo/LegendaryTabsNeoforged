package vodmordia.modtabs.layout;

/**
 * Reference frame the per-screen {@code offsetX}/{@code offsetY} are measured from.
 * Two values cover every case:
 *
 * <ul>
 *   <li>{@link #GUI_RELATIVE} — offsets are measured from the host GUI box's top-left.
 *       The bar tracks the GUI on window resize. Use for inventory-style screens.
 *   <li>{@link #SCREEN_ABSOLUTE} — offsets are raw screen-space coordinates.
 *       Use for screens with no host GUI box (Xaero map, advancements, etc.) or when
 *       the bar should pin to a fixed screen position regardless of the GUI.
 * </ul>
 *
 * The layout editor exposes a toggle between the two and renders the active reference
 * frame as a green outline so the choice is visually obvious.
 */
public enum Anchor {
    GUI_RELATIVE,
    SCREEN_ABSOLUTE;

    public Anchor next() {
        return this == GUI_RELATIVE ? SCREEN_ABSOLUTE : GUI_RELATIVE;
    }
}
