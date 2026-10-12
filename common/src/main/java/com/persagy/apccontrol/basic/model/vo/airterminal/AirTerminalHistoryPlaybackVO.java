package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 末端历史回放
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/9/18 14:39
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AirTerminalHistoryPlaybackVO {

    @Schema(description = "调度空间详情")
    private List<SpaceDetail> spaceDetail;

    @Schema(description = "调度空间统计")
    private TreeMap<String, List<SpaceStatistics>> spaceStatistics;

    @Schema(description = "价值选择方案")
    private ValueSelection valueSelection;

    @Getter
    @Setter
    @ToString
    public static class SpaceDetail {

        @Schema(description = "空间ID")
        private String id;

        @Schema(description = "空间本地编码")
        private String localId;

        @Schema(description = "空间本地名称")
        private String localName;

        @Schema(description = "温度需求标签")
        private AirTerminalDemandStatusEnum temperatureDemandStatus;

        @Schema(description = "温度需求标签名称")
        private String temperatureDemandStatusName;

        @Schema(description = "温度需求-当前调度值")
        private BigDecimal temperatureDemandValue;

        @Schema(description = "温度需求-当前实时值")
        private BigDecimal temperatureCurrentValue;

        @Schema(description = "CO2需求标签")
        private AirTerminalDemandStatusEnum carbonDioxideDemandStatus;

        @Schema(description = "CO2需求标签名称")
        private String carbonDioxideDemandStatusName;

        @Schema(description = "CO2需求-当前调度值")
        private BigDecimal carbonDioxideDemandValue;

        @Schema(description = "CO2需求-当前实时值")
        private BigDecimal carbonDioxideCurrentValue;

        @Schema(description = "实时参数集合")
        private List<InfoCodeConfigVO> relInfoCodeConfigs;

        public String getTemperatureDemandStatusName() {
            return temperatureDemandStatus == null ? null : temperatureDemandStatus.getReasonPhrase();
        }

        public String getCarbonDioxideDemandStatusName() {
            return carbonDioxideDemandStatus == null ? null : carbonDioxideDemandStatus.getReasonPhrase();
        }

    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class SpaceStatistics {

        @Schema(description = "温度/CO2需求标签")
        private AirTerminalDemandStatusEnum demandStatus;

        @Schema(description = "温度/CO2需求标签名称")
        private String demandStatusName;

        @Schema(description = "温度/CO2需求标签-总数")
        private Integer demandTotal;

        @Schema(description = "温度/CO2需求标签-当前需求数")
        private Integer demandCount;

        @Schema(description = "温度/CO2需求标签-需求占比")
        private BigDecimal demandRatio;

        public SpaceStatistics(AirTerminalDemandStatusEnum demandStatus, Integer demandTotal, Integer demandCount) {
            this.demandStatus = demandStatus;
            this.demandTotal = demandTotal == null || demandTotal == 0 ? null : demandTotal;
            this.demandCount = demandCount;
            if (demandTotal != null && demandTotal != 0 && demandCount != null) {
                BigDecimal ratio = new BigDecimal(demandCount).divide(new BigDecimal(demandTotal), 3, BigDecimal.ROUND_HALF_UP);
                this.demandRatio = ratio.multiply(new BigDecimal(100)).setScale(1);
            }
        }

        public String getDemandStatusName() {
            return demandStatus == null ? null : demandStatus.getReasonPhrase();
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueSelection {

        @Schema(description = "曲线图数据")
        private List<ValueSelectionTrendTableDto.ElecData> elecDataList = new ArrayList<>();

        @Schema(description = "满足率配置")
        private List<ValueSelectConfigDto> fillRate;

        @Schema(description = "价值选择类型")
        private BasicsConfigTypeEnum valueSelectType;

    }

}
