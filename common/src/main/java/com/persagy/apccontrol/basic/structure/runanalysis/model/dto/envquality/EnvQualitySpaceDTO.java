package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality;

import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 环境品质-空间温度 DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class EnvQualitySpaceDTO {

    public static final EnvQualitySpaceDTO EMPTY = new EnvQualitySpaceDTO();

    /**
     * 温度达标率
     */
    private String tdbComplianceRate;

    /**
     * 空调季类型
     */
    private MidCalendarAirSeasonTypeEnum airSeasonType;

    /**
     * 空气详情
     */
    private List<EnvQualitySpaceDetailDTO> spaceDetails;

    /**
     * 设备类编码 -> 定义
     */
    private Map<String, ClassCodeDefineModel> classCode2Define;

    /**
     * 设备类信息点定义
     */
    private Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines;

}
