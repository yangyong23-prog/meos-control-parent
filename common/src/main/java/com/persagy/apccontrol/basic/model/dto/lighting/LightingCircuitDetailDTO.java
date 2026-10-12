package com.persagy.apccontrol.basic.model.dto.lighting;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 照明-照明回路详情DTO
 *
 * @author wxf
 * @since 7.250601.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明-照明回路详情DTO")
public class LightingCircuitDetailDTO extends DtpRwdEquipmentDTO {

    @Schema(description = "关联控制模块")
    private List<DtpRwdObjectDTO> relController;

    @Schema(description = "关联DDC")
    private List<DtpRwdObjectDTO> relDDC;

    @Schema(description = "服务空间")
    private List<DtpRwdSpaceDTO> relSpace;

    @Schema(description = "服务楼层")
    private List<DtpRwdFloorDTO> relFloor;

    @Schema(description = "服务区域类型")
    private String serviceAreaType;

    @Schema(description = "服务区域类型")
    private String serviceAreaTypeName;

    @Schema(description = "功能类型")
    private String funcType;

    @Schema(description = "功能类型名字")
    private String funcTypeName;

    @Nullable
    @Schema(description = "灯具类型")
    private String lampFuncType;

    @Schema(description = "灯具类型名字")
    private String lampFuncTypeName;
}
