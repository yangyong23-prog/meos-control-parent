package com.persagy.apccontrol.basic.structure.infocodeconfig.context;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.equipmentlist.ConfigEquipmentListHeaderDTO;
import com.persagy.apccontrol.basic.model.dto.equipmentlist.InfoCodeDefineModelCfgDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.page.DefaultListPage;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeConfigResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeGroupResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeConfigQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.service.instance.config.impl.InstanceConfigEquipmentListHeaderService;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheIotRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle.InfoCodeAggregateLifeAware;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 信息点聚合根初始化上下文
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeAggregateInitContext implements LogComponent {

    // ------------------------- 入参 -------------------------

    /**
     * 初始化参数
     */
    private final @NonNull InfoCodeInitByObjectParam param;

    /**
     * 生命周期
     */
    private final @NonNull List<InfoCodeAggregateLifeAware> awares;

    /**
     * 信息点转换器
     */
    private final InfoCodeConverter converter = SpringUtil.getBean(InfoCodeConverter.class);

    /**
     * 信息点模版类型
     */
    private final InfoCodeTemplateTypeResource infoCodeTemplateTypeResource = SpringUtil.getBean(InfoCodeTemplateTypeResource.class);

    /**
     * 信息点分组
     */
    private final InfoCodeGroupResource infoCodeGroupResource = SpringUtil.getBean(InfoCodeGroupResource.class);

    /**
     * 信息点配置
     */
    private final InfoCodeConfigResource infoCodeConfigResource = SpringUtil.getBean(InfoCodeConfigResource.class);

    /**
     * 信息点聚合根生命周期Aware
     */
    private final ObjectProvider<InfoCodeAggregateLifeAware> infoCodeAggregateLifeAware = SpringUtil.getBeanFactory().getBeanProvider(InfoCodeAggregateLifeAware.class);

    /**
     * IOT数据服务
     */
    @SuppressWarnings("rawtypes")
    private final AbstractIotDataCacheServer abstractIotDataCacheServer = SpringUtil.getBean(AbstractIotDataCacheServer.class);

    /**
     * 实例配置模版类型服务
     */
    private final InstanceConfigEquipmentListHeaderService instanceConfigEquipmentListHeaderService = SpringUtil.getBean(InstanceConfigEquipmentListHeaderService.class);

    /**
     * 信息点网关
     */
    private final InfoCodeGateway infoCodeGateway = SpringUtil.getBean(InfoCodeGateway.class);

    /**
     * Redis缓存模版
     */
    private final RedisCacheTemplate redisCacheTemplate = SpringUtil.getBean(RedisCacheTemplate.class);

    /**
     * DTP-RWD客户端服务
     */
    private final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);

    /**
     * 缓存服务
     */
    private final CacheRwdDomainService cacheRwdDomainService = SpringUtil.getBean(CacheRwdDomainService.class);

    /**
     * 缓存IOT规则
     */
    private final CacheIotRuleRepository cacheIotRuleRepository = SpringUtil.getBean(CacheIotRuleRepository.class);

    /**
     * 新缓存规则主表仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRuleRepository cacheRuleRepository =
            SpringUtil.getBean(CacheRuleRepository.class);

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService =
            SpringUtil.getBean(CacheObjectModelStatusService.class);

    // ------------------------- 过程 -------------------------

    /**
     * 模版类型ID -> 关联对象
     */
    private Map<Long, List<DtpRwdObjectDTO>> templateTypeIdAndObjects = Collections.emptyMap();

    /**
     * 模版类型ID -> 模版类型配置
     */
    private Map<Long, InfoCodeTemplateTypeModel> templateTypeIdAndData = Collections.emptyMap();

    /**
     * 过滤模版类型ID -> [分组编码 -> 信息点编码]
     */
    private Map<Long, Map<String, Set<String>>> filterTemplateTypeIdGroupCodeAndInfoCodes = Collections.emptyMap();

    /**
     * 模版类型ID -> 信息点配置
     */
    private Map<Long, List<InfoCodeConfig>> templateTypeIdAndInfoCodeConfigs = Collections.emptyMap();

    /**
     * 对象ID -> 存在IOT规则的信息点
     */
    private Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = Collections.emptyMap();

    /**
     * 分组编码 -> 分组配置
     */
    private Map<String, InfoCodeGroup> groupCodeAndData = Collections.emptyMap();

    /**
     * 上级分组ID -> 分组配置
     */
    private Map<Long, InfoCodeGroup> parentGroupIdAndData = Collections.emptyMap();

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = true;

    // ------------------------- 行为 -------------------------

    /**
     * 填充模版类型
     *
     * @since 5.240602.0
     */
    public void fillTemplateTypes() {
        RunningMonitorTypeEnum runningMonitorType = this.param.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = this.param.getRunningMonitorSecondType();
        Set<Long> templateTypeIds = this.param.getTemplateTypeIds();
        Collection<InfoCodeTemplateTypeEnum> templateTypes = this.param.getTemplateTypes();
        Collection<InfoCodeTemplateTypeModel> templateTypeModels = this.param.getTemplateTypeModels();
        Boolean forceMathTemplate = this.param.getForceMathTemplate();
        Collection<DtpRwdObjectDTO> objects = this.param.getObjects();

        if (CollectionUtils.isEmpty(templateTypeModels)) {
            templateTypeModels = Collections.emptyList();
            Map<String, List<DtpRwdObjectDTO>> classCodeAndObjects = this.param.getObjects().stream()
                    .collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode));
            InfoCodeTemplateTypeQueryDaoParam queryParam = new InfoCodeTemplateTypeQueryDaoParam();
            queryParam.setTemplateTypeIds(templateTypeIds);
            queryParam.setRunningMonitorType(runningMonitorType);
            queryParam.setRunningMonitorSecondType(runningMonitorSecondType);
            queryParam.setTemplateTypes(templateTypes);
            queryParam.setClassCodes(classCodeAndObjects.keySet());
            List<InfoCodeTemplateType> resTemplateTypes = infoCodeTemplateTypeResource.list(queryParam);
            if (CollectionUtils.isNotEmpty(resTemplateTypes)) {
                InfoCodeConverter converter = SpringUtil.getBean(InfoCodeConverter.class);
                templateTypeModels = resTemplateTypes.stream()
                        .map(converter::toTemplateTypeModel)
                        .sorted(Comparator.comparing(item -> Objects.nonNull(item.parseFilterValueObject()) ? 1 : 0))
                        .collect(Collectors.toList());
            } else {
                logProjectInstance().warn("模版不存在", Pair.of("queryParam", queryParam));
            }
        }
        for (InfoCodeAggregateLifeAware aware : awares) {
            templateTypeModels = aware.handleTemplateTypes(this.param, new ArrayList<>(templateTypeModels));
        }
        Map<Long, InfoCodeTemplateTypeModel> templateTypeIdAndData = new HashMap<>();
        Map<Long, List<DtpRwdObjectDTO>> templateTypeIdAndObjects = new HashMap<>();
        for (DtpRwdObjectDTO object : objects) {
            for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
                Long templateTypeId = templateTypeModel.getTemplateTypeId();
                if (BooleanUtils.isTrue(forceMathTemplate) || templateTypeModel.match(object)) {
                    templateTypeIdAndData.putIfAbsent(templateTypeId, templateTypeModel);
                    MapExecutor.dynamicPut(templateTypeIdAndObjects, templateTypeId, object, ArrayList::new);
                }
            }
        }
        if (MapUtils.isNotEmpty(templateTypeIdAndData)) {
            this.failed = false;
            this.templateTypeIdAndData = templateTypeIdAndData;
            this.templateTypeIdAndObjects = templateTypeIdAndObjects;
        }
    }

    /**
     * 填充分组信息点过滤配置
     *
     * @since 7.250802.0
     */
    public void fillInfoCodeGroupConfigFilters() {
        if (this.failed || MapUtils.isEmpty(this.templateTypeIdAndData)) {
            this.failed = true;
            return;
        }
        if (BooleanUtils.isNotTrue(this.param.getFilterCustomGroupInfoCodeConfig())) {
            return;
        }
        RunningMonitorTypeEnum runningMonitorType = this.getParam().getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = this.getParam().getRunningMonitorSecondType();

        Map<Long, Map<String, Set<String>>> templateTypeIdGroupCodeAndInfoCodeFilters = new HashMap<>();
        for (InfoCodeTemplateTypeModel templateTypeModel : this.templateTypeIdAndData.values()) {
            Long templateTypeId = templateTypeModel.getTemplateTypeId();
            String templateTypeKey = templateTypeModel.getTemplateTypeKey();
            ConfigEquipmentListHeaderDTO instanceConfig = instanceConfigEquipmentListHeaderService.getInstanceConfig(
                    runningMonitorSecondType, runningMonitorType, templateTypeKey);
            if (Objects.isNull(instanceConfig)) {
                continue;
            }
            List<InfoCodeDefineModelCfgDTO> configs = instanceConfig.getCfgInfoCodeGroupVOList();
            if (CollectionUtils.isEmpty(configs)) {
                continue;
            }
            for (InfoCodeDefineModelCfgDTO config : configs) {
                String groupCode = config.getGroupCode();
                String infoCode = config.getInfoCode();
                boolean show = config.isShow();
                if (!show) {
                    continue;
                }
                MapExecutor.dynamicPut(templateTypeIdGroupCodeAndInfoCodeFilters, templateTypeId,
                        groupCode, infoCode, HashMap::new, HashSet::new);
            }
        }
        this.filterTemplateTypeIdGroupCodeAndInfoCodes = templateTypeIdGroupCodeAndInfoCodeFilters;
    }

    /**
     * 填充信息点配置
     *
     * @since 5.240602.0
     */
    public void fillInfoCodeConfigs() {
        Collection<InfoCodeTypeEnum> infoCodeTypes = this.param.getInfoCodeTypes();
        Collection<String> groupCodes = this.param.getGroupCodes();
        Collection<InfoCodeConfigQueryConfigValueObject> filterGroups = this.param.getFilterGroups();
        Collection<InfoCodeConfigQueryConfigValueObject> excludeGroups = this.param.getExcludeGroups();
        if (BooleanUtils.isTrue(this.param.getFilterSsePublishInfoCode())) {
            // 当存在信息点类型入参时, 过滤掉不需要推送的信息点类型
            // 当不存在信息点类型入参时, 直接赋值需要推送的信息点类型
            List<InfoCodeTypeEnum> ssePublishInfoCodeTypes = InfoCodeTypeEnum.listSsePublishInfoCodeTypes(
                    BooleanUtils.isTrue(this.param.getFilterSsePublishInfoCodeOld()));
            if (CollectionUtils.isNotEmpty(infoCodeTypes)) {
                infoCodeTypes.removeIf(item -> !ssePublishInfoCodeTypes.contains(item));
            } else {
                infoCodeTypes = ssePublishInfoCodeTypes;
            }
        }
        Pair<Set<String>, Map<String, Set<String>>> filterPair = queryGroupCodesAndGroupCodeAndInfoCodes(filterGroups);
        Pair<Set<String>, Map<String, Set<String>>> excludePair = queryGroupCodesAndGroupCodeAndInfoCodes(excludeGroups);
        Set<String> filterGroupCodes = Optional.ofNullable(filterPair.getLeft()).orElseGet(HashSet::new);
        Map<String, Set<String>> filterGroupCodeAndInfoCodes = Optional.ofNullable(filterPair.getRight()).orElseGet(HashMap::new);
        Set<String> excludeGroupCodes = Optional.ofNullable(excludePair.getLeft()).orElseGet(HashSet::new);
        Map<String, Set<String>> excludeGroupCodeAndInfoCodes = Optional.ofNullable(excludePair.getRight()).orElseGet(HashMap::new);
        if (CollectionUtils.isNotEmpty(groupCodes)) {
            filterGroupCodes.addAll(groupCodes);
        }
        InfoCodeConfigQueryDaoParam queryParam = new InfoCodeConfigQueryDaoParam();
        queryParam.setInfoCodeTypes(infoCodeTypes);
        queryParam.setRelTemplateTypeIds(templateTypeIdAndData.keySet());
        List<InfoCodeConfig> infoCodeConfigs = infoCodeConfigResource.list(queryParam);
        for (InfoCodeAggregateLifeAware aware : awares) {
            infoCodeConfigs = aware.handleInfoCodeConfigs(this.param, infoCodeConfigs);
        }
        // 存在分组和信息点过滤时进行内存过滤
        infoCodeConfigs.removeIf(item -> {
            String infoCode = item.getInfoCode();
            String relGroupCode = item.getRelGroupCode();
            Long relTemplateTypeId = item.getRelTemplateTypeId();
            if (CollectionUtils.isNotEmpty(filterGroupCodes) && !filterGroupCodes.contains(relGroupCode)) {
                return true;
            }
            if (MapUtils.isNotEmpty(this.filterTemplateTypeIdGroupCodeAndInfoCodes)) {
                Map<String, Set<String>> filterGroupCodeAndInfoCodesByTemplateTypeId =
                        this.filterTemplateTypeIdGroupCodeAndInfoCodes.get(relTemplateTypeId);
                if (MapUtils.isNotEmpty(filterGroupCodeAndInfoCodesByTemplateTypeId)) {
                    filterGroupCodeAndInfoCodes.putAll(filterGroupCodeAndInfoCodesByTemplateTypeId);
                }
            }
            if (MapUtils.isNotEmpty(filterGroupCodeAndInfoCodes)) {
                Set<String> infoCodes = filterGroupCodeAndInfoCodes.get(relGroupCode);
                if (CollectionUtils.isNotEmpty(infoCodes) && !infoCodes.contains(infoCode)) {
                    return true;
                }
            }
            if (CollectionUtils.isNotEmpty(excludeGroupCodes) && excludeGroupCodes.contains(relGroupCode)) {
                return true;
            }
            if (MapUtils.isNotEmpty(excludeGroupCodeAndInfoCodes)) {
                Set<String> infoCodes = excludeGroupCodeAndInfoCodes.get(relGroupCode);
                return CollectionUtils.isNotEmpty(infoCodes) && infoCodes.contains(infoCode);
            }
            return false;
        });
        if (CollectionUtils.isEmpty(infoCodeConfigs)) {
            logProjectInstance().warn("信息点配置为空", Arrays.asList(
                    Pair.of("queryParam", queryParam),
                    Pair.of("filterGroupCodes", filterGroupCodes),
                    Pair.of("filterGroupCodeAndInfoCodes", filterGroupCodeAndInfoCodes),
                    Pair.of("excludeGroupCodes", excludeGroupCodes),
                    Pair.of("excludeGroupCodeAndInfoCodes", excludeGroupCodeAndInfoCodes)
            ));
            return;
        }
        this.templateTypeIdAndInfoCodeConfigs = infoCodeConfigs.stream().collect(
                Collectors.groupingBy(InfoCodeConfig::getRelTemplateTypeId));
    }

    /**
     * 填充分组配置
     *
     * @since 5.240602.0
     */
    public void fillGroupConfigs() {
        Set<String> groupCodes = this.templateTypeIdAndInfoCodeConfigs.values().stream()
                .flatMap(Collection::stream)
                .map(InfoCodeConfig::getRelGroupCode)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(groupCodes)) {
            return;
        }
        List<InfoCodeGroup> groupConfigs = infoCodeGroupResource.list(
                new InfoCodeGroupQueryDaoParam().setGroupCodes(Sets.newHashSet(groupCodes)));
        for (InfoCodeAggregateLifeAware aware : awares) {
            groupConfigs = aware.handleInfoCodeGroups(this.param, groupConfigs);
        }
        if (CollectionUtils.isNotEmpty(groupConfigs)) {
            this.failed = false;

            Set<Long> allRelParentIds = new HashSet<>();
            Map<String, InfoCodeGroup> groupCodeAndData = new HashMap<>(64);
            for (InfoCodeGroup groupConfig : groupConfigs) {
                String groupCode = groupConfig.getGroupCode();
                Long relParentId = groupConfig.getRelParentId();
                if (Objects.nonNull(relParentId)) {
                    allRelParentIds.add(relParentId);
                }
                groupCodeAndData.put(groupCode, groupConfig);
            }
            this.groupCodeAndData = groupCodeAndData;
            if (CollectionUtils.isNotEmpty(allRelParentIds)) {
                Map<Long, InfoCodeGroup> parentGroupIdAndData = new HashMap<>();
                for (InfoCodeGroup groupConfig : groupConfigs) {
                    Long relParentId = groupConfig.getId();
                    if (allRelParentIds.contains(relParentId)) {
                        parentGroupIdAndData.put(relParentId, groupConfig);
                        allRelParentIds.remove(relParentId);
                    }
                }
                parentGroupIdAndData.putAll(deepQueryParentGroupConfigs(allRelParentIds));
                this.parentGroupIdAndData = parentGroupIdAndData;
            }
        } else {
            logProjectInstance().warn("分组配置为空", Pair.of("groupCodes", groupCodes));
        }
    }

    /**
     * 填充IOT信息点规则
     *
     * @since 7.250601.0
     */
    public void fillIotInfoCodeRule() {
        Map<String, Set<String>> finalObjectIdAndExistsRuleInfoCodes = new ConcurrentHashMap<>();
        // 规则是否存在是对象级事实，缓存 Key 只用于写入归属；READY 模板统一直接查询规则主表。
        Set<Long> newModelTemplateTypeIds = this.selectNewModelTemplateTypeIds();
        Map<String, Set<String>> newModelRuleInfoCodes =
                this.loadNewModelRuleInfoCodes(newModelTemplateTypeIds);
        newModelRuleInfoCodes.forEach((objectId, existsRuleInfoCodes) ->
                MapExecutor.dynamicPutAll(finalObjectIdAndExistsRuleInfoCodes,
                        objectId, existsRuleInfoCodes, HashSet::new));
        this.templateTypeIdAndObjects.forEach((templateTypeId, objects) -> {
            InfoCodeTemplateTypeModel templateTypeModel = this.templateTypeIdAndData.get(templateTypeId);
            Set<String> objectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            if (templateTypeModel == null || CollectionUtils.isEmpty(objectIds)) {
                return;
            }
            if (newModelTemplateTypeIds.contains(templateTypeId)) {
                // READY 后主表空结果也是最终结果，不再回退旧规则表或远程查询。
                return;
            }
            String templateTypeKey = templateTypeModel.getTemplateTypeKey();
            boolean supportedByNewModel = this.cacheObjectModelStatusService.isSupported(
                    templateTypeModel.getRunningMonitorType());
            Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = Collections.emptyMap();
            if (!supportedByNewModel && templateTypeModel.allowCache()) {
                // 其他监测系统仍沿用旧规则表；末端、照明未 READY 时直接进入下方实时查询。
                Map<String, Set<String>> objectIdAndExistsRuleInfoCodesCache =
                        this.cacheIotRuleRepository.mapObjectIdAndInfoCodes(templateTypeKey, objectIds);
                if (MapUtils.isNotEmpty(objectIdAndExistsRuleInfoCodesCache)) {
                    objectIdAndExistsRuleInfoCodes = objectIdAndExistsRuleInfoCodesCache;
                }
            }
            if (MapUtils.isEmpty(objectIdAndExistsRuleInfoCodes)) {
                objectIdAndExistsRuleInfoCodes = this.redisCacheTemplate.asyncRefreshTemplate(
                        InfoCodeGateway.CACHE_KEY_TEMPLATE_OBJECT_IOT_RULE, templateTypeKey, () -> {
                            InfoCodeObjectQueryParam objectQueryParam = new InfoCodeObjectQueryParam();
                            objectQueryParam.setRunningMonitorType(templateTypeModel.getRunningMonitorType());
                            objectQueryParam.setRunningMonitorSecondType(templateTypeModel.getRunningMonitorSecondType());
                            objectQueryParam.setTemplateTypeId(templateTypeModel.getTemplateTypeId());
                            objectQueryParam.setTemplateTypes(Lists.newArrayList(templateTypeModel.getTemplateType()));
                            objectQueryParam.setTemplateTypeModels(Lists.newArrayList(templateTypeModel));
                            objectQueryParam.setClassCode(templateTypeModel.getClassCode());
                            objectQueryParam.setObjectType(templateTypeModel.getObjectType());
                            List<DtpRwdObjectDTO> rwdObjects = this.infoCodeGateway.listTemplateTypeObjects(objectQueryParam);
                            if (CollectionUtils.isEmpty(rwdObjects)) {
                                return Collections.emptyMap();
                            }
                            return this.queryObjectIdAndExistsRuleInfoCodes(templateTypeId, rwdObjects, null);
                        });
            }
            objectIdAndExistsRuleInfoCodes.forEach((objectId, existsRuleInfoCodes) ->
                    MapExecutor.dynamicPutAll(finalObjectIdAndExistsRuleInfoCodes,
                            objectId, existsRuleInfoCodes, HashSet::new));
        });
        this.objectIdAndExistsRuleInfoCodes = finalObjectIdAndExistsRuleInfoCodes;
    }

    /**
     * 选择当前请求中读取新规则模型的模板。
     *
     * <p>同一个一级运行监测类型在一次聚合初始化中只读取一次 READY，避免模板数量放大
     * Redis 查询次数。末端、照明未 READY 时不进入结果集合，后续直接使用实时规则查询；
     * 其他监测系统继续使用原有旧链路。</p>
     *
     * @return 读取新规则模型的模板类型 ID
     * @since v7.260701.0
     */
    private @NonNull Set<Long> selectNewModelTemplateTypeIds() {
        if (LocalContextStorage.isDeprecatedVersion()) {
            return Collections.emptySet();
        }
        Map<RunningMonitorTypeEnum, Boolean> readyByRunningMonitorType =
                new EnumMap<>(RunningMonitorTypeEnum.class);
        Set<Long> result = new HashSet<>();
        this.templateTypeIdAndObjects.forEach((templateTypeId, objects) -> {
            InfoCodeTemplateTypeModel templateTypeModel =
                    this.templateTypeIdAndData.get(templateTypeId);
            if (templateTypeModel == null
                    || CollectionUtils.isEmpty(objects)
                    || !templateTypeModel.allowCache()) {
                return;
            }
            boolean ready = readyByRunningMonitorType.computeIfAbsent(
                    templateTypeModel.getRunningMonitorType(),
                    this.cacheObjectModelStatusService::isReady);
            if (ready) {
                result.add(templateTypeId);
            }
        });
        return result;
    }

    /**
     * 按当前请求一次性加载所有已路由到新模型的规则信息点。
     *
     * <p>业务读取只按对象、信息点和部署端规则类型查询 {@code cache_rule}；缓存 Key
     * 只保留为写入替换和孤儿清理的归属信息，不参与规则是否存在的判断。方法汇总本次
     * READY 模板的对象和 IOT 信息点后执行一次 SQL，避免模板数量放大数据库往返。</p>
     *
     * @param newModelTemplateTypeIds 读取新规则模型的模板类型 ID
     * @return 对象 ID 到已实例化信息点编码集合
     * @since v7.260701.0
     */
    private @NonNull Map<String, Set<String>> loadNewModelRuleInfoCodes(
            @NonNull Set<Long> newModelTemplateTypeIds) {
        Set<String> objectIds = new LinkedHashSet<>();
        Set<String> infoCodes = new LinkedHashSet<>();
        this.templateTypeIdAndObjects.forEach((templateTypeId, objects) -> {
            if (!newModelTemplateTypeIds.contains(templateTypeId)) {
                return;
            }
            for (DtpRwdObjectDTO object : objects) {
                objectIds.add(object.getId());
            }
            for (InfoCodeConfig infoCodeConfig : this.templateTypeIdAndInfoCodeConfigs
                    .getOrDefault(templateTypeId, Collections.emptyList())) {
                if (infoCodeConfig.getInfoCodeType().isIotInfoCode()) {
                    infoCodes.add(infoCodeConfig.getInfoCode());
                }
            }
        });
        return this.cacheRuleRepository.mapInfoCodesByObjectIds(
                objectIds, infoCodes, this.cacheIotRuleRepository.cacheIotRuleType());
    }

    /**
     * 查询对象ID与存在IOT规则的信息点映射
     *
     * @param templateTypeId 模版类型ID
     * @param objects        对象列表
     * @param terminalType   终端类型，为空时云走云边走边
     * @return 对象ID -> 存在IOT规则的信息点集合
     * @since 7.260302.0
     */
    public @NonNull Map<String, Set<String>> queryObjectIdAndExistsRuleInfoCodes(
            @NonNull Long templateTypeId,
            @NonNull List<DtpRwdObjectDTO> objects,
            @Nullable TerminalEnum terminalType) {
        InfoCodeConfigQueryDaoParam queryParam = new InfoCodeConfigQueryDaoParam();
        queryParam.setInfoCodeTypes(Arrays.stream(InfoCodeTypeEnum.values())
                .filter(InfoCodeTypeEnum::isIotInfoCode).collect(Collectors.toList()));
        queryParam.setRelTemplateTypeIds(Lists.newArrayList(templateTypeId));
        List<InfoCodeConfig> infoCodeConfigs = infoCodeConfigResource.list(queryParam);
        for (InfoCodeAggregateLifeAware aware : awares) {
            infoCodeConfigs = aware.handleInfoCodeConfigs(this.param, new ArrayList<>(infoCodeConfigs));
        }
        if (CollectionUtils.isEmpty(infoCodeConfigs)) {
            return new HashMap<>();
        }
        Set<String> allIotInfoCodes = new HashSet<>();
        Set<String> allPersistenceIotInfoCodes = new HashSet<>();
        for (InfoCodeConfig infoCodeConfig : infoCodeConfigs) {
            InfoCodeTypeEnum infoCodeType = infoCodeConfig.getInfoCodeType();
            String infoCode = infoCodeConfig.getInfoCode();
            if (infoCodeType.isIotInfoCode()) {
                if (infoCodeType.isPersistenceIotInfoCode()) {
                    allPersistenceIotInfoCodes.add(infoCode);
                } else {
                    allIotInfoCodes.add(infoCode);
                }
            }
        }
        Map<String, Set<String>> classCodeAndObjectIds = objects.stream().collect(
                Collectors.groupingBy(DtpRwdObjectDTO::getClassCode,
                        Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = new ConcurrentHashMap<>();
        if (terminalType == null) {
            objectIdAndExistsRuleInfoCodes.putAll(this.abstractIotDataCacheServer
                    .mapObjectIdAndExistsRuleInfoCodes(
                            classCodeAndObjectIds, allIotInfoCodes));
        } else {
            switch (terminalType) {
                case CLOUD:
                    objectIdAndExistsRuleInfoCodes.putAll(this.abstractIotDataCacheServer
                            .mapObjectIdAndExistsRuleInfoCodesByCloud(
                                    classCodeAndObjectIds, allIotInfoCodes));
                    break;
                case EDGE:
                    objectIdAndExistsRuleInfoCodes.putAll(this.abstractIotDataCacheServer
                            .mapObjectIdAndExistsRuleInfoCodesByEdge(
                                    classCodeAndObjectIds, allIotInfoCodes));
                    break;
            }
        }
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodesByCloud =
                this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodesByCloud(
                        classCodeAndObjectIds, allPersistenceIotInfoCodes);
        objectIdAndExistsRuleInfoCodesByCloud.forEach((objectId, existsRuleInfoCodes) ->
                MapExecutor.dynamicPutAll(objectIdAndExistsRuleInfoCodes, objectId, existsRuleInfoCodes, HashSet::new));
        return objectIdAndExistsRuleInfoCodes;
    }

    /**
     * 转换聚合根
     *
     * @return 聚合根
     * @since 5.240602.0
     */
    public List<InfoCodeAggregate> toAggregates() {
        DefaultListPage<List<InfoCodeAggregate>> pageable = this.param.getPage();
        List<List<InfoCodeAggregate>> aggregateOfObject = new ArrayList<>();
        for (Map.Entry<Long, List<DtpRwdObjectDTO>> entry : this.templateTypeIdAndObjects.entrySet()) {
            Long templateTypeId = entry.getKey();
            List<InfoCodeConfig> infoCodeConfigs = this.templateTypeIdAndInfoCodeConfigs.get(templateTypeId);
            if (CollectionUtils.isEmpty(infoCodeConfigs)) {
                logProjectInstance().warn("未找到对象关联信息点配置", Arrays.asList(
                        Pair.of("objectIds", entry.getValue().stream()
                                .map(DtpRwdObjectDTO::getId)
                                .collect(Collectors.toSet())),
                        Pair.of("templateTypeId", templateTypeId)
                ));
                continue;
            }
            for (DtpRwdObjectDTO object : entry.getValue()) {
                aggregateOfObject.add(toObjectAggregate(object, infoCodeConfigs));
            }
        }
        if (Objects.nonNull(pageable)) {
            pageable.setTotalCount(aggregateOfObject.size());
            int toIndex = Math.min(pageable.getOffset() + pageable.getPageSize(), aggregateOfObject.size());
            int fromIndex = Math.min(pageable.getOffset(), toIndex);
            aggregateOfObject = aggregateOfObject.subList(fromIndex, toIndex);
        }
        return aggregateOfObject.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

    }

    /**
     * 查询分组编码和分组信息点映射
     *
     * @param filters 过滤条件
     * @return 分组编码 - 分组信息点映射
     * @since 7.250802.0
     */
    private @NonNull Pair<Set<String>, Map<String, Set<String>>> queryGroupCodesAndGroupCodeAndInfoCodes(
            @Nullable Collection<InfoCodeConfigQueryConfigValueObject> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return Pair.of(null, null);
        }
        Map<String, Set<String>> filterGroupCodeAndInfoCodes = new HashMap<>();
        Set<String> filterGroupCodes = new HashSet<>();
        Set<String> deepQueryGroupCodes = new HashSet<>();
        for (InfoCodeConfigQueryConfigValueObject filterByGroup : filters) {
            String groupCode = filterByGroup.getGroupCode();
            Boolean deepQuery = filterByGroup.getDeepQuery();
            Set<String> infoCodes = filterByGroup.getInfoCodes();
            if (BooleanUtils.isTrue(deepQuery)) {
                deepQueryGroupCodes.add(groupCode);
            }
            if (CollectionUtils.isNotEmpty(infoCodes)) {
                filterGroupCodeAndInfoCodes.put(groupCode, infoCodes);
            }
            filterGroupCodes.add(groupCode);
        }
        if (CollectionUtils.isNotEmpty(deepQueryGroupCodes)) {
            List<InfoCodeGroup> firstLevelGroups = infoCodeGroupResource.list(
                    new InfoCodeGroupQueryDaoParam().setGroupCodes(deepQueryGroupCodes));
            Map<Long, InfoCodeGroup> firstLevelGroupIdAndData = firstLevelGroups.stream()
                    .collect(Collectors.toMap(InfoCodeGroup::getId, Function.identity()));
            Map<Long, List<InfoCodeGroup>> groupIdAndChildren = deepQueryChildrenGroupConfigs(firstLevelGroupIdAndData.keySet());
            if (MapUtils.isNotEmpty(groupIdAndChildren)) {
                groupIdAndChildren.forEach((groupId, children) -> {
                    for (InfoCodeGroup child : children) {
                        String childGroupCode = child.getGroupCode();
                        InfoCodeGroup firstLevelGroup = firstLevelGroupIdAndData.get(groupId);
                        if (Objects.nonNull(firstLevelGroup)) {
                            Set<String> infoCodes = filterGroupCodeAndInfoCodes.get(firstLevelGroup.getGroupCode());
                            if (CollectionUtils.isNotEmpty(infoCodes)) {
                                filterGroupCodeAndInfoCodes.put(child.getGroupCode(), infoCodes);
                            }
                        }
                        filterGroupCodes.add(childGroupCode);
                    }
                });
            }
        }
        return Pair.of(filterGroupCodes, filterGroupCodeAndInfoCodes);
    }

    /**
     * 转换对象聚合根
     *
     * @param object          对象
     * @param infoCodeConfigs 信息点配置
     * @return 对象合根
     * @since 7.250601.0
     */
    private List<InfoCodeAggregate> toObjectAggregate(
            @NonNull DtpRwdObjectDTO object,
            List<InfoCodeConfig> infoCodeConfigs) {
        if (CollectionUtils.isEmpty(infoCodeConfigs)) {
            return Lists.newArrayList();
        }
        Map<Long, InfoCodeConfig> infoCodeConfigIdAndData = infoCodeConfigs.stream().collect(
                Collectors.toMap(BaseAssignIdDO::getId, Function.identity(), (o1, o2) -> o1));
        List<InfoCodeAggregate> objectAggregates = new ArrayList<>();
        for (InfoCodeConfig infoCodeConfig : infoCodeConfigs) {
            Long infoCodeConfigId = infoCodeConfig.getId();
            InfoCodeTypeEnum infoCodeType = infoCodeConfig.getInfoCodeType();
            String infoCode = infoCodeConfig.getInfoCode();
            Set<Long> relInfoCodeConfigIds = infoCodeConfig.getRelInfoCodeConfigIds();
            Long relTemplateTypeId = infoCodeConfig.getRelTemplateTypeId();
            String relGroupCode = infoCodeConfig.getRelGroupCode();

            InfoCodeTemplateTypeModel templateTypeModel = this.templateTypeIdAndData.get(relTemplateTypeId);
            if (Objects.isNull(templateTypeModel)) {
                logProjectInstance().warn("未找到模版类型",
                        Pair.of("infoCodeConfigId", infoCodeConfigId));
                continue;
            }
            InfoCodeGroup group = groupCodeAndData.get(relGroupCode);
            if (Objects.isNull(group)) {
                logProjectInstance().warn("未找到模版分组",
                        Pair.of("infoCodeConfigId", infoCodeConfigId));
                continue;
            }
            // 生成唯一标识
            InfoCodeIdentifier identifier = new InfoCodeIdentifier(
                    templateTypeModel.getRunningMonitorType(),
                    templateTypeModel.getRunningMonitorSecondType(),
                    templateTypeModel.getTemplateType(),
                    object, infoCodeType, infoCode, group.getGroupCode());
            // 生成信息点配置模型
            InfoCodeConfigModel configModel = converter.toConfigModel(infoCodeConfig);
            // 生成信息点关联模型
            List<InfoCodeAssembleModel> assembleModels = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(relInfoCodeConfigIds)) {
                relInfoCodeConfigIds.stream().filter(infoCodeConfigIdAndData::containsKey).forEach(item -> {
                    InfoCodeConfig assembleConfig = infoCodeConfigIdAndData.get(item);
                    // 仅允许关联同一个分组下的信息点
                    if (StringUtils.equals(assembleConfig.getRelGroupCode(), relGroupCode)) {
                        assembleModels.add(converter.toAssembleModel(assembleConfig));
                    }
                });
            }
            // 生成信息点分组模型
            InfoCodeGroupModel groupModel = converter.toGroupModel(group);
            groupModel.setRelParent(deepFillRelParentModel(group));
            // 填充唯一标识和聚合根映射
            InfoCodeAggregate aggregate = new InfoCodeAggregate(identifier,
                    templateTypeModel, configModel, assembleModels, groupModel);
            objectAggregates.add(aggregate);
        }
        // 填充信息点规则
        if (BooleanUtils.isNotFalse(this.param.getQueryIotInfoCodeRule())) {
            fillInfoCodeRule(objectAggregates, this.objectIdAndExistsRuleInfoCodes);
            // 过滤信息点规则
            if (BooleanUtils.isNotTrue(this.param.getNotFilterIotInfoCodeRule())) {
                filterInfoCodeRule(objectAggregates, this.objectIdAndExistsRuleInfoCodes,
                        this.param.getFilterBothNotExistIotInfoCodeRule());
            }
        }
        return objectAggregates;
    }

    /**
     * 过滤信息点规则
     *
     * @param aggregates                        聚合根
     * @param objectIdAndExistsRuleInfoCodes    对象ID -> 存在的信息的规则
     * @param filterBothNotExistIotInfoCodeRule 是否仅过滤结果对象都不存在规则的IOT信息点
     * @since 5.240701.0
     */
    private void filterInfoCodeRule(
            @NonNull Collection<InfoCodeAggregate> aggregates,
            @NonNull Map<String, Set<String>> objectIdAndExistsRuleInfoCodes,
            Boolean filterBothNotExistIotInfoCodeRule) {
        // 标识符 -> 关联标识符
        Map<InfoCodeIdentifier, List<InfoCodeIdentifier>> identifierAndRelIdentifiers = new HashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            List<InfoCodeAggregate> assembleAggregates = aggregate.analyzeAssembleAggregate(aggregates);
            if (CollectionUtils.isNotEmpty(assembleAggregates)) {
                identifierAndRelIdentifiers.put(aggregate.getIdentifier(), assembleAggregates.stream()
                        .map(InfoCodeAggregate::getIdentifier)
                        .collect(Collectors.toList()));
            }
        }
        // 需要被删除的关联信息点
        Set<InfoCodeIdentifier> toBeRemovedRelIdentifiers = new HashSet<>();
        // 当对象所有信息点均不存在规则时移除记录
        if (BooleanUtils.isTrue(filterBothNotExistIotInfoCodeRule)) {
            // 所有存在规则的IOT信息点
            Set<String> allExistRuleIotInfoCodes = new HashSet<>();
            for (InfoCodeAggregate aggregate : aggregates) {
                String objectId = aggregate.getIdentifier().getObject().getId();
                InfoCodeTypeEnum infoCodeType = aggregate.getConfigModel().getInfoCodeType();
                String infoCode = aggregate.getConfigModel().getInfoCode();
                if (!infoCodeType.isIotInfoCode()) {
                    continue;
                }
                // 是否存在IOT规则
                boolean existRule = objectIdAndExistsRuleInfoCodes.getOrDefault(
                        objectId, Collections.emptySet()).contains(infoCode);
                if (existRule) {
                    allExistRuleIotInfoCodes.add(infoCode);
                }
            }
            aggregates.removeIf(item -> {
                InfoCodeTypeEnum infoCodeType = item.getConfigModel().getInfoCodeType();
                String infoCode = item.getConfigModel().getInfoCode();

                // 当分组为设定信息点条件时不移除
                if (item.getGroupModel().isIotSettingConditionGroup()) {
                    return false;
                }
                if (!infoCodeType.isIotInfoCode()) {
                    return false;
                }
                // 将不存在规则的IOT信息点过滤
                boolean removed = !allExistRuleIotInfoCodes.contains(infoCode);
                if (removed) {
                    toBeRemovedRelIdentifiers.addAll(identifierAndRelIdentifiers
                            .getOrDefault(item.getIdentifier(), Collections.emptyList()));
                }
                return removed;
            });
            return;
        }
        // 当对象信息点无规则时移除指定信息点
        aggregates.removeIf(item -> {
            String objectId = item.getIdentifier().getObject().getId();
            InfoCodeTypeEnum infoCodeType = item.getConfigModel().getInfoCodeType();
            String infoCode = item.getConfigModel().getInfoCode();

            // 当分组为设定信息点条件时不移除
            if (item.getGroupModel().isIotSettingConditionGroup()) {
                return false;
            }
            // 非IOT信息点不移除
            if (!infoCodeType.isIotInfoCode()) {
                return false;
            }
            // 当对象信息点无规则时移除
            boolean removed = !objectIdAndExistsRuleInfoCodes.getOrDefault(objectId, Collections.emptySet()).contains(infoCode);
            // 对象信息点被删除时需要删除关联信息点
            if (removed && this.parseGroupCodes(item.getGroupModel()).stream()
                    .noneMatch(InfoCodeGroupModel.DYNAMIC_REMOVE_RELATION_INFO_CODE_GROUP_NAME_WHITELIST::contains)) {
                toBeRemovedRelIdentifiers.addAll(identifierAndRelIdentifiers
                        .getOrDefault(item.getIdentifier(), Collections.emptyList()));
            }
            return removed;
        });
        // 当父级不存在规则被移除时将关联信息点一并移除
        if (CollectionUtils.isNotEmpty(toBeRemovedRelIdentifiers)) {
            aggregates.removeIf(item -> toBeRemovedRelIdentifiers.contains(item.getIdentifier()));
        }
    }

    /**
     * 解析所有分组编码
     *
     * @param group 分组模型
     * @return 所有分组编码
     * @since 7.251202.0
     */
    private Set<String> parseGroupCodes(InfoCodeGroupModel group) {
        Set<String> groupCodes = new HashSet<>();
        String groupCode = group.getGroupCode();
        InfoCodeGroupModel relParent = group.getRelParent();
        groupCodes.add(groupCode);
        if (relParent != null) {
            groupCodes.addAll(this.parseGroupCodes(relParent));
        }
        return groupCodes;
    }

    /**
     * 填充信息点规则
     *
     * @param aggregates                     聚合根
     * @param objectIdAndExistsRuleInfoCodes 对象ID -> 存在的信息的规则
     * @since 5.241001.0
     */
    private void fillInfoCodeRule(
            @NonNull Collection<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Set<String>> objectIdAndExistsRuleInfoCodes) {
        objectIdAndExistsRuleInfoCodes = Optional.ofNullable(
                objectIdAndExistsRuleInfoCodes).orElse(Collections.emptyMap());
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            InfoCodeConfigModel configModel = aggregate.getConfigModel();
            InfoCodeTypeEnum infoCodeType = configModel.getInfoCodeType();
            String infoCode = configModel.getInfoCode();

            if (infoCodeType.isIotInfoCode()) {
                configModel.setExistsIotRule(objectIdAndExistsRuleInfoCodes.getOrDefault(
                        objectId, Collections.emptySet()).contains(infoCode));
            }
        }
    }

    /**
     * 递归填充转换关联父级配置模型
     *
     * @param groupConfig 分组配置
     * @return 分组配置
     * @since 5.240602.0
     */
    private InfoCodeGroupModel deepFillRelParentModel(@Nullable InfoCodeGroup groupConfig) {
        if (Objects.isNull(groupConfig)) {
            return null;
        }
        Long relParentId = groupConfig.getRelParentId();
        if (Objects.nonNull(relParentId)) {
            InfoCodeConverter converter = SpringUtil.getBean(InfoCodeConverter.class);
            InfoCodeGroup parentGroup = parentGroupIdAndData.get(relParentId);
            Long relParentsId = parentGroup.getRelParentId();
            InfoCodeGroupModel parentModel = converter.toGroupModel(parentGroup);
            if (Objects.nonNull(relParentsId)) {
                parentModel.setRelParent(this.deepFillRelParentModel(parentGroupIdAndData.get(relParentsId)));
            }
            return parentModel;
        }
        return converter.toGroupModel(groupConfig);
    }

    /**
     * 递归填充父级分组配置
     *
     * @param relParentIds 关联父级分组ID
     * @since 5.240602.0
     */
    private Map<Long, InfoCodeGroup> deepQueryParentGroupConfigs(
            @Nullable Set<Long> relParentIds) {
        if (CollectionUtils.isEmpty(relParentIds)) {
            return Maps.newHashMap();
        }
        Map<Long, InfoCodeGroup> parentGroupIdAndData = new HashMap<>();
        List<InfoCodeGroup> parentGroupConfigs = infoCodeGroupResource.list(
                new InfoCodeGroupQueryDaoParam().setIds(relParentIds));
        if (CollectionUtils.isEmpty(parentGroupConfigs)) {
            return Maps.newHashMap();
        }
        Set<Long> allRelParentIds = new HashSet<>();
        for (InfoCodeGroup parentGroupConfig : parentGroupConfigs) {
            Long relParentId = parentGroupConfig.getRelParentId();
            if (Objects.nonNull(relParentId)) {
                allRelParentIds.add(relParentId);
            }
            parentGroupIdAndData.put(parentGroupConfig.getId(), parentGroupConfig);
        }
        if (CollectionUtils.isNotEmpty(allRelParentIds)) {
            parentGroupIdAndData.putAll(deepQueryParentGroupConfigs(allRelParentIds));
        }
        return parentGroupIdAndData;
    }

    /**
     * 递归填充子级分组配置
     *
     * @param groupCodeIds 分组ID
     * @return 分组ID -> 下级分组
     * @since 5.240501.0
     */
    private Map<Long, List<InfoCodeGroup>> deepQueryChildrenGroupConfigs(
            @Nullable Set<Long> groupCodeIds) {
        if (CollectionUtils.isEmpty(groupCodeIds)) {
            return Maps.newHashMap();
        }
        Map<Long, List<InfoCodeGroup>> groupIdAndChildren = new HashMap<>();
        List<InfoCodeGroup> childrenGroupConfigs = infoCodeGroupResource.list(
                new InfoCodeGroupQueryDaoParam().setRelParentIds(groupCodeIds));
        if (CollectionUtils.isEmpty(childrenGroupConfigs)) {
            return Maps.newHashMap();
        }
        Set<Long> allRelChildrenIds = new HashSet<>();
        for (InfoCodeGroup childrenGroupConfig : childrenGroupConfigs) {
            Long childrenGroupConfigId = childrenGroupConfig.getId();
            Long relParentId = childrenGroupConfig.getRelParentId();
            if (Objects.nonNull(relParentId)) {
                MapExecutor.dynamicPut(groupIdAndChildren, relParentId, childrenGroupConfig, ArrayList::new);
            }
            allRelChildrenIds.add(childrenGroupConfigId);
        }
        if (CollectionUtils.isNotEmpty(allRelChildrenIds)) {
            Map<Long, List<InfoCodeGroup>> childrenGroupIdAndChildren = deepQueryChildrenGroupConfigs(allRelChildrenIds);
            for (Map.Entry<Long, List<InfoCodeGroup>> entry : groupIdAndChildren.entrySet()) {
                List<InfoCodeGroup> childGroups = entry.getValue();
                List<InfoCodeGroup> finalChildGroups = new ArrayList<>(childGroups);
                for (InfoCodeGroup childGroup : childGroups) {
                    List<InfoCodeGroup> children = childrenGroupIdAndChildren.get(childGroup.getId());
                    if (CollectionUtils.isNotEmpty(children)) {
                        finalChildGroups.addAll(children);
                    }
                }
                entry.setValue(finalChildGroups);
            }
        }
        return groupIdAndChildren;
    }

    @Override
    public String logTag() {
        return "信息点配置-聚合根初始化上下文";
    }
}
