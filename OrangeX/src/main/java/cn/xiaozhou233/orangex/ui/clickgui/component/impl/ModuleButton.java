package cn.xiaozhou233.orangex.ui.clickgui.component.impl;

import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.option.Option;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;
import cn.xiaozhou233.orangex.ui.clickgui.component.Component;
import cn.xiaozhou233.orangex.ui.clickgui.component.option.OptionComponent;
import cn.xiaozhou233.orangex.ui.clickgui.component.option.OptionComponentFactory;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Component {

    private final Module module;

    @Getter
    private boolean expanded;
    @Getter
    private final List<OptionComponent> options = new ArrayList<>();

    public ModuleButton(Panel parent,Module module,int offsetX,int offsetY) {
        super(parent,offsetX,offsetY,120,20);

        this.module = module;

        int optionY = 20;

        for (Option<?> option : module.getOptions()) {

            OptionComponent component =
                    OptionComponentFactory.create(
                            parent,
                            option,
                            offsetX,
                            offsetY + optionY
                    );

            if(component != null)
                options.add(component);

            optionY += 18;
        }
    }


    @Override
    public void drawScreen(int mouseX,int mouseY,float partialTicks) {

        int color = module.isEnabled()
                ? 0xff55ffff
                : 0xffffffff;

        drawString(
                module.getName(),
                getX() + 5,
                getY() + 5,
                color
        );


        if(expanded) {
            for(OptionComponent option : options) {
                option.drawScreen(mouseX,mouseY,partialTicks);
            }
        }
    }


    private void drawOptions(int mouseX,int mouseY,float partialTicks) {

        for(OptionComponent option : options) {
            option.drawScreen(mouseX,mouseY,partialTicks);
        }
    }


    @Override
    public void mouseClicked(int mouseX,int mouseY,int mouseButton) {

        if(isHovered(mouseX,mouseY)) {

            if(mouseButton == 0)
                module.toggle();

            if(mouseButton == 1) {

                expanded = !expanded;

                parent.updateLayout();
            }
        }


        if(expanded) {
            for(OptionComponent option : options) {
                option.mouseClicked(mouseX,mouseY,mouseButton);
            }
        }
    }


    private void mouseClickedOptions(int mouseX,int mouseY,int mouseButton) {

        for(OptionComponent option : options) {
            option.mouseClicked(mouseX,mouseY,mouseButton);
        }
    }


    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

        if (expanded) {
            mouseReleasedOptions(mouseX,mouseY,mouseButton);
        }
    }


    private void mouseReleasedOptions(int mouseX,int mouseY,int mouseButton) {

    }


    @Override
    public void keyTyped(char typedChar, int keyCode) {

        if (expanded) {
            keyTypedOptions(typedChar,keyCode);
        }
    }


    private void keyTypedOptions(char typedChar,int keyCode) {

    }


    protected void drawString(String text,int x,int y,int color) {

    }

    @Override
    public int getHeight() {

        if(!expanded)
            return 20;


        return 20 + options.size() * 18;
    }

}