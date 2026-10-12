package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 信息点RWD对象取值逻辑模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfoCodeTakeValueLogicRwdObjectModel extends BaseInfoCodeTackValueLogicModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoCodeTakeValueLogicRwdObjectModel.class);
    private static final String LOG_TAG = "信息点配置-取值逻辑-RWD对象";

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 取值表达式。
     * <p>
     * 用于从 RWD 查询结果对象中提取目标字段值（如 {@code "id"}、{@code "localName"}）。
     * <br>
     * 仅在配置了 {@code queryParam} 时生效；当 {@code queryParam} 为 {@code null}
     * （即纯 {@code sourceValueExpression} 模式）时可不填，框架会跳过对象解析直接执行后处理表达式。
     * </p>
     *
     * @see #validate(String)
     */
    private @Nullable String takeValueExpression;

    /**
     * 数据源后处理表达式（SpEL）。
     * <p>
     * 对查询结果汇总完成后，以当前 {@code sourceMap}（已包含前序数据源 key）为 root 执行此表达式，
     * 用执行结果替换原始 sourceValues。可通过 {@code #root['key']} 引用其他数据源。
     * <br>
     * 非 {@code null} 时生效。
     * </p>
     *
     * @since 7.260501.0
     */
    private @Nullable String sourceValueExpression;

    /**
     * 是否对该数据源执行不定深度递归查询。
     * <p>
     * 为 {@code true} 时，首轮查询完成后，将本轮新增 ID 写入 {@code sourceMap}，
     * 再以相同 {@code queryParam}（占位符替换为已累积 ID）继续查询，
     * 循环直至无新增 ID 为止（含去重保护，防止图环路死循环）。
     * <br>
     * 适用于需要遍历不定深度关系链的场景，例如沿同一图边类型递归查找所有可达对象。
     * </p>
     *
     * @since 7.260509.0
     */
    private boolean recursive = false;

    /**
     * 当查询结果为空时的默认值
     */
    private @Nullable String defaultValueIfEmpty;

    /**
     * 查询条件。
     * <p>
     * 当仅需通过 {@code sourceValueExpression} 对已有数据源做二次计算时，可不填此字段（设为 {@code null}），
     * 框架将跳过 RWD 查询，直接执行后处理表达式。
     * </p>
     */
    private @Nullable DtpRwdObjectQueryParam queryParam;

    /**
     * 校验配置合法性。
     * <p>
     * 规则一：{@code queryParam} 为 {@code null} 时，{@code sourceValueExpression} 不能为空
     * （纯后处理模式必须提供计算表达式）。
     * <br>
     * 规则二：{@code queryParam} 不为 {@code null} 时，{@code takeValueExpression} 不能为空
     * （RWD 查询模式必须提供字段提取表达式）。
     * </p>
     *
     * @param key 当前 source key，仅用于报错时定位
     * @throws IllegalArgumentException 配置非法时抛出
     * @since 7.260509.0
     */
    public void validate(@NonNull String key) {
        if (Objects.isNull(this.queryParam) && StringUtils.isBlank(this.sourceValueExpression)) {
            throw new IllegalArgumentException(
                    "[" + LOG_TAG + "] source key='" + key +
                            "' 的 queryParam 为 null 时，sourceValueExpression 不能为空");
        }
        if (Objects.nonNull(this.queryParam) && StringUtils.isBlank(this.takeValueExpression)) {
            throw new IllegalArgumentException(
                    "[" + LOG_TAG + "] source key='" + key +
                            "' 配置了 queryParam 时，takeValueExpression 不能为空");
        }
    }

    /**
     * 转换默认值为JSON对象
     *
     * @return JsonNode
     * @since 7.250501.0
     */
    public Object toDefaultValueIfEmptyObject() {
        try {
            ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
            JsonNode jsonNode = objectMapper.readTree(defaultValueIfEmpty);
            if (jsonNode.isArray()) {
                return objectMapper.convertValue(jsonNode, List.class);
            }
            if (jsonNode.isObject()) {
                return objectMapper.convertValue(jsonNode, Map.class);
            }
            return jsonNode;
        } catch (JsonProcessingException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "取值逻辑默认值格式错误", e,
                    Pair.of("defaultValueIfEmpty", defaultValueIfEmpty));
            throw new RuntimeException(e);
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-取值逻辑-RWD对象";
    }
}
