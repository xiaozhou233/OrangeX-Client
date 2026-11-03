package cn.xiaozhou233.orangex.runtime.probe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Declarative description of the Minecraft entry class for one mapping set.
 *
 * <p>A version is recognized by locating {@link #minecraftClass()} and checking
 * that its singleton getter/field and a set of anchor classes all belong to the
 * same class loader. A signature therefore proves both the version and the
 * class loader that owns the game classes.</p>
 */
public final class MinecraftSignature {

    private final String minecraftClass;
    private final String instanceGetter;
    private final String instanceField;
    private final List<String> anchors;

    /**
     * @param minecraftClass game entry class, binary or internal name
     * @param instanceGetter static getter returning the singleton, or {@code null}
     * @param instanceField  static singleton field, or {@code null}
     * @param anchors        classes that must be defined by the same loader
     */
    public MinecraftSignature(String minecraftClass,
                              String instanceGetter,
                              String instanceField,
                              List<String> anchors) {
        this.minecraftClass = minecraftClass;
        this.instanceGetter = instanceGetter;
        this.instanceField = instanceField;
        this.anchors = Collections.unmodifiableList(new ArrayList<String>(anchors));
    }

    public static MinecraftSignature of(String minecraftClass,
                                        String instanceGetter,
                                        String instanceField,
                                        String... anchors) {
        return new MinecraftSignature(
                minecraftClass,
                instanceGetter,
                instanceField,
                Arrays.asList(anchors));
    }

    public String minecraftClass() {
        return minecraftClass;
    }

    public String instanceGetter() {
        return instanceGetter;
    }

    public String instanceField() {
        return instanceField;
    }

    public List<String> anchors() {
        return anchors;
    }
}
