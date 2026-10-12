package com.persagy.apccontrol.basic.resource.demand.param.airconditioning;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Collection;

/**
 * 需求配置-空调需求查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigAirconditioningCustomQueryDaoParam {

    /**
     * 查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     */
    private LocalDate date;

    /**
     * ID
     */
    private Long id;

    /**
     * ID
     */
    private Collection<Long> ids;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 需求实例类型
     */
    private DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例类型
     */
    private Collection<DemandInstanceTypeEnum> demandInstanceTypes;

    /**
     * 需求实例ID
     */
    private String demandInstanceId;

    /**
     * 需求实例ID
     */
    private Collection<String> demandInstanceIds;

    /**
     * 关联需求模版ID
     */
    private String relTemplateId;

    /**
     * 关联需求模版ID
     */
    private Collection<String> relTemplateIds;

}
