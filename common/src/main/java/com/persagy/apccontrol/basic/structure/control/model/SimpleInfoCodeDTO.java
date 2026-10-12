package com.persagy.apccontrol.basic.structure.control.model;

import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 数据字典 -> 信息点数据源DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
public class SimpleInfoCodeDTO {

    @Schema(description = "编码, 全局唯一")
    private String code;

    @Schema(description = "前端所使用的编码")
    private String aliasCode;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "精度")
    private Integer scale;

    @Schema(description = "数据类型")
    private String dataType;

    @Schema(description = "数据源定义")
    private List<DtpKnowledgeInfoCodeDataSourceDTO> dataSource;

}
