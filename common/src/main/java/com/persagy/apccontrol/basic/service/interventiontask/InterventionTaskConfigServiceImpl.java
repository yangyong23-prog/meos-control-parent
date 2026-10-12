package com.persagy.apccontrol.basic.service.interventiontask;

import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionTaskConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigOutsideQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskManualControlParam;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigSaveOrUpdateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.resource.intervention.InterventionTaskConfigResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 干预任务配置服务
 */
@Service
public class InterventionTaskConfigServiceImpl implements InterventionTaskConfigService {

    @Resource
    private List<InterventionTaskConfigHandler> interventionTaskConfigHandler;
    @Resource
    InterventionTaskConfigResource interventionTaskConfigService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterventionTaskConfigSaveOrUpdateVO saveOrUpdate(InterventionTaskConfigDTO dto) {
        return executeByRunningMonitorType(dto.getRunningMonitorType(),
                handler -> handler.saveOrUpdate(dto), null);
    }

    @Override
    public boolean manualControl(InterventionTaskManualControlParam param) {
        return executeByRunningMonitorType(param.getRunningMonitorType(),
                handler -> handler.manualControl(param), false);
    }

    @Override
    public void del(Long taskId) {
        InterventionTaskConfig cfg = interventionTaskConfigService.queryByTaskId(taskId);
        if (cfg == null) {
            return;
        }
        executeByRunningMonitorType(cfg.getRunningMonitorType(),
                handler -> {
                    handler.del(taskId, true);
                    return null;
                }, null);
    }
    
    @Override
    public List<InterventionTaskConfigVO> queryList(InterventionTaskConfigQueryParam queryParam) {
        return executeByRunningMonitorType(queryParam.getRunningMonitorType(),
                handler -> handler.queryList(queryParam), new ArrayList<>());
    }
    
    @Override
    public List<? extends InterventionTaskConfigDataDTO> outsideQueryTaskConfigListList(InterventionTaskConfigOutsideQueryParam queryParam) {
        return executeByRunningMonitorType(queryParam.getRunningMonitorType(),
                handler -> handler.outsideQueryTaskConfigListList(queryParam), new ArrayList<>());
    }
    
    /**
     * 策略执行方法
     *
     * @param runningMonitorType 运行监测类型
     * @param handlerFunction    处理方法
     * @param defaultResult      默认结果
     * @param <T>
     * @return
     */
    private <T> T executeByRunningMonitorType(RunningMonitorTypeEnum runningMonitorType,
                                              Function<InterventionTaskConfigHandler, T> handlerFunction, T defaultResult) {
        for (InterventionTaskConfigHandler handler : interventionTaskConfigHandler) {
            if (handler.getRunningMonitorType().equals(runningMonitorType)) {
                return handlerFunction.apply(handler);
            }
        }
        return defaultResult;
    }
    
    
}
