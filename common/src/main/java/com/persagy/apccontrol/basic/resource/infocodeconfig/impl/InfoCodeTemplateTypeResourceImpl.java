package com.persagy.apccontrol.basic.resource.infocodeconfig.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.infocodeconfig.InfoCodeTemplateTypeMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.envet.InfoCodeTemplateTypeRemoveEvent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.envet.InfoCodeTemplateTypeSaveEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 信息点模版类型配置资源实现
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeTemplateTypeResourceImpl
        extends BaseServiceImpl<InfoCodeTemplateTypeMapper, InfoCodeTemplateType>
        implements InfoCodeTemplateTypeResource {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Optional<InfoCodeTemplateType> get(@NonNull Long id) {
        return getByIdIgnoreOpt(id);
    }

    @Override
    public List<InfoCodeTemplateType> list(@NonNull InfoCodeTemplateTypeQueryDaoParam param) {
        param.checkAndSet();
        return lambdaQuery()
                .select(param.toSelectFunctions())
                .eq(Objects.nonNull(param.getTemplateTypeId()), InfoCodeTemplateType::getId, param.getTemplateTypeId())
                .in(CollectionUtils.isNotEmpty(param.getTemplateTypeIds()), InfoCodeTemplateType::getId, param.getTemplateTypeIds())
                .eq(Objects.nonNull(param.getRunningMonitorType()), InfoCodeTemplateType::getRunningMonitorType, param.getRunningMonitorType())
                .in(CollectionUtils.isNotEmpty(param.getRunningMonitorTypes()), InfoCodeTemplateType::getRunningMonitorType, param.getRunningMonitorTypes())
                .eq(Objects.nonNull(param.getRunningMonitorSecondType()), InfoCodeTemplateType::getRunningMonitorSecondType, param.getRunningMonitorSecondType())
                .eq(Objects.nonNull(param.getTemplateType()), InfoCodeTemplateType::getTemplateType, param.getTemplateType())
                .in(CollectionUtils.isNotEmpty(param.getTemplateTypes()), InfoCodeTemplateType::getTemplateType, param.getTemplateTypes())
                .eq(Objects.nonNull(param.getObjectType()), InfoCodeTemplateType::getObjectType, param.getObjectType())
                .eq(StringUtils.isNotBlank(param.getClassCode()), InfoCodeTemplateType::getClassCode, param.getClassCode())
                .in(CollectionUtils.isNotEmpty(param.getClassCodes()), InfoCodeTemplateType::getClassCode, param.getClassCodes())
                .notIn(CollectionUtils.isNotEmpty(param.getExcludeClassCodes()), InfoCodeTemplateType::getClassCode, param.getExcludeClassCodes())
                .in(CollectionUtils.isNotEmpty(param.getRunningMonitorSecondTypes()), InfoCodeTemplateType::getRunningMonitorSecondType, param.getRunningMonitorSecondTypes())
                .in(CollectionUtils.isNotEmpty(param.getObjectTypes()), InfoCodeTemplateType::getObjectType, param.getObjectTypes())
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull InfoCodeTemplateTypeRemoveDaoParam param, boolean publishEvent) {
        boolean result = false;
        List<InfoCodeTemplateType> toBeDelTemplateTypes = lambdaQuery()
                .select(BaseAssignIdDO::getId)
                .eq(Objects.nonNull(param.getRunningMonitorType()), InfoCodeTemplateType::getRunningMonitorType, param.getRunningMonitorType())
                .eq(Objects.nonNull(param.getRunningMonitorSecondType()), InfoCodeTemplateType::getRunningMonitorSecondType, param.getRunningMonitorSecondType())
                .eq(Objects.nonNull(param.getTemplateType()), InfoCodeTemplateType::getTemplateType, param.getTemplateType())
                .in(Objects.nonNull(param.getTemplateTypes()), InfoCodeTemplateType::getTemplateType, param.getTemplateTypes())
                .eq(Objects.nonNull(param.getObjectType()), InfoCodeTemplateType::getObjectType, param.getObjectType())
                .eq(StringUtils.isNotBlank(param.getClassCode()), InfoCodeTemplateType::getClassCode, param.getClassCode())
                .in(CollectionUtils.isNotEmpty(param.getClassCodes()), InfoCodeTemplateType::getClassCode, param.getClassCodes())
                .eq(StringUtils.isNotBlank(param.getInfoCodeFilter()), InfoCodeTemplateType::getInfoCodeFilter, param.getInfoCodeFilter())
                .list();
        if (CollectionUtils.isNotEmpty(toBeDelTemplateTypes)) {
            List<Long> toBeDelTemplateTypeIds = toBeDelTemplateTypes.stream()
                    .map(BaseAssignIdDO::getId)
                    .collect(Collectors.toList());
            result = removeByIdIgnore(toBeDelTemplateTypeIds);
            if (publishEvent) {
                this.applicationEventPublisher.publishEvent(new InfoCodeTemplateTypeRemoveEvent(toBeDelTemplateTypeIds));
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfoCodeTemplateType add(@NonNull InfoCodeTemplateType config, boolean publishEvent) {
        config.checkAddEntity();
        config.clearSaveParameter();
        saveIgnore(config);
        if (publishEvent) {
            this.applicationEventPublisher.publishEvent(new InfoCodeTemplateTypeSaveEvent(config.getId()));
        }
        return config;
    }
}
