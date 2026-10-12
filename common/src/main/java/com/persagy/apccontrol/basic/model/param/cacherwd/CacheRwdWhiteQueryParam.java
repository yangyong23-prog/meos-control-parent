package com.persagy.apccontrol.basic.model.param.cacherwd;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 查询白名单配置参数
 *
 * @author wxf
 * @since 7.260302.0
 */
@Data
@Schema(description = "查询白名单配置参数")
public class CacheRwdWhiteQueryParam {

    /**
     * 运行监测类型
     */
    @NotNull(message = "运行监测类型不能为空")
    @Schema(description = "运行监测类型", required = true)
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 对象类型
     */
    @NotNull(message = "对象类型不能为空")
    @Schema(description = "对象类型", required = true)
    private DtpRwdObjectTypeEnum objType;
}
