package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Set;

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
public class InfoCodeConfigDTO extends SystemDTO {

    @NotNull(message = "{key_ee4b45ecb2e9455f96e58698f2743f71}")
    @Schema(description = "对象ID集合")
    private Set<String> objectIds;

    @Schema(description = "是否返回异常事件信息")
    private Boolean riskEvent;

}
