package com.persagy.apccontrol.basic.structure.infocodeconfig.context;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigByObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectOrderParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.basic.utils.template.SpelUtils;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 信息点配置-物理世界对象详情模版上下文
 *
 * <p>对象需要消费id和objType</p>
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@SuppressWarnings("DuplicatedCode")
@Data
public class InfoCodeRwdObjectDetailByTemplateContext {

    private static final String OBJECT_ID = "objectId";
    private static final Pattern ROOT_MAP_SOURCE_PATTERN = Pattern.compile("#root\\s*\\[\\s*['\"]([^'\"]+)['\"]\\s*]");
    private static final Pattern ROOT_PROPERTY_SOURCE_PATTERN = Pattern.compile("#root\\s*\\.\\s*([A-Za-z_$][\\w$]*)");

    // ------------------------- 入参 -------------------------

    /**
     * 信息点模版
     */
    private final InfoCodeTemplateTypeModel templateTypeModel;

    /**
     * 取值逻辑
     */
    private final InfoCodeTakeValueLogicRwdObjectDetailModel takeValueLogic;

    /**
     * 根对象ID
     */
    private @Nullable Set<@NonNull String> rootObjectIds = null;

    /**
     * 拓展值
     */
    private final Map<String, Object> additionalValues;

    /**
     * 物理世界转换器
     */
    private final DtpRwdConvert dtpRwdConvert = SpringUtil.getBean(DtpRwdConvert.class);

