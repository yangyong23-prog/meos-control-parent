package com.persagy.apccontrol.basic.structure.infocodeconfig.repository.impl;

import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeConfigImportContext;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeGroupConfigImportContext;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeImportContext;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeSheetImportContext;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeConfigResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeGroupResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeConfigQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.repository.InfoCodeRepository;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 信息点配置仓储实现
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeRepositoryImpl implements InfoCodeRepository, BizComponent<InfoCodeRepository> {

    private final InfoCodeConverter infoCodeConverter;
    private final InfoCodeGroupResource infoCodeGroupResource;
    private final InfoCodeConfigResource infoCodeConfigResource;
    private final InfoCodeTemplateTypeResource infoCodeTemplateTypeResource;

    @Override
    public List<InfoCodeConfig> list(@NonNull InfoCodeConfigQueryDaoParam param) {
        return infoCodeConfigResource.list(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSheetGroupConfig(
            @NonNull List<InfoCodeGroupConfigImportContext> configContexts,
            @NonNull Map<String, InfoCodeGroupConfigImportContext> groupCodeAndConfigContext,
            @Nullable Long parentConfigId) {
        // 删除所有分组配置
        infoCodeGroupResource.lambdaUpdate().remove();
        // 递归新增分组配置
        deepAddSheetGroupConfig(configContexts, groupCodeAndConfigContext, parentConfigId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public @NonNull Set<Long> addSheetConfig(@NonNull InfoCodeImportContext context) {
        RunningMonitorTypeEnum runningMonitorType = context.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = context.getRunningMonitorSecondType();
        InfoCodeTemplateTypeEnum templateType = context.getTemplateType();
        List<InfoCodeSheetImportContext> sheetContexts = context.getSheetContexts();
        if (CollectionUtils.isEmpty(sheetContexts)) {
            return Collections.emptySet();
        }

        Set<String> toBeDelClassCodes = sheetContexts.stream()
                .map(InfoCodeSheetImportContext::getClassCode)
                .collect(Collectors.toSet());

        InfoCodeTemplateTypeRemoveDaoParam removeParam = new InfoCodeTemplateTypeRemoveDaoParam()
                .setRunningMonitorType(runningMonitorType)
                .setRunningMonitorSecondType(runningMonitorSecondType)
                .setTemplateType(templateType)
                .setClassCodes(toBeDelClassCodes);
        // 公共模板删除后无法再反查旧项目缓存范围，因此必须在删除前保留模板 ID。
        Set<Long> existingTemplateTypeIds = this.listTemplateTypeIds(removeParam);

        // 模板和关联信息点配置都属于公共库，必须在公共库事务中一起替换；项目缓存由上层
        // 在事务完成后按项目上下文清理，避免公共请求误操作某一个项目的缓存表。
        if (CollectionUtils.isNotEmpty(existingTemplateTypeIds)) {
            this.infoCodeConfigResource.del(existingTemplateTypeIds);
        }

        // 删除指定系统指定类型下所有模版（不发布事件，由缓存重建统一处理）
        this.infoCodeTemplateTypeResource.del(removeParam, false);

        for (InfoCodeSheetImportContext sheetContext : sheetContexts) {
            // 新增模版类型配置（不发布事件，由缓存重建统一处理）
            InfoCodeTemplateType templateTypeConfig = infoCodeConverter.toEntity(
                    sheetContext, runningMonitorType, runningMonitorSecondType, templateType);
            templateTypeConfig = infoCodeTemplateTypeResource.add(templateTypeConfig, false);
            Long templateTypeId = templateTypeConfig.getId();
            sheetContext.setTemplateTypeIdAfterAdded(templateTypeId);

            List<InfoCodeConfigImportContext> configContexts = sheetContext.getConfigContexts();

            // 强制新增信息点配置
            MultiKeyMap<String, List<MultiKey<String>>> groupCodeInfoCodeAndRel = new MultiKeyMap<>();
            List<InfoCodeConfig> infoCodeConfigs = new ArrayList<>();
            for (InfoCodeConfigImportContext configContext : configContexts) {
                InfoCodeTypeEnum infoCodeType = configContext.getInfoCodeType();
                String infoCode = configContext.getInfoCode();
                List<Pair<InfoCodeTypeEnum, String>> relInfoCodeTypeAndCodes = configContext.getRelInfoCodeTypeAndCodes();
                String relGroupCode = configContext.getRelGroupCode();

                if (CollectionUtils.isNotEmpty(relInfoCodeTypeAndCodes)) {
                    MultiKey<String> key = new MultiKey<>(relGroupCode, infoCodeType.value(), infoCode);
                    relInfoCodeTypeAndCodes.forEach(item -> MapExecutor.dynamicPut(groupCodeInfoCodeAndRel, key,
                            new MultiKey<>(relGroupCode, item.getLeft().value(), item.getRight()), ArrayList::new));
                }
                infoCodeConfigs.add(infoCodeConverter.toEntity(configContext, templateTypeId));
            }
            infoCodeConfigs = infoCodeConfigResource.addForce(infoCodeConfigs);
            // [分组编码/信息点类型/信息点编码] -> 数据库ID
            MultiKeyMap<String, Long> groupCodeInfoCodeAndConfigId = new MultiKeyMap<>();
            // 设置信息点对应的数据库ID
            infoCodeConfigs.forEach(item -> groupCodeInfoCodeAndConfigId.put(
                    item.getRelGroupCode(), item.getInfoCodeType().value(), item.getInfoCode(), item.getId()));
            // 设置上下文中新增后的ID
            configContexts.forEach(item -> item.setConfigIdAfterAdded(groupCodeInfoCodeAndConfigId.get(
                    item.getRelGroupCode(), item.getInfoCodeType().value(), item.getInfoCode())));

            groupCodeInfoCodeAndRel.forEach((key, relKey) -> {
                Long infoCodeId = groupCodeInfoCodeAndConfigId.get(key);
                if (CollectionUtils.isNotEmpty(relKey)) {
                    Set<Long> relInfoCodeIds = relKey.stream()
                            .map(groupCodeInfoCodeAndConfigId::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    // 更新关联配置ID
                    if (CollectionUtils.isNotEmpty(relInfoCodeIds)) {
                        infoCodeConfigResource.editRelConfigId(infoCodeId, relInfoCodeIds);
                    }
                }
            });
        }
        return existingTemplateTypeIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public @NonNull Set<Long> delSheetConfig(
            @NonNull InfoCodeTemplateTypeRemoveDaoParam param) {
        Set<Long> templateTypeIds = this.listTemplateTypeIds(param);
        if (CollectionUtils.isEmpty(templateTypeIds)) {
            return Collections.emptySet();
        }
        if (!this.infoCodeTemplateTypeResource.del(param, false)) {
            return Collections.emptySet();
        }
        this.infoCodeConfigResource.del(templateTypeIds);
        return templateTypeIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfoCodeNameByIds(@NonNull Set<Long> ids, @NonNull String infoCodeName) {
        this.updateInfoCodeNameByIds(ids, infoCodeName, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfoCodeNameByIds(
            @NonNull Set<Long> ids,
            @Nullable String infoCodeName,
            @Nullable String infoCodeAliasName) {
        infoCodeConfigResource.updateInfoCodeNameByIds(ids, infoCodeName, infoCodeAliasName);
    }

    /**
     * 递归新增Sheet表分组配置
     *
     * @param configContexts            顶级配置上下文
     * @param groupCodeAndConfigContext 分组编码 -> 配置上下文
     * @param parentConfigId            上级配置ID
     * @since 5.240602.0
     */
    private void deepAddSheetGroupConfig(
            @NonNull List<InfoCodeGroupConfigImportContext> configContexts,
            @NonNull Map<String, InfoCodeGroupConfigImportContext> groupCodeAndConfigContext,
            @Nullable Long parentConfigId) {
        if (CollectionUtils.isEmpty(configContexts)) {
            return;
        }
        List<InfoCodeGroup> groupConfigs = configContexts.stream()
                .map(item -> item.toAddEquipmentInfoCodeGroupConfig(parentConfigId))
                .collect(Collectors.toList());
        groupConfigs = infoCodeGroupResource.addForce(groupConfigs);

        for (InfoCodeGroup groupConfig : groupConfigs) {
            Long groupConfigId = groupConfig.getId();
            String groupCode = groupConfig.getGroupCode();

            InfoCodeGroupConfigImportContext configContext = groupCodeAndConfigContext.get(groupCode);
            configContext.setGroupConfigIdAfterAdded(groupConfigId);
            List<InfoCodeGroupConfigImportContext> children = configContext.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                deepAddSheetGroupConfig(children, groupCodeAndConfigContext, groupConfigId);
            }
        }
    }

    /**
     * 在公共库中查询删除参数对应的模板类型ID。
     *
     * <p>{@link InfoCodeTemplateTypeQueryDaoParam} 不包含信息点过滤条件，因此先按其余条件
     * 查询，再在内存中补充该条件，确保返回范围与删除语句一致。</p>
     *
     * @param param 删除参数
     * @return 模板类型ID
     * @since v7.260701.0
     */
    private @NonNull Set<Long> listTemplateTypeIds(
            @NonNull InfoCodeTemplateTypeRemoveDaoParam param) {
        InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryParam.setRunningMonitorType(param.getRunningMonitorType());
        queryParam.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
        queryParam.setTemplateType(param.getTemplateType());
        queryParam.setTemplateTypes(param.getTemplateTypes());
        queryParam.setObjectType(param.getObjectType());
        queryParam.setClassCode(param.getClassCode());
        queryParam.setClassCodes(param.getClassCodes());
        queryParam.addColumns(InfoCodeTemplateType::getId, InfoCodeTemplateType::getInfoCodeFilter);
        return this.infoCodeTemplateTypeResource.list(queryParam).stream()
                .filter(item -> StringUtils.isBlank(param.getInfoCodeFilter())
                        || Objects.equals(param.getInfoCodeFilter(), item.getInfoCodeFilter()))
                .map(InfoCodeTemplateType::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public String logTag() {
        return "信息点配置-Repository";
    }
}
