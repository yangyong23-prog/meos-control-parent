package com.persagy.apccontrol.basic.service.demand.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.model.param.demand.lighting.*;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.*;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 照明需求查询服务
 *
 * @author yuyao
 * @since 24Q1 T3S1-5.17.0
 */
public interface DemandLightingQueryService {

    /**
     * 查询-照明回路对象配置统计
     *
     * @param param 查询参数
     * @return 返回树结构对象
     * @since 24Q1 T3S1-5.17.0
     */
    CircuitObjCfgStatisticsVO countCircuitObjCfgStatistics(CircuitObjCfgStatisticsQueryParam param);

    /**
     * 查询照明管控空间树
     *
     * @param param 查询参数
     * @return 返回树结构对象
     * @since 24Q1 T3S1-5.17.0
     */
    DemandLightingTreeObjectVO treeObject(DemandTreeQueryParam param);

    /**
     * 查询照明场景配置并检查, 检查失败时抛出异常
     *
     * @param projectId  项目ID
     * @param templateId 模版ID
     * @param sceneId    场景ID
     * @return 照明场景配置
     * @since 24Q1 T3S2-5.18.0
     */
    DemandConfigLightingScene getSceneAndCheckThrow(
            @NonNull String projectId, @NonNull Long templateId, @NonNull Long sceneId);

    /**
     * 获取场景配置数据
     *
     * @param groupCode
     * @param projectId
     * @param instanceId
     * @param templateId
     * @param sceneId
     * @return
     */
    DemandConfigLighting getSceneObjectSettings(String groupCode, String projectId, String instanceId, Long templateId, Long sceneId);

    /**
     * 查询-某场景下实例对应的配置信息
     *
     * @param param
     * @return com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandConfigLightingDataVO
     * @author yuecaipu
     * @date 2024/3/13 10:46
     */
    DemandConfigLightingDataVO getSceneObjectSettings(DemandLightingInstanceQueryParam param);

    /**
     * 获取此模板对应的照明回路功能树
     *
     * @param groupCode
     * @param projectId
     * @param templateId
     * @param sceneId
     * @return
     */
    DemandLightingTemplateTypeVO getTemplateFuncTypeList(String groupCode, String projectId, Long templateId, Long sceneId);

    /**
     * 获取照明回路列表
     *
     * @param groupCode
     * @param viewParam
     * @return
     */
    DemandLightingCircuitView getTemplateCircuitView(String groupCode, DemandLightingCircuitViewParam viewParam);

    /**
     * 获取所有空间对应的场景对象配置
     *
     * @param projectId  项目ID
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @return spaceId -> 空间对应的场景对象配置
     */
    Map<String, List<DemandLightingFuncTypeSettingDTO>> listAllSpaceSceneObjectSettings(String projectId, Long templateId, Long sceneId);

    /**
     * 获取引用对象的配置实例
     *
     * @param instanceQueryParam 实例查询参数
     * @param demandTemplate     需求模版
     * @return 引用对象的配置实例
     * @since 24Q1 T3S1-5.17.0
     */
    DemandConfigLighting getReferenceSceneObjectSettings(DemandLightingInstanceQueryParam instanceQueryParam, DemandTemplate demandTemplate);

    /**
     * 查询空间功能类型列表 自动需求
     *
     * @param groupCode
     * @param instanceQueryParam
     * @param filterExists
     * @return
     */
    List<DemandLightingSpaceFuncTypeVO> listSpaceFuncTypeList(String groupCode, DemandLightingInstanceQueryParam instanceQueryParam, boolean filterExists);

    /**
     * 查询空间功能类型列表 干预需求
     *
     * @param param 查询参数
     * @return {@link List<DemandLightingSpaceFuncTypeVO>}
     * @author yuecaipu
     * @date 2024/7/22 15:16
     * @since v5.240702
     */
    List<DemandLightingSpaceFuncTypeVO> listSpaceFuncTypeList(DemandLightingParam param);
    /**
     * 查询楼层关联照明回路
     *
     * @param param 查询参数
     * @return 楼层关联照明回路
     * @since 24Q1 T3S2-5.18.0
     */
    List<DemandLightingFloorWithCircuitVO> listFloorWithCircuits(
            @NonNull @Valid DemandLightingFloorWithCircuitsQueryParam param);

    /**
     * 根据模板ID，获取与此模板相同类型下，存在场景配置数据的其余模板列表
     *
     * @param groupCode
     * @param projectId
     * @param templateId
     * @return
     */
    List<DemandTemplateVO> listExistsSceneConfigTemplate(String groupCode, String projectId, Long templateId);

    /**
     * 查询场景数据
     *
     * @param groupCode
     * @param projectId
     * @param templateId
     * @return
     */
    List<DemandConfigLightingScene> listSceneConfig(String groupCode, String projectId, Long templateId);

    /**
     * 查询类型下所有场景数据
     *
     * @param param
     * @return
     */
    List<DemandLightingAllSceneVO> listAllScene(DemandTemplateBatchQueryParam param);

    /**
     * 查询-空间回路状态统计
     *
     * @param param
     * @return
     */
    List<DemandLightingSpaceCircuitVO> listSpaceCfgStatistics(CircuitObjCfgStatisticsQueryParam param);


    /**
     * 根据场景获取所有配置
     *
     * @param projectId
     * @param sceneId
     * @return {@link List<DemandConfigLighting>}
     * @author yuecaipu
     * @date 2024/7/26 18:01
     * @since
     */
    List<DemandConfigLighting> listLightingConfigBySceneId(String projectId, Long sceneId);


    /**
     * 空间是否允许反馈条件
     *
     * @param param 查询参数
     * @return 是否可以配置反馈条件
     */
    public Boolean spaceFeedbackAdjustSettingsCheck(SpaceFeedbackAdjustSettingsCheckParam param);
}
