package cn.xiaozhou233.orangex.nativebridge;

import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class NativeKeyMapper {

    private static final Map<Integer, Integer> KEY_MAP = new HashMap<>();

    static {
        // Control keys
        KEY_MAP.put(0x08, Keyboard.KEY_BACK);
        KEY_MAP.put(0x09, Keyboard.KEY_TAB);
        KEY_MAP.put(0x0C, Keyboard.KEY_NUMPAD5);
        KEY_MAP.put(0x0D, Keyboard.KEY_RETURN);
        KEY_MAP.put(0x13, Keyboard.KEY_PAUSE);
        KEY_MAP.put(0x14, Keyboard.KEY_CAPITAL);
        KEY_MAP.put(0x1B, Keyboard.KEY_ESCAPE);
        KEY_MAP.put(0x20, Keyboard.KEY_SPACE);

        // Navigation
        KEY_MAP.put(0x21, Keyboard.KEY_PRIOR);
        KEY_MAP.put(0x22, Keyboard.KEY_NEXT);
        KEY_MAP.put(0x23, Keyboard.KEY_END);
        KEY_MAP.put(0x24, Keyboard.KEY_HOME);
        KEY_MAP.put(0x25, Keyboard.KEY_LEFT);
        KEY_MAP.put(0x26, Keyboard.KEY_UP);
        KEY_MAP.put(0x27, Keyboard.KEY_RIGHT);
        KEY_MAP.put(0x28, Keyboard.KEY_DOWN);

        // Insert/Delete
        KEY_MAP.put(0x2D, Keyboard.KEY_INSERT);
        KEY_MAP.put(0x2E, Keyboard.KEY_DELETE);

        // Number row
        KEY_MAP.put(0x30, Keyboard.KEY_0);
        KEY_MAP.put(0x31, Keyboard.KEY_1);
        KEY_MAP.put(0x32, Keyboard.KEY_2);
        KEY_MAP.put(0x33, Keyboard.KEY_3);
        KEY_MAP.put(0x34, Keyboard.KEY_4);
        KEY_MAP.put(0x35, Keyboard.KEY_5);
        KEY_MAP.put(0x36, Keyboard.KEY_6);
        KEY_MAP.put(0x37, Keyboard.KEY_7);
        KEY_MAP.put(0x38, Keyboard.KEY_8);
        KEY_MAP.put(0x39, Keyboard.KEY_9);

        // Alphabet
        KEY_MAP.put(0x41, Keyboard.KEY_A);
        KEY_MAP.put(0x42, Keyboard.KEY_B);
        KEY_MAP.put(0x43, Keyboard.KEY_C);
        KEY_MAP.put(0x44, Keyboard.KEY_D);
        KEY_MAP.put(0x45, Keyboard.KEY_E);
        KEY_MAP.put(0x46, Keyboard.KEY_F);
        KEY_MAP.put(0x47, Keyboard.KEY_G);
        KEY_MAP.put(0x48, Keyboard.KEY_H);
        KEY_MAP.put(0x49, Keyboard.KEY_I);
        KEY_MAP.put(0x4A, Keyboard.KEY_J);
        KEY_MAP.put(0x4B, Keyboard.KEY_K);
        KEY_MAP.put(0x4C, Keyboard.KEY_L);
        KEY_MAP.put(0x4D, Keyboard.KEY_M);
        KEY_MAP.put(0x4E, Keyboard.KEY_N);
        KEY_MAP.put(0x4F, Keyboard.KEY_O);
        KEY_MAP.put(0x50, Keyboard.KEY_P);
        KEY_MAP.put(0x51, Keyboard.KEY_Q);
        KEY_MAP.put(0x52, Keyboard.KEY_R);
        KEY_MAP.put(0x53, Keyboard.KEY_S);
        KEY_MAP.put(0x54, Keyboard.KEY_T);
        KEY_MAP.put(0x55, Keyboard.KEY_U);
        KEY_MAP.put(0x56, Keyboard.KEY_V);
        KEY_MAP.put(0x57, Keyboard.KEY_W);
        KEY_MAP.put(0x58, Keyboard.KEY_X);
        KEY_MAP.put(0x59, Keyboard.KEY_Y);
        KEY_MAP.put(0x5A, Keyboard.KEY_Z);

        // Windows keys
        KEY_MAP.put(0x5B, Keyboard.KEY_LMETA);
        KEY_MAP.put(0x5C, Keyboard.KEY_RMETA);
        KEY_MAP.put(0x5D, Keyboard.KEY_APPS);

        // Numpad
        KEY_MAP.put(0x60, Keyboard.KEY_NUMPAD0);
        KEY_MAP.put(0x61, Keyboard.KEY_NUMPAD1);
        KEY_MAP.put(0x62, Keyboard.KEY_NUMPAD2);
        KEY_MAP.put(0x63, Keyboard.KEY_NUMPAD3);
        KEY_MAP.put(0x64, Keyboard.KEY_NUMPAD4);
        KEY_MAP.put(0x65, Keyboard.KEY_NUMPAD5);
        KEY_MAP.put(0x66, Keyboard.KEY_NUMPAD6);
        KEY_MAP.put(0x67, Keyboard.KEY_NUMPAD7);
        KEY_MAP.put(0x68, Keyboard.KEY_NUMPAD8);
        KEY_MAP.put(0x69, Keyboard.KEY_NUMPAD9);

        KEY_MAP.put(0x6A, Keyboard.KEY_MULTIPLY);
        KEY_MAP.put(0x6B, Keyboard.KEY_ADD);
        KEY_MAP.put(0x6D, Keyboard.KEY_SUBTRACT);
        KEY_MAP.put(0x6E, Keyboard.KEY_DECIMAL);
        KEY_MAP.put(0x6F, Keyboard.KEY_DIVIDE);

        // Function keys
        KEY_MAP.put(0x70, Keyboard.KEY_F1);
        KEY_MAP.put(0x71, Keyboard.KEY_F2);
        KEY_MAP.put(0x72, Keyboard.KEY_F3);
        KEY_MAP.put(0x73, Keyboard.KEY_F4);
        KEY_MAP.put(0x74, Keyboard.KEY_F5);
        KEY_MAP.put(0x75, Keyboard.KEY_F6);
        KEY_MAP.put(0x76, Keyboard.KEY_F7);
        KEY_MAP.put(0x77, Keyboard.KEY_F8);
        KEY_MAP.put(0x78, Keyboard.KEY_F9);
        KEY_MAP.put(0x79, Keyboard.KEY_F10);
        KEY_MAP.put(0x7A, Keyboard.KEY_F11);
        KEY_MAP.put(0x7B, Keyboard.KEY_F12);

        // Lock keys
        KEY_MAP.put(0x90, Keyboard.KEY_NUMLOCK);
        KEY_MAP.put(0x91, Keyboard.KEY_SCROLL);

        // OEM keys
        KEY_MAP.put(0xBA, Keyboard.KEY_SEMICOLON);
        KEY_MAP.put(0xBB, Keyboard.KEY_EQUALS);
        KEY_MAP.put(0xBC, Keyboard.KEY_COMMA);
        KEY_MAP.put(0xBD, Keyboard.KEY_MINUS);
        KEY_MAP.put(0xBE, Keyboard.KEY_PERIOD);
        KEY_MAP.put(0xBF, Keyboard.KEY_SLASH);
        KEY_MAP.put(0xC0, Keyboard.KEY_GRAVE);
        KEY_MAP.put(0xDB, Keyboard.KEY_LBRACKET);
        KEY_MAP.put(0xDC, Keyboard.KEY_BACKSLASH);
        KEY_MAP.put(0xDD, Keyboard.KEY_RBRACKET);
        KEY_MAP.put(0xDE, Keyboard.KEY_APOSTROPHE);
    }


    public static int map(int vk, long lParam) {

        boolean extended = (lParam & 0x1000000) != 0;


        // Shift
        if (vk == 0x10) {
            return extended ? Keyboard.KEY_RSHIFT : Keyboard.KEY_LSHIFT;
        }


        // Ctrl
        if (vk == 0x11) {
            return extended ? Keyboard.KEY_RCONTROL : Keyboard.KEY_LCONTROL;
        }


        // Alt
        if (vk == 0x12) {
            return extended ? Keyboard.KEY_RMENU : Keyboard.KEY_LMENU;
        }


        Integer key = KEY_MAP.get(vk);

        return key == null ? Keyboard.KEY_NONE : key;
    }
}