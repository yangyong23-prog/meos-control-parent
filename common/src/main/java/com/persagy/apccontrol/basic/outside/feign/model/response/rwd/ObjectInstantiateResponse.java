package com.persagy.apccontrol.basic.outside.feign.model.response.rwd;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 物理世界对象规则实例化响应
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ObjectInstantiateResponse extends BaseResponse {

    /**
     * ID
     */
    private String id;

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 对象类编码
     */
    private String classCode;

    /**
     * 对象实例ID
     */
    private String objectId;

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 原始数据类型
     */
    private String origDataType;

    /**
     * 规则类型
     *
     * <p>[1: 持久化计算, 2: 孪生计算], 默认为 [1]
     */
    private Integer ruleType;

    /**
     * 采集类型
     *
     * <p>[1: 数值, 2: 文本], 默认为 [1]
     */
    private Integer collectType;

    /**
     * 采集周期
     *
     * <p>单位: 分
     */
    private Integer period;

    /**
     * 统计周期
     *
     * <p>单位: 分
     */
    private Integer statPeriod;

    /**
     * 计算类型
     *
     * <p>[1: 直采, 2: 衍生(继承/计算)], 默认为 [1]
     */
    private Integer calculateType;

    /**
     * 衍生计算依赖的参数信息
     */
    private JSONArray deriveInfos;

    /**
     * 规则内容
     *
     * <ul>
     *     <li><b>直采类型</b> - [leftnear: 临近值, linearinterpolation: 线性插值, enumvalue: 开关值, neardiffcal: 临近差分值, lineardiffcal: 线性插值差分值]</li>
     *     <li><b>衍生继承</b> - [inherit: 间接继承, inheritdiffcal: 差分继承]</li>
     *     <li><b>衍生计算</b> - 内容填写具体的计算公式,
     *     比如 {@code local function fun() local _result = _A_*_B_ return _result end return tostring(fun());},
     *     其中变化的是 {@code  _A_*_B_，_A_，_B_} 分表是参与计算的参数命名</li>
     * </ul>
     */
    private String content;

    /**
     * 备注
     */
    private String remark;

}
