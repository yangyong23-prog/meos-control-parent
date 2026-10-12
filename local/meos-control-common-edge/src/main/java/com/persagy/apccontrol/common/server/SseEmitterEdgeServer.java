package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.dto.ReleaseInterventionDTO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * SSE 边缘端处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/6/27 17:35
 */
@Slf4j
@Component
public class SseEmitterEdgeServer {

    @Lazy
    @Resource
    private SseHandlerContext sshHandlerContext;

    /**
     * 设定点位-人工干预信息推送
     *
     * @param interventionDTO
     */
    public void releaseIntervention(ReleaseInterventionDTO interventionDTO) {
        //直接走按需订阅 通过对象进行过滤
        sshHandlerContext.getStrategy(SseEmitterEventEnum.RELEASE_INTERVENTION).doOperation(interventionDTO.getProjectId(), interventionDTO);
    }


    /**
     * 设定点位-人工干预信息推送
     *
     * @param interventionDTOS
     */
    public void releaseInterventions(List<ReleaseInterventionDTO> interventionDTOS) {
        if (CollectionUtil.isEmpty(interventionDTOS)) {
            return;
        }
        for (ReleaseInterventionDTO interventionDTO : interventionDTOS) {
            this.releaseIntervention(interventionDTO);
        }
    }

}
