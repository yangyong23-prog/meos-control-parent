package com.persagy.apccontrol.basic.service.demand.instancegroup;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupQueryByInstanceIdParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceGroupQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandTreeBuildingFloorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandInstanceGroupVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeObjectVO;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * 需求查询服务
 *
 * @author wxf
 * @since v7.250601.0
 */
public interface DemandInstanceGroupService extends LogComponent {
    DemandTreeObjectVO treeBuildingFloorInstances(DemandTreeBuildingFloorSpaceQueryParam param);

    Boolean addInstanceGroup(DemandInstanceGroupParam param);

    Boolean editInstanceGroup(DemandInstanceGroupParam param);

    Boolean deleteInstanceGroup(DemandInstanceGroupParam param);

    Boolean copyInstanceGroup(DemandInstanceGroupParam param);

    Boolean checkNameExists(DemandInstanceGroupParam param);

    List<DemandInstanceGroupVO> listInstanceGroup(DemandInstanceGroupQueryParam param);

    List<DemandInstanceGroupVO> listInstanceGroupByInstanceId(@NonNull DemandInstanceGroupQueryByInstanceIdParam param);
}
