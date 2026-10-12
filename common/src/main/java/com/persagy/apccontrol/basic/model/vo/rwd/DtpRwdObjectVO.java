package com.persagy.apccontrol.basic.model.vo.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * 物理世界对象VO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@Schema(title = "物理世界对象VO")
@FieldNameConstants
public class DtpRwdObjectVO {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象本地ID")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "对象类型")
    private String objType;

    @Schema(description = "类型编码")
    private String classCode;

    @Schema(description = "是否有效")
    private Boolean valid;
}
