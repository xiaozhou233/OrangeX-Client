package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.module.option.impl.DoubleOption;
import cn.xiaozhou233.orangex.module.option.impl.IntOption;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;

public class SliderComponent extends OptionComponent {

    private final double min;
    private final double max;

    private boolean dragging;


    public SliderComponent(
            Panel parent,
            IntOption option,
            int offsetX,
            int offsetY
    ) {
        super(parent,option,offsetX,offsetY,120,18);

        this.min = option.getMin();
        this.max = option.getMax();
    }

    public SliderComponent(
            Panel parent,
            DoubleOption option,
            int offsetX,
            int offsetY
    ) {
        super(parent,option,offsetX,offsetY,120,18);

        this.min = option.getMin();
        this.max = option.getMax();
    }


    @Override
    public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(getX(), getY(), getX() + width, getY() + height, 0xff252525);

        double value = getValue();

        double percent =
                (value - min) / (max - min);


        int barWidth = width - 10;


        drawString(
                option.getName(),
                getX() + 5,
                getY() + 3,
                0xffffffff
        );


        // Background bar
        drawRect(
                getX() + 5,
                getY() + 15,
                getX() + 5 + barWidth,
                getY() + 18,
                0xff555555
        );


        // Value bar
        drawRect(
                getX() + 5,
                getY() + 15,
                getX() + 5 + (int)(barWidth * percent),
                getY() + 18,
                0xff55ffff
        );


        drawString(
                String.valueOf(value),
                getX() + width - 25,
                getY() + 3,
                0xffaaaaaa
        );


        if(dragging)
            updateValue(mouseX);
    }


    @Override
    public void mouseClicked(int mouseX,int mouseY,int mouseButton) {

        if(mouseButton == 0 && isHovered(mouseX,mouseY)) {
            dragging = true;
            updateValue(mouseX);
        }
    }


    @Override
    public void mouseReleased(int mouseX,int mouseY,int mouseButton) {

        if(mouseButton == 0)
            dragging = false;
    }


    private void updateValue(int mouseX) {

        double percent =
                (mouseX - (getX() + 5))
                        / (double)(width - 10);


        percent = Math.max(0,Math.min(1,percent));


        double value =
                min + (max - min) * percent;


        if(option instanceof IntOption) {

            ((IntOption) option)
                    .setValue((int)value);

        } else if(option instanceof DoubleOption) {

            ((DoubleOption) option)
                    .setValue(value);
        }
    }


    private double getValue() {

        if(option instanceof IntOption) {

            return ((IntOption) option)
                    .getValue();

        }


        if(option instanceof DoubleOption) {

            return ((DoubleOption) option)
                    .getValue();

        }


        return 0;
    }
}