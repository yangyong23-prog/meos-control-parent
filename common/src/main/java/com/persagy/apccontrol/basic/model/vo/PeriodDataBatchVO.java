package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量查询分精度数据返回对象
 */
@Data
public class PeriodDataBatchVO {

    @Schema(description = "对象 ID")
    private String objectId;

    @Schema(description = "对象名称")
    private String objectName;

    @Schema(description = "数据集合")
    private List<CurveInfoCodeVO> data;


    @Data
    public static class CurveInfoCodeVO {

        @Schema(description = "信息点")
        private String infoCode;
        @Schema(description = "分精度数据")
        private List<DataVO> data;
    }

    @Data
    public static class DataVO {
        @Schema(description = "值")
        private BigDecimal ipValue;
        @Schema(description = "时间")
        private Object time;
    }

}
