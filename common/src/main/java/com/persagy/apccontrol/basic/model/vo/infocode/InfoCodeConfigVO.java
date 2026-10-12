package com.persagy.apccontrol.basic.model.vo.infocode;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/6/28 18:30
 * @Version 1.0
 */
@Data
public class InfoCodeConfigVO {
    /**
     * 拓展值
     */
    private Map<String, Object> additionalMapValue;

    private List<InfoCodeDataSourceDefineModel> dataSource;

    /**
     * 信息点时间
     */
    private LocalDateTime dataTime;

    /**
     * 信息点值
     */
    private Object dataValue;

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 信息点名称
     */
    private String infoCodeName;

    /**
     * 信息点别名
     */
    private String aliasName;

    /**
     * 信息点类型
     */
    private InfoCodeTypeEnum infoCodeType;

    /**
     * 是否存在IOT规则, 仅为IOT信息点类型时存在
     */
    private Boolean existsIotRule;

    /**
     * 展示类型
     */
    private String showStyle;

    /**
     * 关联信息点配置
     *
     * @deprecated 5.241002.0版本开始使用 {@link #relInfoCodeConfigs} 字段
     */
    @Deprecated
    private InfoCodeConfigVO relInfoCodeConfig;

    /**
     * 关联信息点配置
     */
    private List<InfoCodeConfigVO> relInfoCodeConfigs;

    /**
     * 关联模版类型ID
     */
    private String relTemplateTypeId;

    /**
     * 精度
     */
    private Integer scale;

    /**
     * 信息点顺序
     */
    private Integer sequence;

    /**
     * 单位
     */
    private String unit;

    /**
     * 原始数据类型
     */
    private String originDataType;

    /**
     * 仅保留信息点
     *
     * @since 7.250802.0
     */
    @JsonIgnore
    public void onlyRetainInfoCode() {
        this.dataValue = null;
        this.dataTime = null;
        this.additionalMapValue = null;
        this.existsIotRule = null;
    }

    /**
     * 转换关联信息点键
     *
     * @return 关联信息点键
     * @since 7.250403.0
     */
    @JsonIgnore
    public @NonNull List<@NonNull MultiKey<@NonNull String>> toRelInfoCodeKeys() {
        return fillRelInfoCodeKeys(new ArrayList<>(), this);
    }
    /**
     * 创建当前实例
     *
     * @param aggregate     聚合根
     * @param allAggregates 所有聚合根
     * @return 当前实例
     * @since 5.240602.0
     */
    public static @NonNull InfoCodeConfigVO of(
            @NonNull InfoCodeAggregate aggregate,
            @Nullable List<@NonNull InfoCodeAggregate> allAggregates) {
        List<InfoCodeConfigVO> relInfoCodeConfigs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allAggregates)) {
            List<InfoCodeAggregate> assembleAggregates = aggregate.analyzeAssembleAggregate(allAggregates);
            if (CollectionUtils.isNotEmpty(assembleAggregates)) {
                for (InfoCodeAggregate assembleAggregate : assembleAggregates) {
                    relInfoCodeConfigs.add(of(assembleAggregate, allAggregates));
                }
            }
        }
        InfoCodeConfigVO configVo = SpringUtil.getBean(InfoCodeConverter.class).toConfigVo(aggregate);
        if (CollectionUtils.isNotEmpty(relInfoCodeConfigs)) {
            configVo.setRelInfoCodeConfigs(relInfoCodeConfigs);
            if (LocalContextStorage.isDeprecatedVersion()) {
                configVo.setRelInfoCodeConfig(IterableUtils.first(relInfoCodeConfigs));
            }
        }
        return configVo;
    }

    /**
     * 填充关联信息点键
     *
     * @param target         目标源
     * @param infoCodeConfig 信息点配置, 为空时则为当前对象
     * @return 关联信息点键
     * @since 7.250403.0
     */
    @JsonIgnore
    private @NonNull List<@NonNull MultiKey<@NonNull String>> fillRelInfoCodeKeys(
            @NonNull List<@NonNull MultiKey<@NonNull String>> target,
            @NonNull InfoCodeConfigVO infoCodeConfig) {
        List<InfoCodeConfigVO> relInfoCodeConfigs = infoCodeConfig.getRelInfoCodeConfigs();
        if (CollectionUtils.isEmpty(relInfoCodeConfigs)) {
            return target;
        }
        for (InfoCodeConfigVO relInfoCodeConfig : relInfoCodeConfigs) {
            target.add(new MultiKey<>(relInfoCodeConfig.getInfoCodeType().value(), relInfoCodeConfig.getInfoCode()));
            if (CollectionUtils.isNotEmpty(relInfoCodeConfig.getRelInfoCodeConfigs())) {
                fillRelInfoCodeKeys(target, relInfoCodeConfig);
            }
        }
        return target;
    }

}
