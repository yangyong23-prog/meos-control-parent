package com.persagy.apccontrol.basic.model.vo.duty;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.outside.SystemGraphVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningTimeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.persagy.apccontrol.basic.service.duty.strategy.ColdSourceHomePageCardDataHandler.*;

/**
 * 冷热源子卡片数据
 *
 * @author yuyao
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HotColdChildCardDataVO extends BasicsChildCardDataVO {


    @Schema(description = "卡片标签数据")
    private CardTagVO cardTag;

    @Schema(description = "需求模版名称")
    private String templateName;

    @Schema(description = "模版标签颜色")
    private String templateTag;

    @Schema(description = "对象类")
    private String classCode;

    @Schema(description = "热源系统类型")
    private JSONArray heatSoureType;

    @Schema(description = "图类型编码")
    private String graphTypeCode;

    @Schema(description = "跳转的系统图名称")
    private String sysName;

    @Schema(description = "系统可以跳转")
    private Boolean canJump;

    @Schema(description = "系统id")
    private String systemId;

    @Schema(description = "系统名称")
    private String localName;

    @Schema(description = "开启时间")
    private LocalDateTime openDateTime;

    @Schema(description = "关闭时间")
    private LocalDateTime closeDateTime;

    @Schema(description = "开关机时间")
    private List<RunningTimeVO> openCloseTimes;

    @Schema(description = "运行台数")
    private RunningStatisticsVO runningStatistics;

    @Schema(description = "冷站EER")
    private Object sysCOP;

    @Schema(description = "冷站COP")
    private Object chillersCOP;

    @Schema(description = "冷机出水温度")
    private Object primaryChillWaterMainSupplyTemp;

    @Schema(description = "冷机回水温度")
    private Object primaryChillWaterMainReturnTemp;

    @Schema(description = "冷站运行状态")
    private RunStatusEnum runStatus;

    //信息点列表
    @Schema(description = " 信息点")
    private List<InfoCodeValueVO> infoCodeMap;

    @Schema(description = "热源一次侧供水干管温度")
    private Object heatWaterMainSupplyTemp;

    @Schema(description = "热源一次侧回水干管温度")
    private Object heatWaterMainReturnTemp;

    @Schema(description = "热源一次侧供回水温差")
    private Object waterDetaTemp;

    @Schema(description = "热源二次侧供水干管温度")
    private Object heatWaterMainSupplyTemp2;

    @Schema(description = "热源二次侧供回水温差")
    private Object heatWaterMainReturnTemp2;

    @Schema(description = "热源二次侧供回水温差")
    private Object heatSecondarySideWaterDetaTemp;

    /**
     * 组装响应信息
     *
     * @param rwdSystem               系统信息
     * @param drawingsThatCanBeJumped 可跳转的系统图
     * @param infoCodeMap             信息点map
     * @return 响应的对象
     */
    public static HotColdChildCardDataVO of(DtpRwdObjectDTO rwdSystem,
                                            HashMap<String, SystemGraphVO> drawingsThatCanBeJumped,
                                            Map<String, Object> infoCodeMap) {
        HotColdChildCardDataVO result = processSystemInformationAndSystemDiagrams(rwdSystem, drawingsThatCanBeJumped);
        result.setSysCOP(infoCodeMap.getOrDefault(SYS_COP_CODE, null));
        result.setChillersCOP(infoCodeMap.getOrDefault(CHILLERS_COP_CODE, null));
        result.setPrimaryChillWaterMainSupplyTemp(infoCodeMap.getOrDefault(PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP_CODE, null));
        result.setPrimaryChillWaterMainReturnTemp(infoCodeMap.getOrDefault(PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP_CODE, null));
        return result;
    }

    /**
     * 处理系统图和对象信息
     *
     * @param rwdSystem               系统信息
     * @param drawingsThatCanBeJumped 可跳转的系统图
     * @return 响应数据结构
     */
    public static HotColdChildCardDataVO processSystemInformationAndSystemDiagrams(
            DtpRwdObjectDTO rwdSystem,
            HashMap<String, SystemGraphVO> drawingsThatCanBeJumped) {
        HotColdChildCardDataVO result = new HotColdChildCardDataVO();
        result.setClassCode(rwdSystem.getClassCode());
        result.setLocalName(rwdSystem.getLocalName());
        String systemId = rwdSystem.getId();
        result.setSystemId(systemId);
        if (drawingsThatCanBeJumped.containsKey(systemId)) {
            result.setCanJump(true);
            SystemGraphVO systemGraphVO = drawingsThatCanBeJumped.get(systemId);
            result.setSysName(systemGraphVO.getSysName());
            result.setGraphTypeCode(systemGraphVO.getGraphTypeCode());
        } else {
            result.setCanJump(false);
            result.setSysName(rwdSystem.getLocalName());
        }
        return result;
    }


}
