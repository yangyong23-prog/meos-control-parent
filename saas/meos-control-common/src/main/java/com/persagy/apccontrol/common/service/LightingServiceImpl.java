package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 照明服务实现
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Slf4j
@Service
@Validated
public class LightingServiceImpl extends AbstractLightingService {

    @Override
    public Map<String, Map<String, Map<String, String>>> groupMapInfo(String groupCode, String projectId, List<String> objectIdList) {
        return Collections.emptyMap();
    }

    @Override
    public String logTag() {
        return "照明服务";
    }

}
