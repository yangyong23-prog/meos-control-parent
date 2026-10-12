package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

/**
 * 缓存-物理世界对象查询参数
 *
 * @author JiYinchuan
 * @since 7.260401.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CacheRwdObjectQueryDaoParam extends BaseRepositoryQueryParam<CacheRwdObject> {

    /**
     * 实例类型
     */
    private DtpRwdObjectTypeEnum objectType;

    /**
     * 实例类型列表
     */
    private Collection<DtpRwdObjectTypeEnum> objectTypes;

    /**
     * 实例ID
     */
    private String objectId;

    /**
     * 实例ID列表
     */
    private Collection<String> objectIds;

    /**
     * 运行监测类型
     */
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测类型列表
     */
    private Collection<RunningMonitorTypeEnum> runningMonitorTypes;

    /**
     * 自定义索引 key
     */
    private String customIndexKey;

    /**
     * 自定义索引 key 列表
     */
    private Collection<String> customIndexKeys;

}
