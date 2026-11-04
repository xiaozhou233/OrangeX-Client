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
                if (loaderType == LoaderType.VANILLA
                        || loaderType == LoaderType.MCP
                        || loaderType == LoaderType.NEOFORGE) {
                    // 26.x runtimes use Mojang names for classes, fields and
                    // methods, so no obfuscation mapping is needed.
                    resolver = new IdentityFallbackMappingResolver();
                } else {
                    throw new RuntimeException(
                            "Unsupported loader for 26.2: " + loaderType);
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

    public static MappingResolver getResolver() {
        if (!isInitialized()) {
            System.out.println("Resolver not initialized!");
            return null;
        }

        return resolver;
    }
}