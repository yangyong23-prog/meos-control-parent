package com.persagy.apccontrol.basic.model.vo.intervention;


import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskTimeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 照明干预配置-保存编辑返回VO
 */
@Data
public class InterventionTaskConfigSaveOrUpdateVO {
    
    @Schema(description = "异常编码")
    private String errorCode;
    
    @Schema(description = "重复时段")
    private List<InterventionTaskTimeDTO> repetitionTimeSlot = new ArrayList<>();

    @Schema(description = "照明重复配置")
    private List<LightingRepetition> lightingRepetitions = new ArrayList<>();

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class LightingRepetition extends DtpRwdObjectDTO {

        @Schema(description = "具体配置")
        private InterventionTaskConfigDTO config;

    }
}
