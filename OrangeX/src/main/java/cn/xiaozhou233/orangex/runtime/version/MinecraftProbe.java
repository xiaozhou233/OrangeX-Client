package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.ClassProbe;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MinecraftProbe {
    private final String minecraftClass;
    private final String minecraftGetter;
    private final String minecraftInstanceField;
    private final String[] minecraftAnchors;

    public MinecraftProbe(String minecraftClass, String minecraftGetter, String minecraftInstanceField, String... minecraftAnchors) {
        this.minecraftClass = minecraftClass;
        this.minecraftGetter = minecraftGetter;
        this.minecraftInstanceField = minecraftInstanceField;
        this.minecraftAnchors = minecraftAnchors;
    }

    public ProbeResult probe(ClassLoader loader) {
        int score = 0;
        List<String> matched = new ArrayList<>();

        // Class Check
        Class<?> mcClass = ClassProbe.find(loader, minecraftClass);
        if (mcClass != null) {
            score += 10;
            matched.add(minecraftClass + " found + 10");
        } else {
            return new ProbeResult(score, matched);
        }

        // Method(Getter) Check
        try {
            mcClass.getDeclaredMethod(minecraftGetter);
            score += 10;
            matched.add(minecraftGetter + " found + 10");
        } catch (NoSuchMethodException e) {
            return new ProbeResult(score, matched);
        }

        // Instance Field Check (Tips: Some version of minecraft does not have this field)
        try {
            mcClass.getDeclaredField(minecraftInstanceField);
            score += 5;
            matched.add(minecraftInstanceField + " found + 5");
        } catch (NoSuchFieldException ignore) {

        }

        // Anchor Check
        for (String anchor : minecraftAnchors) {
            if (ClassProbe.exists(loader, anchor)) {
                score += 10;
                matched.add(anchor + " found + 10");
            }
        }

        return new ProbeResult(score, matched);
    }

    @AllArgsConstructor
    @Getter
    public static class ProbeResult {
        private final int score;
        private final List<String> matched;
    }
}
