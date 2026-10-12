package com.persagy.apccontrol.basic.outside.feign.model.param.midpermission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 云端登录
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/10/26 17:36
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class LoginUserParam {

    /** 加密后的登录名 */
    private String loginName;

    /** 加密后的登录密码 */
    private String password;

    private String loginDevice = "PC";

    private Boolean isAdminLogin = false;

    private String verifyCode;

    private String longitude;

    private String latitude;

    private String mode = "0";

    public LoginUserParam(String loginName, String password) {
        this.loginName = loginName;
        this.password = password;
    }

}
