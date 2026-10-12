package com.persagy.apccontrol.basic.model.dto.rwd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物理世界房间DTO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界空间DTO")
@FieldNameConstants
public class DtpRwdSpaceDTO extends DtpRwdObjectDTO {

    /**
     * 自定义信息点-服务系统ID
     */
    public static final String CUSTOM_INFO_CODE_SERVICE_SYSTEM_IDS = "serviceSystemIds";

    @Schema(description = "建筑ID")
    private String buildingId;

    @Schema(description = "楼层ID")
    private String floorId;

    @Nullable
    @Schema(description = "系统ID")
    private Set<String> systemIds;

    @Nullable
    @Schema(description = "空气(干球)温度")
    private String tdb;

    @Nullable
    @Schema(description = "二氧化碳浓度")
    private String co2;

    @Nullable
    @Schema(description = "相对湿度")
    private String rh;

    @Nullable
    @Schema(description = "空间功能类型")
    private String roomFuncType;

    @Schema(description = "bimId")
    private String bimId;

    @Schema(description = "bimLocation")
    private String bimLocation;

    @Schema(description = "outline")
    private String outline;

    // ------------------------- 拓展字段 -------------------------

    @Nullable
    @Schema(description = "空间功能类型枚举")
    @FieldNameConstants.Exclude
    private BaseEnumVO roomFuncTypeVO;

    /**
     * 获取建筑楼层ID和空间映射
     *
     * @param sources 数据源
     * @return [建筑ID-楼层ID] -> 建筑楼层下空间
     * @since 7.250101.0
     */
    @JsonIgnore
    public static MultiKeyMap<String, List<DtpRwdSpaceDTO>> mapBuildingFloorIdAndSpace(
            @Nullable List<DtpRwdSpaceDTO> sources) {
        return CollectionUtils.isNotEmpty(sources)
                ? sources.stream()
                .filter(item -> Objects.nonNull(item.getBuildingId()))
                .collect(Collectors.groupingBy(item -> new MultiKey<>(item.getBuildingId(), item.getFloorId()),
                        MultiKeyMap::new, Collectors.toList()))
                : new MultiKeyMap<>();
    }

    /**
     * 是否支持反馈调节
     *
     * @param circuitsBySpace 关联空间的照明回路
     * @return 是否支持反馈调节
     * @since 7.250801.0
     */
    @JsonIgnore
    public boolean allowFeedbackAdjust(@Nullable List<DtpRwdCircuitDTO> circuitsBySpace) {
        return MapUtils.isNotEmpty(this.getUndefinedProperties())
                && StringUtils.equalsAny(this.analyzeProperties(
                InfoCodeEnum.REPLACED_BY_NAT_LIGHTING.getCode()), "1", "2")
                && CollectionUtils.isNotEmpty(circuitsBySpace)
                && circuitsBySpace.stream().anyMatch(item ->
                StringUtils.equals(item.getFuncType(), DtpRwdCircuitFuncTypeEnum.GENERAL_LIGHTING.getValue()));
    }

    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    public static class Fields {
        public static final String co2 = "cO2";
        public static final String rh = "rH";
    }
}
