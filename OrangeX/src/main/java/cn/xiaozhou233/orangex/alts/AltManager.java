package cn.xiaozhou233.orangex.alts;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventTick;
import cn.xiaozhou233.orangex.ui.altmanager.GUIAltManager;
import cn.xiaozhou233.orangex.utils.SessionUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.util.Session;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AltManager {
    private Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private List<Alt> alts = new ArrayList<>();
    private File file;
    public AltManager() {
        file = new File(OrangeX.getOrangeXDir(), "alts.json");
        load();
    }

    @Subscribe
    public void onTick(EventTick event) {
        // Return when key is not pressed
        if (!Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
            return;

        // Return when not in multiplayer menu
        if (!(mc.currentScreen instanceof GuiMultiplayer))
            return;

        // Right SHIFT + Multiplayer Menu -> Display Alt List
        mc.displayGuiScreen(new GUIAltManager());
    }

    public void addAlt(Alt alt) {
        alts.add(alt);
        save();
    }

    public void removeAlt(String name) {
        alts.removeIf(alt -> alt.getName().equals(name));
        save();
    }

    public void removeAlt(Alt alt) {
        alts.remove(alt);
        save();
    }

    public Alt get(int index) {
        return alts.get(index);
    }

    public int size() {
        return alts.size();
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(alts);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            if (!file.exists()) {
                return;
            }

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Alt>>() {}.getType();

            try (FileReader reader = new FileReader(file)) {
                List<Alt> loaded = gson.fromJson(reader, listType);
                if (loaded != null) {
                    alts = loaded;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login(Alt alt) {
        Session session = new Session(
                alt.getName(),
                alt.getUuid().toString(),
                alt.getAccessToken(),
                "mojang"
        );
        SessionUtil.setSession(session);
        System.out.println("Logging in as " + alt.getName());
        System.out.println("UUID: " + alt.getUuid());
    }
}
