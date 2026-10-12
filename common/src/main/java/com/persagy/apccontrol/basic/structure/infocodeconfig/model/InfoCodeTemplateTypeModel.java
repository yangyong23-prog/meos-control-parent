package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeFilterValueObject;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.basic.utils.template.SpelUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 信息点模版类型模型
 *
 * <p>------------------------- 公共方法 -------------------------</p>
 * <ul>
 *     <li><b>match</b> - 对象是否匹配该模版</li>
 *     <li><b>toDtpRwdObjectQueryParam</b> - 转换物理世界查询参数</li>
 *     <li><b>mapInfoCodeQueryFilters</b> - 获取信息点筛选过滤条件</li>
 *     <li><b>parseFilterValueObject</b> - 解析信息点过滤值对象, 不为JSON时返回空</li>
 *     <li><b>infoCodeFilterIsJson</b> - 信息点过滤条件是否为JSON</li>
 * </ul>
 * <p>------------------------- 私有方法 -------------------------</p>
 * <ul>
 *     <li><b>handleFilterObjectQueryParamWithSource</b> - 处理过滤查询参数带数据源</li>
 *     <li><b>addFilterIotInfoCodeRuleObjectCallback</b> - 添加过滤IOT信息点规则对象回调</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@SuppressWarnings("rawtypes")
