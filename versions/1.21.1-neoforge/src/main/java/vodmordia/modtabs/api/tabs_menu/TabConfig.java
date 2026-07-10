package vodmordia.modtabs.api.tabs_menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define tab configuration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TabConfig {
    /**
     * The config key for this tab (used in config file)
     */
    String configKey();

    /**
     * Default enabled state
     */
    boolean defaultEnabled() default true;

    /**
     * Default order
     */
    int defaultOrder() default 0;
}