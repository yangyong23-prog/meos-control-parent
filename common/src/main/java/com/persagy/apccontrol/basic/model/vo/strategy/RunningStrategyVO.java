package com.persagy.apccontrol.basic.model.vo.strategy;

import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningTimeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 运行策略VO
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Schema(title = "运行策略VO")
public class RunningStrategyVO<T> {

    /**
     * 运行类型-开关设定
     */
    public static final String RUNNING_TYPE_OPEN_CLOSE = "openClose";

    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "对象名称")
    private String objectName;

    @Schema(description = "对象本地名称")
    private String objectLocalName;

    @Schema(description = "运行状态")
    private RunStatusEnum runStatus;

    @Schema(description = "开启时间")
    private LocalDateTime openDateTime;

    @Schema(description = "关闭时间")
    private LocalDateTime closeDateTime;

    @Schema(description = "运行关闭时间")
    private List<RunningTimeVO> openCloseTimes;

    @Schema(description = "运行计划")
    private T runningPlan;

    @Schema(description = "字段名称 -> 运行类型")
    private Map<String, RunningLogTypeEnum> fieldNameAndRunningType;

    @Schema(description = "字段名称 -> 干预结束时间")
    private Map<String, LocalDateTime> fieldNameAndInterveneEndTime;

    @Schema(description = "数据更新时间")
    private LocalDateTime updateTime;

}
