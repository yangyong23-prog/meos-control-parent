package com.persagy.apccontrol.basic.structure.demand.gateway.instance;

import com.persagy.apccontrol.basic.model.base.domain.Gateway;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceConfigAddOrEditParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.identifier.DemandInstanceConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 需求实例网关
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandInstanceGateway extends Gateway<DemandInstanceConfigIdentifier, DemandInstanceConfigEntity> {

    /**
     * 查询实例配置数量
     *
     * @param param 查询参数
     * @return 实例配置数量
     * @since 7.250301.0
     */
    int countInstanceConfigs(@NonNull @Valid DemandInstanceConfigQueryParam param);

    /**
     * 根据实例配置查询实例ID
     *
     * @param param 查询参数
     * @return 实例ID
     * @since 7.250901.0
     */
    Set<String> listInstanceIds(@NonNull @Valid DemandInstanceConfigQueryParam param);

    /**
     * 查询实例配置
     *
     * @param param 查询参数
     * @return 实例配置
     * @since 5.240902.0
     */
    List<DemandInstanceConfigEntity> listInstanceConfigs(@NonNull @Valid DemandInstanceConfigQueryParam param);

    /**
     * 查询实例下的空间功能类型和空间ID映射
     *
     * @param templateType 模版类型
     * @param instance     实例
     * @return 空间功能类型 -> 关联空间ID
     * @since 5.240902.0
     */
    Map<DtpKnowledgeInfoCodeDataSourceDTO, List<String>> mapChildrenRoomFuncTypeAndSpaceIds(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceValueObject instance);

    /**
     * 新增或编辑实例配置
     *
     * @param param 新增或编辑参数
     * @since 5.241102.0
     */
    void addOrEditInstanceConfig(@NonNull @Valid DemandInstanceConfigAddOrEditParam param);

}