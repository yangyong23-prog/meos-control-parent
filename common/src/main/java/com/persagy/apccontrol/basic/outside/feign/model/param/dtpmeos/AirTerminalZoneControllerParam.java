package com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 查询-冰蓄冷需求计算结果参数
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class AirTerminalZoneControllerParam {

    /**
     * 项目ID
     */
    @NotBlank(message = "Project id can not be blank")
    private String projectId = ContextUtil.getProjectId();

    /**
     * 系统ID
     */
    @NotBlank(message = "System id can not be blank")
    private String systemId;

    /**
     * 系统类型
     */
    @NotNull(message = "System type can not be blank")
    private DtpRwdClassCodeEnum systemType;

    /**
     * 开始时间
     */
    @NotNull(message = "Begin date can not be blank")
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    @NotNull(message = "End date can not be blank")
    private LocalDate endDate;

}
