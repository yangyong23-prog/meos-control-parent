package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningCO2QualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

@Data
public class SpaceDetailVO {

    /**
     * 空间id
     */
    private String spaceId;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 楼层id
     */
    private String floorId;
    /**
     * 楼层名称
     */
    private String floorName;
    /**
     * 建筑id
     */
    private String buildingId;
    /**
     * 建筑名称
     */
    private String buildingName;
    /**
     * 基本信息
     */
    private BasicInfoVO basicInfo;
    /**
     * 温度数据分析
     */
    private DataAnalysisVO dataAnalysis;
    /**
     * 温度标准
     */
    private DemandConfigAirconditioningTdbSeasonValueObject temperatureStandard;

    /**
     * 二氧化碳标准
     */
    private DemandConfigAirconditioningCO2QualityValueObject co2Standard;

    @Data
    public static class BasicInfoVO {
        /**
         * 营业时间, 前闭后闭，一天可能有多段 eg. 00:00-24:00
         */
        private LinkedHashSet<String> businessTimeRangeList;
        /**
         * 空调需求时间, 前闭后闭，一天可能有多段 eg. 00:00-24:00
         */
        private LinkedHashSet<String> airConditioningDemandTimeRangeList;

        /**
         * 是否算法自动计算,当配置是算法自动计算的时候，就不获取空调需求时间
         */
        private Boolean isAlgorithmCompute;
    }

    @Data
    public static class DataAnalysisVO {
        /**
         * 温度
         */
        private List<DataValueVO> temperatureRealValue;
        /**
         * 二氧化碳
         */
        private List<DataValueVO> co2RealValue;
        /**
         * 目标二氧化碳
         */
        private List<DataValueVO> targetCo2;
        /**
         * 目标温度
         */
        private List<DataValueVO> targetTemperature;
        /**
         * 温度标准
         */
        private DemandConfigAirconditioningTdbSeasonValueObject temperatureStandard;
        /**
         * 二氧化碳标准
         */
        private DemandConfigAirconditioningCO2QualityValueObject co2Standard;

        /**
         * 单空间co2当日达标率
         */
        private BigDecimal co2ComplianceRate;
        /**
         * 单空间温度当日达标率
         */
        private BigDecimal tdbComplianceRate;
    }


    public void fillBySpaceDTO(DtpRwdSpaceDTO space) {
        this.spaceId = space.getId();
        this.spaceName = space.getLocalName();
        this.floorId = space.getFloorId();
        this.buildingId = space.getBuildingId();
    }
}

