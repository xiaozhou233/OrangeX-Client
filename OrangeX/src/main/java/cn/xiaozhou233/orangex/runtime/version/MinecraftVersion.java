package cn.xiaozhou233.orangex.runtime.version;

import lombok.Getter;

/**
 * Minecraft versions supported by OrangeX.
 *
 * <p>The numbers are OrangeX's internal version ids, <strong>not</strong>
 * Minecraft network protocol numbers (for example, 1.8.9 is id 15 here, while
 * its network protocol is 47).</p>
 */
@Getter
public enum MinecraftVersion {
    V1_7_10(13),
    V1_8_9(15),
    V1_12_2(23),
    V1_14_4(28),
    V1_16_5(35),
    V1_16_5_ACTUAL(36),
    V1_17(37),
    V1_20_1(47),
    V1_20_6(50),
    V1_21_0(51),
    V1_21_1(52),
    V1_21_4(54),
    V1_21_5(55),
    V1_21_6(56),
    V1_21_10(60),
    V1_21_11(61),
    V26_1(100),
    V26_2(110),
    UNKNOWN(0);

    public static final int UNKNOWN_ID = 0;

    private final int id;

    MinecraftVersion(int id) {
        this.id = id;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public static MinecraftVersion fromId(int id) {
        for (MinecraftVersion version : values()) {
            if (version.id == id) {
                return version;
            }
        }
        return UNKNOWN;
    }
}
