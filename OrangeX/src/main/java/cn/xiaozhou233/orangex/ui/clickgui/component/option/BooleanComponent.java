package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.module.option.impl.BooleanOption;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;

public class BooleanComponent extends OptionComponent{

    private final BooleanOption option;


    public BooleanComponent(
            Panel parent,
            BooleanOption option,
            int offsetX,
            int offsetY
    ) {
        super(parent,option,offsetX,offsetY,120,18);
        this.option = option;
    }


    @Override
    public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(getX(), getY(), getX() + width, getY() + height, 0xff252525);

        drawString(
                option.getName(),
                getX() + 5,
                getY() + 5,
                0xffffffff
        );


        drawString(
                option.getValue() ? "ON" : "OFF",
                getX() + width - 25,
                getY() + 5,
                option.getValue()
                        ? 0xff55ffff
                        : 0xffffffff
        );
    }


    @Override
    public void mouseClicked(int mouseX,int mouseY,int mouseButton) {

        if (!isHovered(mouseX,mouseY))
            return;


        if (mouseButton == 0) {
            option.setValue(!option.getValue());
        }
    }
}