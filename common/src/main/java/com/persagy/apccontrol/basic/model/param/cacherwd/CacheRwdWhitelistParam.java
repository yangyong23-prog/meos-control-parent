package com.persagy.apccontrol.basic.model.param.cacherwd;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cacherwd.WhitelistTypeEnum;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirDtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceGraphVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 保存白名单配置参数
 *
 * @author wxf
 * @since 7.260302.0
 */
@Data
@Schema(description = "保存白名单配置参数")
public class CacheRwdWhitelistParam {

    /**
     * 运行监测类型
     */
    @NotNull(message = "运行监测类型不能为空")
    @Schema(description = "运行监测类型", required = true)
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 白名单类型
     */
    @Schema(description = "白名单类型：FULL-全量可管控空间，CUSTOM-用户配置", 
            example = "FULL", defaultValue = "CUSTOM")
    @NotNull(message = "白名单类型不能为空")
    private WhitelistTypeEnum whitelistType;

    /**
     * 对象列表（空间 DTO）
     * 当 whitelistType=CUSTOM 时必填，当 whitelistType=FULL 时忽略
     */
    @Schema(description = "对象列表（空间 DTO），仅在 CUSTOM 模式下需要")
    private List<DtpRwdSpaceDTO> objects;
}
