package com.persagy.apccontrol.basic.sse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/26 16:52
 */
@Getter
@Setter
@ToString
@Builder
public class SseEmitterObject {

    @Schema(description = "集团编码")
    private String groupCode;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "事件ID")
    private String eventId;

    @Schema(description = "事件类型")
    private SseEmitterEventEnum eventType;

    @Schema(description = "事件名称")
    private String eventName;

    @Schema(description = "事件描述")
    private String comment;

    @Schema(description = "事件内容")
    private Object data;

}
