package com.persagy.apccontrol.basic.resource.param.runningLog;

import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RunningLogPageQueryParam {

    @Schema(name = "项目id")
    private String projectId;

    @Schema(name = "系统id")
    private String systemId;

    @Schema(name = "开始时间")
    private LocalDateTime startTime;

    @Schema(name = "结束时间")
    private LocalDateTime endTime;

    @Schema(name = "日志标识")
    private RunningLogTypeEnum type;

    @Schema(name = "日志分类，一级分类")
    private List<String> firstLevelCategory;

    @Schema(name = "日志分类，二级分类")
    private List<String> secondLevelCategory;

    @Schema(name = "日志分类，三级分类")
    private List<String> thirdLevelCategory;

    @Schema(name = "操作对象id")
    private List<String> objectIds;

    @Schema(name = "关键字模糊匹配")
    private String keyWord;

    @Schema(name = "生效时间")
    private LocalDateTime effectTime;

    @Schema(name = "生效时间，仅限于生效开始时间的筛选")
    private LocalDateTime effectTimeStart;

    @Schema(name = "生效时间，仅限于生效开始时间的筛选")
    private LocalDateTime effectTimeEnd;

    @Schema(name = "创建时间 传递后获取创建时间之后的数据")
    private LocalDateTime createTime;

    @NotNull(message = "{key_e81fab70967c4213bb4f88bb0b57ef02}")
    @Min(value = 1)
    private int pageNumber;

    @NotNull(message = "{key_0ef36dc530554230b031ee4f6d775d9e}")
    @Min(value = 1)
    private int pageSize;

    @Schema(name = "排序方式")
    private String orderBy;
}
