import cn.xiaozhou233.orangex.mapping.MappingClass;
import cn.xiaozhou233.orangex.mapping.MappingManager;
import cn.xiaozhou233.orangex.mapping.MappingResolver;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import cn.xiaozhou233.orangex.runtime.loader.LoaderType;
import cn.xiaozhou233.orangex.runtime.version.MinecraftVersion;

public class Main {
    public static void main(String[] args) {
        MappingManager.init(new RuntimeInfo(
                null, LoaderType.FORGE, MinecraftVersion.V1_8_9, MinecraftVersion.V1_8_9.getId()
        ));

        MappingResolver resolver = MappingManager.getResolver();

        System.out.println("Resolver: " + resolver);

        System.out.println(resolver.mapMethod("net/minecraft/client/Minecraft", "getMinecraft", "()Lnet/minecraft/client/Minecraft;").getTargetDescriptor());
    }
}
