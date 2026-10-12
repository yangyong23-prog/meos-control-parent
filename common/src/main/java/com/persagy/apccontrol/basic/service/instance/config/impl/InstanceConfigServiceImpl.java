package com.persagy.apccontrol.basic.service.instance.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.convert.InstanceConfigConvert;
import com.persagy.apccontrol.basic.mapper.InstanceConfigMapper;
import com.persagy.apccontrol.basic.model.entity.instance.config.InstanceConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigSaveParam;
import com.persagy.apccontrol.basic.model.vo.instance.InstanceConfigVO;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.basic.service.instance.config.InstanceConfigService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 实例配置服务实现类
 *
 * @author MuAnFu
 * @date 2025-06-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InstanceConfigServiceImpl extends ServiceImpl<InstanceConfigMapper, InstanceConfig> implements InstanceConfigService {

    private final InstanceConfigConvert instanceConfigConvert;
    private final PaasDataTransferClientService paasDataTransferClientService;

    @Override
    public Boolean saveInstanceConfig(InstanceConfigSaveParam param, Boolean isEdge) {
        InstanceConfig instanceConfig = getInstanceConfig(param.getConfigType(), param.getObjectType(), param.getRunningMonitorSecondType(),
                param.getRunningMonitorType(), param.getRelMsg());
        if (Objects.isNull(instanceConfig)) {
            instanceConfig = new InstanceConfig();
            instanceConfig.setConfigType(param.getConfigType());
            instanceConfig.setObjectType(param.getObjectType());
            instanceConfig.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
            instanceConfig.setRunningMonitorType(param.getRunningMonitorType());
            instanceConfig.setConfigData(param.getConfigData());
            instanceConfig.setProjectId(ContextUtil.getProjectId());
            instanceConfig.setRelMsg(param.getRelMsg());
            boolean save = this.save(instanceConfig);
            if (save && isEdge) {
                //同步至云端
                paasDataTransferClientService.sendCloudToAsync(PaasDataTransferParam.of(DataTypeEnum.ADD, instanceConfig));
            }
            return save;
        }
        instanceConfig.setConfigData(param.getConfigData());
        instanceConfig.clearUpdateParameter();
        boolean updated = this.updateById(instanceConfig);
        if (updated && isEdge) {
            //同步至云端
            paasDataTransferClientService.sendCloudToAsync(PaasDataTransferParam.of(DataTypeEnum.EDIT, instanceConfig));
        }
        return updated;
    }

    @Override
    public List<InstanceConfigVO> listInstanceConfig(InstanceConfigQueryParam queryParam) {
        LambdaQueryWrapper<InstanceConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(InstanceConfig::getConfigType, queryParam.getConfigType())
                .eq(InstanceConfig::getProjectId, ContextUtil.getProjectId())
                .eq(Objects.nonNull(queryParam.getObjectType()), InstanceConfig::getObjectType, queryParam.getObjectType())
                .eq(Objects.nonNull(queryParam.getRunningMonitorSecondType()), InstanceConfig::getRunningMonitorSecondType, queryParam.getRunningMonitorSecondType())
          .eq(InstanceConfig::getRunningMonitorType, queryParam.getRunningMonitorType())
          .eq(StringUtils.isNotEmpty(queryParam.getTemplateTypeKey()), InstanceConfig::getRelMsg, queryParam.getTemplateTypeKey());
        List<InstanceConfig> list = this.list(qw);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return instanceConfigConvert.toListVO(list);
    }

    @Override
    public InstanceConfig getInstanceConfig(@NonNull InstanceConfigTypeEnum configType,
                                            @NonNull DtpRwdObjectTypeEnum objectType,
                                            RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                            @NonNull RunningMonitorTypeEnum runningMonitorType,
                                            String relMsg) {
        LambdaQueryWrapper<InstanceConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(InstanceConfig::getConfigType, configType)
                .eq(InstanceConfig::getObjectType, objectType)
                .eq(InstanceConfig::getProjectId, ContextUtil.getProjectId())
                .eq(Objects.nonNull(runningMonitorSecondType), InstanceConfig::getRunningMonitorSecondType, runningMonitorSecondType)
          .eq(InstanceConfig::getRunningMonitorType, runningMonitorType)
          .eq(StringUtils.isNotEmpty(relMsg), InstanceConfig::getRelMsg, relMsg);
        return this.getOne(qw);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "实例配置服务实现类";
    }
}
