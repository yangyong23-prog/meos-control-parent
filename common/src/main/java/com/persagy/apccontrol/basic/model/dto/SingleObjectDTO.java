package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 根据对象ID查询
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/23 16:39
 */
@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Schema(title = "对象ID信息")
public class SingleObjectDTO extends SystemDTO {

    //@NotBlank(message = "对象ID不可为空")
    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "空间ID")
    private String spaceId;

    @Schema(description = "对象类型")
    private String objectType;

    @Schema(description = "设备类编码")
    private String classCode;

}
