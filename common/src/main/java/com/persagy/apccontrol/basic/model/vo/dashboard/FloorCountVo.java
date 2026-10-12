package com.persagy.apccontrol.basic.model.vo.dashboard;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 16:36
 */
@Data
@Schema(description = "Dashboard照明-楼层统计")
public class FloorCountVo {

    @Schema(description = "楼层ID")
    private String id;

    @Schema(description = "楼层名称")
    private String localName;

    @Schema(description = "需求一致性")
    private String demandConsistency;

    @Schema(description = "多开总数")
    private Integer totalMoreOpenCount;

    @Schema(description = "少开总数")
    private Integer totalMoreLessCount;

    @Schema(description = "实际开启总数")
    private Integer totalRealOpenCount;

    @Schema(description = "需求开启总数")
    private Integer totalDemandOpenCount;

    @Schema(description = "存在干预空间总数")
    private Integer totalInterventionSpaceCount;

    @Schema(description = "需求干预空间总数")
    private Integer totalDemandInterventionSpaceCount;

    @Schema(description = "存在手动空间总数")
    private Integer totalManualSpaceCount;

    @Schema(description = "楼层下对象类和对象ID映射")
    private Map<DtpRwdClassCodeEnum, Set<String>> classCodeAndObjectIds;

    @Schema(description = "需求偏离空间")
    private List<DemandDeviatedSpacesVo> demandDeviatedSpaces;

    @Schema(description = "需求偏离空间数量")
    private Integer totalDemandDeviatedSpaceCount;


}
