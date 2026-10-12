package com.persagy.apccontrol.basic.service.runanalysis;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.runanalysis.*;
import com.persagy.apccontrol.basic.model.vo.runanalysis.*;
import com.persagy.apccontrol.basic.outside.enums.EnergyModelTagEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.SyncHistoryStorageParam;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RunAnalysisService extends LogComponent {

    List<EnergyModelTagEnum> ENERGY_ITEM_TAG_LIST = Arrays.asList(EnergyModelTagEnum.COOL, EnergyModelTagEnum.HEAT,
            EnergyModelTagEnum.COOL_HEAT, EnergyModelTagEnum.AIR_TERMINAL);
    //冷站效率指标信息点
    List<String> COLD_INFO_CODES = Arrays.asList(DashBoardInfoCodeEnum.SYS_COP.getCode(), DashBoardInfoCodeEnum.CHILLERS_COP.getCode(),
            DashBoardInfoCodeEnum.WTF_CH.getCode(), DashBoardInfoCodeEnum.WTF_C.getCode(), DashBoardInfoCodeEnum.CT_HEAT_FACTOR.getCode(),
            DashBoardInfoCodeEnum.THERMAL_UNBALANCE_RATE.getCode());
    //冷热源效率指标信息点
    List<String> COLD_HEAT_INFO_CODES = Arrays.asList(DashBoardInfoCodeEnum.SYS_COP.getCode(), DashBoardInfoCodeEnum.CHILLERS_COP.getCode(),
            DashBoardInfoCodeEnum.WTF_CH.getCode());

    //末端分析设备类
    List<String> TERMINAL_CLASS_CODES = Arrays.asList(DtpRwdClassCodeEnum.ACATAH.getValue(), DtpRwdClassCodeEnum.ACATFU.getValue(),DtpRwdClassCodeEnum.ACATFC.getValue());
    //新风分析设备类
    List<String> FRESH_AIR_CLASS_CODES = Arrays.asList(DtpRwdClassCodeEnum.ACATAH.getValue(), DtpRwdClassCodeEnum.ACATFU.getValue());

    RunAnalysisStatisticsVO statistics(RunAnalysisBaseParam param);

    EnergyCurveDataVO listEnergyCurveData(EnergyCoverDataParam param);

    List<AnalysisDataTreeVO> getAnalysisDataTree(AnalysisDataTreeParam param);

    List<AnalysisTypeVO> listAnalysisType(LocalDate date);

    Boolean storageTagSync(SyncHistoryStorageParam param);


    /**
     * 查询空间分析数据
     *
     * @param param 查询参数
     * @return 空间分享数据
     */
    public AirConditionerSpaceAnalysisVO querySpaceAnalysisList(BasicsAnalysisParam param);
    AirConditionerSpaceAnalysisVO getSpaceAnalysisList(BasicsAnalysisParam param);

    Collection<AnalysisInfoCodeVO> listInfoCode(AnalysisInfoCodeParam param);

    List<AnalysisEquipmentVO> listEquip(AnalysisListEquipmentParam param);

    SpaceDetailVO spaceDetail(AnalysisSpaceDetailParam param);

    List<ClassCodeVO> listTerminalClassCode(RunningMonitorSecondTypeEnum secondType);

    Map<String,Integer> getSpaceTagCount(@NotNull LocalDate day, @NotNull RunningMonitorSecondTypeEnum tagType, List<String> spaceIds);

    SpaceSummaryVo getSpaceSummary(BasicsAnalysisParam param);
}
