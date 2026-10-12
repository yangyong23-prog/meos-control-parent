package com.persagy.apccontrol.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.crypto.digest.MD5;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.utils.SaltMD5Util;
import com.persagy.apccontrol.common.model.entity.RcUser;
import com.persagy.apccontrol.common.resource.RcUserService;
import com.persagy.apccontrol.controller.dto.LoginDTO;
import com.persagy.apccontrol.controller.vo.LoginVO;
import com.persagy.apccontrol.service.ILoginService;
import com.persagy.apccontrol.service.ITokenService;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author shaohongbo
 * @date 2023年03月27日 16:29
 */
@Slf4j
@Service
public class LoginServiceImpl implements ILoginService {

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private ITokenService iTokenService;

    @Autowired
    private RcUserService rcUserService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String password = loginDTO.getPassword();
        String userName = loginDTO.getUserName();

        RcUser dbUser = rcUserService.queryUserByUserName(projectCfg.getProjectId(), userName);
        checkUserValidity(dbUser, password);

        return LoginVO.builder()
                .token(iTokenService.createToken(userName))
                .functions(dbUser.getFunctions())
                .projectId(projectCfg.getProjectId())
                .groupCode(projectCfg.getGroupCode())
                .projectName(projectCfg.getProjectName())
                .userName(dbUser.getUserName())
                .userId(dbUser.getUserId())
                .build();
    }


    private void checkUserValidity(RcUser dbUser, String password) {
        if (dbUser == null) {
            throw BusinessException.wrap(ErrorCodeConstants.USER_ERROR_A0210.getCode(), LocaleMessageUtil.getMessage("key_402e2d2062214d30bbae356e5d0c71c1"));
        }
        try {
            boolean verifySaltPassword = SaltMD5Util.verifySaltPassword(password, dbUser.getPassword());
            boolean md5 = StringUtils.equals(MD5.create().digestHex("password"), dbUser.getPassword());
            boolean ys = password.equals(dbUser.getPassword());
            if (verifySaltPassword || md5 || ys) {
                log.info("登录验证成功-verifySaltPassword:{}-md5:{}-ys:{}", verifySaltPassword, md5, ys);
            } else {
                throw BusinessException.wrap(ErrorCodeConstants.USER_ERROR_A0210.getCode(), LocaleMessageUtil.getMessage("key_402e2d2062214d30bbae356e5d0c71c1"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isNotBlank(dbUser.getState()) && "0".equals(dbUser.getState())) {
            throw BusinessException.wrap(ErrorCodeConstants.USER_ERROR_A0210.getCode(), LocaleMessageUtil.getMessage("key_6535de0549c94a9e96cb1fcfd6d30a3b"));
        }

        Date validTime = dbUser.getValidTime();
        if (validTime != null && System.currentTimeMillis() > DateUtils.addDays(validTime, 1).getTime()) {
            //账号无效
            throw BusinessException.wrap(ErrorCodeConstants.USER_ERROR_A0210.getCode(), LocaleMessageUtil.getMessage("key_2324cb7d214b4eb18c90382f66c758c2"));
        }


    }
}
