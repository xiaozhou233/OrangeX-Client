package cn.xiaozhou233.orangex.utils;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {

    private static final int MAX_ATTEMPTS = 8;

    public static double generateNoise(double min, double max) {
        double mean = (min + max) / 2.0;
        double range = max - min;
        double stdDev = Math.max(1.0, range / 3.0);

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            double value = random.nextGaussian() * stdDev + mean;
            if (value >= min && value <= max) {
                return value;
            }
        }

        // fallback: clamp gaussian
        return clamp(random.nextGaussian() * stdDev + mean, min, max);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
