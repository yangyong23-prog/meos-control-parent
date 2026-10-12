package com.persagy.apccontrol.basic.structure.demand.repository.param;

import lombok.Data;

import java.util.Set;

/**
 * 根据实例删除配置参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigByInstanceRepositoryRemoveParam {

    /**
     * 是否发布事件
     */
    private boolean publishEvent = true;

    /**
     * 配置ID
     */
    private Set<Long> configIds;

    /**
     * 模版ID
     */
    private Long templateId;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 需求实例ID
     */
    private Set<String> instanceIds;

}
