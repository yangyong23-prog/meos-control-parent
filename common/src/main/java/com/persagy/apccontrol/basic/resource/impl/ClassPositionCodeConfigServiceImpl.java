package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.persagy.apccontrol.basic.mapper.ClassPositionCodeConfigMapper;
import com.persagy.apccontrol.basic.model.entity.ClassPositionCodeConfig;
import com.persagy.apccontrol.basic.resource.ClassPositionCodeConfigService;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备半剖图配置
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/2/23 10:39
 */
@Service
public class ClassPositionCodeConfigServiceImpl extends SuperServiceImpl<ClassPositionCodeConfigMapper, ClassPositionCodeConfig> implements ClassPositionCodeConfigService {

    @Override
    public List<ClassPositionCodeConfig> queryClassPositionCodeConfigsByClassCode(String groupCode, String projectId, String classCode) {
        LambdaQueryChainWrapper<ClassPositionCodeConfig> chainWrapper = this.lambdaQuery();
        chainWrapper.and(wrapper -> wrapper
                .eq(StrUtil.isNotBlank(projectId), ClassPositionCodeConfig::getProjectId, projectId)
                .or().eq(ClassPositionCodeConfig::getProjectId, "0")
        );
        chainWrapper.and(wrapper -> wrapper
                .eq(StrUtil.isNotBlank(classCode), ClassPositionCodeConfig::getClassCode, classCode)
                .or().eq(ClassPositionCodeConfig::getClassCode, "0")
        );
        return chainWrapper.list();
    }

}
