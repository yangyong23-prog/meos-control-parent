package com.persagy.apccontrol.basic.resource;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyDateCfg;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyDateCfgQueryDaoParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 策略日期表服务
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
public interface StrategyDateCfgService extends IService<StrategyDateCfg> {

    // ------------------------- 统计相关 -------------------------

    /**
     * 查询数量
     *
     * @param param 查询参数
     * @return 数量
     * @since v5.6.0
     */
    int count(@NonNull @Valid StrategyDateCfgQueryDaoParam param);

    /**
     * 统计已配置的日期
     *
     * @param strategyIds 策略ID
     * @return 日期列表
     * @since v5.6.0
     */
    List<LocalDate> statisticDateInValid(@NonNull Collection<Long> strategyIds);

    // ------------------------- 查询相关 -------------------------

    /**
     * 查询实体ID列表
     *
     * @param param 查询参数
     * @return 实体ID列表
     * @since v5.7.0
     */
    List<String> listIds(@NonNull @Valid StrategyDateCfgQueryDaoParam param);

    /**
     * 查询实体列表
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.6.0
     */
    List<StrategyDateCfg> list(@NonNull @Valid StrategyDateCfgQueryDaoParam param);

    /**
     * 查询未被同步实体
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.7.0
     */
    List<StrategyDateCfg> listNotSynced(@NonNull StrategyDateCfgQueryDaoParam param);

    // ------------------------- 删除相关 -------------------------

    /**
     * 根据策略ID删除
     *
     * @param strategyIds 策略ID
     * @return 删除条数
     * @since v5.6.0
     */
    int delByStrategyId(@NonNull Collection<Long> strategyIds);

    /**
     * 生成主键ID
     *
     * @param projectId 项目ID
     * @param systemId  系统ID
     * @param objectId  对象ID
     * @param date      日期
     * @return 主键ID
     * @since v5.6.0
     */
    default String generateId(
            @NonNull String projectId, @NonNull String systemId,
            @NonNull String objectId, @NonNull LocalDate date) {
        return generateId(projectId, systemId, objectId, date, null);
    }

    /**
     * 生成主键ID
     *
     * @param projectId 项目ID
     * @param systemId  系统ID
     * @param objectId  对象ID
     * @param date      日期
     * @param status    状态
     * @return 主键ID
     * @since v5.6.0
     */
    default String generateId(
            @NonNull String projectId, @NonNull String systemId,
            @NonNull String objectId, @NonNull LocalDate date,
            @Nullable StateTypeEnum status) {
        return DigestUtils.md5Hex(projectId + systemId + objectId + date
                + Optional.ofNullable(status).orElse(StateTypeEnum.VALID));
    }
}
