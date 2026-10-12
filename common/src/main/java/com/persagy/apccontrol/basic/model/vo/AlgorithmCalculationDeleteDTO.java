package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 算法删除参数
 * @author: yancheng
 * @date: 2023/3/27
 * @version: V1.0
 */
@Data
@Schema(title = "算法删除参数")
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmCalculationDeleteDTO {

    @Schema(description = "城市 暂无")
    private String groupCode;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "系统编码")
    private String systemId;
}
