package com.persagy.apccontrol.basic.model.dto.rwd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.vo.DemandControlSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物理世界楼层DTO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界楼层DTO")
@FieldNameConstants
public class DtpRwdFloorDTO extends DtpRwdObjectDTO implements Comparable<DtpRwdFloorDTO> {

    /**
     * 信息点-建筑ID
     */
    public static final String INFO_CODE_BUILDING_ID = "buildingId";

    // ------------------------- 标准字段 -------------------------

    @Schema(description = "建筑ID")
    private String buildingId;

    @Schema(description = "楼层顺序")
    @JsonAlias(Fields.floorSequenceId)
    private Integer floorSequenceId;

    @Schema(description = "层高")
    private BigDecimal height;

    @Schema(description = "楼板标高")
    private BigDecimal floorElevation;

    // ------------------------- 拓展字段 -------------------------

    @Schema(description = "楼层下空间")
    @FieldNameConstants.Exclude
    private List<DtpRwdSpaceDTO> spaces;

    @Schema(description = "楼层下空间功能类型")
    @FieldNameConstants.Exclude
    private List<InfoCodeDataSourceDefineModel> roomFuncTypes;

    @Schema(description = "楼层下管控空间")
    @FieldNameConstants.Exclude
    private BigDecimal avgData;

    @Schema(description = "楼层下管控空间")
    @FieldNameConstants.Exclude
    private List<DemandControlSpaceVO> controlSpaceList;

    @Override
    public int compareTo(@NonNull DtpRwdFloorDTO o) {
        return Comparator.nullsLast(Integer::compareTo)
                .compare(getFloorSequenceId(), o.getFloorSequenceId());
    }

    /**
     * 获取建筑ID和楼层映射
     *
     * @param sources 数据源
     * @return 建筑ID -> 建筑下楼层
     * @since 7.250101.0
     */
    @JsonIgnore
    public static Map<String, List<DtpRwdFloorDTO>> mapBuildingIdAndFloors(@Nullable List<DtpRwdFloorDTO> sources) {
        return CollectionUtils.isNotEmpty(sources)
                ? sources.stream()
                .filter(item -> Objects.nonNull(item.getBuildingId()))
                .collect(Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId))
                : Maps.newLinkedHashMap();
    }

    @SuppressWarnings("unused")
    public static class Fields {
        public static final String floorSequenceId = "floorSequenceID";
    }
}
