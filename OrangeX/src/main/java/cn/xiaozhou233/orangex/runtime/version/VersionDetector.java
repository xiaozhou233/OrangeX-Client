package cn.xiaozhou233.orangex.runtime.version;

import cn.xiaozhou233.orangex.runtime.version.probes.V189Probe;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class VersionDetector {
    @Getter
    private static List<ProbeScore> scores = null;
    // List of VersionProbe
    private static final List<VersionProbe> PROBES = Arrays.asList(
            new V189Probe()
    );


    // Detect minecraft version
    public static MinecraftVersion detect(ClassLoader loader) {
        // score all probes
        scores = score(loader);

        // The most suitable version
        MinecraftVersion bestVersion = MinecraftVersion.UNKNOWN;

        // highest score
        int bestScore = 0;

        // multiple versions have the same score
        boolean conflict = false;


        for (ProbeScore probeScore : scores) {
            int score = probeScore.getScore();

            // No features detected, skip
            if (score <= 0) {
                continue;
            }

            if (score > bestScore) {
                bestScore = score;
                bestVersion = probeScore.getVersion();

                // new best version, no conflict
                conflict = false;
            }


            // same score, conflict
            else if (score == bestScore) {

                conflict = true;
            }
        }


        // Failed: no features detected / multiple versions have the same score
        if (bestScore <= 0 || conflict) {
            return MinecraftVersion.UNKNOWN;
        }


        return bestVersion;
    }


    // Score all probes
    public static List<ProbeScore> score(ClassLoader loader) {

        List<ProbeScore> result = new ArrayList<ProbeScore>();

        for (VersionProbe probe : PROBES) {

            int score = probe.score(loader);

            result.add(new ProbeScore(
                    probe.getVersion(),
                    score
            ));
        }

        return Collections.unmodifiableList(result);
    }


    // Score result
    @Getter
    public static final class ProbeScore {
        private final MinecraftVersion version;
        private final int score;

        public ProbeScore(MinecraftVersion version, int score) {
            this.version = version;
            this.score = score;
        }

    }

    private VersionDetector() {
    }
}