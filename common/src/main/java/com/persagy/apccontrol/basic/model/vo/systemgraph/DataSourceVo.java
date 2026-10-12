package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author mj
 * @date 2021/11/4 15:28
 */
@ApiModel(value = "DataSourceVo", description = "对应字典数据信息点的dataSource")
@Data
@Accessors(chain = true)
public class DataSourceVo {
    @Schema(description = "code", required = true)
    private String code;
    @Schema(description = "name", required = true)
    private String name;

}
