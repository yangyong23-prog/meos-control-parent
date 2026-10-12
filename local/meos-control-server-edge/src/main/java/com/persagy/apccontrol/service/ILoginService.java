package com.persagy.apccontrol.service;

import com.persagy.apccontrol.controller.dto.LoginDTO;
import com.persagy.apccontrol.controller.vo.LoginVO;

/**
 * 登录
 *
 * @author shaohongbo
 * @date 2023/3/25 01:07
 */
public interface ILoginService {


    LoginVO login(LoginDTO loginDTO);
}
