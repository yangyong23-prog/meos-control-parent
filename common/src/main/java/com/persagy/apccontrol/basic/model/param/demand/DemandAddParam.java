package com.persagy.apccontrol.basic.model.param.demand;


import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "添加需求配置")
public class DemandAddParam extends DemandBasicsOperateParam {

    @Schema(description = "配置分组名称")
    private String configGroupName;

    @Schema(description = "需求配置")
    @NotNull(message = "{key_89218e334c1e498f995a1c62bf81d504}")
    private JsonNode configData;

    @Schema(description = "是否为新增空间功能类型")
    private Boolean addRoomFuncType;

    @Schema(description = "是否是删除")
    private Boolean deleteFlag = false;

}
