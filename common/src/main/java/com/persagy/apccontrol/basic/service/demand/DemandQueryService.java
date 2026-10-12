package com.persagy.apccontrol.basic.service.demand;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.param.demand.*;
import com.persagy.apccontrol.basic.model.vo.demand.*;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.framework.common.entity.R;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 需求查询服务
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandQueryService {

    /**
     * 根据标签日期参数查询模版标签
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param param        查询参数
     * @return 模版标签
     * @since v6.0.0
     */
    String getTemplateTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull BaseDemandTemplateParam param);

    /**
     * 查询需求管理-对象树
     *
     * @param param 查询参数
     * @return 空间数
     */
    DemandTreeControlledObjectVO demandTree(@NonNull DemandTreeQueryParam param);

    /**
     * 查询需求实例树
     *
     * @param param 查询参数
     * @return 需求实例树
     * @since 5.241102.0
     */
    R<DemandTreeInstanceProjectVO> treeDemandInstance(@NonNull @Valid DemandTreeInstanceQueryParam param);

    /**
     * 查询实例配置
     *
     * @param param 查询参数
     * @return 实例配置
     * @since 5.241102.0
     */
    R<Collection<DemandInstanceConfigProjectVO>> listInstanceConfig(
            @NonNull @Valid DemandInstanceConfigViewQueryParam param);

    /**
     * 查询楼层下需求空间
     *
     * @param param 查询参数
     * @return 楼层下需求空间
     * @since 5.241102.0
     */
    R<Collection<DemandSpaceInFloorVO>> listDemandSpaceInFloor(
            @NonNull @Valid DemandSpaceInFloorQueryParam param);

    /**
     * 查询存在自定义配置的实例
     *
     * @param param 查询参数
     * @return 存在自定义配置的实例
     * @since 5.241102.0
     */
    R<Collection<DemandCustomizedConfigInstanceVO>> listCustomizedConfigInstance(
            @NonNull @Valid DemandCustomizedConfigInstanceQueryParam param);

    /**
     * 查询楼层下引用上级的需求空间
     *
     * @param param 查询参数
     * @return 楼层下引用上级的需求空间
     * @since 5.241102.0
     */
    R<Collection<DemandReferenceParentSpaceInFloorVO>> listDemandReferenceParentSpaceByInFloor(
            @NonNull @Valid DemandReferenceParentSpaceInFloorQueryParam param);


    /**
     * 查询管控对象列表
     *
     * @param param 查询参数
     * @return 管控对象列表
     * @since 5.240902.0
     */
    List<DtpRwdObjectDTO> listControlledObject(@NonNull @Valid DemandObjectQueryParam param);

    /**
     * 查询屏蔽对象树
     *
     * @param param 查询参数
     * @return 屏蔽对象树
     * @since 5.240902.0
     */
    DemandTreeShieldedObjectVO treeShieldedObject(@NonNull @Valid DemandTreeShieldedObjectQueryParam param);

    /**
     * 查询需求实例子集空间功能类型
     *
     * @param param 查询参数
     * @return 需求实例子集空间功能类型
     * @since 5.240902.0
     */
    List<DemandInstanceChildrenSpaceFuncTypeVO> listChildrenSpaceFuncTypes(
            @NonNull @Valid DemandInstanceChildrenSpaceFuncTypeQueryParam param);

    /**
     * 查询
     *
     * @param param 查询参数
     */
    List<DemandConfigVO<BaseDemandConfig>> queryDemandConfig(DemandConfigQueryParam param);

    /**
     * 查询需求配置预计算结果
     *
     * @param param 查询参数
     * @return 需求配置预计算结果
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResultVO<BaseDemandConfig>> listConfigResultsByInstances(
            @NonNull @Valid DemandConfigResultBatchQueryParam param);

    /**
     * 查询需求管理建筑楼层空间树
     *
     * @param param 查询参数
     * @return 需求管理建筑楼层空间树
     * @since 7.250202.0
     */
    Collection<DemandTreeBuildingFloorSpaceVO> treeBuildingFloorSpace(
            @NonNull @Valid DemandTreeBuildingFloorSpaceQueryParam param);

    /**
     * 查询空间功能类型树
     *
     * @param param 查询参数
     * @return 空间功能类型树
     */
    List<DemandTreeSpaceRoomFuncTypeVO> treeSpaceRoomFuncTypes(DemandTreeSpaceRoomFuncTypeQueryParam param);

    /**
     * 查询自定义配置实例接口
     *
     * @param param 查询参数
     * @return 自定义配置实例
     */
    List<DemandConfigGroupVO> listConfigGroups(DemandTemplateSceneParam param);

}
