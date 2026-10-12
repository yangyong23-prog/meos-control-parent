package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateUpdateDaoParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 需求模版服务
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface DemandTemplateService extends BaseService<DemandTemplate> {

    /**
     * 查询需求模版
     *
     * @param param 查询参数
     * @return 需求模版
     * @since v6.0.0
     */
    Optional<DemandTemplate> get(@NonNull DemandTemplateQueryDaoParam param);

    /**
     * 根据需求类型查询启用的有效需求模版
     *
     * @param templateId 模版ID
     * @param demandType 需求类型
     * @return 启用的有效需求模版
     * @since 24Q1 T3S2-5.18.0
     */
    DemandTemplate getEnabledByDemandType(@NonNull Long templateId, @NonNull DemandTypeEnum demandType);

    /**
     * 查询需求模版
     *
     * @param param 查询参数
     * @return 需求模版
     * @since v6.0.0
     */
    List<DemandTemplate> list(@NonNull DemandTemplateQueryDaoParam param);

    /**
     * 新增需求模版
     *
     * @param projectId 项目ID
     * @param param     新增参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean add(@NonNull String projectId, @NonNull @Valid DemandTemplateSaveDaoParam param);

    /**
     * 新增需求模版
     *
     * @param projectId 项目ID
     * @param params    新增参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean add(@NonNull String projectId, @NonNull @Valid Collection<DemandTemplateSaveDaoParam> params);

    /**
     * 新增自定义需求模版
     *
     * @param projectId 项目ID
     * @param params    新增参数
     * @return 新增后的模版实体
     * @since v6.0.0
     */
    List<DemandTemplate> addCustom(
            @NonNull String projectId,
            @NonNull @Valid Collection<DemandTemplateSaveDaoParam> params);

    /**
     * 编辑需求模版
     *
     * @param projectId 项目ID
     * @param param     编辑参数
     * @return 是否成功
     * @since v6.0.0
     */
    boolean edit(@NonNull String projectId, @NonNull @Valid DemandTemplateUpdateDaoParam param);

    /**
     * 编辑需求模版
     *
     * @param projectId 项目ID
     * @param params    编辑参数
     * @return 编辑后的模版实体
     * @since v6.0.0
     */
    List<DemandTemplate> edit(
            @NonNull String projectId,
            @NonNull @Valid Collection<DemandTemplateUpdateDaoParam> params);

}
