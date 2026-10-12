package com.persagy.apccontrol.basic.model.vo.instance;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 冷源&热源&冷热源树VO
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@Schema(title = "冷源&热源&冷热源树VO")
public class InstanceTreeColdHotSystemVO {

    @Schema(description = "系统类编码")
    private String classCode;

    @Schema(description = "系统类编码名称")
    private String classCodeName;

    @Schema(description = "系统实例")
    private List<DtpRwdObjectDTO> systems;

}
