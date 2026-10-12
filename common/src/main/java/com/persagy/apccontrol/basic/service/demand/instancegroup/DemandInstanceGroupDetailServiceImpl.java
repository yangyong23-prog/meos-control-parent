package com.persagy.apccontrol.basic.service.demand.instancegroup;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroup;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupDetailResource;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGroupDetailServiceImpl implements DemandInstanceGroupDetailService {

    private final DemandInstanceGroupDetailResource demandInstanceGroupDetailResource;
    private final DemandInstanceGroupResource demandInstanceGroupResource;

    @Override
    public void deleteDetailByGroupId(Long groupId) {
        demandInstanceGroupDetailResource.lambdaUpdate().eq(DemandInstanceGroupDetail::getRelGroupId, groupId).remove();
    }

    @Override
    public List<DemandInstanceGroupDetail> ListDetailByGroupId(Long groupId) {
        return demandInstanceGroupDetailResource.lambdaQuery().eq(DemandInstanceGroupDetail::getRelGroupId, groupId).list();
    }

    /**
     * 根据实例id查询详情列表
     *
     * @param instanceIds 实例id
     * @return {@code List<DemandInstanceGroupDetail> }
     * @since 7.250602.0
     */
    @Override
    public List<DemandInstanceGroupDetail> ListDetailByInstanceIds(Collection<String> instanceIds) {
        return demandInstanceGroupDetailResource.lambdaQuery().in(DemandInstanceGroupDetail::getInstanceId, instanceIds)
                .eq(DemandInstanceGroupDetail::getProjectId, ContextUtil.getProjectId()).list();
    }

    /**
     * 根据实例id和编组id查询详情
     *
     * @param instanceId 实例id
     * @param groupId    组id
     * @return {@code DemandInstanceGroupDetail }
     * @since 7.250602.0
     */
    @Override
    public DemandInstanceGroupDetail ListDetailByInstanceIdAndGroupId(String instanceId, Long groupId) {
        return demandInstanceGroupDetailResource.lambdaQuery().eq(DemandInstanceGroupDetail::getInstanceId, instanceId)
                .eq(DemandInstanceGroupDetail::getRelGroupId, groupId).one();
    }

    @Override
    public List<DemandInstanceGroupDetail> ListDetailByGroupIds(Collection<Long> groupIds) {
        return demandInstanceGroupDetailResource.lambdaQuery().in(DemandInstanceGroupDetail::getRelGroupId, groupIds).list();
    }

    @Override
    public List<DemandInstanceGroupDetail> ListDetailByTemplateType(@NonNull DemandTemplateTypeEnum templateType) {
        List<DemandInstanceGroup> demandInstanceGroups = demandInstanceGroupResource.lambdaQuery()
                .select(DemandInstanceGroup::getId).eq(DemandInstanceGroup::getTemplateType, templateType)
                .eq(DemandInstanceGroup::getProjectId, ContextUtil.getProjectId()).list();
        if (CollectionUtils.isEmpty(demandInstanceGroups)) {
            return new ArrayList<>();
        }
        Set<Long> ids = new HashSet<>();
        demandInstanceGroups.forEach(demandInstanceGroup -> ids.add(demandInstanceGroup.getId()));
        return demandInstanceGroupDetailResource.lambdaQuery().in(DemandInstanceGroupDetail::getRelGroupId, ids).list();
    }

    @Override
    public Boolean batchSaveDetail(Long groupId, Collection<String> instanceIds, InstanceTypeEnum instanceType) {
        List<DemandInstanceGroupDetail> demandInstanceGroupDetails = new ArrayList<>();
        for (String instanceId : instanceIds) {
            DemandInstanceGroupDetail demandInstanceGroupDetail = DemandInstanceGroupDetail.builder()
                    .projectId(ContextUtil.getProjectId())
                    .instanceId(instanceId)
                    .instanceType(instanceType)
                    .relGroupId(groupId)
                    .build();
            demandInstanceGroupDetails.add(demandInstanceGroupDetail);
        }
        return demandInstanceGroupDetailResource.saveBatch(demandInstanceGroupDetails);
    }
}
