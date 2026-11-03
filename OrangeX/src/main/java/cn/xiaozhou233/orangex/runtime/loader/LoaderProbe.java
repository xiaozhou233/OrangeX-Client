package cn.xiaozhou233.orangex.runtime.loader;

/**
 * One way to recognize a mod loader. Probes are checked in registration order.
 */
public interface LoaderProbe {

    LoaderType type();

    boolean matches(ClassLoader loader);
}
