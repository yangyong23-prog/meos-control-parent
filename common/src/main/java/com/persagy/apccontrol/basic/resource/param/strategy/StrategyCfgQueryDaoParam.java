package com.persagy.apccontrol.basic.resource.param.strategy;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 策略配置查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Accessors(chain = true)
public class StrategyCfgQueryDaoParam {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 系统类型
     */
    private DtpRwdSystemClassCodeEnum systemType;

    /**
     * 系统ID
     */
    private String systemId;

    /**
     * 业务类型
     */
    private StrategyBusinessTypeEnum businessType;

    /**
     * 策略类型
     */
    private StrategyTypeEnum strategyType;

    /**
     * 对象ID
     */
    private String objectId;

    /**
     * 状态
     */
    private StateTypeEnum status = StateTypeEnum.VALID;

}
