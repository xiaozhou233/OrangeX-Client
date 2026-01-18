package cn.xiaozhou233.orangex.module.value;

public class NumberValue extends Value<Double> {

    private final double min;
    private final double max;
    private final double increment;

    public NumberValue(String name, String description, Double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }
}
