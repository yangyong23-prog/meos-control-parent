package com.persagy.apccontrol.basic.outside.dto.dangerevent;

import com.persagy.apccontrol.basic.model.enumtype.dangerevent.StatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险事件队列消息
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/02/13 11:31
 * @since 7.250201.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("风险事件队列消息")
public class DangerEventMsgDTO {

    private List<InstanceTreeStatusDiffs> instanceTreeStatusDiffs;

    @ApiModel("风险事件变化信息")
    @Data
    public static class InstanceTreeStatusDiffs implements Serializable {
        private static final long serialVersionUID = 7362506138901892592L;
        @ApiModelProperty("风险事件变化信息")
        private DangerEventMsgDTO.InstanceTreeStatus instanceTreeStatus;

        @ApiModelProperty("状态变化详情")
        private DiffDetail diffDetail;
    }

    @ApiModel("风险事件变化信息")
    @Data
    public static class InstanceTreeStatus implements Serializable {
        private static final long serialVersionUID = -8194302876025617470L;

        @ApiModelProperty("风险事件面客状态")
        private StatusEnum displayStatus;
        @ApiModelProperty("风险事件id")
        private String id;
        @ApiModelProperty("状态更新时间")
        private LocalDateTime statusUpdateTime;
    }

    @ApiModel("状态变化详情")
    @Data
    public static class DiffDetail implements Serializable {
        private static final long serialVersionUID = -8194302876025617470L;

        @ApiModelProperty("状态变化前后时间")
        private DiffItem statusUpdateTimeDiff;
        @ApiModelProperty("状态变化前后状态")
        private DiffItem currentStatusDiff;
    }

    @ApiModel("状态变化项")
    @Data
    public static class DiffItem implements Serializable {
        private static final long serialVersionUID = -8194302876025617470L;

        @ApiModelProperty("是否变化")
        private Boolean changed;
        @ApiModelProperty("新数值")
        private String newValue;
        @ApiModelProperty("旧数值")
        private String oldValue;
    }

}
