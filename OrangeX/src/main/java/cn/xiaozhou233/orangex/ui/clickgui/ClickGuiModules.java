package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.modules.Module;
import cn.xiaozhou233.orangex.modules.ModuleCategory;
import cn.xiaozhou233.orangex.value.KeybindValue;
import cn.xiaozhou233.orangex.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Module and category helpers shared by the ClickGUI renderers.
 */
public final class ClickGuiModules {

    private ClickGuiModules() {
    }

    public static List<Value<?>> settingsOf(Module module) {
        List<Value<?>> settings = new ArrayList<Value<?>>();
        for (Value<?> value : module.getVisibleValues()) {
            // The module keybind is shown on the collapsed row instead.
            if (!(value instanceof KeybindValue)) {
                settings.add(value);
            }
        }
        return settings;
    }

    public static String displayName(ModuleCategory category) {
        String name = category.name();
        return name.substring(0, 1) + name.substring(1).toLowerCase(Locale.ROOT);
    }
}
