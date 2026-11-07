package cn.xiaozhou233.orangex.input;

/**
 * Canonical key codes used by {@link cn.xiaozhou233.orangex.event.impl.EventKey}.
 *
 * <p>All events use the 1.8.9 / LWJGL 2 numbering, regardless of the running
 * Minecraft version. Modern versions report GLFW key codes, so
 * {@link #fromGlfw(int)} translates them before the event is posted. Keys that
 * did not exist in 1.8.9 map to {@link #NONE}.</p>
 */
public final class KeyCodes {

    /** LWJGL 2 {@code Keyboard.KEY_NONE}. */
    public static final int NONE = 0;

    /** LWJGL 2 {@code Keyboard.KEY_M}. */
    public static final int M = 50;

    /** LWJGL 2 {@code Keyboard.KEY_RSHIFT}. */
    public static final int RSHIFT = 54;

    /**
     * Translates a GLFW key code (26.x) to the 1.8.9 LWJGL 2 key code used by
     * {@code EventKey}.
     */
    public static int fromGlfw(int key) {
        switch (key) {
            // Printable keys.
            case 32: return 57;  // SPACE
            case 39: return 40;  // APOSTROPHE
            case 44: return 51;  // COMMA
            case 45: return 12;  // MINUS
            case 46: return 52;  // PERIOD
            case 47: return 53;  // SLASH
            case 48: return 11;  // 0
            case 49: return 2;   // 1
            case 50: return 3;   // 2
            case 51: return 4;   // 3
            case 52: return 5;   // 4
            case 53: return 6;   // 5
            case 54: return 7;   // 6
            case 55: return 8;   // 7
            case 56: return 9;   // 8
            case 57: return 10;  // 9
            case 59: return 39;  // SEMICOLON
            case 61: return 13;  // EQUAL
            case 65: return 30;  // A
            case 66: return 48;  // B
            case 67: return 46;  // C
            case 68: return 32;  // D
            case 69: return 18;  // E
            case 70: return 33;  // F
            case 71: return 34;  // G
            case 72: return 35;  // H
            case 73: return 23;  // I
            case 74: return 36;  // J
            case 75: return 37;  // K
            case 76: return 38;  // L
            case 77: return 50;  // M
            case 78: return 49;  // N
            case 79: return 24;  // O
            case 80: return 25;  // P
            case 81: return 16;  // Q
            case 82: return 19;  // R
            case 83: return 31;  // S
            case 84: return 20;  // T
            case 85: return 22;  // U
            case 86: return 47;  // V
            case 87: return 17;  // W
            case 88: return 45;  // X
            case 89: return 21;  // Y
            case 90: return 44;  // Z
            case 91: return 26;  // LEFT_BRACKET
            case 92: return 43;  // BACKSLASH
            case 93: return 27;  // RIGHT_BRACKET
            case 96: return 41;  // GRAVE_ACCENT

            // Function keys.
            case 256: return 1;    // ESCAPE
            case 257: return 28;   // ENTER
            case 258: return 15;   // TAB
            case 259: return 14;   // BACKSPACE
            case 260: return 210;  // INSERT
            case 261: return 211;  // DELETE
            case 262: return 205;  // RIGHT
            case 263: return 203;  // LEFT
            case 264: return 208;  // DOWN
            case 265: return 200;  // UP
            case 266: return 201;  // PAGE_UP
            case 267: return 209;  // PAGE_DOWN
            case 268: return 199;  // HOME
            case 269: return 207;  // END
            case 280: return 58;   // CAPS_LOCK
            case 281: return 70;   // SCROLL_LOCK
            case 282: return 69;   // NUM_LOCK
            case 283: return 183;  // PRINT_SCREEN
            case 284: return 197;  // PAUSE

            case 290: return 59;   // F1
            case 291: return 60;   // F2
            case 292: return 61;   // F3
            case 293: return 62;   // F4
            case 294: return 63;   // F5
            case 295: return 64;   // F6
            case 296: return 65;   // F7
            case 297: return 66;   // F8
            case 298: return 67;   // F9
            case 299: return 68;   // F10
            case 300: return 87;   // F11
            case 301: return 88;   // F12
            case 302: return 100;  // F13
            case 303: return 101;  // F14
            case 304: return 102;  // F15
            case 305: return 103;  // F16
            case 306: return 104;  // F17
            case 307: return 105;  // F18
            case 308: return 113;  // F19

            // Keypad.
            case 320: return 82;   // KP_0
            case 321: return 79;   // KP_1
            case 322: return 80;   // KP_2
            case 323: return 81;   // KP_3
            case 324: return 75;   // KP_4
            case 325: return 76;   // KP_5
            case 326: return 77;   // KP_6
            case 327: return 71;   // KP_7
            case 328: return 72;   // KP_8
            case 329: return 73;   // KP_9
            case 330: return 83;   // KP_DECIMAL
            case 331: return 181;  // KP_DIVIDE
            case 332: return 55;   // KP_MULTIPLY
            case 333: return 74;   // KP_SUBTRACT
            case 334: return 78;   // KP_ADD
            case 335: return 156;  // KP_ENTER
            case 336: return 141;  // KP_EQUAL

            // Modifiers.
            case 340: return 42;   // LEFT_SHIFT
            case 341: return 29;   // LEFT_CONTROL
            case 342: return 56;   // LEFT_ALT
            case 343: return 219;  // LEFT_SUPER
            case 344: return 54;   // RIGHT_SHIFT
            case 345: return 157;  // RIGHT_CONTROL
            case 346: return 184;  // RIGHT_ALT
            case 347: return 220;  // RIGHT_SUPER
            case 348: return 221;  // MENU

            default:
                return NONE;
        }
    }

    private KeyCodes() {
    }
}
