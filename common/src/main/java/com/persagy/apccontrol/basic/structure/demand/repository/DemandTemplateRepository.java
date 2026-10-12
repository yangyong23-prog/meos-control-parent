package com.persagy.apccontrol.basic.structure.demand.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 需求模版仓储
 *
 * <p>需求模版具有<b>是否为预置模版</b>和<b>是否启用</b>两个概念, 使用时需要注意
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandTemplateRepository extends Repository {

    /**
     * 根据ID查询模版
     *
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param templateId 模版ID
     * @return 模版
     * @since 5.240902.0
     */
    DemandTemplate getByTemplateId(boolean isThrows, @NonNull Long templateId);

    /**
     * 根据ID查询模版
     *
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param templateId 模版ID
     * @param date       查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     * @return 模版
     * @since 5.241102.0.0
     */
    DemandTemplate getByTemplateIdAndDate(boolean isThrows, @NonNull Long templateId, @Nullable LocalDate date);

    /**
     * 根据模版信息查询模版
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param templateType 模版类型
     * @param templateTag  模版标签
     * @return 模版
     * @since 5.240902.0
     */
    DemandTemplate getByTemplateByTemplateTypeTag(
            boolean isThrows,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull String templateTag);

    /**
     * 根据模版ID查询模版
     *
     * @param templateIds 模版ID
     * @return 模版
     * @since 7.250801.0
     */
    List<DemandTemplate> listByTemplateIds(@NonNull Set<Long> templateIds);

    /**
     * 根据模版类型查询模版ID
     *
     * @param templateType 模版类型
     * @return 模版ID
     * @since 5.240202.0
     */
    List<Long> listTemplateIdsByTemplateType(@NonNull DemandTemplateTypeEnum templateType);

}
