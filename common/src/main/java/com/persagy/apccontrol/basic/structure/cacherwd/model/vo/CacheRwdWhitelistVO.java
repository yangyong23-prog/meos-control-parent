package com.persagy.apccontrol.basic.structure.cacherwd.model.vo;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cacherwd.WhitelistTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 白名单配置 VO
 *
 * @author wxf
 * @since 7.260302.0
 */
@Data
@Schema(description = "白名单配置 VO")
public class CacheRwdWhitelistVO {

    /**
     * 运行监测类型
     */
    @Schema(description = "运行监测类型")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 白名单类型
     */
    @Schema(description = "白名单类型：FULL-全量可管控空间，CUSTOM-用户配置")
    private WhitelistTypeEnum whitelistType;

    /**
     * 对象列表（空间 DTO）
     */
    @Schema(description = "对象列表（空间 DTO）")
    private List<DtpRwdSpaceDTO> objects;
}
