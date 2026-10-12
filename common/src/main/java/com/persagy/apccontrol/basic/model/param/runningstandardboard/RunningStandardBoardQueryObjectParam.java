package com.persagy.apccontrol.basic.model.param.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 14:21
 */
@Data
@Accessors(chain = true)
@Schema(title = "运行标准看板-查询参数")
public class RunningStandardBoardQueryObjectParam {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @Schema(description = "对象id列表")
    private List<String> objIds;

}
