package com.persagy.apccontrol.basic.service.instance.config.impl;


import com.persagy.apccontrol.basic.model.dto.equipmentlist.ConfigEquipmentListHeaderDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigQueryParam;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;

/**
 * 设备列表配置 服务
 */
public interface InstanceConfigEquipmentListHeaderService {


    /**
     * 配置保存/修改
     *
     * @param param
     * @return
     */
    Boolean saveInstanceConfig(@Valid ConfigEquipmentListHeaderDTO param);


    /**
     * 配置查询,只查询配置信息
     *
     * @param runningMonitorSecondType
     * @param runningMonitorType
     * @param templateTypeKey          模版类型 key
     * @return
     */
    ConfigEquipmentListHeaderDTO getInstanceConfig(RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                                   @NonNull RunningMonitorTypeEnum runningMonitorType,
                                                   @NonNull String templateTypeKey);

    /**
     * 查询配置和全量表头
     *
     * @param param 查询参数
     * @return 配置和全量表头
     */
    ConfigEquipmentListHeaderDTO listAllConfigAndHeader(InstanceConfigQueryParam param);

}
