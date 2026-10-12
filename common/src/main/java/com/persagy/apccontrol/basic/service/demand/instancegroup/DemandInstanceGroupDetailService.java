package com.persagy.apccontrol.basic.service.demand.instancegroup;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

public interface DemandInstanceGroupDetailService {

    void deleteDetailByGroupId(Long groupId);

    List<DemandInstanceGroupDetail> ListDetailByGroupId(Long groupId);

    /**
     * 根据实例id查询详情列表
     *
     * @param instanceIds 实例id
     * @return {@code List<DemandInstanceGroupDetail> }
     * @since 7.250602.0
     */
    List<DemandInstanceGroupDetail> ListDetailByInstanceIds(Collection<String> instanceIds);

    /**
     * 根据实例id和编组id查询详情
     *
     * @param instanceId 实例id
     * @param groupId    组id
     * @return {@code DemandInstanceGroupDetail }
     * @since 7.250602.0
     */
    DemandInstanceGroupDetail ListDetailByInstanceIdAndGroupId(String instanceId, Long groupId);

    /**
     * 根据编组id列表查询编组实例细节
     *
     * @param groupIds 组ID
     * @return {@code List<DemandInstanceGroupDetail> }
     * @since 7.250403.0
     */
    List<DemandInstanceGroupDetail> ListDetailByGroupIds(Collection<Long> groupIds);


    /**
     * 通过模版查询模版下全量明细
     *
     * @param templateType
     * @return
     */
    List<DemandInstanceGroupDetail> ListDetailByTemplateType(@NonNull DemandTemplateTypeEnum templateType);

    Boolean batchSaveDetail(Long groupId, Collection<String> instanceIds, InstanceTypeEnum instanceType);
}
