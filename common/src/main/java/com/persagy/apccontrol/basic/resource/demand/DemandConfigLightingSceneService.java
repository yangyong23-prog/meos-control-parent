package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneRemoveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneUpdateDaoParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 需求管理-照明需求-配置场景服务
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
public interface DemandConfigLightingSceneService extends BaseService<DemandConfigLightingScene> {

    /**
     * 查询照明需求场景配置
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 照明需求场景配置
     * @since 24Q1 T3S1-5.17.0
     */
    DemandConfigLightingScene get(
            @NonNull String projectId,
            @NonNull @Valid DemandConfigLightingSceneQueryDaoParam param);

    /**
     * 查询照明需求场景配置
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 照明需求场景配置
     * @since 24Q1 T3S1-5.17.0
     */
    List<DemandConfigLightingScene> list(
            @NonNull String projectId,
            @NonNull @Valid DemandConfigLightingSceneQueryDaoParam param);

    /**
     * 新增照明需求场景配置
     *
     * @param projectId 项目ID
     * @param param     新增参数
     * @return 新增后的实例对象
     * @since 24Q1 T3S1-5.17.0
     */
    DemandConfigLightingScene add(
            @NonNull String projectId,
            @NonNull @Valid DemandConfigLightingSceneSaveDaoParam param);

    /**
     * 编辑照明需求场景配置
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @return 是否成功
     * @since 24Q1 T3S1-5.17.0
     */
    boolean edit(@NonNull String projectId, @NonNull @Valid DemandConfigLightingSceneUpdateDaoParam param);

    /**
     * 删除照明需求场景配置
     *
     * @param projectId 项目ID
     * @param param     删除参数
     * @return 是否成功
     * @since 24Q1 T3S1-5.17.0
     */
    boolean del(@NonNull String projectId, @NonNull @Valid DemandConfigLightingSceneRemoveDaoParam param);

}
