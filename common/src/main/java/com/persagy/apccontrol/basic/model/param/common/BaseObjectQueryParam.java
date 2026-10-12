package com.persagy.apccontrol.basic.model.param.common;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.DynamicInfoCodeFilter;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基础运行监测对象查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
public abstract class BaseObjectQueryParam<T extends DtpRwdObjectDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseObjectQueryParam.class);
    private static final String LOG_TAG = "公共对象查询";

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    protected final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
    @JsonIgnore
    @SuppressWarnings("rawtypes")
    @EqualsAndHashCode.Exclude
    protected final AbstractIotDataCacheServer abstractIotDataCacheServer = SpringUtil.getBean(AbstractIotDataCacheServer.class);
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    protected final DtpRwdConvert dtpRwdConvert = SpringUtil.getBean(DtpRwdConvert.class);

    /**
     * 是否使用 MySQL 缓存
     */
    @EqualsAndHashCode.Exclude
    private boolean mysqlCache = false;

    /**
     * 过滤对象ID, 为空时不过滤
     */
    protected Set<String> filterObjectIds;

    /**
     * 过滤设备类编码
     */
    protected Set<String> filterClassCodes;

    /**
     * 过滤排除设备类编码
     */
    protected Set<String> filterExcludeClassCodes;

    /**
     * 过滤所在建筑ID, 为空时不过滤
     */
    protected Set<String> filterBuildingIds;

    /**
     * 过滤所在楼层ID, 为空时不过滤
     */
    protected Set<String> filterFloorIds;

    /**
     * 查询字段, 为空时查询标准字段
     *
     * @see #addWithColumn(String...)
     * @see #toWithColumnArray()
     */
    private Set<String> withColumns;

    /**
     * 过滤信息码规则, 为空时不过滤
     */
    private Set<String> filterInfoCodeRules;

    /**
     * 过滤信息点数值, 为空时不过滤
     */
    private Map<String, Object> filterInfoCodeValueMap;

    /**
     * 排除信息点数值, 为空时不排除
     */
    private Map<String, Object> excludeInfoCodeValues;

    /**
     * 查询对象
     *
     * @param originalQueryParam 原始查询参数
     * @param realQueryParam     新地查询参数
     * @param customQuery        自定义查询
     * @return 查询结果
     * @since 7.250201.0
     */
    protected abstract List<T> queryObject(
            @NonNull DtpRwdObjectQueryParam originalQueryParam,
            @NonNull DtpRwdObjectQueryParam realQueryParam,
            @Nullable Function<DtpRwdObjectQueryParam, List<T>> customQuery);

    /**
     * 缓存键类型
     *
     * @return 缓存键类型
     * @since 7.260401.0
     */
    protected abstract String mysqlCacheKeyType();

    /**
     * 缓存键
     *
     * @return 缓存键
     * @since 7.260401.0
     */
    public String mysqlCacheKey() {
        return this.mysqlCacheKeyType() + "_" + this.hashCode();
    }

    /**
     * 查询对象
     *
     * @param queryParam 查询参数
     * @return 查询结果
     * @since 7.250201.0
     */
    public List<T> queryObject(@NonNull DtpRwdObjectQueryParam queryParam) {
        return queryObject(queryParam, null);
    }

    /**
     * 查询对象
     *
     * @param queryParam  查询参数
     * @param customQuery 自定义查询
     * @return 查询结果
     * @since 7.250201.0
     */
    public List<T> queryObject(
            @NonNull DtpRwdObjectQueryParam queryParam,
            @Nullable Function<DtpRwdObjectQueryParam, List<T>> customQuery) {
        // 填充必须查询字段
        DtpRwdObjectQueryParam realQueryParam = fillRequiredWithColumns(queryParam);
        // 查询对象
        List<T> result = queryObject(queryParam, realQueryParam, customQuery);
        // 过滤基础条件
        filterBasicCondition(queryParam, result);
        // 过滤扩展功能项信息点规则、信息点数值
        filterInfoCodeCondition(result);
        // 根据查询字段清除无用字段值
        return DtpRwdObjectDTO.clearUselessFieldValueByWithColumns(result, this.withColumns);
    }

    /**
     * 添加必须查询字段
     *
     * @param column 查询字段
     * @since 7.250501.0
     */
    public void addRequiredWithColumn(@NonNull String @Nullable ... column) {
        if (CollectionUtils.isEmpty(this.withColumns)) {
            return;
        }
        addWithColumn(column);
    }

    /**
     * 添加查询字段
     *
     * @param column 查询字段
     * @since 7.250201.0
     */
    public void addWithColumn(@NonNull String @Nullable ... column) {
        if (ArrayUtils.isNotEmpty(column)) {
            // setWithColumns 可能接收不可变集合，追加字段前复制，避免修改调用方集合失败。
            Set<String> withColumns = new LinkedHashSet<>(
                    Optional.ofNullable(this.withColumns).orElse(Collections.emptySet()));
            withColumns.addAll(Arrays.asList(Objects.requireNonNull(column)));
            this.withColumns = withColumns;
        }
    }

    /**
     * 添加查询字段(按字段常量类反射展开, 如 {@link DtpRwdObjectDTO.Fields})
     *
     * @param cls 字段常量类
     * @since 7.261001.0
     */
    public void addWithColumn(@NonNull Class<?> cls) {
        Set<String> fieldNames = Stream.concat(FieldUtils.getAllFieldsList(cls).stream(),
                        FieldUtils.getAllFieldsList(DtpRwdObjectDTO.Fields.class).stream())
                .map(field -> {
                    try {
                        return String.valueOf(FieldUtils.readStaticField(field));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(fieldNames)) {
            this.addWithColumn(fieldNames.toArray(new String[0]));
        }
    }

    /**
     * 转换为查询字段数组
     *
     * @return 查询字段数组
     * @since 7.250201.0
     */
    public String[] toWithColumnArray() {
        return Optional.ofNullable(this.withColumns).orElseGet(HashSet::new).toArray(new String[0]);
    }

    /**
     * 是否可缓存
     *
     * @return 是否可缓存
     * @since 7.251002.0
     */
    public boolean canCache() {
        return MapUtils.isEmpty(this.filterInfoCodeValueMap) && MapUtils.isEmpty(this.excludeInfoCodeValues);
    }

    /**
     * 根据过滤条件过滤
     *
     * @param targets      目标源
     * @param queryParam   查询参数
     * @param relationFrom 主对象筛选条件
     * @param objTo        关联从对象ID
     * @return 目标源
     * @since 7.250201.0
     */
    protected Collection<String> filterByRelationFrom(
            @Nullable Collection<String> targets,
            @NonNull DtpRwdObjectQueryParam queryParam,
            @NonNull DtpRwdRelationTypeEnum relationFrom,
            @Nullable Set<String> objTo) {
        DtpRwdObjectQueryParam copyQueryParam = dtpRwdConvert.copy(queryParam);
        copyQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id));
        copyQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(relationFrom).setObjTo(objTo));
        List<String> ids = dtpRwdClientService.listObject(copyQueryParam).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        return Objects.nonNull(targets) ? CollectionUtils.intersection(ids, targets) : ids;
    }

    /**
     * 根据过滤条件过滤
     *
     * @param targets    目标源
     * @param queryParam 查询参数
     * @param relationTo 从对象筛选条件
     * @param objFrom    关联主对象ID
     * @return 目标源
     * @since 7.250201.0
     */
    protected Collection<String> filterByRelationTo(
            @Nullable Collection<String> targets,
            @NonNull DtpRwdObjectQueryParam queryParam,
            @NonNull DtpRwdRelationTypeEnum relationTo,
            @Nullable Set<String> objFrom) {
        DtpRwdObjectQueryParam copyQueryParam = dtpRwdConvert.copy(queryParam);
        copyQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id));
        copyQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(relationTo).setObjFrom(objFrom));
        List<String> ids = dtpRwdClientService.listObject(copyQueryParam).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        return Objects.nonNull(targets) ? CollectionUtils.intersection(ids, targets) : ids;
    }

    /**
     * 根据ID过滤对象
     *
     * @param targets   目标源
     * @param filterIds 过滤ID
     * @since 7.250201.0
     */
    protected void filterByIds(@NonNull List<T> targets, @Nullable Collection<String> filterIds) {
        if (CollectionUtils.isEmpty(targets) || Objects.isNull(filterIds)) {
            return;
        }
        // 当过滤ID为空时, 清除目标源
        if (CollectionUtils.isEmpty(filterIds)) {
            targets.clear();
            return;
        }
        // 当过滤ID不为空时, 过滤目标源
        targets.removeIf(target -> !filterIds.contains(target.getId()));
    }

    /**
     * 填充必须查询字段
     *
     * @param queryParam 查询参数
     * @return 新地查询参数
     * @see #filterBasicCondition(DtpRwdObjectQueryParam, List)
     * @since 7.250201.0
     */
    private DtpRwdObjectQueryParam fillRequiredWithColumns(@NonNull DtpRwdObjectQueryParam queryParam) {
        DtpRwdObjectQueryParam realQueryParam = dtpRwdConvert.copy(queryParam);
        // 当原始查询参数中包含了过滤条件, 入参过滤条件则无法赋值, 所以只能进行内存过滤
        if (CollectionUtils.isNotEmpty(this.filterObjectIds)) {
            if (Objects.nonNull(realQueryParam.readPropertyValue(DtpRwdObjectDTO.Fields.id))) {
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.addInfoCode(DtpRwdObjectDTO.Fields.id, this.filterObjectIds);
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterBuildingIds)) {
            if (realQueryParam.getInfoCodes().containsKey(DtpRwdConvert.INFO_CODE_BUILDING_ID)) {
                realQueryParam.addWithColumn(DtpRwdConvert.INFO_CODE_BUILDING_ID);
            } else {
                realQueryParam.addInfoCode(DtpRwdConvert.INFO_CODE_BUILDING_ID, this.filterBuildingIds);
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterFloorIds)) {
            if (realQueryParam.getInfoCodes().containsKey(DtpRwdConvert.INFO_CODE_FLOOR_ID)) {
                realQueryParam.addWithColumn(DtpRwdConvert.INFO_CODE_FLOOR_ID);
            } else {
                realQueryParam.addInfoCode(DtpRwdConvert.INFO_CODE_FLOOR_ID, this.filterFloorIds);
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterClassCodes)) {
            if (realQueryParam.getInfoCodes().containsKey(DtpRwdObjectDTO.Fields.classCode)
                    || CollectionUtils.isNotEmpty(realQueryParam.getClassCode())) {
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
            } else {
                realQueryParam.setClassCode(this.filterClassCodes);
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterExcludeClassCodes)) {
            if (realQueryParam.getInfoCodes().containsKey(DtpRwdObjectDTO.Fields.classCode)
                    || CollectionUtils.isNotEmpty(realQueryParam.getClassCode())) {
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.classCode);
            } else {
                realQueryParam.addInfoCode(DtpRwdObjectDTO.Fields.classCode,
                        DtpRwdCriteriaSignEnum.NOT_EQUALS_IN, this.filterExcludeClassCodes);
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterInfoCodeRules)) {
            realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.classCode);
        }
        if (MapUtils.isNotEmpty(this.filterInfoCodeValueMap) || MapUtils.isNotEmpty(this.excludeInfoCodeValues)) {
            realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        }
        realQueryParam.addWithColumn(toWithColumnArray());
        return realQueryParam;
    }

    /**
     * 过滤基础条件
     *
     * @param originalQueryParam 原始查询参数
     * @param targets            目标源
     * @see #fillRequiredWithColumns(DtpRwdObjectQueryParam)
     * @since 7.250201.0
     */
    private void filterBasicCondition(@NonNull DtpRwdObjectQueryParam originalQueryParam, @NonNull List<T> targets) {
        // 当原始查询参数中包含了过滤条件, 入参过滤条件则无法赋值, 所以只能进行内存过滤
        if (Objects.nonNull(originalQueryParam.readPropertyValue(DtpRwdObjectDTO.Fields.id))
                && CollectionUtils.isNotEmpty(this.filterObjectIds)) {
            targets.removeIf(item -> !this.filterObjectIds.contains(item.getId()));
        }
        if (Objects.nonNull(originalQueryParam.readPropertyValue(DtpRwdObjectDTO.Fields.classCode))
                || originalQueryParam.getInfoCodes().containsKey(DtpRwdObjectDTO.Fields.classCode)) {
            if (CollectionUtils.isNotEmpty(this.filterClassCodes)) {
                targets.removeIf(item -> !this.filterClassCodes.contains(item.getClassCode()));
            }
            if (CollectionUtils.isNotEmpty(this.filterExcludeClassCodes)) {
                targets.removeIf(item -> this.filterExcludeClassCodes.contains(item.getClassCode()));
            }
        }
        if (originalQueryParam.getInfoCodes().containsKey(DtpRwdConvert.INFO_CODE_BUILDING_ID)
                && CollectionUtils.isNotEmpty(this.filterBuildingIds)) {
            targets.removeIf(item -> !this.filterBuildingIds.contains(
                    item.analyzeProperties(DtpRwdConvert.INFO_CODE_BUILDING_ID)));
        }
        if (originalQueryParam.getInfoCodes().containsKey(DtpRwdConvert.INFO_CODE_FLOOR_ID)
                && CollectionUtils.isNotEmpty(this.filterFloorIds)) {
            targets.removeIf(item -> !this.filterFloorIds.contains(
                    item.analyzeProperties(DtpRwdConvert.INFO_CODE_FLOOR_ID)));
        }
    }

    /**
     * 过滤基础条件
     *
     * @param targets 目标源
     * @see #fillRequiredWithColumns(DtpRwdObjectQueryParam)
     * @since 7.250201.0
     */
    private void filterInfoCodeCondition(@NonNull List<T> targets) {
        if (CollectionUtils.isEmpty(targets)) {
            return;
        }
        filterByInfoCodeRules(targets);
        filterByInfoCodeValues(targets);
    }

    /**
     * 根据信息点规则过滤
     *
     * @param targets 目标源
     */
    @SuppressWarnings("unchecked")
    private void filterByInfoCodeRules(List<T> targets) {
        if (CollectionUtils.isEmpty(this.filterInfoCodeRules)) {
            return;
        }

        // 分组<对象类,对象id集合>
        Map<String, Set<String>> classCodeAndIdsMap = targets.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        DtpRwdObjectDTO::getClassCode,
                        dto -> Collections.singleton(dto.getId()),
                        (set1, set2) -> {
                            Set<String> merged = new HashSet<>(set1);
                            merged.addAll(set2);
                            return merged;
                        }
                ));

        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes =
                abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                        classCodeAndIdsMap,
                        filterInfoCodeRules
                );

        // 规则不包含当前对象，或者规则不包含当前对象的信息点，直接返回
        targets.removeIf(item -> {
            Set<String> ruleCodes = objectIdAndExistsRuleInfoCodes.get(item.getId());
            return ruleCodes == null || Collections.disjoint(ruleCodes, filterInfoCodeRules);
        });
    }

    /**
     * 根据信息点数值过滤
     *
     * @param targets 目标源
     */
    @SuppressWarnings("unchecked")
    private void filterByInfoCodeValues(@Nullable List<T> targets) {
        if (CollectionUtils.isEmpty(targets) || (MapUtils.isEmpty(this.filterInfoCodeValueMap)
                && MapUtils.isEmpty(this.excludeInfoCodeValues))) {
            return;
        }
        Set<String> objectIds = targets.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> filterInfoCodes = new HashSet<>();
        Set<String> excludeInfoCodes = new HashSet<>();
        if (MapUtils.isNotEmpty(this.filterInfoCodeValueMap)) {
            filterInfoCodes = this.filterInfoCodeValueMap.keySet();
        }
        if (MapUtils.isNotEmpty(this.excludeInfoCodeValues)) {
            excludeInfoCodes = this.excludeInfoCodeValues.keySet();
        }
        // 不存在过滤信息点时跳过
        if (CollectionUtils.isEmpty(filterInfoCodes) && CollectionUtils.isEmpty(excludeInfoCodes)) {
            return;
        }
        // 查询信息点值
        Set<String> infoCodes = Stream.of(filterInfoCodes, excludeInfoCodes)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer
                .listInfoCodeValueInOneHour(objectIds, infoCodes);
        // 当IOT信息点不存在时跳过
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            // 存在信息点值过滤, 且其中存在任意一个不为 null 时清空结果
            if (MapUtils.isNotEmpty(this.filterInfoCodeValueMap)
                    && this.filterInfoCodeValueMap.values().stream().anyMatch(Objects::nonNull)) {
                targets.clear();
                return;
            }
            return;
        }
        Map<String, Map<String, InfoCodeValueVO>> objectIdAndInfoCodeValues = new HashMap<>();
        infoCodeValues.forEach(item -> MapExecutor.dynamicPut(objectIdAndInfoCodeValues,
                item.getObjectId(), item.getInfoCode(), item, HashMap::new));
        Iterator<T> iterator = targets.iterator();
        while (iterator.hasNext()) {
            String objectId = iterator.next().getId();
            Map<String, InfoCodeValueVO> infoCodeAndValue = objectIdAndInfoCodeValues.get(objectId);
            // 当前对象无IOT信息点值时跳过
            if (MapUtils.isEmpty(infoCodeAndValue)) {
                // 存在过滤信息点时移除
                if (MapUtils.isNotEmpty(this.filterInfoCodeValueMap)) {
                    iterator.remove();
                    continue;
                }
                continue;
            }
            if (MapUtils.isNotEmpty(this.filterInfoCodeValueMap)) {
                List<DynamicInfoCodeFilter> dynamicInfoCodeFilters = this.filterInfoCodeValueMap.entrySet().stream()
                        .map(item -> new DynamicInfoCodeFilter(item.getKey(), item.getValue()))
                        .collect(Collectors.toList());
                // 过滤信息点值任意一个不满足则移除
                if (!dynamicInfoCodeFilters.stream().allMatch(item ->
                        item.equalsDataValue(infoCodeAndValue.get(item.getInfoCode())))) {
                    iterator.remove();
                    continue;
                }
            }
            if (MapUtils.isNotEmpty(this.excludeInfoCodeValues)) {
                List<DynamicInfoCodeFilter> dynamicInfoCodeFilters = this.excludeInfoCodeValues.entrySet().stream()
                        .map(item -> new DynamicInfoCodeFilter(item.getKey(), item.getValue()))
                        .collect(Collectors.toList());
                // 排除信息点任意一个满足则移除
                if (dynamicInfoCodeFilters.stream().anyMatch(item ->
                        item.equalsDataValue(infoCodeAndValue.get(item.getInfoCode())))) {
                    iterator.remove();
                }
            }
        }
    }
}
