package com.persagy.apccontrol.basic.model.param.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 物理世界-孪生控制对象查询参数
 *
 * @author JiYinchuan
 * @since 5.240601.0
 */
@Data
@Schema(title = "楼层关联空间查询参数")
public class DtpRwdTwinControlObjectQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "设备类编码")
    @NotEmpty(message = "{key_8ed7086a90aa4ce3be81d08bacfa7340}")
    private List<String> classCodes;

}
