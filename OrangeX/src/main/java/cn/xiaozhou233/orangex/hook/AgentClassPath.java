package cn.xiaozhou233.orangex.hook;

import javassist.ClassPath;
import javassist.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Resolves game classes from the <strong>loaded</strong> class bytes through
 * the JuiceAgent.
 *
 * <p>Some launchers (for example Lunar's Genesis class loader) serve
 * MCP-named class resources but the bytes inside still use obfuscated class
 * names. Javassist then throws errors such as
 * {@code cannot find net.minecraft.util.IThreadListener: od found in ...}.
 * The agent can return the current, runtime-named bytecode instead.</p>
 */
final class AgentClassPath implements ClassPath {

    private final ClassLoader loader;

    AgentClassPath(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public InputStream openClassfile(String classname) throws NotFoundException {
        if (isPlatformClass(classname)
                || classname.startsWith("cn.xiaozhou233.")
                || classname.startsWith("orangexbridge.")) {
            return null;
        }

        byte[] bytes = readBytes(classname);
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

    private byte[] readBytes(String classname) {
        String internalName = classname.replace('.', '/');

        try {
            byte[] bytes = JuiceAgentBridge.getClassBytesByName(internalName);
            if (bytes != null) {
                return bytes;
            }
        } catch (Throwable ignored) {
            // The class may not be loaded yet.
        }

        if (loader == null) {
            return null;
        }

        try {
            Class.forName(classname, false, loader);
            return JuiceAgentBridge.getClassBytesByName(internalName);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isPlatformClass(String classname) {
        return classname.startsWith("java.")
                || classname.startsWith("javax.")
                || classname.startsWith("jdk.")
                || classname.startsWith("sun.")
                || classname.startsWith("com.sun.")
                || classname.startsWith("javassist.")
                || classname.startsWith("org.greenrobot.")
                || classname.startsWith("com.google.gson.");
    }

    @Override
    public URL find(String classname) {
        return null;
    }
}
