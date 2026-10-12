package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_basics_config(配置日志表)】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcBasicsConfig
 */
@Mapper
@Repository
public interface RcBasicsConfigMapper extends SuperMapper<RcBasicsConfig> {

    List<RcBasicsConfig> queryListConfig(@Param("projectId") String projectId, @Param("type") BasicsConfigTypeEnum type,
                                         @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<RcBasicsConfig> queryListConfig(@Param("projectId") String projectId, @Param("systemId") String systemId, @Param("type") BasicsConfigTypeEnum type,
                                         @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<RcBasicsConfig> querySyn(@Param("projectId") String projectId, @Param("type") List<BasicsConfigTypeEnum> type);

    /**
     * 按缓存对象模型状态固定主键幂等写入基础配置。
     *
     * @param config 缓存对象模型状态配置
     * @return 影响行数
     * @since v7.260701.0
     */
    int upsertCacheObjectModelStatus(
            @NonNull @Param("config") RcBasicsConfig config);

    /**
     * 编辑温湿度目标忽略空间ID
     *
     * @param ew                   查询条件
     * @param ignoreSpaceIdsString 忽略空间ID字符串
     * @return 操作影响条数
     * @since v5.1.0
     */
    int editHumitureIgnoreSpaceIds(
            @NonNull @Param("ew") AbstractWrapper<?, ?, ?> ew,
            @NonNull @Param("ignoreSpaceIdsString") String ignoreSpaceIdsString);

}




