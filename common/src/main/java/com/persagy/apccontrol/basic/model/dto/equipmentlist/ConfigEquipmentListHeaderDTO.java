package com.persagy.apccontrol.basic.model.dto.equipmentlist;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 自定义设备列表表头配置对象
 */
@Data
@NoArgsConstructor
public class ConfigEquipmentListHeaderDTO {
    
    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
    
    @Schema(description = "运行监测一级类型")
    @NotNull(message = "{key_261245b8e14a4b48b25eb41878934c8b}")
    private @NonNull RunningMonitorTypeEnum runningMonitorType;
    
    @Schema(description = "模版类型 key")
    private String templateTypeKey;
    
    @Schema(description = "全量表头信息")
    private List<InfoCodeGroupVO> infoCodeGroupVOList;
    
    @Schema(description = "配置自定义表头的信息")
    private List<InfoCodeDefineModelCfgDTO> cfgInfoCodeGroupVOList;
    
    @Schema(description = "是否有配置")
    private boolean isConfig;
    
    
}
