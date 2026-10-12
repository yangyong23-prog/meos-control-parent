package com.persagy.apccontrol.basic.model.dto.knowledge;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 数据字典-信息点数据源定义DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeInfoCodeDataSourceDTO extends BaseResponse {

    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "父级编码")
    private String parentCode;

    public DtpKnowledgeInfoCodeDataSourceDTO(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
