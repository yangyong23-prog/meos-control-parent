package com.persagy.apccontrol.basic.structure.demand.repository;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 需求配置结果刷新仓储。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface DemandConfigResultRefreshRepository {

    /**
     * 查询当前已接入结果预计算的模板类型。
     *
     * <p>调用方必须使用该统一列表遍历结果预计算范围，不得自行硬编码需求类型或模板类型。</p>
     *
     * @return 支持结果预计算的模板类型
     * @since v7.260701.0
     */
    @NonNull List<DemandTemplateTypeEnum> listSupportedTemplateTypes();

    /**
     * 判断模板类型是否属于当前已接入结果预计算的需求类型。
     *
     * @param templateType 模板类型
     * @return 是否支持结果预计算
     * @since v7.260701.0
     */
    boolean supportsTemplateType(@NonNull DemandTemplateTypeEnum templateType);

    /**
     * 判断模板是否属于当前已接入结果预计算的需求类型。
     *
     * @param templateId 模板ID
     * @return 是否支持结果预计算
     * @since v7.260701.0
     */
    boolean supportsTemplate(@NonNull Long templateId);

    /**
     * 刷新单个实例。
     *
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @param instance   实例
     * @since v7.260701.0
     */
    void refreshInstance(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull @Valid DemandInstanceValueObject instance);

    /**
     * 刷新当前实例及下级实例。
     *
     * @param templateId   模板ID
     * @param sceneId      场景ID
     * @param instanceType 当前实例类型
     * @param instanceIds  当前实例ID
     * @since v7.260701.0
     */
    void refreshByInstanceAndChildren(
            @NonNull Long templateId,
            @Nullable Long sceneId,
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull Set<String> instanceIds);

    /**
     * 刷新模板下所有实例。
     *
     * @param templateId 模板ID
     * @since v7.260701.0
     */
    void refreshByTemplate(@NonNull Long templateId);

    /**
     * 刷新模板类型下所有模板的所有实例。
     *
     * @param templateType 模板类型
     * @since v7.260701.0
     */
    void refreshByTemplateType(@NonNull DemandTemplateTypeEnum templateType);

    /**
     * 根据模板类型和变更实例刷新对应模板、场景下的实例。
     *
     * @param templateType 模板类型
     * @param instances    变更实例
     * @since v7.260701.0
     */
    void refreshByDtxChangedInstances(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<DemandInstanceValueObject> instances);

    /**
     * 更新屏蔽状态，不重新计算配置内容。
     *
     * @param templateType 模板类型
     * @param instanceIds  空间实例ID
     * @param shielded     是否屏蔽
     * @since v7.260701.0
     */
    void updateShielded(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> instanceIds,
            boolean shielded);

    /**
     * 删除模板类型下指定实例的预计算结果。
     *
     * @param templateType 模板类型
     * @param instances    实例集合
     * @since v7.260701.0
     */
    void removeByTemplateTypeAndInstances(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<DemandInstanceValueObject> instances);

}
