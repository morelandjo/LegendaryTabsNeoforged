package vodmordia.modtabs.api.tabs_menu;

public enum TabPositioning {
    GUI_RELATIVE,         // Default - tabs hug the top edge of the GUI box
    SCREEN_TOP,           // Tabs fixed to top of screen with configurable offset
    SCREEN_BOTTOM,        // Tabs fixed to bottom of screen with configurable offset
    SCREEN_RIGHT,         // Vertical tabs stacked along the right edge of the screen
    GUI_RELATIVE_RIGHT,   // Vertical tabs hugging the GUI's right edge, centered with GUI
    CUSTOM;               // Use custom position functions provided in ScreenInfo

    public boolean isVertical() {
        return this == SCREEN_RIGHT || this == GUI_RELATIVE_RIGHT;
    }
}