@NoArgsConstructor
public class InfoCodeTemplateTypeModel implements ValueObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoCodeTemplateTypeModel.class);
    private static final String LOG_TAG = "信息点配置-模版类型模型";

    private static final Pattern ROOT_MAP_SOURCE_PATTERN = Pattern.compile("#root\\s*\\[\\s*['\"]([^'\"]+)['\"]\\s*]");
    private static final Pattern ROOT_PROPERTY_SOURCE_PATTERN = Pattern.compile("#root\\s*\\.\\s*([A-Za-z_$][\\w$]*)");

    private static final List<Pair<String, DtpRwdCriteriaSignEnum>> INFO_CODE_FILTER_SIGNS = Lists.newArrayList(
            Pair.of("=%", DtpRwdCriteriaSignEnum.LIKE_RIGHT),
            Pair.of("%=", DtpRwdCriteriaSignEnum.LIKE_LEFT),
            Pair.of("%", DtpRwdCriteriaSignEnum.LIKE),
            Pair.of("=", null)
    );

    /**
     * 物理世界对象转换器
     */
    private static final DtpRwdConvert dtpRwdConvert = SpringUtil.getBean(DtpRwdConvert.class);

    /**
     * 物理世界查询服务
     */
    private static final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);

    /**
     * IOT服务
     */
    private static final AbstractIotDataCacheServer abstractIotDataCacheServer = SpringUtil.getBean(AbstractIotDataCacheServer.class);

    /**
     * 信息点网关
     */
    private static final InfoCodeGateway infoCodeGateway = SpringUtil.getBean(InfoCodeGateway.class);

    /**
     * 缓存服务
     */
    private static final RedisCacheTemplate redisCacheTemplate = SpringUtil.getBean(RedisCacheTemplate.class);

    /**
     * 缓存-信息点模版仓储
     */
    private static final CacheInfoCodeTemplateRepository cacheInfoCodeTemplateRepository = SpringUtil.getBean(CacheInfoCodeTemplateRepository.class);

    /**
     * 序列化/反序列化
     */
    private static final ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

    /**
     * 新缓存对象模型的固定支持范围服务。
     *
     * @since v7.260701.0
     */
    private static final @NonNull CacheObjectModelStatusService CACHE_OBJECT_MODEL_STATUS_SERVICE =
            SpringUtil.getBean(CacheObjectModelStatusService.class);

    /**
     * 模版类型ID
     */
    private @NonNull Long templateTypeId;

    /**
     * 运行监测类型
     */
    private @NonNull RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    private @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 模版类型
     */
    private @NonNull InfoCodeTemplateTypeEnum templateType;

    /**
     * 对象类型
     */
    private @NonNull DtpRwdObjectTypeEnum objectType;

    /**
     * 设备类编码
     */
    private @NonNull String classCode;

    /**
     * 信息点过滤
     */
    private @Nullable String infoCodeFilter;

    /**
     * 设备类顺序
     */
    private @Nullable Integer sequence;

    // ------------------------- 拓展字段 -------------------------

    /**
     * 设备类编码名称
     */
    @EqualsAndHashCode.Exclude
    private String classCodeName;

    /**
     * 模版类型名称
     */
    @EqualsAndHashCode.Exclude
    private String templateTypeName;

    /**
     * 信息点过滤条件
     */
    @EqualsAndHashCode.Exclude
    private Map<String, Object> infoCodeFilterData;

    /**
     * 信息点定义
     */
    @EqualsAndHashCode.Exclude
    private List<InfoCodeDefineModel> infoCodeDefines;

    /**
     * 信息点过滤值对象, 仅当信息点过滤为JSON时存在
     */
    @EqualsAndHashCode.Exclude
    private InfoCodeFilterValueObject filterValueObject;

    /**
     * 缓存设备类对象ID
     */
    @EqualsAndHashCode.Exclude
    private Set<String> cacheClassCodeObjectIds;

    /**
     * 模版下对象
     */
    @EqualsAndHashCode.Exclude
    private List<DtpRwdObjectDTO> objects;

    /**
     * 获取唯一ID
     *
     * @return 唯一ID
     * @since 7.250802.0
     */
    @SneakyThrows
    @JsonIgnore
    public String getTemplateTypeKey() {
        if (Objects.nonNull(this.filterValueObject) && StringUtils.isNotBlank(this.filterValueObject.getUniqueKey())) {
            return this.filterValueObject.getUniqueKey();
        }
        if (this.infoCodeFilterIsJson()) {
            String uniqueKey = ObjectMapperUtils.readValue(this.infoCodeFilter,
                    InfoCodeFilterValueObject.class).getUniqueKey();
            if (StringUtils.isNotBlank(uniqueKey)) {
                return uniqueKey;
            }
        }
        return new StringJoiner("-")
                .add(this.runningMonitorType.getValue())
                .add(Objects.nonNull(this.runningMonitorSecondType) ? this.runningMonitorSecondType.getValue() : "")
                .add(this.templateType.getValue())
                .add(this.classCode)
                .add(Objects.nonNull(this.infoCodeFilter) ? DigestUtils.md5Hex(this.infoCodeFilter) : "")
                .toString();
    }

    /**
     * 对象是否匹配该模版
     *
     * @param target 待匹配对象, 必须存在 {@literal id} 和 {@literal classCode} 字段
     * @return 对象是否匹配该模版
     * @since 5.240602.0
     */
    @SuppressWarnings("unchecked")
    public boolean match(@NonNull DtpRwdObjectDTO target) {
        if (!StringUtils.equals(classCode, target.getClassCode())) {
            return false;
        }
        // 如果为JSON过滤时单独处理
        InfoCodeFilterValueObject filterValueObject = parseFilterValueObject();
        if (Objects.nonNull(filterValueObject)) {
            String objectId = target.getId();
            if (Objects.isNull(cacheClassCodeObjectIds)) {
                cacheClassCodeObjectIds = new ConcurrentHashSet<>();
                DtpRwdObjectQueryParam queryParam = filterValueObject.getQueryParam();
                if (Objects.nonNull(queryParam)) {
                    InfoCodeObjectQueryParam objectQueryParam = new InfoCodeObjectQueryParam();
                    objectQueryParam.setRunningMonitorType(this.runningMonitorType);
                    objectQueryParam.setRunningMonitorSecondType(this.runningMonitorSecondType);
                    objectQueryParam.setTemplateTypeId(this.templateTypeId);
                    objectQueryParam.setTemplateTypes(Lists.newArrayList(this.templateType));
                    objectQueryParam.setTemplateTypeModels(Lists.newArrayList(this));
                    objectQueryParam.setClassCode(this.classCode);
                    objectQueryParam.setObjectType(this.objectType);
                    List<DtpRwdObjectDTO> objects = infoCodeGateway.listTemplateTypeObjects(objectQueryParam);
                    objects.forEach(item -> cacheClassCodeObjectIds.add(item.getId()));
                }
            }
            return cacheClassCodeObjectIds.contains(objectId);
        }
        // 如果不为JSON时, 则为简单根据信息点匹配
        Map<String, Object> infoCodeQueryFilters = mapInfoCodeQueryFilters();
        if (MapUtils.isNotEmpty(infoCodeQueryFilters)) {
            StringJoiner joiner = new StringJoiner(" && ");
            for (Map.Entry<String, Object> entry : infoCodeQueryFilters.entrySet()) {
                String infoCode = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Map) {
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    valueMap.forEach((sign, matchString) -> {
                        DtpRwdCriteriaSignEnum signEnum = BaseEnum.analyze(sign, DtpRwdCriteriaSignEnum.class);
                        switch (signEnum) {
                            case LIKE_RIGHT:
                                joiner.add(String.format("%s.matches('%s')", infoCode, String.format("(%s).*", matchString)));
                                break;
                            case LIKE_LEFT:
                                joiner.add(String.format("%s.matches('%s')", infoCode, String.format(".*(%s)", matchString)));
                                break;
                            case LIKE:
                                joiner.add(String.format("%s.matches('%s')", infoCode, String.format(".*(%s).*", matchString)));
                                break;
                            default:
                                throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_6bf6f3cebee54333a21f7ecfa63ab151"));
                        }
                    });
                    continue;
                }
                Collection<Object> valueCollection = value instanceof Collection
                        ? (Collection<Object>) value : Lists.newArrayList(value);
                valueCollection.forEach(item -> {
                    String expression = String.format("(%s == '%s')", infoCode, item);
                    if (item instanceof String && NumberUtils.isParsable((String) item)) {
                        expression += String.format(" || (%s == %s)", infoCode, item);
                    }
                    joiner.add(expression);
                });
            }
            Boolean match = target.analyzeExpressionByRwdObject(joiner.toString(), Boolean.class);
            return BooleanUtils.isTrue(match);
        }
        return true;
    }

    /**
     * 转换物理世界查询参数
     *
     * @return 物理世界查询参数
     * @since 5.240602.0
     */
    public @Nullable DtpRwdObjectQueryParam toDtpRwdObjectQueryParam() {
        InfoCodeFilterValueObject filterValueObject = parseFilterValueObject();
        DtpRwdObjectQueryParam result;
        if (Objects.isNull(filterValueObject)) {
            result = new DtpRwdObjectQueryParam();
            result.setGroupCode(ContextUtil.getGroupCode());
            result.setProjectId(ContextUtil.getProjectId());
            result.setClassCode(classCode);
            // 设置信息点过滤条件
            result.setInfoCodes(mapInfoCodeQueryFilters());
            if (!LocalContextStorage.isDeprecatedVersion()) {
                result.addWithColumn(InfoCodeGateway.listObjectRequiredColumns());
            }
        } else {
            result = filterValueObject.getQueryParam();
        }
        return result;
    }

    /**
     * 获取信息点筛选过滤条件
     *
     * @return 信息点筛选过滤条件
     * @since 5.240602.0
     */
    @SuppressWarnings("unchecked")
    public @NonNull Map<@NonNull String, @NonNull Object> mapInfoCodeQueryFilters() {
        if (StringUtils.isBlank(infoCodeFilter)) {
            return Maps.newHashMap();
        }
        InfoCodeFilterValueObject filterValueObject = parseFilterValueObject();
        if (Objects.nonNull(filterValueObject)) {
            DtpRwdObjectQueryParam queryParam = filterValueObject.getQueryParam();
            if (Objects.isNull(queryParam)) {
                return Maps.newHashMap();
            }
            return objectMapper.convertValue(queryParam, Map.class);
        }
        try {
            for (Pair<String, DtpRwdCriteriaSignEnum> infoCodeFilterSign : INFO_CODE_FILTER_SIGNS) {
                String sign = infoCodeFilterSign.getLeft();
                DtpRwdCriteriaSignEnum singEnum = infoCodeFilterSign.getRight();
                if (infoCodeFilter.contains(sign)) {
                    int signIndex = infoCodeFilter.indexOf(sign);
                    String infoCode = infoCodeFilter.substring(0, signIndex);
                    String filterValue = infoCodeFilter.substring(signIndex + sign.length());
                    // 为null时则代表相等
                    if (Objects.isNull(singEnum)) {
                        return new DtpRwdObjectQueryParam().addInfoCode(infoCode, filterValue).getInfoCodes();
                    } else {
                        return new DtpRwdObjectQueryParam().addInfoCode(infoCode, singEnum, filterValue).getInfoCodes();
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "信息点过滤表达式格式错误", e, Pair.of("infoCodeFilter", infoCodeFilter));
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, String.format(LocaleMessageUtil.getMessage("key_192815f47f9548da8e36d877e7e52a6b"), infoCodeFilter));
        }
        throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_42968998e5bc4f0d9ae524ced64a31b4"));
    }

    /**
     * 解析信息点过滤值对象, 不为JSON时返回空
     *
     * @return 信息点过滤值对象
     * @since 7.250101.0
     */
    public @Nullable InfoCodeFilterValueObject parseFilterValueObject() {
        return parseFilterValueObject(null, false);
    }

    /**
     * 解析信息点过滤值对象, 不为JSON时返回空
     *
     * @param queryParamCallback 查询参数回调
     * @return 信息点过滤值对象
     * @since 5.240802.0
     */
    public @Nullable InfoCodeFilterValueObject parseFilterValueObject(
            @Nullable Consumer<@NonNull DtpRwdObjectQueryParam> queryParamCallback) {
        return parseFilterValueObject(queryParamCallback, false);
    }

    /**
     * 强制实时解析信息点过滤值对象, 跳过数据库与Redis快照复用。
     *
     * <p>供缓存写入/重建链路使用, 确保 dataJson 与对象缓存基于最新配置生成；
     * 同一实例本次流程内已解析过则直接复用内存值, 避免同一模板被重复远程解析。</p>
     *
     * @param forceRefresh 是否强制实时解析
     * @return 信息点过滤值对象
     * @since 7.260801.0
     */
    public @Nullable InfoCodeFilterValueObject parseFilterValueObject(boolean forceRefresh) {
        return parseFilterValueObject(null, forceRefresh);
    }

    /**
     * 解析信息点过滤值对象, 不为JSON时返回空
     *
     * @param queryParamCallback 查询参数回调
     * @param forceRefresh       是否强制实时解析, 跳过数据库与Redis快照复用
     * @return 信息点过滤值对象
     * @since 7.260801.0
     */
    public @Nullable InfoCodeFilterValueObject parseFilterValueObject(
            @Nullable Consumer<@NonNull DtpRwdObjectQueryParam> queryParamCallback,
            boolean forceRefresh) {
        if (!infoCodeFilterIsJson()) {
            return null;
        }
        String templateTypeKey = this.getTemplateTypeKey();

        // 创建过滤值对象Supplier
        Supplier<InfoCodeFilterValueObject> filterValueObjectSupplier =
                this.buildFilterValueObjectSupplier(queryParamCallback);

        // 强制刷新: 跳过全部快照复用; 本流程内已解析过则直接复用内存值, 避免重复远程解析
        if (forceRefresh) {
            if (Objects.nonNull(this.filterValueObject)) {
                return this.filterValueObject;
            }
            this.filterValueObject = filterValueObjectSupplier.get();
            return this.filterValueObject;
        }

        // 如已经存在则直接返回, 避免重复执行
        if (Objects.nonNull(this.filterValueObject)) {
            return this.filterValueObject;
        }

        // 先从数据库缓存中查询
        if (this.allowCache()) {
            // 主查询参数缺失的缓存快照视为未命中, 回退实时解析, 避免坏快照被长期复用
            this.filterValueObject = cacheInfoCodeTemplateRepository.getByKeyOpt(templateTypeKey)
                    .map(cacheTemplate -> Optional.ofNullable(cacheTemplate.getDataJson())
                            .filter(dataJson -> Objects.nonNull(dataJson.getQueryParam()))
                            .orElseGet(filterValueObjectSupplier))
                    .orElseGet(filterValueObjectSupplier);
            if (Objects.nonNull(filterValueObject)) {
                return this.filterValueObject;
            }
        }

        // 数据库中不存在，走现有逻辑
        this.filterValueObject = redisCacheTemplate.asyncRefreshTemplate(
                InfoCodeGateway.CACHE_KEY_TEMPLATE, templateTypeKey, filterValueObjectSupplier::get);
        return this.filterValueObject;
    }

    /**
     * 构建过滤值对象实时解析Supplier。
     *
     * @param queryParamCallback 查询参数回调
     * @return 实时解析信息点过滤值对象
     * @since 7.260801.0
     */
    private @NonNull Supplier<InfoCodeFilterValueObject> buildFilterValueObjectSupplier(
            @Nullable Consumer<@NonNull DtpRwdObjectQueryParam> queryParamCallback) {
        return () -> {
            InfoCodeFilterValueObject filterValueObject = ObjectMapperUtils.readValueFailSafe(
                    infoCodeFilter, InfoCodeFilterValueObject.class);
            if (filterValueObject == null) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "信息点过滤值对象解析失败",
                        Pair.of("infoCodeFilter", infoCodeFilter));
                return null;
            }
            DtpRwdObjectQueryParam queryParam = filterValueObject.getQueryParam();
            Map<String, InfoCodeTakeValueLogicRwdObjectModel> source = filterValueObject.getSource();

            // 处理过滤查询参数带数据源
            if (Objects.nonNull(queryParam)) {
                queryParam = handleFilterObjectQueryParamWithSource(queryParam, source);
            }
            if (Objects.isNull(queryParam)) {
                filterValueObject.setQueryParam(null);
                return filterValueObject;
            }
            if (Objects.nonNull(queryParamCallback)) {
                queryParamCallback.accept(queryParam);
            }
            filterValueObject.setQueryParam(queryParam);
            return filterValueObject;
        };
    }

    /**
     * 处理过滤查询参数带数据源
     *
     * @param queryParam 查询参数
     * @param source     占位符数据源
     * @return 查询参数带数据源
     * @since 5.240802.0
     */
    private @Nullable DtpRwdObjectQueryParam handleFilterObjectQueryParamWithSource(
            @NonNull DtpRwdObjectQueryParam queryParam,
            @Nullable Map<@NonNull String, @NonNull InfoCodeTakeValueLogicRwdObjectModel> source) {
        if (MapUtils.isEmpty(source)) {
            return queryParam;
        }
        Map<String, Object> sourceMap = new HashMap<>(16);
        for (Map.Entry<String, InfoCodeTakeValueLogicRwdObjectModel> entry : source.entrySet()) {
            String key = entry.getKey();
            InfoCodeTakeValueLogicRwdObjectModel tackValueLogic = entry.getValue();
            tackValueLogic.validate(key);
            String takeValueExpression = tackValueLogic.getTakeValueExpression();
            String defaultValueIfEmpty = tackValueLogic.getDefaultValueIfEmpty();
            DtpRwdObjectQueryParam tackValueLogicQueryParam = tackValueLogic.getQueryParam();
            Object sourceValue = null;
            if (Objects.nonNull(tackValueLogicQueryParam)) {
                tackValueLogicQueryParam = tackValueLogicQueryParam.replacePlaceholder(sourceMap);
                if (Objects.nonNull(tackValueLogicQueryParam)) {
                    List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(tackValueLogicQueryParam);
                    List<Object> tempSourceValues = new ArrayList<>();
                    // takeValueExpression 为 null 时是纯后处理模式，跳过对象解析
                    if (CollectionUtils.isNotEmpty(objects) && StringUtils.isNotBlank(takeValueExpression)) {
                        for (DtpRwdObjectDTO object : objects) {
                            Object tempValue = object.analyzeExpressionByRwdObject(takeValueExpression, Object.class);
                            if (Objects.nonNull(tempValue)) {
                                tempSourceValues.add(tempValue);
                            }
                        }
                        sourceValue = tempSourceValues.size() == 1 ? IterableUtils.first(tempSourceValues) : tempSourceValues;
                    }
                }
            }
            // 数据源后处理表达式：以当前 sourceMap 为 root 执行 SpEL，用结果替换原始 sourceValue
            String sourceValueExpression = tackValueLogic.getSourceValueExpression();
            if (StringUtils.isNotBlank(sourceValueExpression)) {
                Expression expression = SpelUtils.getSpelExpressionParser().parseExpression(sourceValueExpression);
                StandardEvaluationContext context = SpelUtils.getDefaultStandardEvaluationContext(sourceMap);
                sourceValue = expression.getValue(context);
            }
            // 不定深度递归查询：将本轮新增 ID 写入 sourceMap，用累积 ID 再次执行相同 queryParam，直至无新增
            // 当配置了单向关联（relationTo+objFrom 或 relationFrom+objTo）时，自动探索反方向关系
            if (tackValueLogic.isRecursive()) {
                Set<Object> visitedIds = new LinkedHashSet<>();
                if (sourceValue instanceof Collection) {
                    visitedIds.addAll((Collection<?>) sourceValue);
                } else if (ObjectUtils.isEmpty(sourceValue)) {
                    // sourceValue 为空时无需进入递归
                } else {
                    visitedIds.add(sourceValue);
                }
                if (visitedIds.isEmpty()) {
                    sourceValue = null;
                } else {
                    // 提取 queryParam 引用的占位符 key，用于递归时更新正确的 sourceMap 条目
                    List<String> recursiveSourceKeys = Objects.nonNull(tackValueLogic.getQueryParam())
                            ? matchSourceKeys(tackValueLogic.getQueryParam().placeholderString(), sourceMap)
                            : Collections.emptyList();
                    // 构建反向关联查询参数模板
                    DtpRwdObjectQueryParam reverseQueryParamTemplate = DtpRwdObjectQueryParam
                            .buildReverseRelationQueryParam(tackValueLogic.getQueryParam());
                    Set<Object> newIds = new LinkedHashSet<>(visitedIds);
                    int recursiveDepth = 0;
                    final int maxRecursiveDepth = 10;
                    while (CollectionUtils.isNotEmpty(newIds) && recursiveDepth < maxRecursiveDepth) {
                        recursiveDepth++;
                        // 构建递归查询用的临时 sourceMap，将上一轮新增 ID 更新到 queryParam 引用的占位符键上
                        Map<String, Object> recursiveSourceMap = new HashMap<>(sourceMap);
                        for (String sourceKey : recursiveSourceKeys) {
                            recursiveSourceMap.put(sourceKey, new ArrayList<>(newIds));
                        }
                        // 执行正向查询
                        Set<Object> roundIds = executeRecursiveQuery(
                                tackValueLogic.getQueryParam(), recursiveSourceMap,
                                tackValueLogic.getTakeValueExpression(), visitedIds);
                        // 执行反向查询（如果存在反向模板）
                        if (Objects.nonNull(reverseQueryParamTemplate)) {
                            Set<Object> reverseRoundIds = executeRecursiveQuery(
                                    reverseQueryParamTemplate, recursiveSourceMap,
                                    tackValueLogic.getTakeValueExpression(), visitedIds);
                            roundIds.addAll(reverseRoundIds);
                        }
                        if (CollectionUtils.isEmpty(roundIds)) {
                            break;
                        }
                        visitedIds.addAll(roundIds);
                        newIds = roundIds;
                    }
                    if (recursiveDepth >= maxRecursiveDepth) {
                        LogExecutor.project().warn(LOGGER, LOG_TAG, "递归查询达到深度上限, 主动终止",
                                Lists.newArrayList(
                                        Pair.of("sourceKey", key),
                                        Pair.of("maxRecursiveDepth", maxRecursiveDepth),
                                        Pair.of("visitedIdsCount", visitedIds.size())));
                    }
                    sourceValue = visitedIds.size() == 1 ? IterableUtils.first(visitedIds) : new ArrayList<>(visitedIds);
                }
            }
            if (StringUtils.isNotBlank(defaultValueIfEmpty) && ObjectUtils.isEmpty(sourceValue)) {
                sourceValue = tackValueLogic.toDefaultValueIfEmptyObject();
            }
            sourceMap.put(key, sourceValue);
        }
        if (MapUtils.isNotEmpty(sourceMap)) {
            return queryParam.replacePlaceholder(sourceMap);
        }
        return queryParam;
    }

    /**
     * 信息点过滤条件是否为JSON
     *
     * @return 信息点过滤条件是否为JSON
     * @since 5.240802.0
     */
    public boolean infoCodeFilterIsJson() {
        return StringUtils.isNotBlank(infoCodeFilter) && infoCodeFilter.startsWith(JsonToken.START_OBJECT.asString());
    }

    /**
     * 是否允许缓存
     *
     * @return 是否允许缓存
     * @since 7.260302.0
     */
    public boolean allowCache() {
        return InfoCodeTemplateTypeEnum.allowCache(
                this.runningMonitorType, this.templateType);
    }

    /**
     * 查询模版关联对象
     *
     * @return 模版关联对象
     * @since 7.250901.0
     */
    public List<DtpRwdObjectDTO> queryTemplateObjects() {
        return this.queryTemplateObjects(false);
    }

    /**
     * 查询缓存对象模型写入所需的模板对象快照。
     *
     * <p>只有该入口会为本版本支持的末端、照明模板请求 classCode 对应的完整存储字段；普通
     * 业务查询继续使用原有简单字段，避免缓存写入需求扩大旧接口响应。</p>
     *
     * @return 缓存写入所需模板对象
     * @since v7.260701.0
     */
    public List<DtpRwdObjectDTO> queryTemplateObjectsForCacheWrite() {
        return this.queryTemplateObjects(true);
    }

    /**
     * 按普通业务或缓存写入场景查询模板对象。
     *
     * @param cacheWrite 是否为缓存对象模型写入快照
     * @return 模板对象
     * @since v7.260701.0
     */
    private List<DtpRwdObjectDTO> queryTemplateObjects(boolean cacheWrite) {
        boolean objectModelCacheWrite = cacheWrite && this.allowCache()
                && InfoCodeTemplateTypeModel.CACHE_OBJECT_MODEL_STATUS_SERVICE.isSupported(
                        this.runningMonitorType);
        DtpRwdObjectQueryParam queryParam = this.toDtpRwdObjectQueryParam();
        InfoCodeFilterValueObject filterValueObject = this.getFilterValueObject();
        if (Objects.isNull(queryParam)) {
            return Collections.emptyList();
        }
        if (!objectModelCacheWrite && LocalContextStorage.isDeprecatedVersion()) {
            return BusinessThreadPoolExecutor.parallelExecute(() -> dtpRwdClientService.listObject(queryParam));
        }
        if (objectModelCacheWrite) {
            // 相同 classCode 始终使用同一份完整快照字段，避免不同 Key 覆盖共享对象时丢字段。
            queryParam.addWithColumn(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                    this.classCode).toArray(new String[0]));
        } else {
            queryParam.addWithColumn(InfoCodeGateway.listObjectRequiredColumns());
        }
        if (Objects.nonNull(filterValueObject)) {
            Set<String> filterIotInfoCodeRules = filterValueObject.getFilterIotInfoCodeRules();
            if (CollectionUtils.isNotEmpty(filterIotInfoCodeRules)) {
                queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.classCode);
            }
        }
        List<DtpRwdObjectDTO> objects = BusinessThreadPoolExecutor.parallelExecute(() ->
                dtpRwdClientService.listObject(queryParam));
        if (objectModelCacheWrite) {
            // 写入字段按模板 classCode 组装，返回其他 classCode 对象时其登记字段未被请求，
            // 整列覆盖存在丢字段风险；聚合告警暴露模板配置异常，不阻断构建。
            List<DtpRwdObjectDTO> crossClassObjects =
                    InfoCodeTemplateTypeModel.listCrossClassObjects(this.classCode, objects);
            if (CollectionUtils.isNotEmpty(crossClassObjects)) {
                LogExecutor.project().warn(LOGGER, LOG_TAG, "模板缓存写入返回非模板classCode对象",
                        Lists.newArrayList(
                                Pair.of("templateTypeKey", this.getTemplateTypeKey()),
                                Pair.of("templateClassCode", this.classCode),
                                Pair.of("crossObjectCount", crossClassObjects.size()),
                                Pair.of("sample", crossClassObjects.stream().limit(5)
                                        .map(object -> object.getId() + ":" + object.getClassCode())
                                        .collect(Collectors.toList()))));
            }
        }
        if (Objects.nonNull(filterValueObject)) {
            Set<String> filterIotInfoCodeRules = filterValueObject.getFilterIotInfoCodeRules();
            if (CollectionUtils.isNotEmpty(filterIotInfoCodeRules)) {
                this.removeIfNotExistsIotInfoCodeRule(objects, filterIotInfoCodeRules);
            }
        }
        return objects;
    }

    /**
     * 筛选查询结果中不属于模板 classCode 的对象。
     *
     * <p>缓存写入字段按模板 classCode 的白名单组装，返回其他 classCode 的对象时其登记字段
     * 未被请求，整列覆盖存在丢字段风险；该方法用于识别该异常并触发告警。</p>
     *
     * @param classCode 模板 classCode
     * @param objects   查询返回对象
     * @return 非模板 classCode 的对象；入参为空或全部匹配时为空列表
     * @since v7.260802.3
     */
    static @NonNull List<DtpRwdObjectDTO> listCrossClassObjects(
            @Nullable String classCode, @Nullable List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return Collections.emptyList();
        }
        return objects.stream()
                .filter(object -> !StringUtils.equals(classCode, object.getClassCode()))
                .collect(Collectors.toList());
    }

    /**
     * 过滤不存在IOT规则的对象
     *
     * @param objects                对象
     * @param filterIotInfoCodeRules IOT信息点
     * @since 7.250701.0
     */
    @SuppressWarnings("unchecked")
    private void removeIfNotExistsIotInfoCodeRule(
            @NonNull List<DtpRwdObjectDTO> objects,
            @NonNull Set<String> filterIotInfoCodeRules) {
        if (CollectionUtils.isEmpty(objects) || CollectionUtils.isEmpty(filterIotInfoCodeRules)) {
            return;
        }
        Map<String, Set<String>> classCodeAndIds = objects.stream().collect(
                Collectors.groupingBy(DtpRwdObjectDTO::getClassCode,
                        Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndIds, filterIotInfoCodeRules);
        objects.removeIf(item -> {
            String objectId = item.getId();
            Set<String> existsRuleInfoCodes = objectIdAndExistsRuleInfoCodes
                    .getOrDefault(objectId, Collections.emptySet());

            // 判断每个信息点规则是否存在, 不存在时删除对象
            for (String filterIotInfoCodeRule : filterIotInfoCodeRules) {
                if (!existsRuleInfoCodes.contains(filterIotInfoCodeRule)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * 创建信息点模板类型
     *
     * @return 信息点模板类型
     * @since 7.251202.0
     */
    public static List<InfoCodeTemplateTypeModel> from(List<InfoCodeTemplateType> templateTypes) {
        if (CollectionUtils.isEmpty(templateTypes)) {
            return Collections.emptyList();
        }
        InfoCodeConverter converter = SpringUtil.getBean(InfoCodeConverter.class);
        return templateTypes.stream().map(converter::toTemplateTypeModel).collect(Collectors.toList());
    }

    /**
     * 匹配查询参数字符串中引用的数据源键
     *
     * @param queryParamString 查询参数字符串
     * @param sourceMap 数据源映射
     * @return 数据源键列表
     * @since 7.260519.0
     */
    private static @NonNull List<@NonNull String> matchSourceKeys(
            @NonNull String queryParamString,
            @NonNull Map<String, Object> sourceMap) {
        List<String> sourceKeys = sourceMap.keySet().stream()
                .filter(item -> queryParamString.contains(DtpRwdObjectQueryParam.placeholderKey(item)))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sourceKeys)) {
            return sourceKeys;
        }
        Set<String> rootSourceKeys = extractRootSourceKeys(queryParamString);
        if (CollectionUtils.isEmpty(rootSourceKeys)) {
            return Collections.emptyList();
        }
        return sourceMap.keySet().stream()
                .filter(rootSourceKeys::contains)
                .collect(Collectors.toList());
    }

    /**
     * 从 SpEL root 引用中提取数据源键
     *
     * @param expressionString 表达式字符串
     * @return 数据源键集合
     * @since 7.260519.0
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
     * 执行递归查询并收集未访问的 ID
     *
     * @param queryParamTemplate 查询参数模板（可含未解析的占位符）
     * @param recursiveSourceMap 递归用的 sourceMap（已更新累积 ID）
     * @param takeValueExpression 取值表达式
     * @param visitedIds 已访问的 ID 集合
     * @return 本轮新增的 ID 集合
     * @since 7.260519.0
     */
    private static @NonNull Set<@NonNull Object> executeRecursiveQuery(
            @NonNull DtpRwdObjectQueryParam queryParamTemplate,
            @NonNull Map<String, Object> recursiveSourceMap,
            @Nullable String takeValueExpression,
            @NonNull Set<Object> visitedIds) {
        DtpRwdObjectQueryParam nextQueryParam = queryParamTemplate.replacePlaceholder(recursiveSourceMap);
        if (Objects.isNull(nextQueryParam)) {
            return Collections.emptySet();
        }
        List<DtpRwdObjectDTO> nextObjects = dtpRwdClientService.listObject(nextQueryParam);
        Set<Object> roundIds = new LinkedHashSet<>();
        // takeValueExpression 为 null 时是纯后处理模式，跳过对象解析
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
}
