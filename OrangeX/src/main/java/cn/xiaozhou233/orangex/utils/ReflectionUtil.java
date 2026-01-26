package cn.xiaozhou233.orangex.utils;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

public class ReflectionUtil {

    private static Field rightClickDelayField;

    static {
        try {
            rightClickDelayField = Minecraft.class.getDeclaredField("rightClickDelayTimer");
            rightClickDelayField.setAccessible(true);
        } catch (Exception e) {
            System.out.println("[ReflectionUtil] Failed to get rightClickDelayTimer field");
            e.printStackTrace();
        }
    }

    public static int getRightClickDelay() throws IllegalAccessException {
        return rightClickDelayField.getInt(Minecraft.getMinecraft());
    }

    public static void setRightClickDelay(int value) throws IllegalAccessException {
        rightClickDelayField.setInt(Minecraft.getMinecraft(), value);
    }
}
