/**
 * Copyright 2024 bejson.com
 */
package com.persagy.apccontrol.basic.outside.dto.lighting;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author 薛涛
 * @Description
 * @Date 11:16 2024/5/15
 * @Param
 * @return
 **/
@Data
public class InfoCodeParameters {

    private String infoCode;
    private String infoCodeName;
    private List<?> dataSource;
    private Object infoCodeValue;
    private String updateTime;

    /**
     * 拓展值
     */
    private JSONObject additionalMapValue;

    /**
     * 从信息点配置创建当前实例
     *
     * @param config        信息点配置
     * @param infoCodeValue 信息点值
     * @return 当前实例
     * @since 7.250101.0
     */
    @Nullable
    public static InfoCodeParameters ofConfig(
            @Nullable RcInfoCodeCfg config,
            @Nullable String infoCodeValue) {
        if (Objects.isNull(config)) {
            return null;
        }
        List<DtpKnowledgeInfoCodeDataSourceDTO> configDataSource = config.getDataSource();
        InfoCodeParameters result = new InfoCodeParameters();
        result.setInfoCode(config.getInfoCode());
        result.setInfoCodeName(config.getName());
        result.setDataSource(Objects.nonNull(configDataSource)
                ? configDataSource.stream().map(item -> new InfoCodeDataSourceDefineModel(
                item.getCode(), item.getName())).collect(Collectors.toList())
                : null);
        result.setInfoCodeValue(infoCodeValue);
        return result;
    }

    /**
     * 创建空实例
     *
     * @return 空实例
     * @since 7.250101.0
     */
    public static InfoCodeParameters ofEmpty(@NonNull String infoCode, @Nullable String infoCodeName) {
        InfoCodeParameters result = new InfoCodeParameters();
        result.setInfoCode(infoCode);
        result.setInfoCodeName(infoCodeName);
        return result;
    }
}