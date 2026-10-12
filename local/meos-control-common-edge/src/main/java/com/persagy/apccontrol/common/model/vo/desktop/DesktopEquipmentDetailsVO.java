package com.persagy.apccontrol.common.model.vo.desktop;

import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 桌面端-设备详情VO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "桌面端-设备详情VO")
public class DesktopEquipmentDetailsVO extends DtpRwdObjectVO {

    @Schema(description = "信息点详情")
    private List<RcInfoCodeCfg> infoCodeDetails;

    @Nullable
    @Schema(description = "手动状态过期时间")
    private String manualStatusExpireTime;

    @Nullable
    @Schema(description = "关联空间")
    private DtpRwdObjectVO inSpace;

}
