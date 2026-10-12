package com.persagy.apccontrol.basic.structure.demand.repository.scene;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 需求场景仓储
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandSceneRepository extends Repository {

    /**
     * 根据ID和日期查询场景
     *
     * @param isThrows 是否在查询失败|数据为空时抛出异常
     * @param sceneId  场景ID
     * @param date     查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     * @return 场景
     * @since 5.240902.0
     */
    DemandSceneEntity getByIdAndDate(boolean isThrows, @NonNull Long sceneId, @Nullable LocalDate date);

    /**
     * 根据ID和日期查询场景
     *
     * @param sceneIds 场景ID
     * @param date     查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     * @return 场景
     * @since 5.240902.0
     */
    List<DemandSceneEntity> listByIdsAndDate(@NonNull Set<Long> sceneIds, @Nullable LocalDate date);

    /**
     * 查询需求场景
     *
     * @param param 查询参数
     * @return 需求场景
     * @since 5.241102.0
     */
    List<DemandSceneEntity> list(@NonNull DemandSceneQueryDaoParam param);

    void saveOrUpdate(DemandSceneEntity scene);

    void delSceneByTemplateId(@NonNull Set<Long> templateIds);

    void delSceneById(@NonNull Set<Long> Ids);

}