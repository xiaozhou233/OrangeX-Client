package cn.xiaozhou233.orangex.input.v26_2;

import cn.xiaozhou233.orangex.input.ClientInput;
import cn.xiaozhou233.orangex.input.internal.VersionInput;
import cn.xiaozhou233.orangex.runtime.RuntimeInfo;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * 26.2 input implementation.
 *
 * <p>Loaded reflectively only on 26.2; the wrapper keeps 1.8.9 runtimes free
 * of these references.</p>
 */
public final class V26_2Input implements VersionInput {

    /** GLFW_KEY_M. */
    private static final int TOGGLE_KEY = 77;

    private final Minecraft minecraft;

    private Screen shellScreen;

    public V26_2Input(RuntimeInfo runtimeInfo, Object minecraft) {
        this.minecraft = (Minecraft) minecraft;
    }

    @Override
    public boolean isInWorld() {
        return minecraft.level != null;
    }

    @Override
    public boolean isInputScreenOpen() {
        return shellScreen != null && minecraft.gui.screen() == shellScreen;
    }

    @Override
    public boolean isScreenOpen() {
        return minecraft.gui.screen() != null;
    }

    @Override
    public boolean isInputScreenInstance(Object screen) {
        return shellScreen != null && shellScreen == screen;
    }

    @Override
    public void openInputScreen() {
        if (isInputScreenOpen()) {
            return;
        }
        shellScreen = new V26_2InputScreen();
        minecraft.gui.setScreen(shellScreen);
        ClientInput.onScreenOpened(getGuiMouseX(), getGuiMouseY());
    }

    @Override
    public void closeInputScreen() {
        if (!isInputScreenOpen()) {
            shellScreen = null;
            ClientInput.reset();
            return;
        }
        minecraft.gui.setScreen(null);
        shellScreen = null;
        ClientInput.reset();
    }

    @Override
    public boolean isToggleKeyDown() {
        return InputConstants.isKeyDown(minecraft.getWindow(), TOGGLE_KEY);
    }

    @Override
    public boolean isToggleEventDriven() {
        return false;
    }

    @Override
    public double getGuiMouseX() {
        return minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
    }

    @Override
    public double getGuiMouseY() {
        return minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
    }
}
