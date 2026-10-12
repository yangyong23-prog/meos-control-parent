package com.persagy.apccontrol.basic.model.vo.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 安防筛选配置
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/8 14:17
 * @since v5.240801.0
 */
@Data
public class SecurityScreenConfigVO {

    @Schema(description = "对象类")
    private String classCode;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "条件")
    private String condition;

    @Schema(description = "条件值")
    private String conditionValue;

    @Schema(description = "分组名称")
    private String groupName;

}
