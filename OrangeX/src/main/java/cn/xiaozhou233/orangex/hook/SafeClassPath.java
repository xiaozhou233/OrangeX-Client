package cn.xiaozhou233.orangex.hook;

import javassist.ClassPath;

import java.io.InputStream;
import java.net.URL;

/**
 * Wraps a {@link ClassPath} so launcher-specific exceptions are treated as
 * "not found" instead of aborting Javassist's class path search.
 */
final class SafeClassPath implements ClassPath {

    private final ClassPath delegate;

    SafeClassPath(ClassPath delegate) {
        this.delegate = delegate;
    }

    @Override
    public InputStream openClassfile(String classname) {
        try {
            return delegate.openClassfile(classname);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public URL find(String classname) {
        try {
            return delegate.find(classname);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
