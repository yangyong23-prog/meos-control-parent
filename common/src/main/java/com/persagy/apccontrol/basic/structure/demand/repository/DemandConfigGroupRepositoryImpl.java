package com.persagy.apccontrol.basic.structure.demand.repository;

import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigGroupNameCheckParam;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigGroupService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryQueryParam;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * 需求配置组仓储实现
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigGroupRepositoryImpl implements DemandConfigGroupRepository {

    private final DemandConfigConverter demandConfigConverter;
    private final DemandConfigGroupService demandConfigGroupService;

    @Override
    public Optional<DemandConfigGroup> getOpt(@NonNull DemandConfigGroupRepositoryQueryParam param) {
        return list(demandConfigConverter.toGroupBatchQueryParam(param)).stream().findFirst();
    }

    @Override
    public List<DemandConfigGroup> list(@NonNull DemandConfigGroupRepositoryBatchQueryParam param) {
        return demandConfigGroupService.list(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandConfigGroup add(@NonNull DemandConfigGroup configGroup) {
        DemandConfigGroupNameCheckParam checkParam = new DemandConfigGroupNameCheckParam();
        checkParam.setTemplateId(configGroup.getRelTemplateId());
        checkParam.setSceneId(configGroup.getRelSceneId());
        checkParam.setConfigGroupName(configGroup.getGroupName());
        if (checkConfigGroupNameExists(checkParam)) {
            logProjectInstance().warn("分组名称已存在", Arrays.asList(
                    Pair.of("groupCode", configGroup.getGroupName()),
                    Pair.of("relTemplateId", configGroup.getRelTemplateId()),
                    Pair.of("relSceneId", configGroup.getRelSceneId())
            ));
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_4753efb1799d4a7f8251dae6d98df900"));
        }
        return demandConfigGroupService.add(configGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editGroupName(@NonNull Long configGroupId, @NonNull String groupName) {
        DemandConfigGroupRepositoryQueryParam queryParam = new DemandConfigGroupRepositoryQueryParam();
        queryParam.setConfigGroupId(configGroupId);
        Optional<DemandConfigGroup> configGroupOpt = getOpt(queryParam);
        if (!configGroupOpt.isPresent()) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_140b89158c8a434dac66baa360eeafbe"));
        }
        DemandConfigGroup configGroup = configGroupOpt.get();
        // 分组名称一致不需要编辑
        if (StringUtils.equals(configGroup.getGroupName(), StringUtils.trimToNull(groupName))) {
            return;
        }
        DemandConfigGroupNameCheckParam checkParam = new DemandConfigGroupNameCheckParam();
        checkParam.setTemplateId(configGroup.getRelTemplateId());
        checkParam.setSceneId(configGroup.getRelSceneId());
        checkParam.setConfigGroupName(configGroup.getGroupName());
        checkParam.setExcludeConfigGroupId(configGroupId);
        if (checkConfigGroupNameExists(checkParam)) {
            logProjectInstance().warn("分组名称已存在", Arrays.asList(
                    Pair.of("groupCode", groupName),
                    Pair.of("relTemplateId", configGroup.getRelTemplateId()),
                    Pair.of("relSceneId", configGroup.getRelSceneId())
            ));
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_4753efb1799d4a7f8251dae6d98df900"));
        }
        demandConfigGroupService.editGroupName(configGroupId, groupName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(@NonNull Set<Long> configGroupIds) {
        demandConfigGroupService.removeByIdIgnore(configGroupIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delByTemplateSceneId(@NonNull Long templateId, @Nullable Long sceneId) {
        demandConfigGroupService.lambdaUpdate()
                .eq(DemandConfigGroup::getRelTemplateId, templateId)
                .eq(Objects.nonNull(sceneId), DemandConfigGroup::getRelSceneId, sceneId)
                .remove();
    }

    @Override
    public String generateGroupName(@NonNull String groupName, @NonNull Long templateId, @Nullable Long sceneId) {
        DemandConfigGroupNameCheckParam groupNameCheckParam = new DemandConfigGroupNameCheckParam();
        groupNameCheckParam.setConfigGroupName(groupName);
        groupNameCheckParam.setTemplateId(templateId);
        groupNameCheckParam.setSceneId(sceneId);
        int groupNameRepeatCount = 1;
        while (this.checkConfigGroupNameExists(groupNameCheckParam)) {
            groupNameCheckParam.setConfigGroupName(groupName + "-" + groupNameRepeatCount++);
        }
        return groupNameCheckParam.getConfigGroupName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, DemandConfigGroupEntity> copyByIds(
            @NonNull Set<Long> configGroupIds,
            @NonNull Long templateId,
            @Nullable Long sceneId) {
        if (CollectionUtils.isEmpty(configGroupIds)) {
            return Collections.emptyMap();
        }
        DemandConfigGroupRepositoryBatchQueryParam queryParam = new DemandConfigGroupRepositoryBatchQueryParam();
        queryParam.setConfigGroupIds(configGroupIds);
        List<DemandConfigGroup> configGroups = demandConfigGroupService.list(queryParam);
        if (CollectionUtils.isEmpty(configGroups)) {
            return Collections.emptyMap();
        }
        Map<Long, DemandConfigGroupEntity> fromConfigGroupIdAndToEntity = new HashMap<>();
        for (DemandConfigGroup configGroup : configGroups) {
            Long fromConfigGroupId = configGroup.getId();
            configGroup.setGroupName(this.generateGroupName(configGroup.getGroupName(), templateId, sceneId));
            configGroup.setRelTemplateId(templateId);
            configGroup.setRelSceneId(sceneId);
            DemandConfigGroup toConfigGroup = demandConfigGroupService.add(configGroup);
            fromConfigGroupIdAndToEntity.put(fromConfigGroupId, DemandConfigGroupEntity.of(toConfigGroup));
        }
        return fromConfigGroupIdAndToEntity;
    }

    @Override
    public boolean checkConfigGroupNameExists(@NonNull DemandConfigGroupNameCheckParam param) {
        String configGroupName = param.getConfigGroupName();
        // 特殊分组名称不需要校验
        if (StringUtils.equals(configGroupName, FICTITIOUS_GROUP_NAME)) {
            return false;
        }
        DemandConfigGroupRepositoryBatchQueryParam queryParam = demandConfigConverter.toGroupBatchQueryParam(param);
        queryParam.addColumns(BaseAssignIdDO::getId);
        return CollectionUtils.isNotEmpty(list(queryParam));
    }

    @Override
    public String logTag() {
        return "需求管理-需求配置组仓储";
    }
}
