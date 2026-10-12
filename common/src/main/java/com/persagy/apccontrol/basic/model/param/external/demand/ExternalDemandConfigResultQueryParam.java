package com.persagy.apccontrol.basic.model.param.external.demand;

import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "需求管理-需求配置结果查询参数")
public class ExternalDemandConfigResultQueryParam extends BaseDemandTemplateParam {
    
    @Schema(description = "场景时间-照明场景是必须的")
    private LocalDateTime sceneTime;
}
