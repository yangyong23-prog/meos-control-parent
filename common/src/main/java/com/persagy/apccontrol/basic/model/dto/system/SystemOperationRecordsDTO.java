package com.persagy.apccontrol.basic.model.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author tangzy
 * @date 2023年03月17日 15:51
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Schema(title = "系统运行记录入参")
@ToString(callSuper = true)
public class SystemOperationRecordsDTO {


    @Schema(description = "项目Id", required = true)
    @NotBlank(message = "{key_ccd21ed6ba89436194c304f8bdd8db01}")
    private String projectId;

    //前端传拼接过的参数  不用传运行模式  后端用=去接收
    /**
     * 记录类型枚举字段说明: LogTypeEnum
     */
    @Schema(description = "记录类型", required = true)
    @NotBlank(message = "{key_cc8e6149bcb1480caf1d143b1ab4975d}")
    private String recordType;

    @Schema(description = "查询开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "查询结束时间", required = true)
    private LocalDateTime endTime;
    /**
     * 运行模式枚举字段说明: RunModeEnum
     */
    @Schema(description = "运行模式")
    private String runMode;

    /**
     * 操作类型枚举字段说明: OperateTypeEnum
     */
    @Schema(description = "操作类型")
    private String operatType;

    @Schema(description ="模糊搜索关键字")
    private String word;

}
