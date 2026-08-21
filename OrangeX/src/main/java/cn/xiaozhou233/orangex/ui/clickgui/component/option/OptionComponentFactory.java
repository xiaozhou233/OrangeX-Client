package cn.xiaozhou233.orangex.ui.clickgui.component.option;

import cn.xiaozhou233.orangex.module.option.Option;
import cn.xiaozhou233.orangex.module.option.impl.*;
import cn.xiaozhou233.orangex.ui.clickgui.Panel;

public class OptionComponentFactory {

    public static OptionComponent create(
            Panel panel,
            Option<?> option,
            int x,
            int y
    ) {

        if (option instanceof BooleanOption) {
            return new BooleanComponent(
                    panel,
                    (BooleanOption) option,
                    x,
                    y
            );
        }

        if (option instanceof IntOption) {
            return new SliderComponent(
                    panel,
                    (IntOption) option,
                    x,
                    y
            );
        }

        if (option instanceof DoubleOption) {
            return new SliderComponent(
                    panel,
                    (DoubleOption) option,
                    x,
                    y
            );
        }

        if (option instanceof ModeOption) {
            return new ModeComponent(
                    panel,
                    (ModeOption) option,
                    x,
                    y
            );
        }

        if (option instanceof ColorOption) {
            return new ColorComponent(
                    panel,
                    (ColorOption) option,
                    x,
                    y
            );
        }

        if (option instanceof StringOption) {
            return new StringComponent(
                    panel,
                    (StringOption) option,
                    x,
                    y
            );
        }

        return null;
    }
}