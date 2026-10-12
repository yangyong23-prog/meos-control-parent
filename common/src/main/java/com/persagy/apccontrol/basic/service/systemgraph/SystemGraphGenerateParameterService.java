package com.persagy.apccontrol.basic.service.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphGenerateParameter;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author persagy
* @description 针对表【system_graph_generate_parameter】的数据库操作Service
* @createDate 2024-06-25 11:57:27
*/
public interface SystemGraphGenerateParameterService extends IService<SystemGraphGenerateParameter> {

    SystemGraphGenerateParameter getSystemGraphGenerateParameterByDuplicateId(Long duplicateId);

    RequestSystemGraphDTO getParameterDataByDuplicateId(Long duplicateId);

    Boolean saveAndUpdateSystemGraphGenerateParameter(Long duplicateId, RequestSystemGraphDTO dto);
}
