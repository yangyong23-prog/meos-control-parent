package com.persagy.apccontrol.basic.model.entity.strategy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 策略配置表
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName(value = "strategy_cfg")
public class StrategyCfg extends BaseDO implements Serializable {

    private static final long serialVersionUID = 2746942412377774051L;

    /**
     * 唯一ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 系统类型
     */
    @TableField(value = "system_type")
    private DtpRwdSystemClassCodeEnum systemType;

    /**
     * 系统ID
     */
    @TableField(value = "system_id")
    private String systemId;

    /**
     * 业务类型
     */
    @TableField(value = "business_type")
    private StrategyBusinessTypeEnum businessType;

    /**
     * 策略类型
     */
    @TableField(value = "strategy_type")
    private StrategyTypeEnum strategyType;

    /**
     * 对象ID
     */
    @TableField(value = "object_id")
    private String objectId;

    /**
     * 设备类编码
     */
    @TableField(value = "class_code")
    private String classCode;

    /**
     * 配置状态
     */
    @TableField(value = "st")
    private StateTypeEnum st;

    /**
     * 备注说明
     */
    @TableField(value = "memo")
    private String memo;

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