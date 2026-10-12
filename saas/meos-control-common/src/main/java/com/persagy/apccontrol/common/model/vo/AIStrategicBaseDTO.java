package com.persagy.apccontrol.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @Description: AI策略设置 查询请求
 * @author: yancheng
 * @date: 2023/3/18
 * @version: V1.0
 */

@Data
@Schema(title = "AI策略设置 查询请求")
public class AIStrategicBaseDTO {

    @Schema(description = "主键ID" )
    @NotEmpty(message = "{key_26525506f274445b98a26817a4736cb3}")
    private String configId;
}
