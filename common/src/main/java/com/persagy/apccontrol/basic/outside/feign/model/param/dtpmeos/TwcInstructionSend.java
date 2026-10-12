package com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos;

import com.persagy.apccontrol.basic.model.dto.PointInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author shaohongbo
 * @date 2023年03月29日 20:04
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Schema(title = "手动设定控制指令")
@ToString(callSuper = true)
public class TwcInstructionSend {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    private String projectId;

    @Schema(description = "系统ID")
    private String systemId;

    @Schema(description = "空间ID")
    private String spaceId;

    @Schema(description = "对象ID")
    @NotBlank(message = "{key_1af4a612374f4faa99db5ad6ec0600d3}")
    private String objectId;

    @Schema(description = "修改信息")
    @NotNull(message = "{key_fd1b0f7900db4a7999e0704d4d943b5a}")
    private List<PointInfoDTO> pointInfos;

}

