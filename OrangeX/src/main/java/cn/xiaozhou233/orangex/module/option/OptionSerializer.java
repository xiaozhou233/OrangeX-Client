package cn.xiaozhou233.orangex.module.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class OptionSerializer {
    public static JsonObject toJson(List<Option<?>> options) {
        JsonObject json = new JsonObject();
        for (Option<?> option : options) {
            json.add(option.getName(), option.serialize());
        }
        return json;
    }

    public static void fromJson(JsonObject json, List<Option<?>> options) {
        if (json == null) return;
        for (Option<?> option : options) {
            JsonElement element = json.get(option.getName());
            if (element != null) {
                option.deserialize(element);
            }
        }
    }
}