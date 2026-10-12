package com.persagy.apccontrol.basic.model.entity.strategy;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.config.dao.handler.SplitStringToListTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 策略日期表
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName(value = "strategy_date_cfg", autoResultMap = true)
public class StrategyDateCfg extends BaseDO implements Serializable {

    private static final long serialVersionUID = 6375392004960882703L;

    /**
     * 唯一ID
     */
    @TableId(value = "id")
    private String id;

    /**
     * 关联策略配置ID
     */
    @TableField(value = "strategy_id")
    private Long strategyId;

    /**
     * 策略日期
     */
    @TableField(value = "`date`")
    private LocalDate date;

    /**
     * 忽略计划ID
     */
    @TableField(value = "ignore_plan_ids", typeHandler = SplitStringToListTypeHandler.class)
    private List<String> ignorePlanIds;

    /**
     * 配置状态
     */
    @TableField(value = "st")
    private StateTypeEnum st;

    /**
     * 同步时间
     */
    @TableField(value = "sync_time")
    private LocalDateTime syncTime;

    /**
     * 同步信息
     */
    @TableField(value = "sync_message")
    private String syncMessage;

}