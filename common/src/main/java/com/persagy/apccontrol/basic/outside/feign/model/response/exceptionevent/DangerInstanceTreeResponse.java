package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.enums.MonitoringSituationsEnum;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.dto.EnumToObjectDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 风险事件实例树响应
 *
 * @author MuAnFu
 * @date 2025-09-25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DangerInstanceTreeResponse extends BaseResponse {
    /**
     * 风险事件实例树id
     */
    private String id;
    /**
     * 风险事件实例树名称
     */
    private String name;
    /**
     * 一级风控目标
     */
    private EnumToObjectDTO riskMeasureAimLevel1;
    /**
     * 二级风控目标
     */
    private EnumToObjectDTO riskMeasureAimLevel2;
    /**
     * 常见性
     */
    private EnumToObjectDTO ordinary;
    /**
     * 是否已关注(是否主动管理)
     */
    private Boolean followMark;
    /**
     * 超征兆问题等级
     */
    private EnumToObjectDTO symptomaticValueLevel;
    /**
     * 异常问题等级
     */
    private EnumToObjectDTO exceptionValueLevel;
    /**
     * 风险影响类别
     */
    private List<RiskImpactCategories> riskImpactCategories;
    /**
     * 风险事件实例树关联的RWD实例id
     */
    private String rwdInstanceId;
    /**
     * 策略覆盖比例
     */
    private Double strategyCoverRatio;
    /**
     * 监控情况
     */
    private List<MonitoringSituationsEnum> monitoringSituations;
}
