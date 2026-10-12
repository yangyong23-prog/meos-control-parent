package com.persagy.apccontrol.basic.model.dto.strategy;

import com.persagy.apccontrol.basic.model.entity.strategy.StrategyCfg;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyDateCfg;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyPlanCfg;
import com.persagy.validate.utility.executor.ObjectExecutor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 策略同步DTO
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@Schema(title = "策略同步DTO")
public class StrategySyncDTO {

    // ------------------------- 保存/更新 -------------------------

    @Schema(description = "策略配置")
    private List<StrategyCfg> strategies;

    @Schema(description = "策略日期配置")
    private List<StrategyDateCfg> strategyDateList;

    @Schema(description = "策略计划配置")
    private List<StrategyPlanCfg> strategyPlans;

    // ------------------------- 删除 -------------------------

    @Schema(description = "待删除的策略配置ID")
    private Set<Long> delStrategyIds;

    @Schema(description = "待删除的策略日期配置ID")
    private Set<String> delStrategyDateIds;

    @Schema(description = "待删除的策略计划配置ID")
    private Set<Long> delStrategyPlanIds;

    /**
     * 填充默认数据
     *
     * @return 当前实例
     * @since v5.7.0
     */
    public StrategySyncDTO fillDefaultData() {
        this.strategies = Optional.ofNullable(this.strategies).orElseGet(ArrayList::new);
        this.strategyDateList = Optional.ofNullable(this.strategyDateList).orElseGet(ArrayList::new);
        this.strategyPlans = Optional.ofNullable(this.strategyPlans).orElseGet(ArrayList::new);
        this.delStrategyIds = Optional.ofNullable(this.delStrategyIds).orElseGet(HashSet::new);
        this.delStrategyDateIds = Optional.ofNullable(this.delStrategyDateIds).orElseGet(HashSet::new);
        this.delStrategyPlanIds = Optional.ofNullable(this.delStrategyPlanIds).orElseGet(HashSet::new);
        return this;
    }

    /**
     * 同步前置操作
     *
     * @since v5.7.0
     */
    public void beforeSync() {
        fillSyncInfo(LocalDateTime.now(), "OK");
    }

    /**
     * 同步失败操作
     *
     * @since v5.7.0
     */
    public void failedSync(String failedMessage) {
        fillSyncInfo(null, failedMessage);
    }

    /**
     * 填充同步信息
     *
     * @param syncTime    同步时间
     * @param syncMessage 同步信息
     * @since v5.7.0
     */
    private void fillSyncInfo(@Nullable LocalDateTime syncTime, @NonNull String syncMessage) {
        ObjectExecutor.notEmptyExecute(strategies, () -> strategies.forEach(item -> {
            item.setSyncTime(syncTime);
            item.setSyncMessage(syncMessage);
        }));
        ObjectExecutor.notEmptyExecute(strategyDateList, () -> strategyDateList.forEach(item -> {
            item.setSyncTime(syncTime);
            item.setSyncMessage(syncMessage);
        }));
        ObjectExecutor.notEmptyExecute(strategyPlans, () -> strategyPlans.forEach(item -> {
            item.setSyncTime(syncTime);
            item.setSyncMessage(syncMessage);
        }));
    }
}
