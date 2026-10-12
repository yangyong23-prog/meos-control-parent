package com.persagy.apccontrol.basic.model.param.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 14:21
 */
@Data
@Accessors(chain = true)
@Schema(title = "运行标准看板-查询参数")
public class RunningStandardBoardQueryParam {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @NotBlank(message = "{key_82c482b128f746fca3038e09a27ed7b6}")
    @Schema(description = "查询日期 格式yyyy-MM-dd")
    private String workDate;

    @Schema(description = "页码")
    private Integer page;
    @Schema(description = "每页条数")
    private Integer pageSize;

}
