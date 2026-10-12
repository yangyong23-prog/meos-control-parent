package com.persagy.apccontrol.basic.service.interventiontask;


import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigOutsideQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskManualControlParam;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigSaveOrUpdateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 干预任务配置服务
 */
public interface InterventionTaskConfigHandler {

    @NonNull
    RunningMonitorTypeEnum getRunningMonitorType();

    /**
     * 新增或修改配置
     * @param dto dto
     */
    InterventionTaskConfigSaveOrUpdateVO saveOrUpdate(InterventionTaskConfigDTO dto);

    /**
     * 手动控制
     * @param param  控制参数
     * @since 7.260101.0
     */
    default boolean manualControl(InterventionTaskManualControlParam param) {
        return false;
    }

    /**
     * 删除配置
     * @param taskId taskId
     */
    void del(Long taskId, boolean logRecord);

    /**
     * 查询配置
     * @param queryParam
     * @return
     */
    List<InterventionTaskConfigVO> queryList(InterventionTaskConfigQueryParam queryParam);

    /**
     * 外部接口查询配置
     * @param queryParam
     * @return
     */
    List<? extends InterventionTaskConfigDataDTO> outsideQueryTaskConfigListList(InterventionTaskConfigOutsideQueryParam queryParam);

}
