package cn.xiaozhou233.orangex.runtime;

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

    public boolean probe(ClassLoader loader) {
        // Class Check
        Class<?> mcClass = ClassProbe.find(loader, minecraftClass);
        if (mcClass == null) {
            return false;
        }

        // Method(Getter) Check
        try {
            mcClass.getDeclaredMethod(minecraftGetter);
        } catch (NoSuchMethodException e) {
            return false;
        }

        // Instance Field Check (Tips: Some version of minecraft does not have this field)
        try {
            mcClass.getDeclaredField(minecraftInstanceField);
        } catch (NoSuchFieldException ignore) {
            // empty -> don't check (does not have minecraft field)
            // not empty -> return false
            System.out.println("[DEBUG] minecraftInstanceField: " + minecraftInstanceField + " -> " + !minecraftInstanceField.isEmpty());
            if(!minecraftInstanceField.isEmpty())
                return false;
        }

        // Anchor Check
        for (String anchor : minecraftAnchors) {
            if (!ClassProbe.exists(loader, anchor)) {
                return false;
            }
        }

        return true;
    }
}
