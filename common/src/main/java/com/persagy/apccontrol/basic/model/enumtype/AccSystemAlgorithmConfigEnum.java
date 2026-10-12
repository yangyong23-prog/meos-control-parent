package com.persagy.apccontrol.basic.model.enumtype;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * 
 * Description: 冷热源系统对应的算法包配置
 * Company: Persagy 
 * @author hengxi 
 * @version 1.0
 * @since: 2023年12月22日: 下午4:41:36
 * Update By hengxi 2023年12月22日: 下午4:41:36
 */
@Slf4j
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AccSystemAlgorithmConfigEnum {

    ACCC_OFFCENTER_COOL("ACCC", "coolSourceType","1","离心式制冷机","chiller","cooling"),
	ACAC_OFFCENTER_COOL("ACAC", "coolSourceType", "1", "空调冷源", "chiller", "cooling"),
    ACCC_SCREW_COOL("ACCC", "coolSourceType","2","离心式制冷机","chiller","cooling"),
    ACCI_AIR_COOL("ACCI", "coolHeatSourceType","4","空气源热泵","air_source_heat_pump_unit","cooling"),
    ACCI_AIR_HEAT("ACCI", "coolHeatSourceType","4","空气源热泵","air_source_heat_pump_unit","heating"),
    ACCH_MUNICIPAL_HEAT("ACCH", "heatSoureType","8","市政热水","heat_exchanger","heating"),
	ACCH_GAS_HEAT("ACCH", "heatSoureType", "11", "燃气锅炉", "boiler", "heating"),
	ACAH("ACAH", "heatSoureType", "8", "中央供热系统", "boiler", "heating");
	
	public static Set<String> getInfoCodes(String stystemType){
		Set<String> infocodes = Sets.newHashSet();
		AccSystemAlgorithmConfigEnum[] values = AccSystemAlgorithmConfigEnum.class.getEnumConstants();
		for (AccSystemAlgorithmConfigEnum acc : values) {
			if(acc.systemType.equalsIgnoreCase(stystemType)) {
				infocodes.add(acc.getSourceTypeInfoCode());
			}
		}
		return infocodes;
	}
	
	public static List<AccSystemAlgorithmConfigEnum> getAlgorithmConfig(String stystemType,String sourceTypeInfoCode,String sourceTypeValue){
		List<AccSystemAlgorithmConfigEnum> find = Lists.newArrayList();
		AccSystemAlgorithmConfigEnum[] values = AccSystemAlgorithmConfigEnum.class.getEnumConstants();
		for (AccSystemAlgorithmConfigEnum acc : values) {
			if(acc.systemType.equalsIgnoreCase(stystemType) && 
					acc.sourceTypeInfoCode.equalsIgnoreCase(sourceTypeInfoCode) &&
					acc.sourceTypeValue.equalsIgnoreCase(sourceTypeValue)) {
				find.add(acc);
			}
		}
		return find;
	}
	
	public static String getInfoCode(String systemType){
		AccSystemAlgorithmConfigEnum[] values = AccSystemAlgorithmConfigEnum.class.getEnumConstants();
		for (AccSystemAlgorithmConfigEnum acc : values) {
			if(acc.systemType.equalsIgnoreCase(systemType)) {
				return acc.sourceTypeInfoCode;
			}
		}
		throw new RuntimeException("不存在该系统类型: " + systemType);
	} 
	
	public static List<AccSystemAlgorithmConfigEnum> getAlgorithmDefaultConfig(String systemType){
		switch (systemType) {
		case "ACCC":
			return Lists.newArrayList(ACCC_OFFCENTER_COOL);
		case "ACCI":
			return Lists.newArrayList(ACCI_AIR_COOL,ACCI_AIR_HEAT);
		case "ACCH":
			return Lists.newArrayList(ACCH_MUNICIPAL_HEAT);
			case "ACAC":
				return Lists.newArrayList(ACAC_OFFCENTER_COOL);
			case "ACAH":
				return Lists.newArrayList(ACAH);
		default:
			log.error(systemType + " systemType error");
			return Lists.newArrayList();
		}
	}


	/**
	 * 系统类型
	 */
    private final String systemType;
    /**
     * 子类型信息点
     */
    private final String sourceTypeInfoCode;
    /**
     * 子类型值
     */
    private final String sourceTypeValue;
    /**
     * 子类型值中文
     */
    private final String sourceTypeValueStr;
    /**
     * 对应的算法包
     */
    private final String heatingSource;
    /**
     * 冷热源
     */
    private final String mode;
	
	
}
