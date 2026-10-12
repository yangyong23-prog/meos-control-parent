package com.persagy.apccontrol.basic.structure.demand.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigGroupNameCheckParam;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryQueryParam;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 需求配置组仓储
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
public interface DemandConfigGroupRepository extends Repository {

    String FICTITIOUS_GROUP_NAME = "fictitiousGroupName";

    /**
     * 查询配置组
     *
     * @param param 查询参数
     * @return 配置组
     * @since 7.250202.0
     */
    Optional<DemandConfigGroup> getOpt(@NonNull @Valid DemandConfigGroupRepositoryQueryParam param);

    /**
     * 查询配置组
     *
     * @param param 查询参数
     * @return 配置组
     * @since 7.250202.0
     */
    List<DemandConfigGroup> list(@NonNull @Valid DemandConfigGroupRepositoryBatchQueryParam param);

    /**
     * 新增分组配置
     *
     * @param configGroup 分组配置
     * @since 7.250202.0
     */
    DemandConfigGroup add(@NonNull DemandConfigGroup configGroup);

    /**
     * 编辑分组名称
     *
     * @param configGroupId 分组ID
     * @param groupName     分组名称
     * @since 7.250202.0
     */
    void editGroupName(@NonNull Long configGroupId, @NonNull String groupName);

    /**
     * 删除配置组
     *
     * @param configGroupIds 配置组ID
     */
    void del(@NonNull Set<Long> configGroupIds);

    /**
     * 根据模版场景ID删除配置组
     *
     * @param templateId 模版ID
     * @param sceneId    场景ID
     * @since 7.250902.0
     */
    void delByTemplateSceneId(@NonNull Long templateId, @Nullable Long sceneId);

    /**
     * 生成分组名称
     *
     * @param groupName  分组名称
     * @param templateId 模版ID
     * @param sceneId    场景ID
     * @return 分组名称
     * @since 7.250801.0
     */
    String generateGroupName(@NonNull String groupName, @NonNull Long templateId, @Nullable Long sceneId);

    /**
     * 复制配置组
     *
     * @param configGroupIds 配置组ID
     * @param templateId     模版ID
     * @param sceneId        场景ID
     * @return 配置组ID -> 复制后的配置组
     * @since 7.250801.0
     */
    Map<Long, DemandConfigGroupEntity> copyByIds(
            @NonNull Set<Long> configGroupIds,
            @NonNull Long templateId,
            @Nullable Long sceneId);

    /**
     * 检查分组名称是否存在
     *
     * @param param 检查参数
     * @return 分组名称是否存在
     * @since 7.250202.0
     */
    boolean checkConfigGroupNameExists(@NonNull @Valid DemandConfigGroupNameCheckParam param);

}
