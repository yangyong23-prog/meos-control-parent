package com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 查询-冰蓄冷需求计算结果
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpMeosAllControllerParameterResponse extends BaseResponse {

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 信息点名称
     */
    private String infoCodeName;

    /**
     * 设定信息点
     */
    private String setInfoCode;

    /**
     * 设定值
     */
    private String setValue;

    /**
     * 实际信息点
     */
    private String actualInfoCode;

    /**
     * 实际值
     */
    private String actualValue;

}
