package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import com.persagy.apccontrol.basic.outside.vo.riskevent.SimpleRiskEventDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/23 15:35
　　 * @description: 能耗详情
*/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EnergyDetailResponse extends BaseResponse {

    /**
     * 分项编号
     */
    private String itemId;

    /**
     * 分项名称
     */
    private String itemName;

    /**
     * 实际能耗
     */
    private BigDecimal realEnergy;

    /**
     * 目标值
     */
    private BigDecimal targetData;

    /**
     * 管控值
     */
    private BigDecimal controlData;

    /**
     * 占管控值比
     */
    private BigDecimal energyRatioControl;

    /**
     * 占目标值比
     */
    private BigDecimal energyRatioTarget;

    /**
     * 能耗同比
     */
    private BigDecimal sameRatio;


    /**
     * 能耗环比
     */
    private BigDecimal ringRatio;

    /**
     * 同区域对比
     */
    private BigDecimal regionRatio;


}
