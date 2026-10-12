package com.persagy.apccontrol.common.service.demand;

import com.persagy.apccontrol.basic.model.param.demand.*;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import org.springframework.lang.NonNull;

import javax.validation.Valid;

/**
 * 需求服务
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandService<T extends BaseDemandConfig> {

    /**
     * 新增实例配置
     *
     * @param param 新增参数
     * @return 操作是否成功
     * @since 5.241102.0
     */
    boolean addInstanceConfig(@NonNull @Valid DemandInstanceConfigAddOrEditParam param);

    /**
     * 添加实例需求配置
     *
     * @param param 新增参数
     * @return 操作是否成功
     */
    Object addDemandConfig(DemandAddParam param);

    /**
     * 需求引用设置
     *
     * @param param 需求引用设置参数
     */
    void demandReferenceSettings(DemandReferenceParam param);

    /**
     * 需求清除配置
     *
     * @param param 需求清除配置参数
     */
    void demandClearSettings(DemandClearParam param);

    /**
     * 根据模版复制需求配置
     *
     * @param param 复制参数
     * @return 操作是否成功
     * @since 7.250902.0
     */
    boolean copyConfigs(@NonNull @Valid DemandConfigCopyParam param);

    /**
     * 检查配置分组名称是否存在
     *
     * @param param 检查参数
     * @return 配置分组名称是否存在
     * @since 7.250202.0
     */
    boolean checkConfigGroupNameExists(@NonNull @Valid DemandConfigGroupNameCheckParam param);

    /**
     * 清除空调需求空间功能类型
     *
     * @return 操作结果
     * @since 7.250202.0
     */
    boolean clearAirConditioningSpaceRoomFuncTypes();

}
