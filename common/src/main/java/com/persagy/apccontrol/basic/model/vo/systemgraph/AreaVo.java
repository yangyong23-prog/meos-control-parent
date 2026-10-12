package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.AreaDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 *
 * @author houyuanzhi
 * @date 2024/1/8 18:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AreaVo extends AreaDto {

    @ApiModelProperty("管线样式编码")
    private String styleCode;

    private Map<String, JSONObject> textMap;

}
