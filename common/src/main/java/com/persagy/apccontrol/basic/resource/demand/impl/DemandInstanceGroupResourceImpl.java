package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.DemandInstanceGroupMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroup;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupDetailResource;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupDetailQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.structure.demand.converter.instance.group.DemandInstanceGroupConverter;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGroupResourceImpl extends BaseServiceImpl<DemandInstanceGroupMapper, DemandInstanceGroup>
        implements DemandInstanceGroupResource {

    private final DemandInstanceGroupConverter demandInstanceGroupConverter;
    private final DemandInstanceGroupDetailResource demandInstanceGroupDetailResource;

    @Override
    public List<DemandInstanceGroupPO> list(@NonNull DemandInstanceGroupQueryDaoParam param) {
        boolean queryDetail = param.isQueryDetail();
        DemandInstanceGroupDetailQueryDaoParam detailQueryParam = param.getDetailQueryParam();
        List<DemandInstanceGroupPO> groups = lambdaQuery()
                .select(param.toSelectFunctions())
                .eq(Objects.nonNull(param.getId()), DemandInstanceGroup::getId, param.getId())
                .eq(DemandInstanceGroup::getProjectId, ContextUtil.getProjectId())
                .eq(Objects.nonNull(param.getTemplateType()), DemandInstanceGroup::getTemplateType, param.getTemplateType())
                .in(CollectionUtils.isNotEmpty(param.getTemplateTypes()), DemandInstanceGroup::getTemplateType, param.getTemplateTypes())
                .in(CollectionUtils.isNotEmpty(param.getIds()), DemandInstanceGroup::getId, param.getIds())
                .eq(StringUtils.isNotBlank(param.getTag()), DemandInstanceGroup::getTag, param.getTag())
                .list().stream()
                .map(demandInstanceGroupConverter::toPo)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }
        if (queryDetail) {
            detailQueryParam = Optional.ofNullable(detailQueryParam)
                    .orElseGet(DemandInstanceGroupDetailQueryDaoParam::new);
            detailQueryParam.setRelGroupIds(groups.stream().map(BaseAssignIdDO::getId).collect(Collectors.toSet()));
            List<DemandInstanceGroupDetail> details = demandInstanceGroupDetailResource.list(detailQueryParam);
            if (CollectionUtils.isNotEmpty(details)) {
                Map<Long, List<DemandInstanceGroupDetail>> groupIdAndDetails = details.stream().collect(
                        Collectors.groupingBy(DemandInstanceGroupDetail::getRelGroupId));
                for (DemandInstanceGroupPO group : groups) {
                    group.setDetails(groupIdAndDetails.get(group.getId()));
                }
            }
        }
        return groups;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandInstanceGroup add(@NonNull DemandInstanceGroup domain) {
        domain.clearSaveParameter();
        saveValidate(domain);
        return domain;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull Set<Long> ids) {
        return removeByIdIgnore(ids);
    }
}
