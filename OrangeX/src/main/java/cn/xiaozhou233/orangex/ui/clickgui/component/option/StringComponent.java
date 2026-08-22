package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.OrangeX;
import cn.xiaozhou233.orangex.module.option.impl.StringOption;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;


public class StringComponent extends OptionComponent {

    private final StringOption option;

    private boolean typing;


    public StringComponent(
            Panel parent,
            StringOption option,
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


        String value = option.getValue();

        if(typing)
            value += "_";


        int maxValueWidth = 50;
        int valueWidth = (int) OrangeX.getInstance()
                .getStbFontManager()
                .getProxima(18)
                .getStringWidth(value);

        if (valueWidth > maxValueWidth) {
            while ((int) OrangeX.getInstance()
                    .getStbFontManager()
                    .getProxima(18)
                    .getStringWidth("..." + value) > maxValueWidth
                    && value.length() > 0) {
                value = value.substring(1);
            }
            value = "..." + value;
        }


        drawString(
                value,
                getX() + width - 5 - maxValueWidth,
                getY() + 5,
                typing
                        ? 0xff55ffff
                        : 0xffaaaaaa
        );


        
    }


    @Override
    public void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {
        if (typing && !isHovered(mouseX, mouseY)) {
            typing = false;
            return;
        }

        if (!isHovered(mouseX,mouseY))
            return;

        if(mouseButton == 0) {
            typing = true;
        }
    }


    @Override
    public void keyTyped(
            char typedChar,
            int keyCode
    ) {

        if(!typing)
            return;


        // Backspace
        if(keyCode == 14) {

            String value = option.getValue();

            if(value.length() > 0) {
                option.setValue(
                        value.substring(
                                0,
                                value.length() - 1
                        )
                );
            }
            return;
        }


        // Enter
        if(keyCode == 28) {
            typing = false;
            return;
        }


        if(Character.isISOControl(typedChar))
            return;


        option.setValue(
                option.getValue() + typedChar
        );
    }


    @Override
    public void mouseReleased(
            int mouseX,
            int mouseY,
            int mouseButton
    ) {

    }
}