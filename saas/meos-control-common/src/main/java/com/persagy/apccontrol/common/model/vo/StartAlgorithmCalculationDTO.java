package com.persagy.apccontrol.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Schema(title = "启用算法计算")
public class StartAlgorithmCalculationDTO {
    
    @Schema(description = "系统 ID")
    @NotEmpty(message = "系统id列表不能为空")
    private List<String> systemIdList;
    
}
