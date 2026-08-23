package cn.xiaozhou233.orangex.module;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.config.ModuleConfig;
import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.module.impl.TestModule;
import cn.xiaozhou233.orangex.module.impl.render.*;
import lombok.Getter;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ModuleManager {
    public static String userHome = System.getProperty("user.home");
    private final List<Module> modules = new CopyOnWriteArrayList<>();
    private final ModuleConfig config;

    public ModuleManager() {
        config = new ModuleConfig(userHome + "/.orangex/profile.json");
    }

    public void addModule(Module module) {
        modules.add(module);
    }

    public void registerModules() {
        OrangeX.getInstance().getEventBus().register(this);
        addModule(new TestModule());

        // Combat

        // Render
        addModule(new HUD());
        addModule(new ClickGUI());
        addModule(new ESP());
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (module.getClass() == clazz) {
                return (T) module;
            }
        }
        return null;
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public void saveConfig() {
        config.save(modules);
    }

    public void loadConfig() {
        config.load(modules);
    }

    @Subscribe
    public void onKeyDown(EventKey event) {
        for (Module module : OrangeX.getInstance().getModuleManager().getModules()) {
            if (module.getKey() == event.getKey()) {
                module.toggle();
            }
        }
    }
}