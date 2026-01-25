package cn.xiaozhou233.orangex.utils;

import java.util.Random;

public class MathUtil {

    private static final Random random = new Random();

    public static double generateNoise(double min, double max) {
        double mean = (max + min) / 2;
        double stdDev = Math.max(1.0, (max - min) / 3); // stdDev >= 1

        double value;
        do {
            value = random.nextGaussian() * stdDev + mean;
        } while (value < min || value > max);

        return value;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
