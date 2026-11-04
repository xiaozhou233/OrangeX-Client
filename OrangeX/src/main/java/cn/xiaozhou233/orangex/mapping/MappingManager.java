package cn.xiaozhou233.orangex.mapping;

import cn.xiaozhou233.orangex.mapping.impl.IdentityFallbackMappingResolver;
import cn.xiaozhou233.orangex.mapping.impl.MCPMappingResolver;
import cn.xiaozhou233.orangex.mapping.impl.SrgMappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;

public class MappingManager {

    private static MappingResolver resolver;

    public static void init(RuntimeInfo info) {
        MinecraftVersion minecraftVersion = info.getMinecraftVersion();
        LoaderType loaderType = info.getLoaderType();

        switch (minecraftVersion) {
            case V1_8_9: {
                if(loaderType.equals(LoaderType.MCP)) {
                    resolver = new MCPMappingResolver();
                } else if (loaderType.equals(LoaderType.VANILLA)) {
                    resolver = new SrgMappingResolver("mappings/vanilla189/joined.srg");
                } else if (loaderType.equals(LoaderType.FORGE)) {
                    resolver = new SrgMappingResolver("mappings/forge189/joined.srg");
                } else {
                    throw new RuntimeException("Unsupported loader for 1.8.9: " + loaderType);
                }

                break;
            }

            case V26_2: {
                // 26.x normally uses Mojang names. Fabric 26.2 currently ships
                // empty intermediary mappings, so it also runs with Mojang
                // names. Verify the runtime instead of trusting the loader type.
                if (usesMojangNames(info)) {
                    resolver = new IdentityFallbackMappingResolver();
                } else {
                    throw new RuntimeException(
                            "Unsupported runtime naming for 26.2 ("
                                    + loaderType + "): expected "
                                    + "net.minecraft.client.Minecraft");
                }

                break;
            }

            default:
                throw new RuntimeException("Unsupported version " + minecraftVersion);
        }
    }

    public static boolean isInitialized() {
        return resolver != null;
    }

    private static boolean usesMojangNames(RuntimeInfo info) {
        ClassLoader loader = info.getMinecraftClassLoader();
        if (loader == null) {
            return false;
        }

        try {
            Class.forName("net.minecraft.client.Minecraft", false, loader);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static MappingResolver getResolver() {
        if (!isInitialized()) {
            System.out.println("Resolver not initialized!");
            return null;
        }

        return resolver;
    }
}