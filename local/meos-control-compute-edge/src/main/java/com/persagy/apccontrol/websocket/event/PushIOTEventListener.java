package com.persagy.apccontrol.websocket.event;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.opt.OptCtrlFrameOrderGuard;
import com.persagy.apccontrol.service.handler.RunningLogHandle;
import com.persagy.apccontrol.websocket.handler.ComputeIotDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shaohongbo
 * @date 2023年03月29日 17:11
 */
@Slf4j
@Component
public class PushIOTEventListener {

    @Resource
    private RunningLogHandle runningLogHandle;

    @Resource
    private ComputeIotDataHandler computeIotDataHandler;

    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    /** opt 分支 B1（switch.opt.ctrlFrameOrderFix）：16.9 完成帧先到时丢弃后到的进行中帧 */
    @Lazy
    @Autowired
    private OptCtrlFrameOrderGuard optCtrlFrameOrderGuard;
    
    public void onApplicationEvent(FeedbackInfoMsgMqttVO source) {
        
        String infoCode = source.getInfoCode();
        String classCode = source.getClassCode();
        RcInfoCodeCfg rcInfoCodeCfg = this.localRedisCacheHandler.getInfoCodeConfig(classCode, infoCode);

        // 对象详情数据赋值
        RwdObjectBusinessFeildDTO businessFeildDTO = this.localRedisCacheHandler.getObjectBusinessFeildAndSystemInfo(source.getGroupCode(), source.getProjectId(), source.getObjectId(), null);
        source.setLocalId(businessFeildDTO == null ? null : businessFeildDTO.getLocalId());
        source.setLocalName(businessFeildDTO == null ? null : businessFeildDTO.getLocalName());
        source.setObjType(businessFeildDTO == null ? null : businessFeildDTO.getObjType());
        source.setModelType(businessFeildDTO == null || businessFeildDTO.getSystemType() == null ? null : businessFeildDTO.getSystemType().value());

        // opt B1：同一控制指令已完成时丢弃后到的帧（不计算、不写缓存、不推送），运行日志仍照常处理
        if (this.optCtrlFrameOrderGuard.shouldDiscard(source, rcInfoCodeCfg)) {
            this.runningLogHandle.handleLog(source);
            return;
        }

        // 优先判断是否需要进行信息点的计算
        boolean condition = this.computeIotDataHandler.checkComputeInfoCodeDataCondition(source, rcInfoCodeCfg);
        List<FeedbackInfoMsgMqttVO> infoMsgMqttVOS = new ArrayList<>();
        if (condition) {
            infoMsgMqttVOS = this.computeIotDataHandler.computeInfoCodeData(source, rcInfoCodeCfg);
        } else {
            infoMsgMqttVOS.add(source);
        }

        //数据推送
        this.computeIotDataHandler.pushMessage(infoMsgMqttVOS, rcInfoCodeCfg);
        
        // sse推送
        if (CollectionUtil.isNotEmpty(infoMsgMqttVOS)) {
            infoMsgMqttVOS.forEach(item -> this.computeIotDataHandler.sseStrategy(item));
        }

        //运行日志
        this.runningLogHandle.handleLog(source);
    }
    
    
}
