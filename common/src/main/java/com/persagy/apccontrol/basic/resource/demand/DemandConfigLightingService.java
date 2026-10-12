package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.*;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 需求管理-照明需求-配置服务
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
public interface DemandConfigLightingService extends BaseService<DemandConfigLighting> {

    /**
     * 查询照明需求配置
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 照明需求配置
     * @since 24Q1 T3S1-5.17.0
     */
    DemandConfigLighting get(@NonNull String projectId, @NonNull @Valid DemandConfigLightingQueryDaoParam param);

    /**
     * 查询照明需求配置
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 照明需求配置
     * @since 24Q1 T3S1-5.17.0
     */
    List<DemandConfigLighting> list(@NonNull String projectId, @NonNull @Valid DemandConfigLightingQueryDaoParam param);

    /**
     * 新增照明需求配置
     *
     * @param projectId 项目ID
     * @param param     新增参数
     * @return 是否成功
     * @since 24Q1 T3S1-5.17.0
     */
    boolean add(@NonNull String projectId, @NonNull @Valid DemandConfigLightingSaveDaoParam param);

    /**
     * 编辑照明需求配置
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @return 是否成功
     * @since 24Q1 T3S1-5.17.0
     */
    boolean edit(@NonNull String projectId, @NonNull @Valid DemandConfigLightingUpdateDaoParam param);

    /**
     * 编辑照明需求配置
     *
     * @param domains 照明需求配置
     * @return 是否成功
     * @since 7.250602.0
     */
    boolean editDomain(@NonNull Collection<DemandConfigLighting> domains);

    /**
     * 编辑照明需求配置
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @return 是否成功
     * @since 24Q1 T3S1-5.17.0
     */
    boolean clearSetting(@NonNull String projectId, @NonNull @Valid DemandConfigLightingClearSettingDaoParam param);

    /**
     * 删除照明需求配置
     *
     * @param projectId 项目ID
     * @param param     删除参数
     * @return 是否成功
     * @since 24Q1 T3S1-5.17.0
     */
    boolean del(@NonNull String projectId, @NonNull @Valid DemandConfigLightingRemoveDaoParam param);

}
