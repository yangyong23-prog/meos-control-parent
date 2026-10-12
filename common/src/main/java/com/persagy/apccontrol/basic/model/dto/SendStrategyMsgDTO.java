package com.persagy.apccontrol.basic.model.dto;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendStrategyMsgDTO {
    
    /**
     * sse事件类型枚举
     */
    private SseEmitterEventEnum event;
    
    /**
     * 事件内容
     */
    private Object condition;
    
    /**
     * 获取真正的事件对象
     *
     * @return
     */
    @SneakyThrows
    public Object getConditionDto() {
        if (event == null || condition == null) {
            return null;
        }
        switch (event) {
            case INFO_EXTEND_SET:
                return CommonConst.JSON_MAPPER.readValue(JSONObject.toJSONString(condition),
                        FeedbackInfoMsgMqttVO.class);
            default:
                return null;
        }
    }
    
}
