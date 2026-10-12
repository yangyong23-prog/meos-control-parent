package com.persagy.apccontrol.basic.model.param.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author :
 * @date : 2024/11/19 14:21
 */
@Data
@Schema(title = "运行标准看板-查询参数")
public class SpecialBusinessTimeQuoteCountQueryParam {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @NotBlank(message = "{key_82c482b128f746fca3038e09a27ed7b6}")
    @Schema(description = "查询日期 格式yyyy-MM-dd")
    private String workDate;

    @Schema(description = "查询对象")
    private List<RwdObjectQueryParam> objects;



}
