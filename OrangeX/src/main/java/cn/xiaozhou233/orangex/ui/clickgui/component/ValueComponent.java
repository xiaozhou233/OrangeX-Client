package cn.xiaozhou233.orangex.ui.clickgui.component;

public abstract class ValueComponent extends Component {

    public ValueComponent(double x, double y, double width, double height, Component parent) {
        super(x, y, width, height, parent);
    }

    public abstract void applyValue();
}
