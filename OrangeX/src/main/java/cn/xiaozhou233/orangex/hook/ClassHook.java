package cn.xiaozhou233.orangex.hook;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;

public abstract class ClassHook {

    protected abstract String getTargetClass();

    protected abstract void transform(CtClass ctClass) throws Exception;

    public final byte[] transform(ClassLoader loader, byte[] classBytes)
            throws Exception {

        ClassPool pool = ClassPoolUtils.createPool(loader);
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