package com.persagy.apccontrol.init;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.apccontrol.common.config.SystemInitService;
import com.persagy.apccontrol.common.outside.feign.DtpTwinClient;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR;
import com.persagy.apccontrol.common.outside.model.dto.MqttSubscribe;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shaohongbo
 * @date 2023年03月29日 19:29
 */
@Slf4j
@Component
@Order(0)
@ComponentScan("com.persagy.framework.mqtt")
public class MqttSubscribeInitServiceImpl implements SystemInitService {
    
    @Value("${paas.mqtt.enabled:true}")
    private Boolean mqttEnabled;

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private DtpTwinClient dtpTwinClient;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    InfoCodeConfigService infoCodeConfigService;

    @Override
    @Async
    public void init() throws Exception {
        boolean isSynSuccess = false;
        if (!BooleanUtil.isTrue(mqttEnabled)) {
            log.info("不进行MQTT订阅");
            return;
        }

        //查询配置的 设备类
        Map<String, Object> objectMap = this.redisCacheUtil.hGetAll(RedisKey.CLASS_NAME);
        Set<String> classCodes = infoCodeConfigService.getClassCodesForTwinSubscribe();
        if (CollectionUtil.isEmpty(objectMap) && CollUtil.isEmpty(classCodes)) {
            log.info("未配置设备类不进行MQTT订阅！");
            return;
        }
        classCodes.addAll(objectMap.keySet());
        List<MqttSubscribe> mqttSubscribes = buildRCList(classCodes);
        while (!isSynSuccess) {
            log.info("iot边缘端创建mqtt订阅开始,订阅对象类:{}", classCodes);
            try {
                DtpTwinR<Object> objectDtpTwinR = dtpTwinClient.addComputeSubscribe(projectCfg.getProjectId(), projectCfg.getGroupCode(), mqttSubscribes);
                if (objectDtpTwinR.getIsSuccess()) {
                    isSynSuccess = true;
                    log.info("iot边缘端创建mqtt订阅 成功:{}", mqttSubscribes);
                } else {
                    log.info("iot边缘端创建mqtt订阅 失败:{}", objectDtpTwinR);
                }
            } catch (Exception e) {
                log.error("iot边缘端创建mqtt订阅失败,10s后重试:{}", mqttSubscribes, e);
            }
            Thread.sleep(10000L);
        }
    }

    private List<MqttSubscribe> buildRCList(Set<String> cs) {
        List<MqttSubscribe> objects = new ArrayList<>();
        MqttSubscribe obj = null;
        for (String classCode : cs) {
            if (classCode.contains("&")) {
                classCode = classCode.split("&")[0];
            }
            obj = new MqttSubscribe();
            obj.setClassCode(classCode);
            objects.add(obj);
        }
        return objects;
    }

    @Override
    public String getName() {
        return LocaleMessageUtil.getMessage("key_911976b5a9984ef58aca6eeeb60e9dcf");
    }

}
