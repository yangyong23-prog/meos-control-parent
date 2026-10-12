package com.persagy.apccontrol.basic.structure.infocodeconfig.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeGroupConfigImportContext;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeImportContext;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeConfigQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息点配置仓储
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface InfoCodeRepository extends Repository {

    /**
     * 查询信息点配置
     *
     * @param param 查询参数
     * @return 信息点配置
     * @since 7.250901.0
     */
    List<InfoCodeConfig> list(@NonNull @Valid InfoCodeConfigQueryDaoParam param);

    /**
     * 新增Sheet表分组配置
     *
     * @param configContexts            顶级配置上下文
     * @param groupCodeAndConfigContext 分组编码 -> 配置上下文
     * @param parentConfigId            上级配置ID
     * @since 5.240602.0
     */
    void addSheetGroupConfig(
            @NonNull List<InfoCodeGroupConfigImportContext> configContexts,
            @NonNull Map<String, InfoCodeGroupConfigImportContext> groupCodeAndConfigContext,
            @Nullable Long parentConfigId);

    /**
     * 新增Sheet表配置。
     *
     * <p>配置表保存在公共库，返回值用于调用方在完成公共库事务后，按集团和项目清理
     * 各自缓存库中的旧模板缓存。</p>
     *
     * @param context 上下文
     * @return 本次替换前已存在的模板类型ID
     * @since 5.240602.0
     */
    @NonNull Set<Long> addSheetConfig(@NonNull InfoCodeImportContext context);

    /**
     * 删除信息点配置表。
     *
     * <p>模板及其关联信息点配置均在公共库事务内删除，返回的模板类型ID由调用方用于
     * 遍历全部集团和项目清理缓存。</p>
     *
     * @param param 删除参数
     * @return 实际删除的模板类型ID
     * @since v7.260701.0
     */
    @NonNull Set<Long> delSheetConfig(
            @NonNull @Valid InfoCodeTemplateTypeRemoveDaoParam param);

    /**
     * 根据ID更新信息点名称
     *
     * @param ids          ID
     * @param infoCodeName 信息点名称
     * @since 7.250901.0
     */
    void updateInfoCodeNameByIds(@NonNull Set<Long> ids, @NonNull String infoCodeName);

    /**
     * 根据ID更新信息点名称
     *
     * @param ids               ID
     * @param infoCodeName      信息点名称
     * @param infoCodeAliasName 信息点别名
     * @since 7.250901.0
     */
    void updateInfoCodeNameByIds(
            @NonNull Set<Long> ids,
            @Nullable String infoCodeName,
            @Nullable String infoCodeAliasName);

}
