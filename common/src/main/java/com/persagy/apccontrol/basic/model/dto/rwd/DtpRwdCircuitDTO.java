package com.persagy.apccontrol.basic.model.dto.rwd;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.lighting.DemandLightingProperties;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物理世界照明回路DTO
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@Schema(title = "物理世界照明回路DTO")
public class DtpRwdCircuitDTO extends DtpRwdObjectDTO {

    @Schema(description = "服务区域类型")
    private String serviceAreaType;

    @Schema(description = "功能类型")
    private String funcType;

    @Schema(description = "自动控制模式")
    private List<String> autoCtrlType;

    @Nullable
    @Schema(description = "灯具类型")
    private String lampFuncType;

    @Schema(description = "楼层ID")
    private String floorId;

    @Schema(description = "建筑ID")
    private String buildingId;

    // ------------------------- 拓展字段 -------------------------

    @Schema(description = "非夜景照明回路服务空间")
    private List<DtpRwdSpaceDTO> serviceSpaces;

    @Schema(description = "夜景照明回路服务的楼层")
    private List<DtpRwdFloorDTO> serviceFloors;

    @JsonIgnore
    @FieldNameConstants.Exclude
    @Schema(description = "楼层顺序")
    private Integer floorSequenceId;

    @JsonIgnore
    @FieldNameConstants.Exclude
    @Schema(description = "建筑顺序")
    private Integer buildingSequenceId;

    /**
     * 转换为需求模板类型
     *
     * @return 需求模板类型
     * @since 7.250801.0
     */
    public @Nullable DemandTemplateTypeEnum toDemandTemplateType() {
        if (StringUtils.isBlank(this.serviceAreaType)) {
            return null;
        }
        DemandLightingProperties demandLightingProperties = SpringUtil.getBean(DemandLightingProperties.class);
        Map<DemandTemplateTypeEnum, Set<String>> serviceAreaType = demandLightingProperties.getServiceAreaType();
        for (DemandTemplateTypeEnum templateType : DemandTemplateTypeEnum.values()) {
            Set<String> serviceAreaTypes = MapUtils.getObject(serviceAreaType, templateType);
            if (CollectionUtils.isNotEmpty(serviceAreaTypes) && serviceAreaTypes.contains(this.serviceAreaType)) {
                return templateType;
            }
        }
        return null;
    }
}
