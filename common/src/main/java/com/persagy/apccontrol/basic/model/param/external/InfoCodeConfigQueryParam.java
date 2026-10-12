package com.persagy.apccontrol.basic.model.param.external;

import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 信息点配置查询条件
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/8/31 11:32
 */
@Getter
@Setter
@ToString
public class InfoCodeConfigQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_79ae698d8ca34f22a0784247c6543796}")
    private String projectId;

    @Schema(description = "设备类定义")
    @NotNull(message = "{key_c9de9e5a99e24ff7819184114cf6a5ed}")
    private List<ClassCode> classCode;

    @Schema(description = "一级分组名称")
    private String firstGroup;

    @Schema(description = "一级标签名称")
    private String firstTag;

    @Schema(description = "重要等级")
    private String importanceLevel;

}
