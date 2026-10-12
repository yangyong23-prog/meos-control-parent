package com.persagy.apccontrol.basic.model.dto.intervention;

import com.persagy.apccontrol.basic.model.enumtype.intervention.InterventionInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(title = "干预实例")
public class InterventionTaskInstanceDTO {
    
    @Schema(description = "实例 ID列表")
    @NotEmpty(message = "{key_6940e9d0d61c405e94aec265bb8db57f}")
    private List<String> instanceIds;
    
    @Schema(description = "实例类型")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private InterventionInstanceTypeEnum instanceType;
}
