package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.module.option.impl.ColorOption;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;

import java.awt.*;

public class ColorComponent extends OptionComponent {

    private final ColorOption option;


    public ColorComponent(
            Panel parent,
            ColorOption option,
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


        drawRect(
                getX() + width - 25,
                getY() + 3,
                getX() + width - 5,
                getY() + 15,
                option.getValue()
        );
    }


    @Override
    public void mouseClicked(int mouseX,int mouseY,int mouseButton) {

        if(!isHovered(mouseX,mouseY))
            return;


        if(mouseButton == 0) {

            option.setValue(
                    getNextColor()
            );
        }
    }


    private int getNextColor() {

        int color = option.getValue();

        float[] hsb =
                Color.RGBtoHSB(
                        (color >> 16) & 255,
                        (color >> 8) & 255,
                        color & 255,
                        null
                );


        hsb[0] += 0.1f;


        if(hsb[0] > 1)
            hsb[0] = 0;


        return Color.HSBtoRGB(
                hsb[0],
                hsb[1],
                hsb[2]
        );
    }

}