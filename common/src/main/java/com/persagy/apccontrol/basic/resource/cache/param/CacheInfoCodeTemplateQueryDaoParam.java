package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * 缓存-信息点模版查询参数
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CacheInfoCodeTemplateQueryDaoParam extends BaseRepositoryQueryParam<CacheInfoCodeTemplate> {

    /**
     * ID
     */
    private Long id;

    /**
     * ID列表
     */
    private Collection<Long> ids;

    /**
     * 运行监测类型
     */
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测类型列表
     */
    private Collection<RunningMonitorTypeEnum> runningMonitorTypes;

    /**
     * 运行监测二级类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 运行监测二级类型列表
     */
    private Collection<RunningMonitorSecondTypeEnum> runningMonitorSecondTypes;

    /**
     * 模版唯一Key
     */
    private String key;

    /**
     * 模版唯一Key列表
     */
    private Collection<String> keys;

    /**
     * 关联信息点模版ID
     */
    private Long relTemplateId;

    /**
     * 关联信息点模版ID列表
     */
    private Collection<Long> relTemplateIds;

}
