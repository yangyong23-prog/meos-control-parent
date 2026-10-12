package com.persagy.apccontrol.basic.model.param.airconditioning;

import com.persagy.apccontrol.basic.model.enumtype.OnOffStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 强制控制状态编辑参数
 *
 * @author JiYinchuan
 * @since 7.250403.0
 */
@Data
@Schema(title = "强制控制状态编辑参数")
public class ForceControlStatusEditParam {

    @Schema(description = "对象ID")
    @NotBlank(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private String objectId;

    @Schema(description = "强制控制状态")
    @NotNull(message = "{key_03b82999724642aa8fb9d425abb34b2a}")
    private OnOffStateEnum status;

    @Schema(description = "解除强制控制信息点")
    private List<ReleaseForceControlInfoCode> releaseForceControlInfoCodes;

    @Data
    public static class ReleaseForceControlInfoCode {

        @Schema(description = "解除强制控制对象ID")
        private String objectId;

        @Schema(description = "解除强制控制信息点")
        @NotEmpty(message = "{key_c94d8906ae88411da02d5cd1015bb6a1}")
        private Set<String> infoCodes;

    }
}
