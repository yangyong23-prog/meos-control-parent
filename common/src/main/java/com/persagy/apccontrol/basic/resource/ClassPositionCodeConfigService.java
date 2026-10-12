package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.model.entity.ClassPositionCodeConfig;
import com.persagy.framework.database.service.SuperService;

import java.util.List;

/**
 * 设备类业务定义
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
public interface ClassPositionCodeConfigService extends SuperService<ClassPositionCodeConfig> {

    /**
     * 获取当前项目所有的设备类定义
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    List<ClassPositionCodeConfig> queryClassPositionCodeConfigsByClassCode(String groupCode, String projectId, String classCode);

}
