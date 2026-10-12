package com.persagy.apccontrol.basic.model.vo.runanalysis;

import lombok.Data;

import java.util.List;

@Data
public class EnergyCurveDataVO {


    /**
     * 室外温度数据列表
     */
    private List<DataValueVO> outdoorTempData;
    /**
     * 实际能耗数据
     */
    private List<DataValueVO> realEnergyData;
    /**
     * 冷源数据
     */
    private List<DataValueVO> coolResourceData;
    /**
     * 热源数据
     */
    private List<DataValueVO> hotResourceData;
    /**
     * 冷热源数据
     */
    private List<DataValueVO> coolAndHotResourceData;
    /**
     * 末端数据
     */
    private List<DataValueVO> terminalData;
    /**
     * 碳因子数据
     */
    private List<DataValueVO> carbonFactorData;

    /**
     * 电价数据
     */
    private List<DataValueVO> electricityPriceData;

    /**
     * 标签数据，蓄冷、蓄热类型，0-蓄冷，1-蓄热
     */
    private List<DataValueVO> tagData;

    /**
     * 室内温度数据
     */
    private List<DataValueVO> inDoorTempData;

}
