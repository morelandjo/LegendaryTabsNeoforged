package vodmordia.modtabs.config;

/**
 * Configuration class for custom tabs loaded from JSON files
 */
public class CustomTabDefinition {
    public String tabId;
    public boolean enabled = true;
    public String tooltip;
    public int order = 100;
    public IconDefinition icon;
    public ActionDefinition action;
    public String[] requiredMods = new String[0];

    public boolean isValid() {
        return tabId != null && !tabId.trim().isEmpty() &&
               tooltip != null && !tooltip.trim().isEmpty() &&
               icon != null && action != null &&
               icon.isValid() && action.isValid();
    }

    public static class IconDefinition {
        public String item;
        public String fallbackItem = "minecraft:book";
        public String patchouliBook;

        public boolean isValid() {
            return (item != null && !item.trim().isEmpty()) ||
                   (patchouliBook != null && !patchouliBook.trim().isEmpty());
        }
    }

    public static class ActionDefinition {
        public String type;
        public String item;
        public String screenClass;
        public String command;
        public String keybind;
        public String bookId;

        public boolean isValid() {
            if (type == null || type.trim().isEmpty()) {
                return false;
            }

            switch (type) {
                case "use_item":
                    return item != null && !item.trim().isEmpty();
                case "open_screen":
                    return screenClass != null && !screenClass.trim().isEmpty();
                case "run_command":
                    return command != null && !command.trim().isEmpty();
                case "keybind":
                    return keybind != null && !keybind.trim().isEmpty();
                case "open_patchouli_book":
                    return bookId != null && !bookId.trim().isEmpty();
                default:
                    return false;
            }
        }
    }
}