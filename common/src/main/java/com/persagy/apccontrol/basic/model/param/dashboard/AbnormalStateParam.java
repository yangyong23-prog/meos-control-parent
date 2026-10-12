package com.persagy.apccontrol.basic.model.param.dashboard;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collection;

/**
 * 查询设备、空间异常状态查询参数
 *
 * @author LiuYang
 * @since v5.241201.0
 */
@Data
public class AbnormalStateParam {

    @Schema(description = "设备id集合")
    private Collection<String> equipmentIds;

    @Schema(description = "空间id集合")
    private Collection<String> spaceIds;

    @Schema(description = "运行监测二级类型, 不为空时按类型过滤")
    private RunningMonitorTypeEnum runningMonitorType;

}
