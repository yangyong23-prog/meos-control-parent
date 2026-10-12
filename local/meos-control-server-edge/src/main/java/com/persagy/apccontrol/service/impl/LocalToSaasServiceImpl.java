package com.persagy.apccontrol.service.impl;

import cn.hutool.http.Method;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.common.outside.feign.RcSaasClient;
import com.persagy.apccontrol.service.ILocalToSaasService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author shaohongbo
 * @date 2023年03月27日 16:29
 */
@Service
@Slf4j
public class LocalToSaasServiceImpl implements ILocalToSaasService {

    @Autowired
    private RcSaasClient rcSaasClient;


    @Override
    public String meoiAimServerCommon(String groupCode, String projectId, Method method, String uri, String contentType, Object body, Map<String, Object> param) {
        CommonServerForwardVO forwardVO = new CommonServerForwardVO(groupCode, projectId, method, uri, contentType, body, null);
        return this.rcSaasClient.meoiAimServerCommon(groupCode, forwardVO);
    }

    @Override
    public String dtpRwdServerCommon(String groupCode, String projectId, Method method, String uri, String contentType, Object body, Map<String, Object> param) {
        CommonServerForwardVO forwardVO = new CommonServerForwardVO(groupCode, projectId, method, uri, contentType, body, param);
        return this.rcSaasClient.dtpRwdServerCommon(groupCode, forwardVO);
    }

}
