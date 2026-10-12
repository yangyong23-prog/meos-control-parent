package com.persagy.apccontrol.basic.model.vo.outside;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SystemGraphVO {

    @Schema(description = "系统编码")
    private String systemCode;
    @Schema(description = "系统名称")
    private String sysName;
    @Schema(description = "系统图类型")
    private String graphTypeCode;
    @Schema(description = "设备类编码")
    private String classCode;
    @Schema(description = "源设备类编码")
    private String sourceClassCode;
    @Schema(description = "系统对象ID")
    private String systemObjectId;
    @Schema(description = "是否是项目，如果是项目，则为true")
    private Boolean isProject;
}
