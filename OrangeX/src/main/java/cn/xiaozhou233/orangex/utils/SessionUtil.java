package cn.xiaozhou233.orangex.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.lang.reflect.Field;

public class SessionUtil {
    public static boolean setSession(Session session) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            Field sessionField = Minecraft.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(mc, session);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
