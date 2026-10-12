package com.persagy.apccontrol.basic.outside.dto.lighting;

import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * 运行模式 配置VO
 *
 * @author yuyao
 */
@Getter
@Setter
@ToString(callSuper = true)
public class RunningModeDTO extends SingleObjectDTO {

    // 与 objectId 共存，同时生效
    @Schema(description = "对象ID集合")
    private Set<String> objectIds;

    @Schema(description = "手自动状态，0-手动，1-自动")
    @NotBlank(message = "{key_e62950c9326849948ac0fcb23dfe7bd3}")
    private Object model;

    @Schema(description = "操作类型")
    private OperateTypeEnum operateType;

    @Schema(description = "操作原因")
    private String operateReason;

    @Schema(description = "备注说明")
    private String contentInfo;

    // yyyyMMddHHmmss
    @Schema(description = "到期时间")
    private String expireTime;

    @Schema(description = "用户id")
    private String userId;


}
