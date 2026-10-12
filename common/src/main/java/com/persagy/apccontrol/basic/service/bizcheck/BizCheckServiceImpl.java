package com.persagy.apccontrol.basic.service.bizcheck;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.convert.BizCheckConvert;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckConfigParam;
import com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckQueryParam;
import com.persagy.apccontrol.basic.model.param.bizcheck.NotifyPeriodConfigParam;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckConfigVO;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckConfig;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckResult;
import com.persagy.apccontrol.basic.structure.bizcheck.service.BizCheckDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BizCheckServiceImpl implements BizCheckService {

    private final BizCheckConvert bizCheckConvert;
    private final BizCheckDomainService bizCheckDomainService;
    private final DtpRwdClientService dtpRwdClientService;
    /**
     * 保存配置
     */
    @Override
    public Boolean saveConfig(BizCheckConfigParam param) {
        BizCheckConfig configEntity = bizCheckConvert.toConfigEntity(param);
        return bizCheckDomainService.saveConfig(configEntity);
    }

    @Override
    public BizCheckConfigVO getConfig(BizCheckQueryParam param) {
        BizCheckConfig config = bizCheckDomainService.getConfig(param.getBizCode());
        if (config == null) {
            config = new BizCheckConfig();
        }

        BizCheckConfigVO configVO = bizCheckConvert.toConfigVO(config);

        // 获取监控对象数据
        List<DtpRwdObjectDTO> objectDTOS = getMonitoringObjects();
        
        // 构建通知周期配置
        List<NotifyPeriodConfigParam> newConfigs = buildNotifyPeriodConfigs(
            objectDTOS, 
            configVO.getNotifyPeriodConfig()
        );
        
        configVO.setNotifyPeriodConfig(newConfigs);
        return configVO;
    }

    /**
     * 获取监控系统对象列表
     */
    private List<DtpRwdObjectDTO> getMonitoringObjects() {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(Arrays.asList(DtpRwdClassCodeEnum.ACAC_SYSTEM, DtpRwdClassCodeEnum.ACAI_SYSTEM));
        queryParam.addInfoCode(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), Lists.newArrayList("3"));
        queryParam.setWithColumns(Lists.newArrayList(
            DtpRwdObjectDTO.Fields.id, 
            DtpRwdObjectDTO.Fields.classCode, 
            DtpRwdObjectDTO.Fields.localName
        ));
        
        return dtpRwdClientService.listObject(queryParam);
    }

    /**
     * 构建通知周期配置列表
     */
    private List<NotifyPeriodConfigParam> buildNotifyPeriodConfigs(
            List<DtpRwdObjectDTO> objectDTOS, 
            List<NotifyPeriodConfigParam> existingConfigs) {
        
        if (objectDTOS == null || objectDTOS.isEmpty()) {
            return new ArrayList<>();
        }

        return objectDTOS.stream()
                .filter(this::isValidObject)
                .map(objectDTO -> createNotifyConfig(objectDTO, existingConfigs))
                .collect(Collectors.toList());
    }

    /**
     * 验证对象是否有效
     */
    private boolean isValidObject(DtpRwdObjectDTO objectDTO) {
        return objectDTO != null && objectDTO.getId() != null;
    }

    /**
     * 创建通知配置
     */
    private NotifyPeriodConfigParam createNotifyConfig(
            DtpRwdObjectDTO objectDTO, 
            List<NotifyPeriodConfigParam> existingConfigs) {
        
        NotifyPeriodConfigParam notifyConfig = new NotifyPeriodConfigParam();
        notifyConfig.setId(objectDTO.getId());
        notifyConfig.setLocalName(objectDTO.getLocalName());
        notifyConfig.setClassCode(objectDTO.getClassCode());
        
        // 从现有配置中查找相同ID的天数设置
        existingConfigs.stream()
                .filter(config -> config.getId().equals(objectDTO.getId()))
                .findFirst()
                .ifPresent(existingConfig -> notifyConfig.setDays(existingConfig.getDays()));
        
        return notifyConfig;
    }

    @Override
    public List<BizCheckResultVO> getCheckResults(BizCheckQueryParam param) {

        List<BizCheckResult> results = bizCheckDomainService.getResults(param.getBizCode());
        
        // 转换为VO对象
        return results.stream()
                .map(bizCheckConvert::toResultVO)
                .collect(Collectors.toList());
    }

    @Override
    public void triggerAutoCheck() {
        // 调用领域服务启动手动检查
        bizCheckDomainService.autoCheck();
    }

    @Override
    public boolean triggerCheck(BizCheckQueryParam param) {
        BizCheckConfig config = bizCheckDomainService.getConfig(param.getBizCode());
        if (config == null) {
            return false;
        }
        return bizCheckDomainService.startCheck(config, BizCheckTypeEnum.MANUAL);
    }

}
