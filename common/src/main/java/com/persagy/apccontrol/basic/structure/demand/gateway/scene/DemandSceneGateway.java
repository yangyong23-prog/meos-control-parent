package com.persagy.apccontrol.basic.structure.demand.gateway.scene;

import com.persagy.apccontrol.basic.model.base.domain.Gateway;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.param.DemandSceneSaveParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.param.DemandSceneUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 需求场景网关
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandSceneGateway extends Gateway<Long, DemandSceneEntity> {

    /**
     * 根据场景ID查询场景
     *
     * @param sceneId 场景ID
     * @return 场景
     * @since 5.240902.0
     */
    Optional<DemandSceneEntity> getScene(@NonNull Long sceneId);

    /**
     * 查询当前时间所在有效场景
     *
     * @param templateType 模版类型
     * @return 当前时间所在有效场景
     * @since 7.250801.0
     */
    Optional<DemandSceneEntity> getValidSceneByNow(@NonNull DemandTemplateTypeEnum templateType);

    /**
     * 根据指定时间查询所在有效场景
     *
     * @param templateType 模版类型
     * @param dateTime     指定时间
     * @return 当前时间所在有效场景
     * @since 7.250801.0
     */
    Optional<DemandSceneEntity> getValidSceneByTime(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDateTime dateTime);

    /**
     * 查询场景列表
     *
     * @param templateIds 模版id
     * @return 场景列表
     * @since 5.240902.0
     */
    List<DemandSceneEntity> listScenesByTemplateId(@NonNull Set<Long> templateIds);

    /**
     * 查询场景列表
     *
     * @param param 查询参数
     * @return 场景列表
     * @since 5.240902.0
     */
    List<DemandSceneEntity> listScenes(@NonNull @Valid DemandSceneQueryDaoParam param);

    /**
     * 根据日期查询有效场景列表
     *
     * @param templateType 模版类型
     * @param date         日期
     * @return 有效场景列表
     * @since 7.250801.0
     */
    List<DemandSceneEntity> listValidScenesByDate(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date);

    /**
     * 查询有效场景列表
     *
     * @param templateType 模版类型
     * @param date         日期
     * @return 有效场景列表
     * @since 7.250801.0
     */
    List<DemandSceneEntity> listValidScenesByAllDate(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull LocalDate date);

    /**
     * 保存场景
     *
     * @param scene 保存参数
     * @since 5.240902.0
     */
    void saveOrUpdate(DemandSceneEntity scene);

    /**
     * 保存场景
     *
     * @param param 保存参数
     * @since 5.240902.0
     */
    void addScene(@NonNull @Valid DemandSceneSaveParam param);

    /**
     * 编辑场景
     *
     * @param param 编辑参数
     * @since 5.240902.0
     */
    void editScene(@NonNull @Valid DemandSceneUpdateParam param);

    /**
     * 删除场景
     *
     * @param sceneId 场景ID
     * @since 5.240902.0
     */
    void delScene(@NonNull @Valid @NotNull(message = "{key_59537c1a56194bd6ae31797f5dcad6ad}") Long sceneId);

    /**
     * 删除模版下的场景
     *
     * @param templateIds 模版ID
     * @since 5.240902.0
     */
    void delSceneByTemplateId(@NonNull @Valid @NotNull(message = "{key_02bebf76336942da9503b791f451570d}") Set<Long> templateIds);

}
