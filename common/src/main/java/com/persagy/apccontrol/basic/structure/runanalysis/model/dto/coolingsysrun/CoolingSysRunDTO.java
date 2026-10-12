package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun;

import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.structure.control.model.SimpleInfoCodeDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.WeatherDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 冷站系统运行DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CoolingSysRunDTO extends RunAnalysisReportDataDto {

    /**
     * 天气数据
     */
    private WeatherDTO weather;

    /**
     * 效率配置
     */
    private List<ShellConfigVO> efficiencyConfigs;

    /**
     * 系统数据
     */
    private List<SystemEnergyDTO> systems;

    /**
     * 设备类编码 -> 信息点定义
     */
    private Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines;

    /**
     * 设备类编码 -> 控制台信息点
     */
    private Map<String, List<SimpleInfoCodeDTO>> scheduleInfoCodeDefines;

}
