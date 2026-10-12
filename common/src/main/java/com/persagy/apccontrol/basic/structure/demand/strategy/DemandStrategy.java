package com.persagy.apccontrol.basic.structure.demand.strategy;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.external.demand.ExternalDemandConfigResultQueryParam;
import com.persagy.apccontrol.basic.model.vo.external.demand.ExternalDemandConfigResultVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandInstanceCustomizedTagDTO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 需求管理策略接口
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>getTemplateAndCheckThrow</b> - 查询需求模版并校验</li>
 *     <li><b>listTemplateType</b> - 查询模版类型列表</li>
 *     <li><b>listAllDemandSpace</b> - 查询所有需求空间</li>
 *     <li><b>notifyTemplateChanged</b> - 模版变更消息通知</li>
 *     <li><b>demandType</b> - 查询需求类型</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandStrategy {

    /**
     * 查询启用的需求模版
     *
     * @param projectId  项目ID
     * @param templateId 模版ID
     * @return 启用的需求模版, 未查询到数据时抛出异常
     * @since 24Q1 T3S2-5.18.0
     */
    DemandTemplate getTemplateAndCheckThrow(@NonNull String projectId, @NonNull Long templateId);

    /**
     * 查询模版类型列表
     *
     * @return 模版类型列表
     * @since 24Q1 T3S1-5.17.0
     */
    List<DemandTemplateTypeEnum> listTemplateTypes();

    /**
     * 查询对应模版指定层级下的需求空间
     *
     * @param templateType 需求类模版型
     * @param instance     实例值对象
     * @param withColumns  需要查询的字段信息, 为空时查询标准字段
     * @return 空间列表
     * @deprecated {@link #listDemandSpace(DemandSpacesQueryParam)}
     */
    @Deprecated
    List<DtpRwdSpaceDTO> listDemandSpaceByInstance(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns);

    /**
     * 查询对应模版指定层级下的需求空间
     *
     * @param param 需求空间查询参数
     * @return 空间列表
     * @since 5.241201.0
     */
    List<DtpRwdSpaceDTO> listDemandSpace(@NonNull @Valid DemandSpacesQueryParam param);

    /**
     * 获取需求实例自定义标签
     *
     * @param templateType 需求类模版型
     * @param instances    需求实例
     * @return 需求实例自定义标签DTO
     * @since 5.241102.0
     */
    List<DemandInstanceCustomizedTagDTO> mapDemandInstanceCustomizedTagNameAndValue(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<DemandInstanceValueObject> instances);

    /**
     * 查询需求类型
     *
     * @return 需求类型
     * @since 24Q1 T3S1-5.17.0
     */
    DemandTypeEnum demandType();
    
    /**
     * 查询需求配置结果
     * 外部调用获取配置
     *
     * @param queryParam
     * @return
     */
    List<? extends ExternalDemandConfigResultVO> externalListConfigResultsByInstance(ExternalDemandConfigResultQueryParam queryParam);
    
}
