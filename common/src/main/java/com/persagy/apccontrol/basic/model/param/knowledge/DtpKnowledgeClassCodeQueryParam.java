package com.persagy.apccontrol.basic.model.param.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 数据字典-设备类查询参数
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@Schema(title = "数据字典-设备类查询参数")
public class DtpKnowledgeClassCodeQueryParam {

    @Schema(description = "是否查询缓存, 如缓存不存在则会自动添加并使用")
    @NotNull(message = "{key_3ab97590181a4fd4ba9ef2b52e32c2bd}")
    private Boolean queryCache = true;

    @Schema(description = "是否在查询失败|数据为空时抛出异常")
    @NotNull(message = "{key_c84fe6cccdeb41de928f527dd7b6e6d5}")
    private Boolean isThrows = true;

    @Schema(description = "类型编码")
    @NotEmpty(message = "{key_f8e360e99f1e4a34ac3f78914c0fef15}")
    private Set<String> classCodes;

}
