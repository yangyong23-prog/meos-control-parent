package com.persagy.apccontrol.basic.resource.demand.po.instance.group;

import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroup;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * 需求实例分组PO
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandInstanceGroupPO extends DemandInstanceGroup {

    /**
     * 需求实例分组详情
     */
    private List<@NonNull DemandInstanceGroupDetail> details;

}