    /**
     * 物理世界服务
     */
    private final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);

    /**
     * 信息点配置服务
     */
    private final InfoCodeConfigService infoCodeConfigService = SpringUtil.getBean(InfoCodeConfigService.class);

    /**
     * 信息点转换器
     */
    private final InfoCodeConverter infoCodeConverter = SpringUtil.getBean(InfoCodeConverter.class);

    // ------------------------- 过程 -------------------------

    @Setter(AccessLevel.NONE)
    private int level = 0;

    /**
     * 聚合根对象ID -> [占位符 -> 数据源]
     */
    @Setter(AccessLevel.NONE)
    private Map<String, Map<String, Set<Object>>> rootObjectIdAndSource = new HashMap<>();

    /**
     * 数据源映射
     */
    @Setter(AccessLevel.NONE)
    private Map<String, Object> sourceMap = new HashMap<>();

    /**
     * 根据取值逻辑查询出的结果
     */
    @Setter(AccessLevel.NONE)
    private @NonNull List<@NonNull DtpRwdObjectDTO> objects = Collections.emptyList();

    /**
     * 聚合根对象ID -> 关联对象
     */
    @Setter(AccessLevel.NONE)
    private Map<String, List<DtpRwdObjectDTO>> rootObjectIdAndObjects = Collections.emptyMap();

    /**
     * 关联上下文
     */
    @Setter(AccessLevel.NONE)
    private @Nullable InfoCodeRwdObjectDetailByTemplateContext relationContext;

    // ------------------------- 结果 -------------------------

    /**
     * 对象ID -> 信息点配置
     */
    @Setter(AccessLevel.NONE)
    private Map<String, List<InfoCodeGroupVO>> objectIdConfigs = new HashMap<>();

    /**
     * 对象ID -> 所在空间
     */
    @Setter(AccessLevel.NONE)
    private Supplier<Map<String, List<DtpRwdSpaceDTO>>> objectIdAndInSpaces = Suppliers.memoize(this::queryInSpaces);

    /**
     * 对象ID -> 所在楼层
     */
    @Setter(AccessLevel.NONE)
    private Supplier<Map<String, DtpRwdFloorDTO>> objectIdAndInFloor = Suppliers.memoize(this::queryInFloors);

    /**
     * 对象ID -> 所在建筑
     */
    @Setter(AccessLevel.NONE)
    private Supplier<Map<String, DtpRwdBuildingDTO>> objectIdAndInBuilding = Suppliers.memoize(this::queryInBuildings);

    /**
     * 构造器
     *
     * @param templateTypeModel 信息点模版
     * @param takeValueLogic    取值逻辑
     * @param additionalValues  拓展值
     * @since 7.250202.0
     */
    public InfoCodeRwdObjectDetailByTemplateContext(
            InfoCodeTemplateTypeModel templateTypeModel,
            InfoCodeTakeValueLogicRwdObjectDetailModel takeValueLogic,
            Map<String, Object> additionalValues) {
        this.templateTypeModel = templateTypeModel;
        this.takeValueLogic = takeValueLogic;
        this.additionalValues = additionalValues;
    }

    /**
     * 构造器
     *
     * @param templateTypeModel 信息点模版
     * @param takeValueLogic    取值逻辑
     * @param rootObjectIds     基础对象ID
     * @param additionalValues  拓展值
     * @since 7.250202.0
     */
    public InfoCodeRwdObjectDetailByTemplateContext(
            InfoCodeTemplateTypeModel templateTypeModel,
            InfoCodeTakeValueLogicRwdObjectDetailModel takeValueLogic,
            @Nullable Set<@NonNull String> rootObjectIds,
            Map<String, Object> additionalValues) {
        this.templateTypeModel = templateTypeModel;
        this.takeValueLogic = takeValueLogic;
        this.rootObjectIds = rootObjectIds;
        this.additionalValues = additionalValues;
    }

    // ------------------------- 行为 -------------------------

    /**
     * 预填充缓存中的自包含对象明细，并递归挂回当前动态信息点配置。
     *
     * <p>仓储已经为每条记录恢复了本次请求专用的 DTO，方法只收集任意深度
     * {@code relObjects} 并查询当前配置，不再重复校验持久化结构。</p>
     *
     * @param cachedResultGroups 按聚合业务键分组的缓存结果
     * @return 已挂回动态配置的结果分组
     * @since v7.260701.0
     */
    public @NonNull List<List<InfoCodeRwdObjectDetailDTO>> prepareCachedResults(
            @NonNull List<List<InfoCodeRwdObjectDetailDTO>> cachedResultGroups) {
        Deque<InfoCodeRwdObjectDetailDTO> pendingObjects = new ArrayDeque<>();
        List<InfoCodeRwdObjectDetailDTO> flattenedObjects = new ArrayList<>();
        for (List<InfoCodeRwdObjectDetailDTO> resultGroup : cachedResultGroups) {
            pendingObjects.addAll(resultGroup);
        }
        while (CollectionUtils.isNotEmpty(pendingObjects)) {
            InfoCodeRwdObjectDetailDTO object = pendingObjects.removeFirst();
            flattenedObjects.add(object);
            List<InfoCodeRwdObjectDetailDTO> relObjects = object.getRelObjects();
            if (CollectionUtils.isNotEmpty(relObjects)) {
                pendingObjects.addAll(relObjects);
            }
        }

        this.objects = new ArrayList<>(flattenedObjects);
        // 清空上一轮上下文的动态配置，避免本次查询没有配置时沿用旧对象 ID 的结果。
        this.objectIdConfigs = new HashMap<>();
        this.fillInfoCodeConfig();
        for (InfoCodeRwdObjectDetailDTO object : flattenedObjects) {
            object.setRelInfoCodeGroupConfigs(this.objectIdConfigs.get(object.getId()));
        }
        return cachedResultGroups;
    }

    /**
     * 填充数据源映射
     *
     * @since 7.250202.0
     */
    public void fillSourceMap() {
        // 合并用户传入的占位符参数, 用户传入的优先覆盖自动解析的数据源
        Map<String, Object> placeholderMap = this.getPlaceholderMap();
        if (MapUtils.isNotEmpty(placeholderMap)) {
            this.sourceMap.putAll(placeholderMap);
        }
        // 数据源中没有定义 [objectId] 时, 默认填充所有当前对象ID
        if (Objects.nonNull(this.rootObjectIds)) {
            this.sourceMap.put(objectIdKey(), this.rootObjectIds);
        }
        if (Objects.isNull(this.takeValueLogic) || !this.takeValueLogic.isExecute()) {
            return;
        }
        Map<String, InfoCodeTakeValueLogicRwdObjectModel> source = this.takeValueLogic.getSource();
        if (MapUtils.isEmpty(source)) {
            return;
        }
        List<String> sourceSequence = this.takeValueLogic.getSourceSequence();
        if (CollectionUtils.isEmpty(sourceSequence)) {
            sourceSequence = new ArrayList<>(source.keySet());
        }
        Map<String, Map<String, Set<Object>>> rootObjectIdAndSource = new HashMap<>();
        for (String key : sourceSequence) {
            InfoCodeTakeValueLogicRwdObjectModel sourceTackValueLogic = source.get(key);
            sourceTackValueLogic.validate(key);
            String defaultValueIfEmpty = sourceTackValueLogic.getDefaultValueIfEmpty();
            DtpRwdObjectQueryParam queryParam = sourceTackValueLogic.getQueryParam();
            String takeValueExpression = sourceTackValueLogic.getTakeValueExpression();

            // 拿到查询参数中存在的占位符
            List<String> sourceKeys = Collections.emptyList();
            List<Object> mathRootSources = new ArrayList<>();
            List<DtpRwdObjectDTO> objects = Collections.emptyList();
            if (Objects.nonNull(queryParam)) {
                String queryParamString = queryParam.placeholderString();
                sourceKeys = this.matchSourceKeys(queryParamString);
                // 匹配到的根数据源
                for (String sourceKey : sourceKeys) {
                    // 跳过用户传入的占位符参数, 这些值已通过mergePlaceholders合并, 无需再匹配根数据源
                    if (placeholderMap.containsKey(sourceKey)) {
                        continue;
                    }
                    Object sourceValue = this.sourceMap.get(sourceKey);
                    if (sourceValue instanceof Collection) {
                        Collection<?> sourceValueCollection = (Collection<?>) sourceValue;
                        if (CollectionUtils.isNotEmpty(sourceValueCollection)) {
                            mathRootSources.addAll(sourceValueCollection);
                        }
                    } else if (ObjectUtils.isNotEmpty(sourceValue)) {
                        mathRootSources.add(sourceValue);
                    }
                }
                // 查询数据源对象
                queryParam = queryParam.replacePlaceholder(this.sourceMap);
                if (Objects.nonNull(queryParam)) {
                    DtpRwdObjectQueryParam finalQueryParma = dtpRwdConvert.copy(queryParam);
                    if (CollectionUtils.isNotEmpty(finalQueryParma.getWithColumns())) {
                        if (finalQueryParma.containsRelationParam() && !finalQueryParma.getWithColumns()
                                .contains(DtpRwdObjectDTO.Fields.relObjIds)) {
                            finalQueryParma.addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
                        }
                    }
                    objects = BusinessThreadPoolExecutor.parallelExecute(() ->
                            dtpRwdClientService.listObject(finalQueryParma));
                }
            }
            // 从对象中解析出数据源值（takeValueExpression 为 null 时说明是纯后处理模式，跳过对象解析）
            Object sourceValue = null;
            if (CollectionUtils.isNotEmpty(objects) && StringUtils.isNotBlank(takeValueExpression)) {
                List<Object> sourceValues = new ArrayList<>();
                for (DtpRwdObjectDTO object : objects) {
                    String objectId = object.getId();
                    Set<String> relObjIds = object.getRelObjIds();
                    Object tempValue = object.analyzeExpressionByRwdObject(takeValueExpression, Object.class);
                    if (Objects.isNull(tempValue)) {
                        continue;
                    }
                    sourceValues.add(tempValue);
                    // 无匹配数据源时, 使用根对象进行关联
                    if (CollectionUtils.isEmpty(mathRootSources)) {
                        if (CollectionUtils.isNotEmpty(this.rootObjectIds)) {
                            for (String rootObjectId : this.rootObjectIds) {
                                MapExecutor.dynamicPut(rootObjectIdAndSource, rootObjectId, key,
                                        objectId, HashMap::new, LinkedHashSet::new);
                            }
                        }
                        continue;
                    }
                    // objectId 作为根数据源时, 优先根据关联查询返回的 relObjIds 建立精确归属
                    if (sourceKeys.contains(OBJECT_ID)) {
                        boolean matched = queryParam.containsRelationParam()
                                && putRootObjectSourceByRelObjIds(rootObjectIdAndSource,
                                this.rootObjectIds, key, objectId, relObjIds);
                        if (!matched && CollectionUtils.isNotEmpty(this.rootObjectIds)) {
                            for (String rootObjectId : this.rootObjectIds) {
                                MapExecutor.dynamicPut(rootObjectIdAndSource, rootObjectId, key,
                                        objectId, HashMap::new, LinkedHashSet::new);
                            }
                        }
                        continue;
                    }
                    // 匹配数据源时, 将数据源进行关联
                    if (CollectionUtils.isNotEmpty(relObjIds)) {
                        for (String relObjId : relObjIds) {
                            // 如果匹配的数据源包含了关联ID, 则放到当前数据源映射对象中
                            for (String sourceKey : sourceKeys) {
                                if (StringUtils.equals(sourceKey, OBJECT_ID)) {
                                    MapExecutor.dynamicPut(rootObjectIdAndSource, relObjId,
                                            key, tempValue, LinkedHashMap::new, LinkedHashSet::new);
                                    continue;
                                }
                                if (MapUtils.isEmpty(rootObjectIdAndSource)
                                        || (rootObjectIdAndSource.containsKey(OBJECT_ID)
                                        && rootObjectIdAndSource.size() == 1)) {
                                    MapExecutor.dynamicPut(rootObjectIdAndSource, relObjId,
                                            key, tempValue, LinkedHashMap::new, LinkedHashSet::new);
                                    continue;
                                }
                                if (MapUtils.isNotEmpty(rootObjectIdAndSource)) {
                                    // 存在根数据源时, 将当前数据源和根数据源进行匹配, 并添加匹配的数据源
                                    Map<String, Set<Object>> finalRootObjectIdAndSource = new HashMap<>();
                                    for (Map.Entry<String, Map<String, Set<Object>>> entry : rootObjectIdAndSource.entrySet()) {
                                        String rootObjectId = entry.getKey();
                                        Map<String, Set<Object>> keyAndSources = entry.getValue();
                                        Set<Object> sources = keyAndSources.get(sourceKey);
                                        if (CollectionUtils.isNotEmpty(sources) && sources.contains(relObjId)) {
                                            MapExecutor.dynamicPut(finalRootObjectIdAndSource,
                                                    rootObjectId, tempValue, LinkedHashSet::new);
                                        }
                                    }
                                    // 当前数据库存在时添加到根数据源中
                                    if (MapUtils.isNotEmpty(finalRootObjectIdAndSource)) {
                                        finalRootObjectIdAndSource.forEach((rootObjectId, localSourceValues) ->
                                                MapExecutor.dynamicPutAll(rootObjectIdAndSource, rootObjectId, key,
                                                        localSourceValues, HashMap::new, LinkedHashSet::new));
                                    }
                                }
                            }
                        }
                    }
                }
                sourceValue = sourceValues.size() == 1 ? IterableUtils.first(sourceValues) : sourceValues;
            }
            // 数据源后处理表达式：以当前 sourceMap 为 root 执行 SpEL，用结果替换原始 sourceValues
            String sourceValueExpression = sourceTackValueLogic.getSourceValueExpression();
            if (StringUtils.isNotBlank(sourceValueExpression)) {
                Expression expression = SpelUtils.getSpelExpressionParser().parseExpression(sourceValueExpression);
                StandardEvaluationContext context = SpelUtils.getDefaultStandardEvaluationContext(this.sourceMap);
                sourceValue = expression.getValue(context);
            }
            // 不定深度递归查询：将本轮新增 ID 写入 sourceMap，用累积 ID 再次执行相同 queryParam，直至无新增
            // 当配置了单向关联（relationTo+objFrom 或 relationFrom+objTo）时，自动探索反方向关系
            if (sourceTackValueLogic.isRecursive()) {
                Set<Object> visitedIds = new LinkedHashSet<>();
                if (sourceValue instanceof Collection) {
                    visitedIds.addAll((Collection<?>) sourceValue);
                } else if (ObjectUtils.isNotEmpty(sourceValue)) {
                    visitedIds.add(sourceValue);
                }
                Set<Object> newIds = new LinkedHashSet<>(visitedIds);
                // 构建反向关联查询参数模板
                DtpRwdObjectQueryParam reverseQueryParamTemplate = DtpRwdObjectQueryParam
                        .buildReverseRelationQueryParam(sourceTackValueLogic.getQueryParam());
                int recursiveDepth = 0;
                final int maxRecursiveDepth = 10;
                while (CollectionUtils.isNotEmpty(newIds) && recursiveDepth < maxRecursiveDepth) {
                    recursiveDepth++;
                    // 构建递归查询用的临时 sourceMap，将上一轮新增 ID 更新到 queryParam 引用的占位符键上
                    Map<String, Object> recursiveSourceMap = new HashMap<>(this.sourceMap);
                    for (String sourceKey : sourceKeys) {
                        if (!placeholderMap.containsKey(sourceKey)) {
                            recursiveSourceMap.put(sourceKey, new ArrayList<>(newIds));
                        }
                    }
                    // 执行正向查询
                    Set<Object> roundIds = this.executeRecursiveQuery(
                            sourceTackValueLogic.getQueryParam(), recursiveSourceMap,
                            sourceTackValueLogic.getTakeValueExpression(), visitedIds);
                    // 执行反向查询（如果存在反向模板）
                    if (Objects.nonNull(reverseQueryParamTemplate)) {
                        Set<Object> reverseRoundIds = this.executeRecursiveQuery(
                                reverseQueryParamTemplate, recursiveSourceMap,
                                sourceTackValueLogic.getTakeValueExpression(), visitedIds);
                        roundIds.addAll(reverseRoundIds);
                    }
                    if (CollectionUtils.isEmpty(roundIds)) {
                        break;
                    }
                    visitedIds.addAll(roundIds);
                    newIds = roundIds;
                }
                sourceValue = visitedIds.size() == 1 ? IterableUtils.first(visitedIds) : new ArrayList<>(visitedIds);
            }
            if (StringUtils.isNotBlank(defaultValueIfEmpty) && ObjectUtils.isEmpty(sourceValue)) {
                sourceValue = sourceTackValueLogic.toDefaultValueIfEmptyObject();
            }
            this.sourceMap.put(key, sourceValue);
        }
        this.rootObjectIdAndSource = rootObjectIdAndSource;
    }

    /**
     * 填充对象
     *
     * @since 7.250301.0
     */
    public void fillObjects() {
        if (!this.takeValueLogic.isExecute()) {
            return;
        }
        List<DtpRwdObjectQueryParam> queryParams = new ArrayList<>();
        queryParams.add(this.takeValueLogic.getQueryParam());
        List<DtpRwdObjectQueryParam> fallbackQueryParams = this.takeValueLogic.getFallbackQueryParams();
        if (CollectionUtils.isNotEmpty(fallbackQueryParams)) {
            queryParams.addAll(fallbackQueryParams);
        }
        for (DtpRwdObjectQueryParam queryParam : queryParams) {
            if (Objects.isNull(queryParam)) {
                continue;
            }
            // 拿到查询参数中存在的占位符
            String queryParamString = queryParam.placeholderString();
            List<String> sourceKeys = this.matchSourceKeys(queryParamString);
            List<DtpRwdObjectDTO> objects = queryObjects(queryParam);
            if (CollectionUtils.isNotEmpty(objects)) {
                this.objects = objects;
                this.rootObjectIdAndObjects = toRootObjectIdAndObjects(objects, sourceKeys);
                return;
            }
        }
    }

    /**
     * 根据查询条件查询对象
     *
     * @param queryParam 查询条件
     * @return 对象列表
     * @since 7.260702.0
     */
    private @NonNull List<@NonNull DtpRwdObjectDTO> queryObjects(
            @NonNull DtpRwdObjectQueryParam queryParam) {
        boolean customOrder = this.takeValueLogic.isCustomOrder();
        Collection<DtpRwdObjectOrderParam> orderParams = Collections.emptyList();
        DtpRwdObjectQueryParam finalQueryParma = queryParam.replacePlaceholder(this.sourceMap);
        if (Objects.isNull(finalQueryParma)) {
            return Collections.emptyList();
        }
        if (customOrder && CollectionUtils.isNotEmpty(finalQueryParma.getOrderCustoms())) {
            orderParams = finalQueryParma.getOrderCustoms();
            finalQueryParma.setOrderCustoms(null);
        }
        finalQueryParma.addWithColumnWithIdRelation();
        if (CollectionUtils.isNotEmpty(finalQueryParma.getWithColumns())) {
            finalQueryParma.addWithColumn(DtpRwdObjectDTO.Fields.id);
            finalQueryParma.addWithColumn(DtpRwdObjectDTO.Fields.objType);
        }
        // 查询对象
        List<DtpRwdObjectDTO> objects = BusinessThreadPoolExecutor.parallelExecute(() ->
                dtpRwdClientService.listObject(finalQueryParma));
        if (customOrder && CollectionUtils.isNotEmpty(orderParams)) {
            Comparator<DtpRwdObjectDTO> comparator = DtpRwdObjectOrderParam.toObjectComparator(
                    orderParams, objects, null);
            if (Objects.nonNull(comparator)) {
                objects.sort(comparator);
            }
        }
        return Objects.isNull(objects) ? Collections.emptyList() : objects;
    }

    /**
     * 填充对象ID和关联上下文
     *
     * @since 7.250301.0
     */
    public void fillObjectIdAndRelation() {
        if (CollectionUtils.isEmpty(this.objects)) {
            return;
        }
        // 将所有关联查出来
        InfoCodeTakeValueLogicRwdObjectDetailModel relationTackValueLogic = this.takeValueLogic.getRelation();
        if (Objects.isNull(relationTackValueLogic)) {
            return;
        }
        Set<String> objectIds = this.objects.stream()
                .map(DtpRwdObjectDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        InfoCodeRwdObjectDetailByTemplateContext relationContext = new InfoCodeRwdObjectDetailByTemplateContext(
                templateTypeModel, relationTackValueLogic, objectIds, additionalValues);
        // 重写层级
        relationContext.level = this.level + 1;
        // 将当前数据源映射向下传递
        relationContext.sourceMap.putAll(this.sourceMap);
        relationContext.rootObjectIdAndSource.putAll(this.rootObjectIdAndSource);
        // 填充数据源映射
        relationContext.fillSourceMap();
        // 填充对象
        relationContext.fillObjects();
        // 填充对象ID和关联对象
        relationContext.fillObjectIdAndRelation();
        // 填充信息点配置
        relationContext.fillInfoCodeConfig();
        this.relationContext = relationContext;
    }

    /**
     * 填充信息点配置
     *
     * @since 7.250301.0
     */
    public void fillInfoCodeConfig() {
        if (CollectionUtils.isEmpty(this.objects)) {
            return;
        }
        InfoCodeConfigQueryValueObject queryInfoCodeConfig = this.takeValueLogic.getQueryInfoCodeConfig();
        if (Objects.isNull(queryInfoCodeConfig)) {
            return;
        }
        RunningMonitorTypeEnum runningMonitorType = this.templateTypeModel.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = this.templateTypeModel.getRunningMonitorSecondType();
        InfoCodeTemplateTypeEnum templateType = this.templateTypeModel.getTemplateType();
        List<InfoCodeConfigQueryConfigValueObject> configs = queryInfoCodeConfig.getConfigs();
        if (CollectionUtils.isEmpty(configs)) {
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_ac635e914894435a89e721b248f0892c"));
        }
        Set<String> objectIds = this.objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        InfoCodeConfigByObjectQueryParam queryParam = new InfoCodeConfigByObjectQueryParam();
        queryParam.setRunningMonitorType(runningMonitorType);
        queryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        queryParam.setTemplateType(templateType);
        queryParam.setObjectIds(objectIds);
        queryParam.setFilterGroups(configs);
        if (MapUtils.isNotEmpty(additionalValues)) {
            if (this.additionalValues.containsKey(BaseInfoCodeAnalyzeStrategy.TIME_RANGE)) {
                Range<ChronoLocalDateTime<?>> between = BaseInfoCodeAnalyzeStrategy.analyzeAdditionalValues(
                        additionalValues, BaseInfoCodeAnalyzeStrategy.TIME_RANGE);
                queryParam.setStartTime(LocalDateTime.from(between.getMinimum()));
                queryParam.setEndTime(LocalDateTime.from(between.getMaximum()));
            }
        }
        List<InfoCodeObjectGroupVO> infoCodeObjectGroups = infoCodeConfigService.listInfoCodeConfigsByObject(queryParam);
        if (CollectionUtils.isEmpty(infoCodeObjectGroups)) {
            return;
        }
        Map<String, List<InfoCodeGroupVO>> objectIdConfigs = new HashMap<>(Math.max(objects.size(), 16));
        for (InfoCodeObjectGroupVO infoCodeObjectGroup : infoCodeObjectGroups) {
            String objectId = infoCodeObjectGroup.getId();
            List<InfoCodeGroupVO> relInfoCodeGroupConfigs = infoCodeObjectGroup.getRelInfoCodeGroupConfigs();

            if (CollectionUtils.isEmpty(relInfoCodeGroupConfigs)) {
                continue;
            }
            MapExecutor.dynamicPutAll(objectIdConfigs, objectId, relInfoCodeGroupConfigs, ArrayList::new);
        }
        this.objectIdConfigs = objectIdConfigs;
    }

    /**
     * 转换结果
     *
     * @return 结果
     * @since 7.240302.0
     */
    public @NonNull List<InfoCodeRwdObjectDetailDTO> toResult() {
        return toResult(this.getObjects());
    }

    /**
     * 转换结果
     *
     * @param objects 对象
     * @return 结果
     * @since 7.240302.0
     */
    public @NonNull List<InfoCodeRwdObjectDetailDTO> toResult(
            @NonNull List<@NonNull DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return Lists.newArrayList();
        }
        return objects.stream().map(item -> toResult(item, this)).collect(Collectors.toList());
    }

    /**
     * 获取用户传入的占位符参数映射
     *
     * @return 占位符参数映射, 为空时返回空Map
     * @since 7.260501.0
     */
    @SuppressWarnings("unchecked")
    private @NonNull Map<String, Object> getPlaceholderMap() {
        if (MapUtils.isEmpty(this.additionalValues)) {
            return Collections.emptyMap();
        }
        Object placeholders = this.additionalValues.get(BaseInfoCodeAnalyzeStrategy.PLACEHOLDERS);
        if (placeholders instanceof Map) {
            return (Map<String, Object>) placeholders;
        }
        return Collections.emptyMap();
    }

    /**
     * Match source keys referenced by the query parameter string.
     *
     * @param queryParamString query parameter string
     * @return source keys
     * @since 7.260501.0
     */
    private @NonNull List<@NonNull String> matchSourceKeys(@NonNull String queryParamString) {
        List<String> sourceKeys = this.sourceMap.keySet().stream()
                .filter(item -> queryParamString.contains(DtpRwdObjectQueryParam.placeholderKey(item)))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sourceKeys)) {
            return sourceKeys;
        }
        Set<String> rootSourceKeys = extractRootSourceKeys(queryParamString);
        if (CollectionUtils.isEmpty(rootSourceKeys)) {
            return Collections.emptyList();
        }
        return this.sourceMap.keySet().stream()
                .filter(rootSourceKeys::contains)
                .collect(Collectors.toList());
    }

    /**
     * Extract source keys from SpEL root references.
     *
     * @param expressionString expression string
     * @return source keys
     * @since 7.260501.0
     */
    static @NonNull Set<@NonNull String> extractRootSourceKeys(@NonNull String expressionString) {
        Set<String> sourceKeys = new LinkedHashSet<>();
        Matcher mapMatcher = ROOT_MAP_SOURCE_PATTERN.matcher(expressionString);
        while (mapMatcher.find()) {
            sourceKeys.add(mapMatcher.group(1));
        }
        Matcher propertyMatcher = ROOT_PROPERTY_SOURCE_PATTERN.matcher(expressionString);
        while (propertyMatcher.find()) {
            sourceKeys.add(propertyMatcher.group(1));
        }
        return sourceKeys;
    }

    /**
     * Write source values back to matching root objects by related object IDs.
     *
     * @param rootObjectIdAndSource root object ID -> source map
     * @param rootObjectIds root object IDs
     * @param sourceKey source key
     * @param sourceValue source value
     * @param relObjIds related object IDs
     * @return whether any root object matched
     * @since 7.260501.0
     */
    static boolean putRootObjectSourceByRelObjIds(
            @NonNull Map<String, Map<String, Set<Object>>> rootObjectIdAndSource,
            @Nullable Set<String> rootObjectIds,
            @NonNull String sourceKey,
            @Nullable Object sourceValue,
            @Nullable Set<String> relObjIds) {
        if (Objects.isNull(sourceValue)
                || CollectionUtils.isEmpty(rootObjectIds)
                || CollectionUtils.isEmpty(relObjIds)) {
            return false;
        }
        boolean matched = false;
        for (String relObjId : relObjIds) {
            if (rootObjectIds.contains(relObjId)) {
                MapExecutor.dynamicPut(rootObjectIdAndSource, relObjId, sourceKey,
                        sourceValue, HashMap::new, LinkedHashSet::new);
                matched = true;
            }
        }
        return matched;
    }

    /**
     * 转换结果
     *
     * @param object  对象
     * @param context 上下文
     * @return 结果
     * @since 7.240302.0
     */
    private @NonNull InfoCodeRwdObjectDetailDTO toResult(
            @NonNull DtpRwdObjectDTO object,
            @NonNull InfoCodeRwdObjectDetailByTemplateContext context) {
        String objectId = object.getId();
        DtpRwdObjectQueryParam queryParam = context.getTakeValueLogic().getQueryParam();
        Map<String, List<InfoCodeGroupVO>> objectIdConfigs = context.getObjectIdConfigs();
        InfoCodeRwdObjectDetailByTemplateContext relationContext = context.getRelationContext();
        Map<String, List<DtpRwdSpaceDTO>> objectIdAndInSpaces = context.getObjectIdAndInSpaces().get();
        Map<String, DtpRwdFloorDTO> objectIdAndInFloor = context.getObjectIdAndInFloor().get();
        Map<String, DtpRwdBuildingDTO> objectIdAndInBuilding = context.getObjectIdAndInBuilding().get();
        List<DtpRwdSpaceDTO> relSpaces = objectIdAndInSpaces.get(objectId);

        if (CollectionUtils.isNotEmpty(relSpaces)) {
            relSpaces.forEach(item -> item.setRelObjIds(null));
        }
        DtpRwdFloorDTO relFloor = objectIdAndInFloor.get(objectId);
        if (Objects.nonNull(relFloor)) {
            relFloor.setRelObjIds(null);
        }
        DtpRwdBuildingDTO relBuilding = objectIdAndInBuilding.get(objectId);
        if (Objects.nonNull(relBuilding)) {
            relBuilding.setRelObjIds(null);
        }
        List<InfoCodeRwdObjectDetailDTO> relObjects = new ArrayList<>();
        if (Objects.nonNull(relationContext)) {
            List<DtpRwdObjectDTO> relationObjects = relationContext.getRootObjectIdAndObjects().get(objectId);
            if (CollectionUtils.isNotEmpty(relationObjects)) {
                for (DtpRwdObjectDTO relationObject : relationObjects) {
                    InfoCodeRwdObjectDetailDTO relation = toResult(relationObject, relationContext);
                    relObjects.add(relation);
                }
            }
        }
        Collection<String> withColumns = queryParam.getWithColumns();
        // 移除不需要返回的字段
        if (CollectionUtils.isNotEmpty(withColumns)) {
            object = IterableUtils.first(DtpRwdObjectDTO.clearUselessFieldValueByWithColumns(
                    Collections.singletonList(object), withColumns));
        }
        InfoCodeRwdObjectDetailDTO resultItem = infoCodeConverter.toRwdObjectDto(object);
        resultItem.setRelInfoCodeGroupConfigs(objectIdConfigs.get(objectId));
        if (CollectionUtils.isNotEmpty(relObjects)) {
            resultItem.setRelObjects(relObjects);
        }
        if (CollectionUtils.isNotEmpty(relSpaces)) {
            resultItem.setInSpaces(relSpaces);
        }
        resultItem.setInFloor(relFloor);
        resultItem.setInBuilding(relBuilding);
        return resultItem;
    }

    /**
     * 转换根对象ID和对象映射
     *
     * @param objects    对象
     * @param sourceKeys 匹配的数据源键
     * @return 根对象ID -> 关联对象
     * @since 7.250301.0
     */
    private Map<String, List<DtpRwdObjectDTO>> toRootObjectIdAndObjects(
            @NonNull List<@NonNull DtpRwdObjectDTO> objects,
            @NonNull List<@NonNull String> sourceKeys) {
        if (CollectionUtils.isEmpty(objects)) {
            return Maps.newHashMap();
        }
        Map<String, Map<String, DtpRwdObjectDTO>> result = new HashMap<>();
        if (Objects.isNull(this.rootObjectIds)) {
            this.rootObjectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        }
        // 关联查询时根据关联ID填充根对象和查询对象映射
        for (DtpRwdObjectDTO object : objects) {
            Set<String> relObjIds = object.getRelObjIds();
            if (CollectionUtils.isEmpty(relObjIds)) {
                // 尝试通过rootObjectIdAndSource精确匹配: 对象ID属于哪个根对象的哪个数据源
                String objectId = object.getId();
                boolean matched = false;
                if (CollectionUtils.isNotEmpty(sourceKeys) && MapUtils.isNotEmpty(rootObjectIdAndSource)) {
                    for (Map.Entry<String, Map<String, Set<Object>>> entry : rootObjectIdAndSource.entrySet()) {
                        String rootObjectId = entry.getKey();
                        Map<String, Set<Object>> keyAndSources = entry.getValue();
                        for (String sourceKey : sourceKeys) {
                            Set<Object> sources = keyAndSources.get(sourceKey);
                            if (CollectionUtils.isNotEmpty(sources) && sources.contains(objectId)) {
                                result.computeIfAbsent(rootObjectId, k -> new LinkedHashMap<>()).put(objectId, object);
                                matched = true;
                                break;
                            }
                        }
                    }
                }
                // 无法精确匹配时, 降级为关联所有根对象
                if (!matched && CollectionUtils.isNotEmpty(this.rootObjectIds)) {
                    for (String baseObjectId : this.rootObjectIds) {
                        result.computeIfAbsent(baseObjectId, k -> new LinkedHashMap<>()).put(object.getId(), object);
                    }
                }
                continue;
            }
            if (CollectionUtils.isNotEmpty(sourceKeys)) {
                for (String relObjId : relObjIds) {
                    for (String sourceKey : sourceKeys) {
                        if (StringUtils.equals(sourceKey, OBJECT_ID)) {
                            result.computeIfAbsent(relObjId, k -> new LinkedHashMap<>()).put(object.getId(), object);
                            continue;
                        }
                        for (Map.Entry<String, Map<String, Set<Object>>> entry : this.rootObjectIdAndSource.entrySet()) {
                            String rootObjectId = entry.getKey();
                            Map<String, Set<Object>> keyAndSources = entry.getValue();
                            Set<Object> sources = keyAndSources.get(sourceKey);
                            if (CollectionUtils.isNotEmpty(sources)) {
                                if (sources.contains(relObjId)) {
                                    result.computeIfAbsent(rootObjectId, k -> new LinkedHashMap<>()).put(object.getId(), object);
                                }
                            }
                        }
                    }
                }
            }
        }
        Map<String, List<DtpRwdObjectDTO>> mapResult = new HashMap<>(result.size());
        for (Map.Entry<String, Map<String, DtpRwdObjectDTO>> entry : result.entrySet()) {
            mapResult.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        return mapResult;
    }

    /**
     * 数据源对象ID键
     *
     * @return 数据源对象ID键
     * @since 7.250301.0
     */
    private String objectIdKey() {
        return this.level <= 1 ? OBJECT_ID : (OBJECT_ID + "_" + this.level);
    }

    /**
     * 执行递归查询并收集未访问的 ID
     *
     * @param queryParamTemplate 查询参数模板（可含未解析的占位符）
     * @param recursiveSourceMap 递归用的 sourceMap（已更新累积 ID）
     * @param takeValueExpression 取值表达式
     * @param visitedIds 已访问的 ID 集合
     * @return 本轮新增的 ID 集合
     * @since 7.260519.0
     */
    private @NonNull Set<@NonNull Object> executeRecursiveQuery(
            @NonNull DtpRwdObjectQueryParam queryParamTemplate,
            @NonNull Map<String, Object> recursiveSourceMap,
            @Nullable String takeValueExpression,
            @NonNull Set<Object> visitedIds) {
        DtpRwdObjectQueryParam nextQueryParam = queryParamTemplate.replacePlaceholder(recursiveSourceMap);
        if (Objects.isNull(nextQueryParam)) {
            return Collections.emptySet();
        }
        DtpRwdObjectQueryParam finalNextQueryParam = dtpRwdConvert.copy(nextQueryParam);
        if (CollectionUtils.isNotEmpty(finalNextQueryParam.getWithColumns())) {
            if (finalNextQueryParam.containsRelationParam() && !finalNextQueryParam.getWithColumns()
                    .contains(DtpRwdObjectDTO.Fields.relObjIds)) {
                finalNextQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
            }
        }
        List<DtpRwdObjectDTO> nextObjects = BusinessThreadPoolExecutor.parallelExecute(() ->
                dtpRwdClientService.listObject(finalNextQueryParam));
        Set<Object> roundIds = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(nextObjects) && StringUtils.isNotBlank(takeValueExpression)) {
            for (DtpRwdObjectDTO nextObject : nextObjects) {
                Object nextValue = nextObject.analyzeExpressionByRwdObject(takeValueExpression, Object.class);
                if (Objects.nonNull(nextValue) && !visitedIds.contains(nextValue)) {
                    roundIds.add(nextValue);
                }
            }
        }
        return roundIds;
    }

    /**
     * 查询对象ID和所在空间映射
     *
     * @return 对象ID -> 所在空间
     * @since 7.250302.0
     */
    private Map<String, List<DtpRwdSpaceDTO>> queryInSpaces() {
        if (!this.takeValueLogic.isQueryInSpaces()) {
            return Maps.newHashMap();
        }
        Map<String, List<DtpRwdSpaceDTO>> objectIdAndInSpaces = new HashMap<>();
        Map<String, Set<String>> objectTypeAndIds = objects.stream().collect(Collectors.groupingBy(
                DtpRwdObjectDTO::getObjType, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        for (Map.Entry<String, Set<String>> entry : objectTypeAndIds.entrySet()) {
            String objectType = entry.getKey();
            Set<String> relObjectIds = entry.getValue();

            List<DtpRwdSpaceDTO> inSpaces = Collections.emptyList();
            DtpRwdObjectTypeEnum objectTypeEnum = BaseEnum.analyze(objectType, DtpRwdObjectTypeEnum.class);
            DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
            spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.localName,
                    DtpRwdObjectDTO.Fields.relObjIds);
            if (CollectionUtils.isNotEmpty(this.takeValueLogic.getQueryInSpaceAdditionalWithColumns())) {
                this.takeValueLogic.getQueryInSpaceAdditionalWithColumns().forEach(spaceQueryParam::addWithColumn);
            }
            switch (objectTypeEnum) {
                case EQUIPMENT:
                    inSpaces = dtpRwdClientService.listSpaceInByEquipmentId(relObjectIds, spaceQueryParam);
                    break;
                case SPACE:
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(objectType.getClass().getSimpleName(), objectType);
            }
            if (CollectionUtils.isNotEmpty(inSpaces)) {
                for (DtpRwdSpaceDTO inSpace : inSpaces) {
                    Set<String> relObjIds = inSpace.getRelObjIds();
                    if (CollectionUtils.isNotEmpty(relObjIds)) {
                        relObjIds.forEach(item -> MapExecutor.dynamicPut(objectIdAndInSpaces,
                                item, inSpace, ArrayList::new));
                    }
                }
            }
        }
        return objectIdAndInSpaces;
    }

    /**
     * 查询对象ID和所在楼层映射
     *
     * @return 对象ID -> 所在楼层
     * @since 7.250202.0
     */
    private Map<String, DtpRwdFloorDTO> queryInFloors() {
        if (!this.takeValueLogic.isQueryInFloor()) {
            return Maps.newHashMap();
        }
        Map<String, DtpRwdFloorDTO> objectIdAndInFloor = new HashMap<>();
        Map<String, Set<String>> objectTypeAndIds = objects.stream().collect(Collectors.groupingBy(
                DtpRwdObjectDTO::getObjType, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        for (Map.Entry<String, Set<String>> entry : objectTypeAndIds.entrySet()) {
            String objectType = entry.getKey();
            Set<String> relObjectIds = entry.getValue();

            List<DtpRwdFloorDTO> inFloors;
            DtpRwdObjectTypeEnum objectTypeEnum = BaseEnum.analyze(objectType, DtpRwdObjectTypeEnum.class);
            DtpRwdObjectQueryParam floorQueryParam = new DtpRwdObjectQueryParam().addWithColumn(
                    DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds);
            switch (objectTypeEnum) {
                case EQUIPMENT:
                case EQUIPMENT_GROUP:
                    inFloors = dtpRwdClientService.listFloorInByEquipmentId(relObjectIds, floorQueryParam);
                    break;
                case SPACE:
                    inFloors = dtpRwdClientService.listFloorBySpaceId(relObjectIds, floorQueryParam);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(objectType.getClass().getSimpleName(), objectType);
            }
            if (CollectionUtils.isNotEmpty(inFloors)) {
                for (DtpRwdFloorDTO inFloor : inFloors) {
                    Set<String> relObjIds = inFloor.getRelObjIds();
                    if (CollectionUtils.isNotEmpty(relObjIds)) {
                        relObjIds.forEach(item -> objectIdAndInFloor.put(item, inFloor));
                    }
                }
            }
        }
        return objectIdAndInFloor;
    }

    /**
     * 查询对象ID和所在建筑映射
     *
     * @return 对象ID -> 所在建筑
     * @since 7.250202.0
     */
    private Map<String, DtpRwdBuildingDTO> queryInBuildings() {
        if (!this.takeValueLogic.isQueryInBuilding()) {
            return Maps.newHashMap();
        }
        Map<String, DtpRwdBuildingDTO> objectIdAndInBuilding = new HashMap<>();
        Map<String, Set<String>> objectTypeAndIds = objects.stream().collect(Collectors.groupingBy(
                DtpRwdObjectDTO::getObjType, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        for (Map.Entry<String, Set<String>> entry : objectTypeAndIds.entrySet()) {
            String objectType = entry.getKey();
            Set<String> relObjectIds = entry.getValue();

            List<DtpRwdBuildingDTO> inBuildings;
            DtpRwdObjectTypeEnum objectTypeEnum = BaseEnum.analyze(objectType, DtpRwdObjectTypeEnum.class);
            DtpRwdObjectQueryParam buildingQueryParam = new DtpRwdObjectQueryParam().addWithColumn(
                    DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds);
            switch (objectTypeEnum) {
                case EQUIPMENT:
                    inBuildings = dtpRwdClientService.listBuildingInByEquipmentId(relObjectIds, buildingQueryParam);
                    break;
                case SPACE:
                    inBuildings = dtpRwdClientService.listBuildingBySpaceId(relObjectIds, buildingQueryParam);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(objectType.getClass().getSimpleName(), objectType);
            }
            if (CollectionUtils.isNotEmpty(inBuildings)) {
                for (DtpRwdBuildingDTO inFloor : inBuildings) {
                    Set<String> relObjIds = inFloor.getRelObjIds();
                    if (CollectionUtils.isNotEmpty(relObjIds)) {
                        relObjIds.forEach(item -> objectIdAndInBuilding.put(item, inFloor));
                    }
                }
            }
        }
        return objectIdAndInBuilding;
    }
}
