package com.persagy.apccontrol.basic.structure.bizcheck.model.dto;

import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 单价配置检查项结果 DTO
 * 包含单价配置检查的各项结果信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitPriceConfigCheckItemDTO {

    /**
     * 价值选择类型
     */
    private String valueSelectType;

    /**
     * 价值选择类型描述
     */
    private String valueSelectTypeName;

    /**
     * 是否需要执行检查（只有费用最低才需要检查）
     */
    private Boolean needCheck;

    /**
     * 期望的电价数据点数（96个15分钟精度）
     */
    private Integer expectedDataCount;

    /**
     * 实际电价数据点数
     */
    private Integer actualDataCount;

    /**
     * 检查状态
     */
    private BizCheckStatusEnum status;

    /**
     * 检查结果描述
     */
    private String resultDescription;

    /**
     * 电价数据信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElectricityPriceData {
        /**
         * 数据时间
         */
        private String dataTime;

        /**
         * 电价值
         */
        private BigDecimal priceValue;

        /**
         * 数据单位
         */
        private String unit;
    }
}