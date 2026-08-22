package cn.xiaozhou233.orangex.nativebridge;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.event.impl.EventKey;
import cn.xiaozhou233.orangex.event.impl.EventKeyDown;
import cn.xiaozhou233.orangex.event.impl.EventKeyUp;
import cn.xiaozhou233.orangex.event.impl.EventMouseButton;
import cn.xiaozhou233.orangex.event.impl.EventMouseMove;
import net.minecraft.client.Minecraft;

public class NativeMessageHandler {

    private static final int WM_KEYDOWN = 0x100;
    private static final int WM_KEYUP = 0x101;
    private static final int WM_SYSKEYDOWN = 0x104;
    private static final int WM_SYSKEYUP = 0x105;

    private static final int WM_MOUSEMOVE = 0x200;

    private static final int WM_LBUTTONDOWN = 0x201;
    private static final int WM_LBUTTONUP = 0x202;
    private static final int WM_RBUTTONDOWN = 0x204;
    private static final int WM_RBUTTONUP = 0x205;
    private static final int WM_MBUTTONDOWN = 0x207;
    private static final int WM_MBUTTONUP = 0x208;

    private static final int WM_XBUTTONDOWN = 0x20B;
    private static final int WM_XBUTTONUP = 0x20C;

    public static boolean onNativeMessage(int msg, long wParam, long lParam) {
        switch (msg) {
            case WM_KEYDOWN:
            case WM_SYSKEYDOWN:
                postKey((int) wParam, lParam, true);
                return false;

            case WM_KEYUP:
            case WM_SYSKEYUP:
                postKey((int) wParam, lParam, false);
                return false;

            case WM_MOUSEMOVE:
                OrangeX.getInstance().getEventBus().post(new EventMouseMove(getX(lParam), getY(lParam)));
                return false;

            case WM_LBUTTONDOWN:
                return postMouse(0, true, lParam);

            case WM_LBUTTONUP:
                return postMouse(0, false, lParam);

            case WM_RBUTTONDOWN:
                return postMouse(1, true, lParam);

            case WM_RBUTTONUP:
                return postMouse(1, false, lParam);

            case WM_MBUTTONDOWN:
                return postMouse(2, true, lParam);

            case WM_MBUTTONUP:
                return postMouse(2, false, lParam);

            case WM_XBUTTONDOWN:
                return postMouse(3 + (int) ((wParam >> 16) & 0xFFFF), true, lParam);

            case WM_XBUTTONUP:
                return postMouse(3 + (int) ((wParam >> 16) & 0xFFFF), false, lParam);
        }

        return false;
    }

    private static void postKey(int vk, long lParam, boolean pressed) {
        int key = NativeKeyMapper.map(vk, lParam);

        if (key == org.lwjgl.input.Keyboard.KEY_NONE) {
            return;
        }

        if (Minecraft.getMinecraft().currentScreen != null) {
            return;
        }

        OrangeX.getInstance().getEventBus().post(new EventKey(key, pressed));

        if (pressed) {
            OrangeX.getInstance().getEventBus().post(new EventKeyDown(key));
        } else {
            OrangeX.getInstance().getEventBus().post(new EventKeyUp(key));
        }
    }

    private static boolean postMouse(int button, boolean pressed, long lParam) {
        OrangeX.getInstance().getEventBus().post(new EventMouseButton(button, pressed, getX(lParam), getY(lParam)));
        return false;
    }

    private static int getX(long lParam) {
        return (short) (lParam & 0xFFFF);
    }

    private static int getY(long lParam) {
        return (short) ((lParam >> 16) & 0xFFFF);
    }
}