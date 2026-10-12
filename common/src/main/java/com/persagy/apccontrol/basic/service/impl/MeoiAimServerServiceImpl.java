package com.persagy.apccontrol.basic.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;
import com.persagy.apccontrol.basic.service.MeoiAimServerService;
import com.persagy.apccontrol.basic.service.systemgraph.impl.RiskEventService;
import com.persagy.apccontrol.basic.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/17 14:45
 */
@Service
@Slf4j
public class MeoiAimServerServiceImpl implements MeoiAimServerService {

    @Value("${inner-url.meoi.meoi-aim.service:}")
    private String meoiAimServerPrefix;

    @Autowired
    private RiskEventService riskEventService;

    @Override
    public String commonRoute(CommonServerForwardVO forwardVO) {
        String response = null;
        try {
            String url = meoiAimServerPrefix + (forwardVO.getUri().startsWith("/") ? forwardVO.getUri() : ("/" + forwardVO.getUri()));
            log.info("请求转发，请求地址：{}，请求参数：{}", url, JSONObject.toJSONString(forwardVO));
            // hutool 工具类，丢响应字段，所以不在使用
            if (Method.GET.equals(forwardVO.getMethod())) {
                response = HttpUtils.httpGetRequest(forwardVO.getGroupCode(), forwardVO.getProjectId(), url, forwardVO.getParam());
            } else if (Method.POST.equals(forwardVO.getMethod())) {
                response = HttpUtils.httpPostRequest(forwardVO.getGroupCode(), forwardVO.getProjectId(), url, forwardVO.getBody(), forwardVO.getParam());
            }
            /*HttpRequest httpRequest = HttpUtil.createPost(url);

            String contentType = StrUtil.isBlank(forwardVO.getContentType()) ? ContentType.APPLICATION_JSON.toString() : forwardVO.getContentType();
            httpRequest.header("group-code", forwardVO.getGroupCode()).header("groupCode", forwardVO.getGroupCode())
                    .header("project-id", forwardVO.getProjectId()).header("projectId", forwardVO.getProjectId()).timeout(180 * 1000);
            if (forwardVO.getBody() != null) {
                httpRequest.body(JsonUtils.toJson(forwardVO.getBody()), contentType);
            }
            if (CollectionUtil.isNotEmpty(forwardVO.getParam())) {
                httpRequest.form(forwardVO.getParam());
            }

            return httpRequest.execute().body();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public List<ExceptionEventEquipDTO> queryRiskEventList(String groupCode, String projectId, List<ObjectClassCodeDTO> objectClassCodeDTOS) {
        CheckDeviceInfoDTO checkDeviceInfoDTO = new CheckDeviceInfoDTO();
        checkDeviceInfoDTO.setResultIot(false);
        checkDeviceInfoDTO.setIotDataRequestVos(objectClassCodeDTOS);
        return queryIotAndRiskEventList(groupCode, projectId, checkDeviceInfoDTO);
    }

    @Override
    public List<ExceptionEventEquipDTO> queryIotAndRiskEventList(String groupCode, String projectId, CheckDeviceInfoDTO infoDTO) {
        if (CollectionUtil.isEmpty(infoDTO.getIotDataRequestVos())) {
            return null;
        }
        return this.riskEventService.queryRiskEventList(infoDTO);
    }


}
