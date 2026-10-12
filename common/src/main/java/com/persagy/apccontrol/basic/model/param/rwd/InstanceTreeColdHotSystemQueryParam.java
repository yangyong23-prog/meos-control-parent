package com.persagy.apccontrol.basic.model.param.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

/**
 * 冷源&热源&冷热源树查询参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@Schema(title = "冷源&热源&冷热源树查询参数")
public class InstanceTreeColdHotSystemQueryParam {

    @Schema(description = "查询字段, 为空仅查询默认字段")
    private Set<String> withColumns;

}
