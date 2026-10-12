package com.persagy.apccontrol.basic.model.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 能耗数据查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
public class EnergyDataQueryParam {

    /**
     * 能耗信息点参数数组 （必传）
     */
    @Size(min = 1, message = "{key_1db951c4b92440a5ad67c0b92856d7b7}")
    private List<EnergyDataInfoQueryParam> objects = new ArrayList<>();

    /**
     * 分精度类型 5min,15min,1h,1d,1m（必传）
     */
    private String ipType;

    /**
     * reg-整点值(默认),avg-平均值,max-最大值,min-最小值,rcov- 切变+整点值
     */
    private String valueType;

    /**
     * 开始时间点 yyyyMMddHHmmss
     */
    private String startTime;

    /**
     * 结束时间点 yyyyMMddHHmmss
     */
    private String endTime;

    /**
     * 数据单位
     */
    private String unit;

    /**
     * 数据状态标签(1-正常, 2-异常, 3-补录, 4-切变点)
     */
    private Integer dataStatus;

}
