package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * 传奇图标vo
 *
 * @author MAF
 * @date 2024/10/31
 */
@Data
public class LegendIconVO {

    @Schema(description = "设备类编码")
    private String classCode;
    @Schema(description = "图例id")
    private String legendOriId;
    @Schema(description = "信息点列表")
    private LinkedList<String> infoCodes;
    @Schema(description = "信息点值列表")
    private List<InfoValueVO> infoValues;
}
