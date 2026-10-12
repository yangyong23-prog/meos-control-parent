package com.persagy.apccontrol.basic.model.dto;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/31 10:18
 */
@Getter
@Setter
@ToString
public class SystemDTO {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @Schema(description = "系统ID")
    //@NotBlank(message = "系统ID不可为空")
    private String systemId;

    @Schema(description = "系统类编码")
    private String systemClassCode;

    @Schema(description = "系统类编码")
    private List<DtpRwdClassCodeEnum> systemClassCodes;

    @Schema(description = "图类型编码")
    private String graphTypeCode;

    @Schema(description = "系统图id")
    private Long systemGraphId;

    @Schema(description = "空间功能code")
    private List<String> funcCodes;

    @Schema(description = "图名")
    private String imageOrProductName;

}
