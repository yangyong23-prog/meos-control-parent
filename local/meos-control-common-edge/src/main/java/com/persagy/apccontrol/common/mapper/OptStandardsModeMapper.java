package com.persagy.apccontrol.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.common.model.entity.OptDemandCustomConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 运行标准过渡 B1：只读查询 dtp-meos 的 {@code demand_custom_config} 中项目的 STANDRAD_CONFIG 记录。
 *
 * <p>该表归 dtp-meos（建表与写入都在 dtp-meos，见 docs/opt/table-ownership.md），我方只读，不建表、不写入。
 * 判定条件与 dtp-meos {@code LocalRedisCacheHandler} 加载 {@code LocalCacheConstant.MEOS_SERVER} 时完全一致：
 * project_id = object_id = 项目、module_code = CALCULATE、config_type = STANDRAD_CONFIG，存在记录即为已切新标准。
 * 走 MyBatis 以便沿用框架的多租户库名处理。
 *
 * <p>必须继承带实体泛型的 {@link BaseMapper}：框架分表拦截器对每条语句按 Mapper 泛型解析实体类，
 * 不带泛型时查询会失败（“[分表策略] 获取实体类失败”）。
 */
@Mapper
@Repository
public interface OptStandardsModeMapper extends BaseMapper<OptDemandCustomConfigDO> {

    @Select("SELECT COUNT(1) FROM demand_custom_config WHERE project_id = #{projectId} AND object_id = #{projectId}"
            + " AND module_code = 'CALCULATE' AND config_type = 'STANDRAD_CONFIG'")
    long countStandardConfig(@Param("projectId") String projectId);
}
