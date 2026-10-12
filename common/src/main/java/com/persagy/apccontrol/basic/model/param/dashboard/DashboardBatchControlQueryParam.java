package com.persagy.apccontrol.basic.model.param.dashboard;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 网格图批量控制查询参数
 *
 * @author JiYinchuan
 * @since 7.250901.0
 */
@Data
public class DashboardBatchControlQueryParam {

    @Schema(description = "运行监测类型")
    @NotNull(message = "运行监测类型不能为空")
    private RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "模版类型")
    @NotNull(message = "模版类型不能为空")
    private InfoCodeTemplateTypeEnum templateType;

    @Schema(description = "对象类型")
    private DtpRwdObjectTypeEnum objectType;

}
