package com.persagy.apccontrol.basic.outside.feign.service.impl;

import com.persagy.apccontrol.basic.outside.dto.lighting.DifyDTO;
import com.persagy.apccontrol.basic.outside.feign.client.DifyServerClient;
import com.persagy.apccontrol.basic.outside.feign.service.DifyServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/26 11:30
　　 * @description: 能耗预算查询
*/
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DifyServerServiceImpl implements DifyServerService {

    private final DifyServerClient difyServerClient;
    private static final String LOG_TAG = "dify 平台服务";

    @Override
    public void webhook(DifyDTO difyDTO) {
        insert(Boolean.FALSE, "send dify reserver fail", difyDTO,
                () -> difyServerClient.webhook(difyDTO));
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }

}
