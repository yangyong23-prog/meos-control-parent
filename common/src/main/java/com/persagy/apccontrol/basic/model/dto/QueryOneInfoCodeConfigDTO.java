package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 查询指定对象的信息点
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 14:32
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "信息点配置及IOT数据")
public class QueryOneInfoCodeConfigDTO extends SystemDTO {

    @NotBlank(message = "{key_1af4a612374f4faa99db5ad6ec0600d3}")
    @Schema(description = "对象ID集合")
    private String objectId;


}
