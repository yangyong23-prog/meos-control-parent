package com.persagy.apccontrol.basic.model.dto.equipmentlist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InfoCodeDefineModelCfgDTO {
    
    @Schema(description = "分组编码")
    private String groupCode;
    
    @Schema(description = "信息点编码")
    private String infoCode;
    
    @Schema(description = "是否搜索")
    private boolean search;
    
    @Schema(description = "是否显示")
    private boolean show;
    
    @Schema(description = "顺序")
    private int order;
    
    @Schema(description = "扩展字段")
    private Object extend;
    
}
