package com.persagy.apccontrol.basic.structure.demand.context;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigAirconditioningCustomMapper;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustom;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.resource.demand.impl.DemandConfigAirconditioningCustomServiceImpl;
import com.persagy.apccontrol.basic.resource.demand.param.airconditioning.DemandConfigAirconditioningCustomQueryDaoParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigCopyPairParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigGroupRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigAddOrEditParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryRemoveParam;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.ValidationException;
import java.util.*;

/**
 * 需求配置复制上下文
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
public class DemandConfigCopyContext<T extends BaseDemandConfig> implements LogComponent {

    // ------------------------- 入参 -------------------------

    /**
     * 复制参数
     */
    private final Pair<DemandConfigCopyPairParam, DemandConfigCopyPairParam> param;

    @SuppressWarnings("unchecked")
    private final DemandConfigGateway<T> demandConfigGateway = SpringUtil.getBean(DemandConfigGateway.class);
    private final DemandSceneGateway demandSceneGateway = SpringUtil.getBean(DemandSceneGateway.class);
    private final DemandConfigRepository demandConfigRepository = SpringUtil.getBean(DemandConfigRepository.class);
    private final DemandConfigGroupRepository demandConfigGroupRepository = SpringUtil.getBean(DemandConfigGroupRepository.class);
    private final DemandConfigAirconditioningCustomMapper demandConfigAirconditioningCustomMapper = SpringUtil.getBean(DemandConfigAirconditioningCustomMapper.class);
    private final DemandConfigAirconditioningCustomServiceImpl demandConfigAirconditioningCustomService = SpringUtil.getBean(DemandConfigAirconditioningCustomServiceImpl.class);

    // ------------------------- 过程 -------------------------

    /**
     * 来源模版
     */
    private @NonNull DemandTemplate fromTemplate;

    /**
     * 来源场景
     */
    private @Nullable DemandSceneEntity fromScene;

    /**
     * 目标模版
     */
    private @NonNull DemandTemplate toTemplate;

    /**
     * 目标场景
     */
    private @Nullable DemandSceneEntity toScene;

    /**
     * 来源需求配置
     */
    private List<DemandConfigAggregate<T>> fromConfigAggregates = Collections.emptyList();

    /**
     * 来源配置分组ID -> 目标配置分组ID
     */
    private Map<Long, Long> fromConfigGroupIdAndToConfigGroupId = Collections.emptyMap();

    /**
     * 来源空调需求定制配置
     */
    private List<DemandConfigAirconditioningCustom> fromAirconditioningConfigs = Collections.emptyList();

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    // ------------------------- 构造器 -------------------------

    /**
     * 构造器
     *
     * @param param 复制参数
     * @since 7.250801.0
     */
    public DemandConfigCopyContext(@NonNull Pair<DemandConfigCopyPairParam, DemandConfigCopyPairParam> param) {
        this.param = param;
        DemandConfigCopyPairParam fromParam = this.param.getLeft();
        DemandConfigCopyPairParam toParam = this.param.getRight();
        if (ObjectUtils.anyNotNull(fromParam.getSceneId(), toParam.getSceneId())
                && ObjectUtils.anyNull(fromParam.getSceneId(), toParam.getSceneId())) {
            throw new ValidationException("Scene id can not be null");
        }
    }

    /**
     * 填充模版和场景
     *
     * @since 7.250902.0
     */
    public void fillTemplateScene() {
        if (this.failed) {
            return;
        }
        DemandConfigCopyPairParam fromParam = param.getLeft();
        Long fromTemplateId = fromParam.getTemplateId();
        Long fromSceneId = fromParam.getSceneId();
        DemandConfigCopyPairParam toParam = param.getRight();
        Long toTemplateId = toParam.getTemplateId();
        Long toSceneId = toParam.getSceneId();
        BaseDemandTemplateParam fromTemplateParam = new BaseDemandTemplateParam();
        fromTemplateParam.setTemplateId(fromTemplateId);
        DemandTemplate fromTemplate = fromTemplateParam.queryAndGetTemplate();
        if (Objects.isNull(fromTemplate)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        DemandSceneEntity fromScene = null;
        BaseDemandTemplateParam toTemplateParam = new BaseDemandTemplateParam();
        toTemplateParam.setTemplateId(toTemplateId);
        DemandTemplate toTemplate = toTemplateParam.queryAndGetTemplate();
        if (Objects.isNull(toTemplate)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        DemandSceneEntity toScene = null;

        if (fromTemplate.getDemandTemplateType() != toTemplate.getDemandTemplateType()) {
            throw new ValidationException("Only supports copying under the same type");
        }
        if (Objects.nonNull(fromSceneId)) {
            fromScene = this.demandSceneGateway.getScene(fromSceneId).orElseThrow(() ->
                    new ValidationException("From scene id not exist"));
            if (fromScene.getDemandTemplateType() != toTemplate.getDemandTemplateType()) {
                throw new ValidationException("Scene mismatch template");
            }
        }
        if (Objects.nonNull(toSceneId)) {
            toScene = this.demandSceneGateway.getScene(toSceneId).orElseThrow(() ->
                    new ValidationException("To scene id not exist"));
            if (toScene.getDemandTemplateType() != toTemplate.getDemandTemplateType()) {
                throw new ValidationException("Scene mismatch template");
            }
        }
        this.fromTemplate = fromTemplate;
        this.fromScene = fromScene;
        this.toTemplate = toTemplate;
        this.toScene = toScene;
    }

    /**
     * 填充需求配置
     *
     * @since 7.250801.0
     */
    public void fillFromConfigs() {
        if (this.failed) {
            return;
        }
        DemandConfigByInstanceRepositoryBatchQueryParam batchQueryParam =
                new DemandConfigByInstanceRepositoryBatchQueryParam();
        batchQueryParam.setTemplateId(this.fromTemplate.getId());
        if (Objects.nonNull(this.fromScene)) {
            batchQueryParam.setSceneId(this.fromScene.getId());
        }
        List<DemandConfigAggregate<T>> configs = demandConfigGateway.listOriginConfigs(batchQueryParam);
        List<DemandConfigAirconditioningCustom> airconditioningConfigs = Collections.emptyList();
        if (this.fromTemplate.getDemandType() == DemandTypeEnum.AIRCONDITIONING) {
            DemandConfigAirconditioningCustomQueryDaoParam customQueryDaoParam =
                    new DemandConfigAirconditioningCustomQueryDaoParam();
            customQueryDaoParam.setProjectId(ContextUtil.getProjectId());
            customQueryDaoParam.setRelTemplateId(this.fromTemplate.getId().toString());
            airconditioningConfigs = demandConfigAirconditioningCustomMapper.list(customQueryDaoParam);
        }
        this.fromConfigAggregates = configs;
        this.fromAirconditioningConfigs = airconditioningConfigs;
    }

    /**
     * 复制配置分组
     *
     * @since 7.250801.0
     */
    public void copyConfigGroups() {
        if (this.failed) {
            return;
        }
        Long toTemplateId = this.toTemplate.getId();
        Long toSceneId = Objects.nonNull(this.toScene) ? this.toScene.getId() : null;
        // 目标侧采用“先删后建”，并记录来源分组ID到目标分组ID的映射，
        // 后续复制配置时需要用这张映射改写配置内部的relConfigGroupId。
        demandConfigGroupRepository.delByTemplateSceneId(this.toTemplate.getId(), toSceneId);
        if (CollectionUtils.isEmpty(this.fromConfigAggregates)) {
            return;
        }
        Map<Long, Long> fromConfigGroupIdAndToConfigGroupId = new HashMap<>();
        for (DemandConfigAggregate<T> fromAggregate : this.fromConfigAggregates) {
            DemandConfigIdentifier identifier = fromAggregate.getIdentifier();
            DemandConfigGroupEntity fromConfigGroup = identifier.getConfigGroup();
            DemandConfigValueObject<T> fromConfig = fromAggregate.getConfig();
            if (Objects.nonNull(fromConfigGroup)) {
                Long fromConfigGroupId = fromConfigGroup.getIdentifier().getGroupId();
                String fromConfigGroupName = fromConfigGroup.getGroupName();
                DemandConfigGroup toConfigGroup = new DemandConfigGroup();
                toConfigGroup.setProjectId(ContextUtil.getProjectId());
                toConfigGroup.setGroupName(demandConfigGroupRepository.generateGroupName(
                        fromConfigGroupName, toTemplateId, toSceneId));
                toConfigGroup.setRelTemplateId(toTemplateId);
                toConfigGroup.setRelSceneId(toSceneId);
                toConfigGroup = demandConfigGroupRepository.add(toConfigGroup);
                fromConfigGroupIdAndToConfigGroupId.put(fromConfigGroupId, toConfigGroup.getId());
            }
            DemandInstanceTypeEnum instanceType = identifier.getInstance().getInstanceType();
            if (instanceType == DemandInstanceTypeEnum.PROJECT && Objects.nonNull(fromConfig)) {
                List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs = fromConfig.getSpaceFuncTypeConfigs();
                if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                    // 项目级配置的空间功能类型会引用额外配置分组，这些分组不一定出现在普通配置聚合根上，
                    // 需要单独复制并写入来源->目标映射，后续复制配置时才能改写relConfigGroupId。
                    Set<Long> spaceFuncTypeConfigGroupIds = new HashSet<>();
                    for (DemandSpaceFuncTypeConfig<T> spaceFuncTypeConfig : spaceFuncTypeConfigs) {
                        Long spaceFuncTypeConfigGroupId = spaceFuncTypeConfig.getRelConfigGroupId();
                        if (Objects.nonNull(spaceFuncTypeConfigGroupId)) {
                            spaceFuncTypeConfigGroupIds.add(spaceFuncTypeConfigGroupId);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigGroupIds)) {
                        Map<Long, DemandConfigGroupEntity> configGroupIdAndEntity = demandConfigGroupRepository
                                .copyByIds(spaceFuncTypeConfigGroupIds, toTemplateId, toSceneId);
                        if (MapUtils.isNotEmpty(configGroupIdAndEntity)) {
                            for (Map.Entry<Long, DemandConfigGroupEntity> entry : configGroupIdAndEntity.entrySet()) {
                                Long fromConfigGroupId = entry.getKey();
                                DemandConfigGroupEntity toConfigGroup = entry.getValue();
                                fromConfigGroupIdAndToConfigGroupId.put(fromConfigGroupId,
                                        toConfigGroup.getIdentifier().getGroupId());
                            }
                        }
                    }
                }
            }
        }
        this.fromConfigGroupIdAndToConfigGroupId = fromConfigGroupIdAndToConfigGroupId;
    }

    /**
     * 复制配置
     *
     * @since 7.250801.0
     */
    public void copyConfigs() {
        if (this.failed) {
            return;
        }
        Long toTemplateId = this.toTemplate.getId();
        Long toSceneId = Objects.nonNull(this.toScene) ? this.toScene.getId() : null;

        // 先删除目标配置
        DemandConfigByInstanceRepositoryRemoveParam removeParam = new DemandConfigByInstanceRepositoryRemoveParam();
        removeParam.setTemplateId(toTemplateId);
        removeParam.setSceneId(toSceneId);
        demandConfigRepository.delConfigByInstance(removeParam);

        if (CollectionUtils.isNotEmpty(this.fromConfigAggregates)) {
            // 复制保存接口按实例集合批量新增配置，这里先按来源配置分组聚合，
            // 保持同一分组下多个实例的配置仍落到目标侧同一个分组。
            Map<Long, List<DemandConfigAggregate<T>>> fromConfigGroupIdAndConfigs = new HashMap<>();
            long fictitiousConfigGroupId = -1;
            for (DemandConfigAggregate<T> fromConfigAggregate : this.fromConfigAggregates) {
                DemandConfigGroupEntity configGroup = fromConfigAggregate.getIdentifier().getConfigGroup();
                // 没有配置分组的配置也要按“原本各自独立”的粒度保存，使用负数临时分组ID
                // 只在本方法内聚合参数，避免多个无分组配置被误合并成同一条批量新增参数。
                Long fromConfigGroupId = Objects.nonNull(configGroup)
                        ? configGroup.getIdentifier().getGroupId()
                        : fictitiousConfigGroupId--;
                MapExecutor.dynamicPut(fromConfigGroupIdAndConfigs, fromConfigGroupId, fromConfigAggregate, ArrayList::new);
            }
            List<DemandConfigAddOrEditParam> addParams = new ArrayList<>();
            for (Map.Entry<Long, List<DemandConfigAggregate<T>>> entry : fromConfigGroupIdAndConfigs.entrySet()) {
                Long fromConfigGroupId = entry.getKey();
                List<DemandConfigAggregate<T>> fromConfigAggregates = entry.getValue();
                DemandInstanceTypeEnum groupInstanceType = null;
                Set<String> groupInstanceIds = new HashSet<>();
                DemandConfigValueObject<T> finalConfig = null;
                for (DemandConfigAggregate<T> fromConfigAggregate : fromConfigAggregates) {
                    DemandConfigIdentifier identifier = fromConfigAggregate.getIdentifier();
                    DemandInstanceTypeEnum instanceType = identifier.getInstance().getInstanceType();
                    String instanceId = identifier.getInstance().getInstanceId();
                    Long templateId = identifier.getTemplateId();
                    DemandSceneEntity scene = identifier.getScene();
                    DemandConfigValueObject<T> config = fromConfigAggregate.getConfig();
                    if (Objects.isNull(groupInstanceType)) {
                        groupInstanceType = instanceType;
                    }
                    groupInstanceIds.add(instanceId);
                    if (Objects.isNull(finalConfig)) {
                        finalConfig = config;
                    }
                    if (Objects.isNull(config)) {
                        continue;
                    }
                    if (instanceType == DemandInstanceTypeEnum.PROJECT) {
                        // 项目级空间功能类型配置内部仍保存来源分组ID，真正新增配置前必须改成目标分组ID，
                        // 否则复制后的配置会继续引用来源模板/场景下的分组。
                        List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs = config.getSpaceFuncTypeConfigs();
                        if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                            for (DemandSpaceFuncTypeConfig<T> spaceFuncTypeConfig : spaceFuncTypeConfigs) {
                                List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes = spaceFuncTypeConfig.getFuncTypes();
                                Long relConfigGroupId = spaceFuncTypeConfig.getRelConfigGroupId();
                                if (Objects.nonNull(relConfigGroupId)) {
                                    Long toConfigGroupId = this.fromConfigGroupIdAndToConfigGroupId.get(relConfigGroupId);
                                    if (Objects.isNull(toConfigGroupId)) {
                                        logProjectInstance().warn("目标空间功能类型配置分组不存在", Arrays.asList(
                                                Pair.of("fromTemplateId", templateId),
                                                Pair.of("fromSceneId", Objects.nonNull(scene) ? scene.getId() : null),
                                                Pair.of("funcTypes", funcTypes),
                                                Pair.of("relConfigGroupId", relConfigGroupId)
                                        ));
                                    }
                                    spaceFuncTypeConfig.setRelConfigGroupId(toConfigGroupId);
                                }
                            }
                        }
                    }
                }
                Long toConfigGroupId = fromConfigGroupId > 0
                        ? this.fromConfigGroupIdAndToConfigGroupId.get(fromConfigGroupId)
                        : null;
                DemandConfigAddOrEditParam addParam = new DemandConfigAddOrEditParam();
                addParam.setTemplateId(toTemplateId);
                addParam.setSceneId(toSceneId);
                addParam.setConfigGroupId(toConfigGroupId);
                addParam.setInstanceType(groupInstanceType);
                addParam.setInstanceIds(groupInstanceIds);
                addParam.setConfigData(finalConfig);
                addParams.add(addParam);
            }
            demandConfigRepository.addOrEditConfig(addParams);
        }
        // 新增空调自定义配置
        if (this.toTemplate.getDemandType() == DemandTypeEnum.AIRCONDITIONING) {
            demandConfigAirconditioningCustomMapper.delete(Wrappers.<DemandConfigAirconditioningCustom>lambdaUpdate()
                    .eq(DemandConfigAirconditioningCustom::getProjectId, ContextUtil.getProjectId())
                    .eq(DemandConfigAirconditioningCustom::getRelTemplateId, toTemplateId)
            );
            if (CollectionUtils.isNotEmpty(this.fromAirconditioningConfigs)) {
                for (DemandConfigAirconditioningCustom airconditioningConfig : this.fromAirconditioningConfigs) {
                    // 空调定制表不走通用需求配置聚合保存，需要随模板复制单独迁移，
                    // clearSaveParameter用于清理主键和审计字段，避免覆盖来源记录。
                    airconditioningConfig.setRelTemplateId(toTemplateId.toString());
                    airconditioningConfig.clearSaveParameter();
                    demandConfigAirconditioningCustomMapper.insert(airconditioningConfig);
                }
                demandConfigAirconditioningCustomService.wirteShellConfigParam(ContextUtil.getGroupCode(),
                        ContextUtil.getProjectId(), toTemplateId.toString());
            }
        }
    }

    @Override
    public String logTag() {
        return "需求管理-复制需求配置";
    }
}
