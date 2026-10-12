package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeStaticFilter;
import com.persagy.apccontrol.basic.structure.common.parser.PropertyDescriptorParser;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.basic.utils.template.SpelUtils;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 对象查询参数
 *
 * <p>------------------------- 公共方法 -------------------------
 * <ul>
 *     <li><b>WithColumn</b>
 *         <ul>
 *             <li><b>addWithColumnByMonitoringSurveillanceStatus</b> - 添加监控状态仅查询信息点</li>
 *             <li><b>addWithColumn</b> - 添加仅查询信息点</li>
 *             <li><b>addWithColumnWithDefault</b> - 添加仅查询信息点带默认值</li>
 *             <li><b>addWithColumnWithIdRelation</b> - 添加ID和关联信息点</li>
 *         </ul>
 *     </li>
 *     <li><b>InfoCode</b>
 *         <ul>
 *             <li><b>filterMonitoringSurveillanceStatus</b> - 过滤监控状态</li>
 *             <li><b>addInfoCode</b> - 添加自定义信息点筛选</li>
 *         </ul>
 *     </li>
 *     <li><b>Function</b>
 *         <ul>
 *             <li><b>containsRelationParam</b> - 是否包含关联参数</li>
 *             <li><b>replacePlaceholder</b> - 替换占位符</li>
 *             <li><b>placeholderString</b> - 解析为占位符字符串, 去掉数组占位符左右的数组符号</li>
 *             <li><b>placeholderKey</b> - 占位符Key</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtpRwdObjectQueryParam implements PropertyDescriptorParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtpRwdObjectQueryParam.class);
    private static final String LOG_TAG = "物理世界对象查询参数";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\[\"(\\$\\{[^}]+})\"]");
    private static final Pattern SPEL_PATTERN = Pattern.compile("\\$\\{([^}]*)}");

    /**
     * 项目ID
     */
    private Collection<String> projectId;

    /**
     * 对象ID
     */
    private Collection<String> id;

    /**
     * 对象名称
     */
    private Collection<String> name;

    /**
     * 对象本地ID
     */
    private Collection<String> localId;

    /**
     * 对象本地名称
     */
    private Collection<String> localName;

    /**
     * 对象类型
     */
    private Collection<String> objType;

    /**
     * 对象编码
     */
    private Collection<String> classCode;

    /**
     * 集团编码
     */
    private Collection<String> groupCode;

    /**
     * 主对象筛选条件
     */
    private DtpRwdObjectRelationQueryParam relationFrom;

    /**
     * 从对象筛选条件
     */
    private DtpRwdObjectRelationQueryParam relationTo;

    /**
     * 是否启用
     */
    private Boolean valid = true;

    // ------------------------- 拓展字段 -------------------------

    /**
     * 分页参数
     */
    private PageQuery pageQuery;

    /**
     * 排序参数
     */
    private Collection<DtpRwdObjectOrderParam> orderCustoms;

    /**
     * 仅查询字段
     */
    private Collection<String> withColumns;

    /**
     * 信息点
     *
     * @see #addInfoCode(String, Object)
     * @see #addInfoCode(String, Collection)
     */
    private Map<String, Object> infoCodes;

    // -------------------------------- WithColumns --------------------------------

    /**
     * 添加监控状态仅查询信息点
     *
     * @return 当前实例
     * @since 5.241102.0
     */
    public DtpRwdObjectQueryParam addWithColumnByMonitoringSurveillanceStatus() {
        if (CollectionUtils.isNotEmpty(this.withColumns)) {
            this.addWithColumn(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());
        }
        return this;
    }

    /**
     * 添加仅查询信息点
     *
     * @param column 字段
     * @return 当前实例
     * @since 5.240701.0
     */
    public DtpRwdObjectQueryParam addWithColumn(@Nullable String... column) {
        if (ArrayUtils.isNotEmpty(column)) {
            // setWithColumns 可能接收不可变集合，追加字段前复制，避免修改调用方集合失败。
            Collection<String> withColumns = new LinkedHashSet<>(
                    Optional.ofNullable(this.withColumns).orElse(Collections.emptyList()));
            withColumns.addAll(Arrays.asList(Objects.requireNonNull(column)));
            this.withColumns = withColumns;
        }
        return this;
    }

    /**
     * 添加仅查询信息点
     *
     * @param column 字段
     * @return 当前实例
     * @since 5.241102.0
     */
    public DtpRwdObjectQueryParam addWithColumnWithDefault(@Nullable Collection<String> column) {
        if (CollectionUtils.isNotEmpty(column)) {
            addWithColumn(column.toArray(new String[0]));
        } else {
            addWithColumn(DtpRwdObjectDTO.Fields.class);
        }
        return this;
    }

    /**
     * 添加仅查询信息点
     *
     * @param column 字段
     * @return 当前实例
     * @since 5.241201.0
     */
    public DtpRwdObjectQueryParam addWithColumnWithDefault(
            @NonNull Class<?> defaultFieldsClass,
            @Nullable String... column) {
        if (ArrayUtils.isNotEmpty(column)) {
            addWithColumn(column);
        } else {
            addWithColumn(defaultFieldsClass);
        }
        return this;
    }

    /**
     * 添加ID和关联信息点
     *
     * @return 当前实例
     * @since 7.250301.0
     */
    public DtpRwdObjectQueryParam addWithColumnWithIdRelation() {
        if (CollectionUtils.isNotEmpty(this.withColumns)) {
            if (!this.withColumns.contains(DtpRwdObjectDTO.Fields.id)) {
                addWithColumn(DtpRwdObjectDTO.Fields.id);
            }
            if (containsRelationParam() && !this.withColumns.contains(DtpRwdObjectDTO.Fields.relObjIds)) {
                addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
            }
        }
        return this;
    }

    /**
     * 添加仅查询信息点
     *
     * @param column 必要字段
     * @return 当前实例
     */
    public DtpRwdObjectQueryParam addWithColumnWithSimple(@Nullable String... column) {
        addWithColumn(DtpRwdObjectDTO.listSimpleWithColumns());
        addWithColumn(column);
        return this;
    }

    /**
     * 添加仅查询信息点
     *
     * @param cls 类
     * @return 当前实例
     * @since 5.240701.0
     */
    public DtpRwdObjectQueryParam addWithColumn(@NonNull Class<?> cls) {
        Set<String> fieldNames = Stream.concat(FieldUtils.getAllFieldsList(cls).stream(),
                        FieldUtils.getAllFieldsList(DtpRwdObjectDTO.Fields.class).stream())
                .map(item -> {
                    try {
                        return String.valueOf(FieldUtils.readStaticField(item));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(fieldNames)) {
            Collection<String> withColumns = Optional.ofNullable(this.withColumns).orElseGet(HashSet::new);
            withColumns.addAll(fieldNames);
            this.withColumns = withColumns;
        }
        return this;
    }

    /**
     * 添加仅查询信息点
     *
     * @param cls    类
     * @param column 字段
     * @return 当前实例
     * @since 5.240701.0
     */
    public DtpRwdObjectQueryParam addWithColumn(@NonNull Class<?> cls, @Nullable String... column) {
        if (ArrayUtils.isNotEmpty(column)) {
            addWithColumn(column);
        } else {
            addWithColumn(cls);
        }
        return this;
    }

    // -------------------------------- CustomInfoCode --------------------------------

    /**
     * 过滤监控状态
     *
     * @return 当前实例
     * @since 5.241102.0
     */
    public DtpRwdObjectQueryParam filterMonitoringSurveillanceStatus() {
        this.addInfoCode(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(), Lists.newArrayList("2", "3"));
        return this;
    }

    /**
     * 添加信息点筛选
     *
     * @param key   键
     * @param value 值
     * @return 当前实例
     * @since v5.8.0
     */
    @JsonAnySetter
    public DtpRwdObjectQueryParam addInfoCode(@NonNull String key, @NonNull Object value) {
        return addInfoCode(key, Sets.newHashSet(value));
    }

    /**
     * 添加信息点筛选
     *
     * @param key   键
     * @param value 值
     * @return 当前实例
     * @since v6.0.0
     */
    public DtpRwdObjectQueryParam addInfoCode(@NonNull String key, @NonNull Collection<?> value) {
        Map<String, Object> infoCodes = Optional.ofNullable(this.infoCodes).orElseGet(HashMap::new);
        Object infoCodeValue = infoCodes.get(key);
        Object tempValue;
        if (ObjectUtils.isEmpty(infoCodeValue)) {
            if (value.size() == 1) {
                tempValue = IterableUtils.first(value);
            } else {
                tempValue = new LinkedHashSet<>(value);
            }
        } else {
            tempValue = new LinkedHashSet<>(value);
        }
        infoCodes.put(key, tempValue);
        this.infoCodes = infoCodes;
        return this;
    }

    /**
     * 添加信息点筛选
     *
     * @param key   键
     * @param value 值
     * @return 当前实例
     * @since 7.250801.0
     */
    public DtpRwdObjectQueryParam addInfoCode(
            @NonNull InfoCodeEnum key,
            @NonNull Object value) {
        return this.addInfoCode(key.value(), value);
    }

    /**
     * 添加信息点筛选
     *
     * @param key   键
     * @param value 值
     * @return 当前实例
     * @since 7.250801.0
     */
    public DtpRwdObjectQueryParam addInfoCode(
            @NonNull InfoCodeEnum key,
            @NonNull Collection<?> value) {
        return this.addInfoCode(key.value(), value);
    }

    /**
     * 添加信息点筛选
     *
     * @param key      键
     * @param innerKey 内部键
     * @param value    值
     * @return 当前实例
     * @since 7.250801.0
     */
    public DtpRwdObjectQueryParam addInfoCode(
            @NonNull InfoCodeEnum key,
            @NonNull DtpRwdCriteriaSignEnum innerKey,
            @Nullable Object value) {
        return this.addInfoCode(key.value(), innerKey, value);
    }

    /**
     * 添加信息点筛选
     *
     * @param key      键
     * @param innerKey 内部键
     * @param value    值
     * @return 当前实例
     * @since v6.0.0
     */
    public DtpRwdObjectQueryParam addInfoCode(
            @NonNull String key, @NonNull DtpRwdCriteriaSignEnum innerKey, Object value) {
        Map<String, Object> tempValue = analyzeInfoCodeMap(key);
        tempValue.put(innerKey.value(), value);
        this.infoCodes.put(key, tempValue);
        return this;
    }

    /**
     * 添加信息点筛选
     *
     * @param sign    标识符
     * @param filters 筛选条件
     * @return 当前实例
     * @since 7.250601.0
     */
    public DtpRwdObjectQueryParam addInfoCode(
            @NonNull DtpRwdCriteriaSignEnum sign,
            @NonNull List<InfoCodeStaticFilter> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return this;
        }
        Map<String, Object> tempValue = analyzeInfoCodeMap(sign.getValue());
        for (InfoCodeStaticFilter filter : filters) {
            String infoCode = filter.getInfoCode();
            DtpRwdCriteriaSignEnum childSign = filter.getSign();
            Object dataValue = filter.getDataValue();
            tempValue.put(infoCode, Objects.nonNull(childSign)
                    ? new JSONObject().fluentPut(childSign.getValue(), dataValue)
                    : dataValue);
        }
        this.infoCodes.put(sign.getValue(), tempValue);
        return this;
    }

    /**
     * 添加信息点筛选
     *
     * @param filters 信息点筛选条件
     * @return 当前实例
     * @since 7.250601.0
     */
    public DtpRwdObjectQueryParam addInfoCode(@NonNull List<InfoCodeStaticFilter> filters) {
        for (InfoCodeStaticFilter filter : filters) {
            String infoCode = filter.getInfoCode();
            DtpRwdCriteriaSignEnum sign = filter.getSign();
            Object dataValue = filter.getDataValue();
            if (Objects.isNull(sign)) {
                this.addInfoCode(infoCode, dataValue);
            } else {
                this.addInfoCode(infoCode, sign, dataValue);
            }
        }
        return this;
    }

    /**
     * 添加信息点筛选
     *
     * @param signAndFilters 标识符-信息点筛选条件
     * @return 当前实例
     * @since 7.250601.0
     */
    public DtpRwdObjectQueryParam addInfoCode(
            @NonNull Map<DtpRwdCriteriaSignEnum, List<InfoCodeStaticFilter>> signAndFilters) {
        for (Map.Entry<DtpRwdCriteriaSignEnum, List<InfoCodeStaticFilter>> entry : signAndFilters.entrySet()) {
            DtpRwdCriteriaSignEnum sign = entry.getKey();
            List<InfoCodeStaticFilter> filters = entry.getValue();
            this.addInfoCode(sign, filters);
        }
        return this;
    }

    // -------------------------------- Functions --------------------------------

    /**
     * 是否为无效参数
     *
     * @return 是否为无效参数
     * @since 7.250501.0
     */
    @JsonIgnore
    public boolean invalid() {
        if (Objects.nonNull(getRelationFrom()) && Objects.isNull(getRelationFrom().getObjTo())) {
            return true;
        }
        if (Objects.nonNull(getRelationTo()) && Objects.isNull(getRelationTo().getObjFrom())) {
            return true;
        }
        if (MapUtils.isNotEmpty(this.infoCodes)) {
            for (Map.Entry<String, Object> entry : this.infoCodes.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    if (((Map<?, ?>) value).values().stream().anyMatch(Objects::isNull)) {
                        return true;
                    }
                }
            }
        }
        return CollectionUtils.isEmpty(getId())
                && CollectionUtils.isEmpty(getName())
                && CollectionUtils.isEmpty(getLocalId())
                && CollectionUtils.isEmpty(getLocalName())
                && CollectionUtils.isEmpty(getObjType())
                && CollectionUtils.isEmpty(getClassCode())
                && MapUtils.isEmpty(getInfoCodes())
                && Objects.isNull(getRelationFrom())
                && Objects.isNull(getRelationTo());
    }

    /**
     * 是否包含关联参数
     *
     * @return 是否包含关联参数
     * @since 7.250201.0
     */
    public boolean containsRelationParam() {
        return ObjectUtils.anyNotNull(this.relationFrom, this.relationTo);
    }

    /**
     * 替换占位符
     *
     * @param source 数据源
     * @return 当前实例
     * @since 5.240801.0
     */
    public @Nullable DtpRwdObjectQueryParam replacePlaceholder(@Nullable Map<String, ?> source) {
        if (MapUtils.isEmpty(source)) {
            return this;
        }
        try {
            ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
            String queryParam = placeholderString();

            StandardEvaluationContext context = SpelUtils.getDefaultStandardEvaluationContext(source);
            context.addPropertyAccessor(new MapAccessor());

            // 匹配占位符并通过表达式替换占位符
            StringBuffer finalQueryParam = new StringBuffer();
            Matcher matcher = SPEL_PATTERN.matcher(queryParam);
            while (matcher.find()) {
                String expressionString = matcher.group(1);
                Expression expression = SpelUtils.getSpelExpressionParser().parseExpression(expressionString);
                Object value = expression.getValue(context);
                if (Objects.isNull(value)) {
                    return null;
                }
                matcher.appendReplacement(finalQueryParam, objectMapper.writeValueAsString(value));
            }
            matcher.appendTail(finalQueryParam);
            queryParam = finalQueryParam.toString();
            DtpRwdObjectQueryParam result = objectMapper.readValue(queryParam, DtpRwdObjectQueryParam.class);
            if (result.invalid()) {
                LogExecutor.project().warn(LOGGER, LOG_TAG, "物理世界参数无效", Pair.of("queryParam", queryParam));
                return null;
            }
            return result;
        } catch (JsonProcessingException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "取值逻辑解析失败", e, Pair.of("object", this));
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_5b07d7a821d74a2aa3594e79055b4d15"));
        }
    }

    /**
     * 解析为占位符字符串, 去掉数组占位符左右的数组符号
     *
     * @return 占位符字符串
     * @since 7.250301.0
     */
    @SneakyThrows
    public String placeholderString() {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        String queryParam = objectMapper.writeValueAsString(this);
        StringBuffer finalQueryParma = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(queryParam);
        while (matcher.find()) {
            String placeholder = "\\" + matcher.group(1);
            matcher.appendReplacement(finalQueryParma, placeholder);
        }
        matcher.appendTail(finalQueryParma);
        return finalQueryParma.toString();
    }

    /**
     * 占位符Key
     *
     * @param key Key
     * @return 占位符Key
     * @since 7.250301.0
     */
    public static String placeholderKey(@NonNull String key) {
        return StringSubstitutor.DEFAULT_VAR_START + key + StringSubstitutor.DEFAULT_VAR_END;
    }

    /**
     * 构建反向关联查询参数，用于递归查询时自动探索反方向关系。
     * <ul>
     *     <li>如果配置了 relationTo + objFrom，则自动构建 relationFrom + objTo 的反向查询</li>
     *     <li>如果配置了 relationFrom + objTo，则自动构建 relationTo + objFrom 的反向查询</li>
     *     <li>如果同时存在 relationTo 和 relationFrom，说明已经覆盖了双向，无需反向</li>
     * </ul>
     *
     * @param queryParam 原始查询参数（可含未解析的占位符）
     * @return 反向查询参数模板，如果无法构建反向则返回 null
     * @since 7.260519.0
     */
    public static @Nullable DtpRwdObjectQueryParam buildReverseRelationQueryParam(
            @NonNull DtpRwdObjectQueryParam queryParam) {
        // 如果同时存在 relationTo 和 relationFrom，说明已经覆盖了双向，无需反向
        if (Objects.nonNull(queryParam.getRelationTo()) && Objects.nonNull(queryParam.getRelationFrom())) {
            return null;
        }
        DtpRwdObjectRelationQueryParam originalRelation;
        boolean swapToFrom;
        if (Objects.nonNull(queryParam.getRelationTo())) {
            originalRelation = queryParam.getRelationTo();
            swapToFrom = true;
        } else if (Objects.nonNull(queryParam.getRelationFrom())) {
            originalRelation = queryParam.getRelationFrom();
            swapToFrom = false;
        } else {
            return null;
        }
        // 构建反向关联参数：交换 objFrom ↔ objTo
        DtpRwdObjectRelationQueryParam reverseRelation = new DtpRwdObjectRelationQueryParam(
                originalRelation.getGraphCode(), originalRelation.getRelCode());
        if (Objects.nonNull(originalRelation.getObjFrom())) {
            reverseRelation.setObjTo(originalRelation.getObjFrom());
        }
        if (Objects.nonNull(originalRelation.getObjTo())) {
            reverseRelation.setObjFrom(originalRelation.getObjTo());
        }
        // 构建反向查询参数（保留原始的非关联字段和占位符）
        DtpRwdObjectQueryParam reverseParam = new DtpRwdObjectQueryParam();
        reverseParam.setProjectId(queryParam.getProjectId());
        reverseParam.setId(queryParam.getId());
        reverseParam.setName(queryParam.getName());
        reverseParam.setLocalId(queryParam.getLocalId());
        reverseParam.setLocalName(queryParam.getLocalName());
        reverseParam.setObjType(queryParam.getObjType());
        reverseParam.setClassCode(queryParam.getClassCode());
        reverseParam.setGroupCode(queryParam.getGroupCode());
        reverseParam.setValid(queryParam.getValid());
        reverseParam.setWithColumns(queryParam.getWithColumns());
        if (MapUtils.isNotEmpty(queryParam.getInfoCodes())) {
            reverseParam.setInfoCodes(new HashMap<>(queryParam.getInfoCodes()));
        }
        if (swapToFrom) {
            reverseParam.setRelationFrom(reverseRelation);
        } else {
            reverseParam.setRelationTo(reverseRelation);
        }
        return reverseParam;
    }

    // -------------------------------- Get/Set --------------------------------

    /**
     * 设置项目ID
     *
     * @param projectId 项目ID
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setProjectId(String projectId) {
        if (StringUtils.isNotBlank(projectId)) {
            this.projectId = Lists.newArrayList(projectId);
        }
        return this;
    }

    /**
     * 设置项目ID
     *
     * @param projectId 项目ID
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setProjectId(Collection<String> projectId) {
        if (CollectionUtils.isNotEmpty(projectId)) {
            this.projectId = projectId;
        }
        return this;
    }

    /**
     * 设置项目ID
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.projectId)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setProjectId(Object object) {
        if (object instanceof Collection) {
            return setProjectId((Collection<String>) object);
        } else if (object instanceof String) {
            return setProjectId((String) object);
        } else {
            return addInfoCode(Fields.projectId, object);
        }
    }

    /**
     * 设置对象ID
     *
     * @param id 对象ID
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setId(String id) {
        if (StringUtils.isNotBlank(id)) {
            this.id = Lists.newArrayList(id);
        }
        return this;
    }

    /**
     * 设置对象ID
     *
     * @param id 对象ID
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setId(Collection<String> id) {
        if (CollectionUtils.isNotEmpty(id)) {
            this.id = id;
        }
        return this;
    }

    /**
     * 设置对象ID
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.id)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setId(Object object) {
        if (object instanceof Collection) {
            return setId((Collection<String>) object);
        } else if (object instanceof String) {
            return setId((String) object);
        } else {
            return addInfoCode(Fields.id, object);
        }
    }

    /**
     * 设置对象名称
     *
     * @param name 对象名称
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setName(String name) {
        if (StringUtils.isNotBlank(name)) {
            this.name = Lists.newArrayList(name);
        }
        return this;
    }

    /**
     * 设置对象名称
     *
     * @param name 对象名称
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setName(Collection<String> name) {
        if (CollectionUtils.isNotEmpty(name)) {
            this.name = name;
        }
        return this;
    }

    /**
     * 设置对象名称
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.name)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setName(Object object) {
        if (object instanceof Collection) {
            return setName((Collection<String>) object);
        } else if (object instanceof String) {
            return setName((String) object);
        } else {
            return addInfoCode(Fields.name, object);
        }
    }

    /**
     * 设置对象本地ID
     *
     * @param localId 对象本地ID
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setLocalId(String localId) {
        if (StringUtils.isNotBlank(localId)) {
            this.localId = Lists.newArrayList(localId);
        }
        return this;
    }

    /**
     * 设置对象本地ID
     *
     * @param localId 对象本地ID
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setLocalId(Collection<String> localId) {
        if (CollectionUtils.isNotEmpty(localId)) {
            this.localId = localId;
        }
        return this;
    }

    /**
     * 设置对象本地ID
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.localId)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setLocalId(Object object) {
        if (object instanceof Collection) {
            return setLocalId((Collection<String>) object);
        } else if (object instanceof String) {
            return setLocalId((String) object);
        } else {
            return addInfoCode(Fields.localId, object);
        }
    }

    /**
     * 设置对象本地名称
     *
     * @param localName 对象本地名称
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setLocalName(String localName) {
        if (StringUtils.isNotBlank(localName)) {
            this.localName = Lists.newArrayList(localName);
        }
        return this;
    }

    /**
     * 设置对象本地名称
     *
     * @param localName 对象本地名称
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setLocalName(Collection<String> localName) {
        if (CollectionUtils.isNotEmpty(localName)) {
            this.localName = localName;
        }
        return this;
    }

    /**
     * 设置对象本地名称
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.localName)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setLocalName(Object object) {
        if (object instanceof Collection) {
            return setLocalName((Collection<String>) object);
        } else if (object instanceof String) {
            return setLocalName((String) object);
        } else {
            return addInfoCode(Fields.localName, object);
        }
    }

    /**
     * 设置对象类型
     *
     * @param objType 对象类型
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setObjType(String objType) {
        if (StringUtils.isNotBlank(objType)) {
            this.objType = Lists.newArrayList(objType);
        }
        return this;
    }

    /**
     * 设置对象类型
     *
     * @param objType 对象类型
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setObjType(Collection<String> objType) {
        if (CollectionUtils.isNotEmpty(objType)) {
            this.objType = objType;
        }
        return this;
    }

    /**
     * 设置对象类型
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.objType)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setObjType(Object object) {
        if (object instanceof Collection) {
            return setObjType((Collection<String>) object);
        } else if (object instanceof String) {
            return setObjType((String) object);
        } else {
            return addInfoCode(Fields.objType, object);
        }
    }

    /**
     * 设置对象编码
     *
     * @param classCode 对象编码
     * @return 当前实例
     * @since v5.1.0
     */
    @JsonIgnore
    public DtpRwdObjectQueryParam setClassCodeEnum(DtpRwdClassCodeEnum classCode) {
        return setClassCode(classCode.value());
    }

    /**
     * 设置对象编码
     *
     * @param classCode 对象编码
     * @return 当前实例
     * @since v5.1.0
     */
    @JsonIgnore
    public DtpRwdObjectQueryParam setClassCodeEnum(Collection<DtpRwdClassCodeEnum> classCode) {
        return setClassCode(classCode.stream().map(DtpRwdClassCodeEnum::value).collect(Collectors.toSet()));
    }

    /**
     * 设置对象编码
     *
     * @param classCode 对象编码
     * @return 当前实例
     * @since 5.240602.0
     */
    public DtpRwdObjectQueryParam setClassCode(String classCode) {
        if (Objects.nonNull(classCode)) {
            this.classCode = Lists.newArrayList(classCode);
        }
        return this;
    }

    /**
     * 设置对象编码
     *
     * @param classCode 对象编码
     * @return 当前实例
     * @since 5.240602.0
     */
    public DtpRwdObjectQueryParam setClassCode(Collection<String> classCode) {
        if (CollectionUtils.isNotEmpty(classCode)) {
            this.classCode = classCode;
        }
        return this;
    }

    /**
     * 设置对象编码
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.classCode)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setClassCode(Object object) {
        if (object instanceof Collection) {
            return setClassCode((Collection<String>) object);
        } else if (object instanceof String) {
            return setClassCode((String) object);
        } else if (object instanceof DtpRwdClassCodeEnum) {
            return setClassCodeEnum((DtpRwdClassCodeEnum) object);
        } else {
            return addInfoCode(Fields.classCode, object);
        }
    }

    /**
     * 设置集团编码
     *
     * @param groupCode 集团编码
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setGroupCode(String groupCode) {
        if (StringUtils.isNotBlank(groupCode)) {
            this.groupCode = Lists.newArrayList(groupCode);
        }
        return this;
    }

    /**
     * 设置集团编码
     *
     * @param groupCode 集团编码
     * @return 当前实例
     * @since v5.1.0
     */
    public DtpRwdObjectQueryParam setGroupCode(Collection<String> groupCode) {
        if (CollectionUtils.isNotEmpty(groupCode)) {
            this.groupCode = groupCode;
        }
        return this;
    }

    /**
     * 设置集团编码
     *
     * @param object 集团编码
     * @return 当前实例
     * @since 5.240801.0
     */
    @JsonSetter(Fields.groupCode)
    @SuppressWarnings("unchecked")
    public DtpRwdObjectQueryParam setGroupCode(Object object) {
        if (object instanceof Collection) {
            return setGroupCode((Collection<String>) object);
        } else if (object instanceof String) {
            return setGroupCode((String) object);
        } else {
            return addInfoCode(Fields.groupCode, object);
        }
    }

    /**
     * 获取信息点过滤
     *
     * @return 信息点过滤
     * @since 7.250601.0
     */
    @JsonAnyGetter
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInfoCodes() {
        if (MapUtils.isEmpty(this.infoCodes)) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>(this.infoCodes.size());
        this.infoCodes.forEach((k, v) -> {
            if (Objects.isNull(k) || Objects.isNull(v)) {
                return;
            }
            if (v instanceof Collection) {
                List<Object> tempV = new ArrayList<>((Collection<Object>) v);
                result.put(k, tempV.size() == 1 ? tempV.get(0) : v);
                return;
            }
            result.put(k, v);
        });
        return result;
    }

    /**
     * 解析信息点Map
     *
     * @param key 键
     * @return Map
     * @since 7.250601.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> analyzeInfoCodeMap(@NonNull String key) {
        Map<String, Object> infoCodes = Optional.ofNullable(this.infoCodes).orElseGet(LinkedHashMap::new);
        Object infoCodeValue = infoCodes.get(key);
        Map<String, Object> tempValue;
        if (Objects.isNull(infoCodeValue)) {
            tempValue = new LinkedHashMap<>(16);
        } else if (infoCodeValue instanceof Map) {
            tempValue = (Map<String, Object>) infoCodeValue;
        } else if (infoCodeValue instanceof Collection) {
            tempValue = new LinkedHashMap<>();
            tempValue.put(DtpRwdCriteriaSignEnum.EQUALS_IN.getValue(), infoCodeValue);
        } else {
            tempValue = new LinkedHashMap<>();
            tempValue.put(DtpRwdCriteriaSignEnum.EQUALS_IN.getValue(), Lists.newArrayList(infoCodeValue));
        }
        this.infoCodes = infoCodes;
        return tempValue;
    }
}
