package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.module.option.Option;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;

@Getter
public abstract class OptionComponent extends Component {

    protected final Option<?> option;


    public OptionComponent(
            Panel parent,
            Option<?> option,
            int offsetX,
            int offsetY,
            int width,
            int height
    ) {
        super(parent,offsetX,offsetY,width,height);
        this.option = option;
    }

    protected void drawRect(
            int left,
            int top,
            int right,
            int bottom,
            int color
    ) {
        GlStateManager.resetColor();
        net.minecraft.client.gui.Gui.drawRect(
                left,
                top,
                right,
                bottom,
                color
        );
    }
}