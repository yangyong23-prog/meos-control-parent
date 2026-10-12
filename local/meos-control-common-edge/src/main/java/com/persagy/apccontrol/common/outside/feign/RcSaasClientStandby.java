package com.persagy.apccontrol.common.outside.feign;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.InterfaceUrlConst;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 云端群控系统
 *
 * @author yuyao
 */
@FeignClient(name = "meos-control-server1", url = "https://ibuilding.yuexiuproperty.cn/meos-control-server")
public interface RcSaasClientStandby {

    @PostMapping(value = InterfaceUrlConst.SHELL_CONFIG)
    String getShellConfig(@RequestHeader(name = "groupCode") String groupCode, @RequestBody JSONObject dto);}
