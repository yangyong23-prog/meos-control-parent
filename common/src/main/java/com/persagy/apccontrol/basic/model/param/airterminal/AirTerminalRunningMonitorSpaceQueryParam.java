package com.persagy.apccontrol.basic.model.param.airterminal;

import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 末端运行监测空间查询参数
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "末端运行监测空间查询参数")
public class AirTerminalRunningMonitorSpaceQueryParam extends RunningMonitorSpaceQueryParam {

    /**
     * 是否 取空调标准的空间 默认取
     */
    private boolean queryAirConditionDemandSpace = true;

    @Override
    protected String mysqlCacheKeyType() {
        return "airTerminal_" + super.mysqlCacheKey();
    }
}
