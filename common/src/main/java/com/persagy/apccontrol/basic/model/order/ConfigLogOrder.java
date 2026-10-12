package com.persagy.apccontrol.basic.model.order;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @Author : yuyao
 * @Date : 2022/3/23 9:51 AM
 * @Description ：
 */
@Schema(description = "配置记录查询")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
public class ConfigLogOrder {

    @Schema(description = "项目编码", required = true)
    @NotEmpty(message = "{key_559782df2a544ab6ae8b55e0a2b0ced8}")
    private String projectId;

    @Schema(description = "配置类型", required = true)
    @NotNull(message = "{key_ab1e8b7a618540a1afa65dc57a12cce6}")
    private BasicsConfigTypeEnum type;

}
