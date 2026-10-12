package com.persagy.apccontrol.basic.model.vo.strategy.chiller;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup.DtpRwdFreezePumpGroupInfoCodeEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.util.List;

/**
 * 末级泵组运行计划VO
 *
 * @author JiYinchuan
 * @since v5.9.0
 */
@Data
@FieldNameConstants
@Schema(title = "末级泵组运行计划VO")
public class LastFreezePumpGroupRunningPlanVO {

    @Schema(description = "设定信息点类型")
    private DtpRwdFreezePumpGroupInfoCodeEnum branchDetaSetType;

    @Schema(description = "反馈信息点类型")
    private String branchDetaType;

    @Schema(description = "对应支路供回水温差/压差设定值")
    private RunningValueVO<BigDecimal> branchDetaSet;

    @Schema(description = "对应支路供回水温差/压差设定单位")
    private String branchDetaSetUnit;

    @Schema(description = "泵组下的设备")
    private List<DtpRwdObjectVO> equipmentList;

}
