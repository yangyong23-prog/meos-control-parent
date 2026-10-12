package com.persagy.apccontrol.basic.model.entity.strategy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 策略计划表
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName(value = "strategy_plan_cfg")
public class StrategyPlanCfg extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1535618972319201422L;

    /**
     * 唯一ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联策略配置ID
     */
    @TableField(value = "strategy_id")
    private Long strategyId;

    /**
     * 策略生效时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 策略失效时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 计划数据
     */
    @TableField(value = "`result`")
    private String result;

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