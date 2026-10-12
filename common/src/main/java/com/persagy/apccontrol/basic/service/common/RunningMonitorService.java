package com.persagy.apccontrol.basic.service.common;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.BatchObjectInfoCodeConfigDTO;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceStatusQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 运行监测服务
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
public interface RunningMonitorService {

    JSONObject countSpaceRelatedEquipManualAutoStatus(LightingInstanceStatusQueryParam instanceParam);

    List<InfoCodeValueVO> batchQueryInfoCode(@NonNull @Valid BatchObjectInfoCodeConfigDTO param);

    /**
     * 获取累计运行时间的配置
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @param systemClassCode
     * @return
     */
    List<JSONObject> listAccRuntimeClass(String groupCode, String projectId, String systemId, String systemClassCode);


}