package com.persagy.apccontrol.basic.structure.bizcheck.context;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.bizcheck.NotifyPeriodConfigParam;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckConfig;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-27 18:02
 **/
@Data
@Builder
public class BizCheckContext {

    /**
     * 业务条线编码
     */
    private RunningMonitorTypeEnum bizCode;

    /**
     * 检查日期
     */
    private LocalDate checkDate;

    private List<NotifyPeriodConfigParam> notifyPeriodConfigs;
}