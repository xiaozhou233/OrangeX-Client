package cn.xiaozhou233.orangex.alts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter @Setter
public class Alt {
    private String name;
    private UUID uuid;
    private String accessToken;
    private long expireTimeMs;
}
