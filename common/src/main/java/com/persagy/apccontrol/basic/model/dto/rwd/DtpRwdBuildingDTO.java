package com.persagy.apccontrol.basic.model.dto.rwd;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * 物理世界建筑DTO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界建筑DTO")
@FieldNameConstants
public class DtpRwdBuildingDTO extends DtpRwdObjectDTO {

    // ------------------------- 标准字段 -------------------------

    @Schema(description = "建筑顺序")
    @JsonAlias(Fields.buildingSequenceId)
    private Integer buildingSequenceId;

    // ------------------------- 拓展字段 -------------------------

    @Schema(description = "建筑下楼层")
    @FieldNameConstants.Exclude
    private List<DtpRwdFloorDTO> floors;

    @SuppressWarnings("unused")
    public static class Fields {
        public static final String buildingSequenceId = "buildingSequenceID";
    }
}
