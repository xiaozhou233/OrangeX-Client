package cn.xiaozhou233.orangex.hook;

import javassist.ClassClassPath;
import javassist.ClassPool;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Javassist setup shared by all hooks.
 */
public final class ClassPoolUtils {

    /**
     * Set by the Loader before OrangeX loads: the jar that was injected into
     * the game class loader.
     */
    private static final String ORANGEX_JAR_PROPERTY = "orangex.jar";

    private static final Set<String> importedPackages = new HashSet<String>();

    private static boolean classPathAdded;

    /**
     * Prepares {@code pool} for compiling injected code.
     *
     * <p>Solves two problems:</p>
     *
     * <ul>
     *   <li>{@link ClassPool#getDefault()} only knows the JVM class path. The
     *   OrangeX jar is injected into the game class loader at runtime, so it
     *   must be added explicitly, otherwise javassist cannot find OrangeX
     *   classes.</li>
     *   <li>Vanilla 1.8.8 contains an obfuscated class in the default package
     *   named {@code cn}, which collides with the first segment of our
     *   package. Javassist resolves qualified names from left to right, finds
     *   that class instead of the package, and fails with the misleading
     *   {@code no such class: cn$xiaozhou233...}. Importing the packages lets
     *   injected code use simple class names and avoids the collision.</li>
     * </ul>
     *
     * @param classNames fully qualified names of the classes referenced by the
     *                   injected code; their packages are imported
     */
    public static synchronized void prepare(ClassPool pool, String... classNames) {
        importPackages(pool, classNames);

        if (classPathAdded) {
            return;
        }

        classPathAdded = true;

        File jar = orangeXJar();
        if (jar == null) {
            System.out.println("[OrangeX] WARNING: OrangeX.jar not found for javassist; "
                    + "injected classes may fail to compile");
            pool.insertClassPath(new ClassClassPath(ClassPoolUtils.class));
            return;
        }

        try {
            pool.insertClassPath(jar.getAbsolutePath());
            System.out.println("[OrangeX] javassist classpath: " + jar.getAbsolutePath());
        } catch (Throwable t) {
            System.out.println("[OrangeX] Failed to add javassist classpath: "
                    + jar.getAbsolutePath() + " (" + t + ")");
        }
    }

    private static void importPackages(ClassPool pool, String[] classNames) {
        for (String className : classNames) {
            int dot = className.lastIndexOf('.');
            if (dot <= 0) {
                continue;
            }

            String packageName = className.substring(0, dot);
            if (importedPackages.add(packageName)) {
                pool.importPackage(packageName);
            }
        }
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
