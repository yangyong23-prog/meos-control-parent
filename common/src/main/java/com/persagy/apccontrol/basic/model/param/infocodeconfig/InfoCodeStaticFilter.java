package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 静态信息点过滤
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Data
@Accessors(chain = true)
public class InfoCodeStaticFilter {

    /**
     * 信息点编码
     */
    @NotBlank(message = "{key_0ad0f2789ec14197b646fbf159c47354}")
    private String infoCode;

    /**
     * 比较符
     */
    private DtpRwdCriteriaSignEnum sign;

    /**
     * 信息点值
     */
    @NotNull(message = "{key_cffa1a9c9d2a469bb1c14f2ca580b760}")
    private Object dataValue;

}
