package vodmordia.modtabs.api.tabs_menu;

public enum TabPositioning {
    GUI_RELATIVE,   // Default - tabs positioned relative to GUI using width/height centering
    SCREEN_TOP,     // Tabs fixed to top of screen with configurable offset
    SCREEN_BOTTOM,  // Tabs fixed to bottom of screen with configurable offset
    CUSTOM          // Use custom position functions provided in ScreenInfo
}
