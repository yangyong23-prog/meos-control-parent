package com.persagy.apccontrol.basic.model.base.domain.model;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 信息点定义模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Accessors(chain = true)
public class InfoCodeDefineModel implements ValueObject {

    /**
     * 信息点编码
     */
    @NonNull
    private String code;

    /**
     * 信息点名称
     */
    @NonNull
    private String name;

    /**
     * 信息点别名
     */
    private String aliasName;

    /**
     * 类型编码
     */
    @NonNull
    private String classCode;

    /**
     * 单位
     */
    @Nullable
    private String unit;

    /**
     * 精度
     */
    @Nullable
    private Integer scale;

    /**
     * 一级标签
     */
    @NonNull
    private String firstTag;

    /**
     * 二级标签
     */
    @Nullable
    private String secondTag;

    /**
     * 原始数据类型
     */
    @NonNull
    private String originDataType;

    /**
     * 数据类型
     */
    @NonNull
    private String dataType;

    /**
     * 数据源
     */
    private List<InfoCodeDataSourceDefineModel> dataSources = new ArrayList<>();

    /**
     * 从信息点定义DTO创建信息点定义模型
     *
     * @param define 信息点定义DTO
     * @return 信息点定义模型
     * @since 7.260201.0
     */
    public static InfoCodeDefineModel from(DtpKnowledgeInfoCodeDTO define) {
        InfoCodeDefineModel model = new InfoCodeDefineModel();
        model.setCode(define.getCode());
        model.setName(define.getName());
        model.setAliasName(define.getName());
        model.setClassCode(define.getClassCode());
        model.setUnit(define.getUnit());
        model.setScale(define.getScale());
        model.setFirstTag(define.getFirstTag());
        model.setSecondTag(define.getSecondTag());
        model.setOriginDataType(define.getOriginDataType());
        model.setDataType(define.getDataType());
        model.setDataSources(Optional.ofNullable(define.getDataSources())
                .map(dataSources -> dataSources.stream()
                        .map(dataSource -> {
                            InfoCodeDataSourceDefineModel dataSourceModel = new InfoCodeDataSourceDefineModel();
                            dataSourceModel.setCode(dataSource.getCode());
                            dataSourceModel.setName(dataSource.getName());
                            dataSourceModel.setAliasName(dataSource.getName());
                            dataSourceModel.setParentCode(dataSource.getParentCode());
                            return dataSourceModel;
                        }).collect(Collectors.toList()))
                .orElse(Collections.emptyList()));
        return model;
    }

    /**
     * 创建自定义信息点定义模型
     *
     * @param code      信息点编码
     * @param name      信息点名称
     * @param classCode 类型编码
     * @param dataType  数据类型
     * @return 自定义信息点定义模型
     * @since 5.241101.0
     */
    public static InfoCodeDefineModel ofCustom(
            @NonNull String code,
            @NonNull String name,
            @NonNull String classCode,
            @NonNull String dataType) {
        return new InfoCodeDefineModel(code, name, classCode, LocaleMessageUtil.getMessage("key_568f433594464826ace9ad965870d8e2"), dataType, dataType);
    }
}
