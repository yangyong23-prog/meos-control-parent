package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.config.dao.partition.AbstractPartitionTableStrategy;
import com.persagy.apccontrol.common.mapper.OptStandardsModeMapper;
import com.persagy.apccontrol.common.model.entity.OptDemandCustomConfigDO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 回归：分表拦截器按 Mapper 泛型解析实体类，OptStandardsModeMapper 必须能解析出实体，
 * 否则 4.91 上出现“[分表策略] 获取实体类失败”，/standards/mode 恒降级为 LEGACY。
 */
class OptStandardsModeMapperEntityTest {

    @Test
    void mapperResolvesEntityClass() {
        Assertions.assertEquals(OptDemandCustomConfigDO.class,
                AbstractPartitionTableStrategy.getEntityClass(OptStandardsModeMapper.class));
    }
}
