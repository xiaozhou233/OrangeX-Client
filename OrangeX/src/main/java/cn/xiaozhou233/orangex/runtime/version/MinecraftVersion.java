package cn.xiaozhou233.orangex.runtime.version;

import lombok.Getter;
import lombok.Setter;

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

    @Setter
    @Getter
    public static int currentProtocolVersion;
    @Getter
    private final int protocolVersion;

    MinecraftVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean is(int current) {
        return current == this.protocolVersion;
    }

    public boolean isNot(int current) {
        return current != this.protocolVersion;
    }

    public boolean greaterThan(int current) {
        return current > this.protocolVersion;
    }

    public boolean greaterThanOrEqual(int current) {
        return current >= this.protocolVersion;
    }

    public boolean lessThan(int current) {
        return current < this.protocolVersion;
    }

    public boolean lessThanOrEqual(int current) {
        return current <= this.protocolVersion;
    }

    public static MinecraftVersion fromProtocolId(int protocolId) {
        for (MinecraftVersion version : values()) {
            if (version.protocolVersion == protocolId) {
                return version;
            }
        }
        return UNKNOWN;
    }
}