package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.config.RedisKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * B2：共享缓存 key 与仓内已有的 RedisKey.buildIotRealKey 一致（rcRunStatus 也写在同一命名空间）。
 */
public class OptRealtimeKeyCompatTest {

    @Test
    void sameKeyAsRedisKey() {
        Assertions.assertEquals(RedisKey.buildIotRealKey("Eq1", "supplyTemp"), OptRealtimeCacheCore.key("Eq1", "supplyTemp"));
    }
}
