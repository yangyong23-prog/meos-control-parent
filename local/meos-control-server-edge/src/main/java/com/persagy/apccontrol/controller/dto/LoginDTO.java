package com.persagy.apccontrol.controller.dto;

import com.persagy.apccontrol.basic.model.base.DomainBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * @author shaohongbo
 * @date 2023年03月27日 15:12
 */

@Data
@Schema(title = "登录参数")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginDTO extends DomainBase {

    @Schema(description = "用户名")
    @NotEmpty(message = "{key_6080ea8639994cb28e99267d5921c619}")
    private String userName;

    @Schema(description = "密码")
    @NotEmpty(message = "{key_e1de71519e3142d28d3b3e301d24cf17}")
    private String password;

}
