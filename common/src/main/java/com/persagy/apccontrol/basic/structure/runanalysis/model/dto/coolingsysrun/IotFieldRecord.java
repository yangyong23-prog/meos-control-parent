package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author weiyizhong
 * @Date 2026/2/7 13:44
 * @Company yushu.biz
 * @Description: 信息点iot 数据 dto
 */
@Data
public class IotFieldRecord {

    @Schema(description = "实例名称")
    private String objName;

    @Schema(description = "实例id")
    private String objId;

    @Schema(description = "信息名称")
    private String infoName;

    @Schema(description = "信息编码")
    private String infoCode;

    @Schema(description = "额定流量")
    private String ratedFlow;

    @Schema(description = "额定流量单位")
    private String ratedFlowUnit;

    @Schema(description = "仅看有开启记录的水泵标识")
    private Boolean startRecordMark;

    @Schema(description = "iot信息值序列")
    private List<IotValueSequence> iotValueSequences;

    @Schema(description = "信息点字典数据")
    private InfoCodeDefineModel infoCodeDefineModel;


}
