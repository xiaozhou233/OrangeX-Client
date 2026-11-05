package cn.xiaozhou233.orangex.hook;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;

public abstract class ClassHook {

    protected abstract String getTargetClass();

    protected abstract void transform(CtClass ctClass) throws Exception;

    /**
     * Fully qualified names of classes referenced by the injected code.
     * Their packages are imported so the injected source can use simple names.
     */
    protected String[] getInjectedClassNames() {
        return new String[0];
    }

    public final byte[] transform(ClassLoader loader, byte[] classBytes)
            throws Exception {

        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(loader));
        ClassPoolUtils.prepare(pool, getInjectedClassNames());

        CtClass ctClass = pool.makeClass(
                new ByteArrayInputStream(classBytes),
                false
        );

        try {
            transform(ctClass);
            return ctClass.toBytecode();
        } finally {
            ctClass.detach();
        }
    }
}