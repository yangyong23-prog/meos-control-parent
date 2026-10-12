package com.persagy.apccontrol.basic.structure.demand.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 需求实例仓储
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandInstanceRepository extends Repository {

    /**
     * 查询实例配置数量
     *
     * @param param 查询参数
     * @return 实例配置数量
     * @since 7.250301.0
     */
    int countConfigs(@NonNull @Valid DemandInstanceConfigRepositoryQueryParam param);

    /**
     * 根据实例配置查询实例ID
     *
     * @param param 查询参数
     * @return 实例ID
     * @since 7.250901.0
     */
    Set<String> listInstanceIds(@NonNull @Valid DemandInstanceConfigRepositoryQueryParam param);

    /**
     * 查询实例配置
     *
     * @param param 查询参数
     * @return 实例配置
     * @since 5.240902.0
     */
    List<DemandInstanceConfig> listConfig(@NonNull @Valid DemandInstanceConfigRepositoryQueryParam param);

    /**
     * 查询实例下级空间ID和空间功能类型映射
     *
     * @param templateType 模版类型
     * @param instance 实例
     * @return 下级空间ID - > 空间功能类型
     * @since 5.240902.0
     */
    Map<String, String> mapChildrenSpaceIdAndRoomFuncType(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceValueObject instance);

    /**
     * 新增实例配置
     *
     * @param configs 实例配置
     * @since 5.240902.0
     */
    void addConfigs(@NonNull List<DemandInstanceConfig> configs);

    /**
     * 编辑实例配置
     *
     * @param configs 实例配置
     * @since 5.240902.0
     */
    void editConfigs(@NonNull List<DemandInstanceConfig> configs);

    /**
     * 删除实例配置
     *
     * @param ids ID集合
     * @since 5.241102.0
     */
    void del(@NonNull Set<Long> ids);

}
