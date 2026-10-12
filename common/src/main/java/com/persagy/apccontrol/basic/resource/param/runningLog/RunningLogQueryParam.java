package com.persagy.apccontrol.basic.resource.param.runningLog;

import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;

@Data
public class RunningLogQueryParam {

    @Schema(name = "项目id")
    @NotBlank(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(name = "系统id")
    private String systemId;

    @Schema(name = "查询日期")
    private LocalDate workDate;

    @Schema(name = "日志标识")
    private RunningLogTypeEnum type;

    @Schema(name = "日志分类，一级分类")
    private Set<String> firstLevelCategory;

    @Schema(name = "日志分类，二级分类")
    private Set<String> secondLevelCategory;

    @Schema(name = "日志分类，三级分类")
    private Set<String> thirdLevelCategory;

    @Schema(name = "操作对象类")
    private Set<String> classCodes;

    @Schema(name = "操作对象id")
    private Set<String> objectIds;

    @Schema(name = "排序方式")
    private String orderBy;

}
