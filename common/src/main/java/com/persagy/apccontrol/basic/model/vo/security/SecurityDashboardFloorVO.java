package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.validate.utility.annotation.NumberToScaleString;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安防-Dashboard楼层VO
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@Schema(title = "安防-Dashboard楼层VO")
public class SecurityDashboardFloorVO {

    @Schema(description = "楼层ID")
    private String id;

    @Schema(description = "楼层名称")
    private String localName;

    @Schema(description = "需求一致性")
    @NumberToScaleString(scale = 10)
    private BigDecimal demandConsistency;

    @Schema(description = "需求空间总数")
    private Map<BaseEnum<?>, Integer> totalDemandSpaceCount;

    @Schema(description = "实际空间总数")
    private Map<BaseEnum<?>, Integer> totalRealSpaceCount;

    @Schema(description = "楼层下对象类和对象ID映射")
    private Map<String, Set<String>> classCodeAndObjectIds;

    @Schema(description = "需求偏离空间")
    private List<SecurityDashboardDeviatedSpaceVO> demandDeviatedSpaces;

}
