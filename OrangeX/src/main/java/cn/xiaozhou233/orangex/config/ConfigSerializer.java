package cn.xiaozhou233.orangex.config;

import cn.xiaozhou233.orangex.modules.ModuleManager;
import com.google.gson.JsonObject;

public interface ConfigSerializer {

    /** On-disk format version shared by the manager and the serializer. */
    int CURRENT_VERSION = 1;

    JsonObject serialize(ModuleManager moduleManager);

    void deserialize(ModuleManager moduleManager, JsonObject json);
}