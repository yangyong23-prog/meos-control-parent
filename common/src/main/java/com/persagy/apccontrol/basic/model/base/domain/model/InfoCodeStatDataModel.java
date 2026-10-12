package com.persagy.apccontrol.basic.model.base.domain.model;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 信息点值模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeStatDataModel{

    /**
     * 单位
     */
    @Nullable
    private String unit;

    /**
     * 信息点编码
     */
    @Nullable
    private String infoCode;

    /**
     * 信息点名称
     */
    @Nullable
    private String name;

    /**
     * 信息点值
     */
    @Nullable
    private Object dataValue;

}
