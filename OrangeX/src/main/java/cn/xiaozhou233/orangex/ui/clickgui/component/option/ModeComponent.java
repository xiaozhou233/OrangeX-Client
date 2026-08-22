package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.module.option.impl.ModeOption;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;

public class ModeComponent extends OptionComponent {

    private final ModeOption option;


    public ModeComponent(
            Panel parent,
            ModeOption option,
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
                option.getValue(),
                getX() + width - 45,
                getY() + 5,
                0xff55ffff
        );
    }


    @Override
    public void mouseClicked(int mouseX,int mouseY,int mouseButton) {

        if (!isHovered(mouseX,mouseY))
            return;


        if(mouseButton == 0) {

            option.next();
        }


        if(mouseButton == 1) {

            option.previous();
        }
    }
}