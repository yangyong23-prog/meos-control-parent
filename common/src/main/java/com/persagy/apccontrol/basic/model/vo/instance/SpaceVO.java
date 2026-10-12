package com.persagy.apccontrol.basic.model.vo.instance;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "空间VO")
public class SpaceVO extends DtpRwdSpaceVO {

    @Schema(description = "空间功能类型名称")
    private String roomFuncTypeName;

    @Schema(description = "空间关联设备")
    private List<DtpRwdObjectDTO> equipments;

}
