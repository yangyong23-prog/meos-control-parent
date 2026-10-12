package com.persagy.apccontrol.basic.model.dto.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 业务数据推送实体类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/12/24 10:19
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BusinessExternalPushDTO {

    @Schema(description = "集团编码")
    private String groupCode;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "业务数据类型")
    private BusinessTypeEnum businessType;

    @Schema(description = "业务数据实体")
    private Object content;

}
