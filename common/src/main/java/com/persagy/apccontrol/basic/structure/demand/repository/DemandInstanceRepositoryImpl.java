package com.persagy.apccontrol.basic.structure.demand.repository;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.mapper.demand.DemandInstanceConfigMapper;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceConfigService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigQueryDaoParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 需求实例仓储实现
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceRepositoryImpl implements DemandInstanceRepository {

    private final DemandInstanceConfigService demandInstanceConfigService;
    private final DemandInstanceConfigMapper demandInstanceConfigMapper;

    @Override
    public int countConfigs(@NonNull DemandInstanceConfigRepositoryQueryParam param) {
        DemandInstanceConfigQueryDaoParam queryDaoParam = toQueryDaoParam(param);
        return demandInstanceConfigMapper.count(ContextUtil.getProjectId(), queryDaoParam);
    }

    @Override
    public Set<String> listInstanceIds(@NonNull DemandInstanceConfigRepositoryQueryParam param) {
        param.setSelectColumns(CollUtil.newLinkedHashSet(DemandInstanceConfig::getDemandInstanceId));
        return this.listConfig(param).stream().map(DemandInstanceConfig::getDemandInstanceId).collect(Collectors.toSet());
    }

    @Override
    public List<DemandInstanceConfig> listConfig(@NonNull DemandInstanceConfigRepositoryQueryParam param) {
        DemandInstanceConfigQueryDaoParam queryDaoParam = toQueryDaoParam(param);
        return demandInstanceConfigMapper.list(ContextUtil.getProjectId(), queryDaoParam, param.toSelectColumns());
    }

    @Override
    public Map<String, String> mapChildrenSpaceIdAndRoomFuncType(
            @NonNull DemandTemplateTypeEnum templateType, @NonNull DemandInstanceValueObject instance) {
        DemandStrategy demandStrategy = templateType.toDemandTypeEnum().demandStrategy();
        List<DtpRwdSpaceDTO> spaces = demandStrategy.listDemandSpaceByInstance(templateType, instance,
                DtpRwdObjectDTO.Fields.id, DtpRwdSpaceDTO.Fields.roomFuncType);
        if (CollectionUtils.isEmpty(spaces)) {
            return Maps.newHashMap();
        }
        return spaces.stream()
                .filter(item -> StringUtils.isNotBlank(item.getRoomFuncType()))
                .collect(Collectors.toMap(DtpRwdSpaceDTO::getId, DtpRwdSpaceDTO::getRoomFuncType, (o1, o2) -> o1));
    }

    @Override
    public void addConfigs(@NonNull List<DemandInstanceConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        demandInstanceConfigService.saveValidate(configs);
    }

    @Override
    public void editConfigs(@NonNull List<DemandInstanceConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        demandInstanceConfigService.updateByIdValidate(configs);
    }

    @Override
    public void del(@NonNull Set<Long> ids) {
        demandInstanceConfigService.removeByIdIgnore(ids);
    }

    /**
     * 转换数据库查询参数
     *
     * @param param 查询参数
     * @return 数据库查询参数
     * @since 7.250301.0
     */
    private DemandInstanceConfigQueryDaoParam toQueryDaoParam(@NonNull DemandInstanceConfigRepositoryQueryParam param) {
        DemandInstanceConfigValueObject filterConfig = param.getFilterConfig();
        DemandInstanceConfigQueryDaoParam queryDaoParam = new DemandInstanceConfigQueryDaoParam();
        queryDaoParam.setDate(param.getDate());
        queryDaoParam.setDemandType(param.getTemplateType().toDemandTypeEnum());
        queryDaoParam.setDemandTemplateType(param.getTemplateType());
        queryDaoParam.setDemandInstanceType(param.getInstanceType());
        queryDaoParam.setDemandInstanceIds(param.getInstanceIds());
        if (Objects.nonNull(filterConfig)) {
            if (Objects.nonNull(filterConfig.getShielded())) {
                queryDaoParam.setShielded(filterConfig.getShielded());
            }
            if (Objects.nonNull(filterConfig.getInBlacklisted())) {
                queryDaoParam.setInBlacklisted(filterConfig.getInBlacklisted());
            }
        }
        return queryDaoParam;
    }

    @Override
    public String logTag() {
        return "需求管理-需求实例仓储";
    }
}
