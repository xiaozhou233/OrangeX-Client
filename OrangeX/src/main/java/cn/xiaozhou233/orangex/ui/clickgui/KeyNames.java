package cn.xiaozhou233.orangex.ui.clickgui;

import cn.xiaozhou233.orangex.input.KeyCodes;

/**
 * Maps raw GLFW-style key codes to the short labels shown in the ClickGUI.
 */
public final class KeyNames {

    private KeyNames() {
    }

    public static String keyName(int key) {
        if (key == KeyCodes.NONE) {
            return "NONE";
        }

        switch (key) {
            case 1: return "ESC";
            case 2: return "1";
            case 3: return "2";
            case 4: return "3";
            case 5: return "4";
            case 6: return "5";
            case 7: return "6";
            case 8: return "7";
            case 9: return "8";
            case 10: return "9";
            case 11: return "0";
            case 12: return "MINUS";
            case 13: return "EQUAL";
            case 14: return "BACKSPACE";
            case 15: return "TAB";
            case 16: return "Q";
            case 17: return "W";
            case 18: return "E";
            case 19: return "R";
            case 20: return "T";
            case 21: return "Y";
            case 22: return "U";
            case 23: return "I";
            case 24: return "O";
            case 25: return "P";
            case 26: return "[";
            case 27: return "]";
            case 28: return "ENTER";
            case 29: return "LCTRL";
            case 30: return "A";
            case 31: return "S";
            case 32: return "D";
            case 33: return "F";
            case 34: return "G";
            case 35: return "H";
            case 36: return "J";
            case 37: return "K";
            case 38: return "L";
            case 39: return ";";
            case 40: return "'";
            case 41: return "`";
            case 42: return "LSHIFT";
            case 43: return "\\";
            case 44: return "Z";
            case 45: return "X";
            case 46: return "C";
            case 47: return "V";
            case 48: return "B";
            case 49: return "N";
            case 50: return "M";
            case 51: return ",";
            case 52: return ".";
            case 53: return "/";
            case 54: return "RSHIFT";
            case 55: return "NUMPAD*";
            case 56: return "LALT";
            case 57: return "SPACE";
            case 58: return "CAPS";
            case 69: return "NUMLOCK";
            case 70: return "SCROLL";
            case 74: return "NUMPAD-";
            case 78: return "NUMPAD+";
            case 82: return "NUMPAD0";
            case 79: return "NUMPAD1";
            case 80: return "NUMPAD2";
            case 81: return "NUMPAD3";
            case 75: return "NUMPAD4";
            case 76: return "NUMPAD5";
            case 77: return "NUMPAD6";
            case 71: return "NUMPAD7";
            case 72: return "NUMPAD8";
            case 73: return "NUMPAD9";
            case 87: return "F11";
            case 88: return "F12";
            case 157: return "RCTRL";
            case 184: return "RALT";
            case 200: return "UP";
            case 203: return "LEFT";
            case 208: return "DOWN";
            case 205: return "RIGHT";
            case 210: return "INSERT";
            case 211: return "DELETE";
            case 199: return "HOME";
            case 207: return "END";
            case 201: return "PAGE UP";
            case 209: return "PAGE DOWN";
            default:
                break;
        }

        if (key >= 59 && key <= 68) {
            return "F" + (key - 58);
        }
        return "KEY " + key;
    }
}
