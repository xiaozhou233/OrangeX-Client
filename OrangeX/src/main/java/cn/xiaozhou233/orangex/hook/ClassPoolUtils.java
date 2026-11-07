package cn.xiaozhou233.orangex.hook;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;

import java.io.File;

/**
 * Javassist setup shared by all hooks.
 *
 * <p>A fresh pool is created for each transform instead of using
 * {@link ClassPool#getDefault()}: on some launchers the current thread has a
 * {@code null} context class loader, which makes the default pool's system
 * path throw while resolving imported class names.</p>
 */
public final class ClassPoolUtils {

    /**
     * Set by the Loader before OrangeX loads: the jar that was injected into
     * the game class loader.
     */
    private static final String ORANGEX_JAR_PROPERTY = "orangex.jar";

    private static boolean bridgeChecked;

    /**
     * Creates a pool that can resolve:
     * <ul>
     *   <li>OrangeX classes from the injected jar;</li>
     *   <li>game classes from their runtime (post-transform) bytes through
     *       the agent, avoiding launchers whose class-loader resources still
     *       contain obfuscated names;</li>
     *   <li>anything else via the game loader and the system loader.</li>
     * </ul>
     *
     * @param loader the game class loader
     */
    public static ClassPool createPool(ClassLoader loader) {
        ClassPool pool = new ClassPool(false);

        File jar = orangeXJar();
        if (jar != null) {
            try {
                pool.insertClassPath(jar.getAbsolutePath());
                System.out.println("[OrangeX] javassist classpath: "
                        + jar.getAbsolutePath());
            } catch (Throwable t) {
                System.out.println("[OrangeX] Failed to add javassist classpath: "
                        + jar.getAbsolutePath() + " (" + t + ")");
            }
        } else {
            System.out.println("[OrangeX] WARNING: OrangeX.jar not found for javassist; "
                    + "injected classes may fail to compile");
            pool.insertClassPath(new ClassClassPath(ClassPoolUtils.class));
        }

        // Runtime bytes of already-loaded game classes (MCP-named even when
        // the launcher's resource stream is still obfuscated).
        pool.appendClassPath(new AgentClassPath(loader));

        if (loader != null) {
            pool.appendClassPath(new SafeClassPath(new LoaderClassPath(loader)));
        }

        ClassLoader system = ClassLoader.getSystemClassLoader();
        if (system != null) {
            pool.appendClassPath(new SafeClassPath(new LoaderClassPath(system)));
        } else {
            pool.appendClassPath(new ClassClassPath(Object.class));
        }

        if (!bridgeChecked) {
            bridgeChecked = true;
            try {
                pool.get("orangexbridge.Hooks");
                System.out.println("[OrangeX] javassist bridge OK");
            } catch (Throwable t) {
                System.out.println("[OrangeX] javassist bridge FAILED");
                t.printStackTrace();
            }
        }

        return pool;
    }

    private static File orangeXJar() {
        String path = System.getProperty(ORANGEX_JAR_PROPERTY);
        if (path == null) {
            return null;
        }

        File jar = new File(path);
        return jar.isFile() ? jar : null;
    }

    private ClassPoolUtils() {
    }
}
