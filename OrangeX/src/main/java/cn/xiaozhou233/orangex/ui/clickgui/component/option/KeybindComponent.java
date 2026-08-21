package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;
import org.lwjgl.input.Keyboard;

public class KeybindComponent extends OptionComponent {

    private final Module module;
    private boolean listening;

    public KeybindComponent(Panel parent, Module module, int offsetX, int offsetY) {
        super(parent, null, offsetX, offsetY, 120, 16);
        this.module = module;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawString("Keybind", getX() + 4, getY() + 4, 0xffffffff);

        String keyName;
        if (listening) {
            keyName = "...";
        } else {
            keyName = module.getKey() != Keyboard.KEY_NONE
                    ? Keyboard.getKeyName(module.getKey()) : "NONE";
        }

        int keyX = getX() + width - 4 -
                (int) OrangeX.getInstance()
                        .getStbFontManager()
                        .getProxima(18)
                        .getStringWidth(keyName);
        int keyColor = listening ? 0xff55ffff : 0xffaaaaaa;
        drawString(keyName, keyX, getY() + 4, keyColor);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isHovered(mouseX, mouseY)) return;
        if (mouseButton == 0) {
            listening = true;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!listening) return;

        if (keyCode == 1) {
            listening = false;
            return;
        }

        if (keyCode == Keyboard.KEY_DELETE) {
            module.setKey(Keyboard.KEY_NONE);
            listening = false;
            return;
        }

        module.setKey(keyCode);
        listening = false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }
}