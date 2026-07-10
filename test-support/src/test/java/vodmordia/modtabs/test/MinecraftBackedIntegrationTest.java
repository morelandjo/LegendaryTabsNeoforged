package vodmordia.modtabs.test;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/** Runs with the target's mapped Minecraft and production outputs on the test classpath. */
final class MinecraftBackedIntegrationTest {
    @Test
    void screenResolverRecognizesTheTargetsMappedMinecraftScreen() throws Exception {
        String screenName = System.getProperty("modtabs.screenClass");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> screen = Class.forName(screenName, false, loader);
        Class<?> resolver = Class.forName("vodmordia.modtabs.utils.ScreenClassResolver");
        Method resolve = resolver.getMethod("resolveScreenClass", String.class);

        assertSame(screen, resolve.invoke(null, screenName));
        assertNull(resolve.invoke(null, "vodmordia.modtabs.test.DoesNotExist"));
    }
}
