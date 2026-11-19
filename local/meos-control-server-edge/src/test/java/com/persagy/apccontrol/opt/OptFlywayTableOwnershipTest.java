package com.persagy.apccontrol.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 运行标准过渡 B6：equip_record*、demand_custom_config、standard_compute_result* 归 dtp-meos。
 * 我方 Flyway 新增的脚本（版本高于 {@link #LEGACY_MAX}）不得建表、改表、写入这些表。
 * 版本不高于 LEGACY_MAX 的历史脚本已在现网执行过，改动会导致 Flyway 校验失败，只登记在
 * docs/opt/table-ownership.md，不在此处校验。
 */
class OptFlywayTableOwnershipTest {

    /** 本次改动时 meos_control 目录下最新的版本号 */
    private static final String LEGACY_MAX = "7.260701.0.2";

    private static final List<String> DTP_MEOS_TABLES = Arrays.asList(
            "equip_record", "demand_custom_config", "standard_compute_result");

    private static final Pattern VERSION = Pattern.compile("^V([0-9._]+)__.*\\.sql$");

    @Test
    void newMigrationsDoNotTouchDtpMeosTables() throws IOException {
        Path dir = Paths.get("src/main/resources/db/flyway");
        Assertions.assertTrue(Files.isDirectory(dir), "找不到 Flyway 目录：" + dir.toAbsolutePath());
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path f : files.filter(Files::isRegularFile).collect(Collectors.toList())) {
                String name = f.getFileName().toString();
                boolean repeatable = name.startsWith("R__");
                Matcher m = VERSION.matcher(name);
                if (!repeatable && !(m.matches() && compare(m.group(1), LEGACY_MAX) > 0)) {
                    continue;
                }
                String sql = new String(Files.readAllBytes(f), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
                for (String t : DTP_MEOS_TABLES) {
                    if (Pattern.compile("\\b" + t + "\\w*").matcher(sql).find()) {
                        violations.add(dir.relativize(f) + " -> " + t);
                    }
                }
            }
        }
        Assertions.assertTrue(violations.isEmpty(),
                "以下脚本涉及 dtp-meos 归属的表（见 docs/opt/table-ownership.md）：" + violations);
    }

    @Test
    void versionCompare() {
        Assertions.assertTrue(compare("7.260701.0.3", LEGACY_MAX) > 0);
        Assertions.assertTrue(compare("7.260801.0.0", LEGACY_MAX) > 0);
        Assertions.assertTrue(compare("6.0.0.43", LEGACY_MAX) < 0);
        Assertions.assertEquals(0, compare("7.260701.0.2", LEGACY_MAX));
    }

    static int compare(String a, String b) {
        String[] x = a.split("[._]");
        String[] y = b.split("[._]");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            long xi = i < x.length ? Long.parseLong(x[i]) : 0;
            long yi = i < y.length ? Long.parseLong(y[i]) : 0;
            if (xi != yi) {
                return Long.compare(xi, yi);
            }
        }
        return 0;
    }
}
