package com.persagy.apccontrol.basic.service.interventiontask;

import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigOutsideQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskManualControlParam;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigSaveOrUpdateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;

import java.util.List;

/**
 * 干预任务配置服务
 */
public interface InterventionTaskConfigService {


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
    boolean manualControl(InterventionTaskManualControlParam param);

    /**
     * 删除配置
     * @param taskId taskId
     */
    void del(Long taskId);


    /**
     * 列表查询
     * @param queryParam 查询参数
     */
    List<InterventionTaskConfigVO> queryList(InterventionTaskConfigQueryParam queryParam);


    /**
     * 外部查询干预配置信息
     * @param queryParam
     * @return 干预具体配置信息 不同业务配置数据不同
     */
    List<? extends InterventionTaskConfigDataDTO> outsideQueryTaskConfigListList(InterventionTaskConfigOutsideQueryParam queryParam);
}
