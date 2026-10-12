package com.persagy.apccontrol.basic.model.vo.runanalysis;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnergyStatisticsVO {

    private EnergyValue totalEnergy;
    private EnergyValue coolEnergy;
    private EnergyValue heatEnergy;
    private EnergyValue coolAndHeatEnergy;
    private EnergyValue airTerminalEnergy;

    @Data
    public static class EnergyValue {
        private String name;
        private String energyModelId;
        private String itemId;
        private BigDecimal dataValue;
    }


}
