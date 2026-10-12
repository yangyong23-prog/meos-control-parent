package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.controller.dto.LoginDTO;
import com.persagy.apccontrol.controller.vo.LoginVO;
import com.persagy.apccontrol.service.ILoginService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 登录
 *
 * @author shaohongbo
 * @date 2023/3/24 23:55
 */
@Tag(name = "登录模块")
@Validated
@RestController
@RequestMapping("/")
@Slf4j
public class LoginController {

    @Autowired
    private ProjectCfg projectCfg;

    @Resource
    private ILoginService iLoginService;


    @Operation(summary = "登录接口")
    @PostMapping("login")
    public R<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());
        return R.success(iLoginService.login(loginDTO));
    }
}
