package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/23 15:35
　　 * @description: 能耗-分项信息列表
*/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EnergyItemInfoListResponse extends BaseResponse {

    /**
     * 分项编号
     */
    private String itemId;

    /**
     * 分项名称
     */
    private String itemName;

    /**
     * 是否是顶级分项节点
     */
    private Boolean isTotal;


}
