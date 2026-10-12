package com.persagy.apccontrol.basic.model.vo.ventilation;

import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.AbnormalEquipmentEnum;
import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipTypeEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
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
public class VentilationEquipmentStatusVO extends DtpRwdEquipmentVO {

    @Schema(description = "异常类型，为空时为正常")
    private List<AbnormalEquipmentEnum> abnormalTypes;

    @Schema(description = "运行状态，为空时无数据")
    private RunStatusEnum runStatus;

    @Schema(description = "是否可控")
    private Boolean isControl;

    @Schema(description = "设备类型")
    private VentilationEquipTypeEnum equipType;

    @Schema(description = "关联空间ID")
    private Set<String> relObjIds;

}
