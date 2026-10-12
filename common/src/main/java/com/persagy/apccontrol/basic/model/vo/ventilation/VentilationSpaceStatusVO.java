package com.persagy.apccontrol.basic.model.vo.ventilation;

import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationSpaceStatusEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * 通风设备异常
 *
 * @author MAF
 * @date 2024/07/22
 */
@Data
@ToString(callSuper = true)
public class VentilationSpaceStatusVO extends DtpRwdSpaceVO {

    @Schema(description = "空间状态")
    private VentilationSpaceStatusEnum spaceStatus;

    @Schema(description = "是否异常")
    private Boolean isAbnormal;

    @Schema(description = "是否可控")
    private Boolean isControl;

    @Schema(description = "信息点配置")
    private List<InfoCodeValueVO> infoCodes;

    @Schema(description = "设备类型")
    private Set<VentilationEquipTypeEnum> equipTypes;
}
