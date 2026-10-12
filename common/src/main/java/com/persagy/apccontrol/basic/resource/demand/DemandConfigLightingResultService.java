package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.param.lighting.CircuitConfigResultsQueryParam;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitConfigResultVO;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingResultQueryDaoParam;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 需求管理-照明需求-配置结果服务
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
public interface DemandConfigLightingResultService extends BaseService<DemandConfigLightingResult> {

    /**
     * 查询照明配置结果
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 照明配置结果
     * @since 24Q1 T3S1-5.17.0
     */
    DemandConfigLightingResult get(@NonNull String projectId, @NonNull DemandConfigLightingResultQueryDaoParam param);

    /**
     * 查询照明配置结果
     *
     * @param param     查询参数
     * @return 照明配置结果
     * @since 7.260101.0
     */
    default List<DemandConfigLightingResult> list(@NonNull DemandConfigLightingResultQueryDaoParam param) {
        return this.list(ContextUtil.getProjectId(), param);
    }

    /**
     * 查询照明配置结果
     *
     * @param param     查询参数
     * @return 照明配置结果
     * @since 24Q1 T3S1-5.17.0
     */
    List<DemandConfigLightingResult> list(@NonNull String projectId,
                                          @NonNull DemandConfigLightingResultQueryDaoParam param);


    /**
     * 批量保存
     *
     * @param param
     */
    void batchSave(@NonNull List<DemandConfigLightingResult> param);

    /**
     * 批量删除 通过场景 ID 加回路 Id 加回路
     * @param circuitId
     */
    void deleteByCircuitId(@NonNull Long templateId, @NonNull Long sceneId, @NonNull List<String> circuitId);

    /**
     * 通过模版场景 DI 删除数据
     *
     * @param templateId
     * @param sceneId
     */
    void deleteByTemplateAndScene(@NonNull Long templateId, Long sceneId);

    /**
     * 查询照明配置结果统计
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 照明配置结果
     * @since 24Q1 T3S1-5.17.0
     */
    int cfgCount(
            @NonNull String projectId,
            @NonNull DemandConfigLightingResultQueryDaoParam param);

    /**
     * 删除
     *
     * @param projectId  项目ID
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @return int
     * @since 24Q1 T3S2-5.18.0
     */
    int deleteByProjectIdAndTemplateIdAndSceneId(@NotNull String projectId, @NotNull Long templateId, Long sceneId);


    /**
     * 查询照明配置结果
     *
     * @param queryParam
     * @return
     */
    List<LightingCircuitConfigResultVO> listCircuitConfigResults(CircuitConfigResultsQueryParam queryParam);


}
