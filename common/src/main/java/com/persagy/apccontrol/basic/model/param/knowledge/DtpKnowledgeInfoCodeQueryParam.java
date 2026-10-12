package com.persagy.apccontrol.basic.model.param.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 数据字典-信息点查询参数
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Data
@Schema(title = "数据字典-信息点查询参数")
public class DtpKnowledgeInfoCodeQueryParam {

    @Schema(description = "类型编码")
    @NotBlank(message = "{key_f8e360e99f1e4a34ac3f78914c0fef15}")
    private String classCode;

    @Schema(description = "信息点编码")
    @NotBlank(message = "{key_0ad0f2789ec14197b646fbf159c47354}")
    private String infoCode;

}
