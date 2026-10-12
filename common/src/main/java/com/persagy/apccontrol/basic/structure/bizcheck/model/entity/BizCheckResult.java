package com.persagy.apccontrol.basic.structure.bizcheck.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *业务检查结果表实体类
 *对应表：biz_check_result
 *
 * @author wxf
 * @date 2026-02-27
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "biz_check_result")
public class BizCheckResult extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *业务条线编码
     */
    @TableField(value = "biz_code")
    private RunningMonitorTypeEnum bizCode;

    /**
     *检查项编码
     */
    @TableField(value = "check_item_code")
    private BizCheckStrategyTypeEnum checkItemCode;

    /**
     *检查状态
     */
    @TableField(value = "check_status")
    private BizCheckStatusEnum checkStatus;

    /**
     *检查类型
     */
    @TableField(value = "check_type")
    private BizCheckTypeEnum checkType;

    /**
     *检查项结果详情
     *JSON格式存储，包含检查项的详细结果信息
     */
    @TableField(value = "check_result")
    private String checkResult;

    /**
     *检查开始时间
     */
    @TableField(value = "check_start_time")
    private LocalDateTime checkStartTime;

    /**
     *检查结束时间
     */
    @TableField(value = "check_end_time")
    private LocalDateTime checkEndTime;

    /**
     *项目ID
     */
    @TableField(value = "project_id")
    private String projectId;
}