package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigUpdateDaoParam;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 需求实例配置服务
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandInstanceConfigService extends BaseService<DemandInstanceConfig> {

    /**
     * 查询需求实例配置
     *
     * @param param 查询参数
     * @return 需求实例配置
     * @since v6.0.0
     */
    Optional<DemandInstanceConfig> getOpt(@NonNull DemandInstanceConfigQueryDaoParam param);

    /**
     * 查询需求实例配置
     *
     * @param param 查询参数
     * @return 需求实例配置
     * @since v6.0.0
     */
    List<DemandInstanceConfig> list(@NonNull DemandInstanceConfigQueryDaoParam param);

    /**
     * 新增需求配置
     *
     * @param projectId 项目ID
     * @param param     新增参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean add(
            @NonNull String projectId,
            @NonNull @Valid DemandInstanceConfigSaveDaoParam param);

    /**
     * 新增需求配置
     *
     * @param projectId 项目ID
     * @param params    新增参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean add(
            @NonNull String projectId,
            @Nullable @Valid Collection<DemandInstanceConfigSaveDaoParam> params);

    /**
     * 编辑需求配置
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean edit(
            @NonNull String projectId,
            @NonNull @Valid DemandInstanceConfigUpdateDaoParam param);

    /**
     * 编辑需求配置
     *
     * @param projectId 项目ID
     * @param params    编辑参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean edit(
            @NonNull String projectId,
            @Nullable @Valid Collection<DemandInstanceConfigUpdateDaoParam> params);

}
