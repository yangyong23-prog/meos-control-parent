package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * 查询指定对象的信息点
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 14:32
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "单对象信息点配置")
public class ObjectInfoCodeConfigDTO {

    //@NotBlank(message = "项目ID不可为空")
    @Schema(description = "项目编码")
    private String projectId;

    @NotBlank(message = "{key_1af4a612374f4faa99db5ad6ec0600d3}")
    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "设备类编码")
    private String classCode;

    @Schema(description = "信息点集合")
    private Set<String> infoCodes;

}
