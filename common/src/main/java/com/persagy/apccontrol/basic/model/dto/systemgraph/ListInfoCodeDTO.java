package com.persagy.apccontrol.basic.model.dto.systemgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 图中设备已绑定的信息点全集
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/6/27 14:25
 * @since v5.240602.0
 */
@Data
public class ListInfoCodeDTO {

    @NotEmpty(message = "{key_0caf479ba08342f29b3d3f9aa880a17c}")
    @Schema(description = "对象id")
    private List<String> objectIdList;

    @Schema(description = "项目id")
    private String projectId;

    @NotBlank(message = "{key_f8296507b36244fbb80600f9eae84a69}")
    @Schema(description = "图名")
    private String imageOrProductName;

}
