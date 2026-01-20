package cn.xiaozhou233.orangex.module;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.module.impl.misc.Test;
import cn.xiaozhou233.orangex.module.impl.render.*;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final List<Module> modules = new CopyOnWriteArrayList<>();

    public ModuleManager() {

    }

    public void init() {
        OrangeX.getInstance().getEventBus().register(this);
        registerAll();
    }

    public void registerAll() {
        register(new ClickGUI());
        register(new HUD());

        register(new Test());

    }

    @Subscribe
    public void onKey(EventKey event) {
        for (Module module : modules) {
            if (module.getKeyBind().getValue() == event.getKey()) {
                module.toggle();
                System.out.println("[ModuleManager] Toggled module: " + module.getName());
            }
        }
    }

    public void register(Module module) {
        modules.add(module);
        System.out.println("[ModuleManager] Registered module: " + module.getName());
    }

    public Module getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Module> getAllModules() {
        return modules;
    }
}
