package com.persagy.apccontrol.common.service.intervention;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.convert.InterventionConvert;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionConfigAddDto;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionAddEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionReleaseParam;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.resource.intervention.param.InterventionConfigQueryDaoParam;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionOperationTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.InterventionConfigQueryInfrastructure;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.common.resource.InterventionConfigOperationResourceService;
import com.persagy.apccontrol.common.service.RunningLogEdgeService;
import com.persagy.apccontrol.common.structure.intervention.infrastructure.InterventionConfigOperationInfrastructure;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionConfigOperationService {

    private final InterventionConvert convert;
    private final InterventionConfigQueryResourceService interventionConfigQueryResourceService;
    private final InterventionConfigOperationResourceService interventionConfigOperationResourceService;
    private final InterventionConfigQueryInfrastructure<InterventionConfigData> interventionConfigQueryInfrastructure;
    private final InterventionConfigOperationInfrastructure<InterventionConfigData> interventionConfigOperationInfrastructure;
    private final RunningLogEdgeService runningLogEdgeService;
    private final PaasDataTransferClientService paasDataTransferClientService;

    @Transactional(rollbackFor = Exception.class)
    public List<Long> interventionAddConfig(InterventionConfigAddDto addDto) {
        InterventionAddEntity addEntity = convert.addConfigDtoToEntity(addDto);
        addEntity.setProjectId(ContextUtil.getProjectId());
        Boolean valid = interventionConfigQueryResourceService.validatedAddConfigSlot(addEntity);
        if (!valid) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_b09f7d8956da4521ac4ae291b2594a9c"));
        }
        return interventionConfigOperationResourceService.addInterventionConfig(addEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean interventionSaveOrUpdate(InterventionConfigAddDto addDto) {
        List<Long> configIds = interventionAddConfig(addDto);
        Long configId = configIds.stream().findFirst().orElse(null);
        if (configId != null) {
            Optional<InterventionConfigModel<InterventionConfigData>> configModelOpt =
                    interventionConfigQueryInfrastructure.of(configId);
            if (!configModelOpt.isPresent()) {
                throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_ecb554df132e4004962498bee4da5cf5"));
            }
            InterventionConfigModel<?> configModel = configModelOpt.get();
            addDto.getRunningMonitorSecondType().toRunningMonitorTypeEnum().getInterventionConfigAnalyzeStrategy()
                    .publishInterventionChangeEvent(InterventionOperationTypeEnum.EDIT, configModel);
        }
        // 保存干预操作日志
        saveInterventionRunningLog(addDto);
        return true;
    }

    /**
     * 解除干预
     *
     * @author yuecaipu
     * @date 2024/11/4 15:41
     * @since v5.241101.0
     */
    public R<Boolean> release(@Valid InterventionReleaseParam param) {
        saveInterventionRunningLog(param.getInterventionId());
        Long interventionId = param.getInterventionId();
        Optional<InterventionConfigModel<InterventionConfigData>> configModelOpt = interventionConfigQueryInfrastructure.of(interventionId);
        if (!configModelOpt.isPresent()) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_ecb554df132e4004962498bee4da5cf5"));
        }
        InterventionConfigModel<InterventionConfigData> configModel = configModelOpt.get();
        boolean result = interventionConfigOperationInfrastructure.release(configModel);
        return R.success(result);
    }

    /**
     * 保存照明干预操作日志
     *
     * @param addDto 参数
     * @author yuecaipu
     * @date 2024/11/4 15:41
     * @since v5.241101.0
     */
    private void saveInterventionRunningLog(InterventionConfigAddDto addDto) {
    
    }

    /**
     * 解除照明干预操作日志
     *
     * @param interventionId 干预ID
     * @author yuecaipu
     * @date 2024/11/4 15:41
     * @since v5.241101.0
     */
    private void saveInterventionRunningLog(Long interventionId) {
    
    }

    public boolean transferInterventionConfigToCloud() {
        InterventionConfigQueryDaoParam queryDaoParam = new InterventionConfigQueryDaoParam();
        List<InterventionConfig> allInterventionConfigs = interventionConfigQueryResourceService.list(queryDaoParam);
        paasDataTransferClientService.syncDbDataToCloud(PaasDataTransferParam.of(DataTypeEnum.ADD, allInterventionConfigs));
        return true;
    }
}
