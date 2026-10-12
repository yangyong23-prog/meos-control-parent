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
public class AdjustmentRecords {

    @Schema(description = "信息名称")
    private String infoName;

    @Schema(description = "信息编码")
    private String infoCode;

    @Schema(description = "信息点字典数据")
    private InfoCodeDefineModel infoCodeDefineModel;

    @Schema(description = "iot信息值序列")
    private List<IotValueSequence> iotValueSequences;

    @Schema(description = "泵组调节记录(如果只有一个值则不用显示泵组的名称不分组)")
    private List<AdjustmentRecordsPump> adjustmentRecordsPumps;

    @Data
    public static class AdjustmentRecordsPump{

        @Schema(description = "泵组名称")
        private String pumpName;

        @Schema(description = "泵组id")
        private String pumpId;

        @Schema(description = "泵组调节记录")
        private List<IotFieldRecord> iotFieldRecords;

    }
}
