package com.persagy.apccontrol.common.outside.feign;

import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR;
import com.persagy.apccontrol.common.outside.model.dto.MqttSubscribe;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * opt 分支 B3：Twin 计算订阅规则（带 topic 参数）。
 * <p>
 * topic 传 "meos" 时 Twin 按其 Nacos {@code mqtt.topic.twin.sub} 中 {@code compute.meos} 映射到 MQTT 主题
 * {@code twin/compute_meos}；不传为 "external"（原有按类订阅使用的主题），两套规则互不影响。
 * 与 {@link DtpTwinClient} 同一服务，单独 contextId，避免改动热点接口。
 */
@FeignClient(name = "dtp-twin-server-edge", contextId = "optDtpTwinSubscribe", url = "${inner-url.bdtp.dtp-twin.service:}")
@RefreshScope
public interface OptTwinSubscribeClient {

    /**
     * 查询已有规则。
     *
     * @param classCodes 类编码
     * @return content 每项含 classCode、infoCode（null 表示该类全部点）
     */
    @PostMapping("/subscribe/rule/queryComputeSubscribe")
    DtpTwinR<Map<String, Object>> query(@RequestHeader("projectId") String projectId, @RequestHeader("groupCode") String groupCode,
                                        @RequestParam("topic") String topic, @RequestBody List<String> classCodes);

    /**
     * 新增规则；infoCode 为空表示该类全部点。
     */
    @PostMapping("/subscribe/rule/addComputeSubscribe")
    DtpTwinR<Object> add(@RequestHeader("projectId") String projectId, @RequestHeader("groupCode") String groupCode,
                         @RequestParam("topic") String topic, @RequestBody List<MqttSubscribe> rules);

    /**
     * 删除规则；infoCode 为空表示删除该类“全部点”规则。
     */
    @PostMapping("/subscribe/rule/delComputeSubscribe")
    DtpTwinR<Object> del(@RequestHeader("projectId") String projectId, @RequestHeader("groupCode") String groupCode,
                         @RequestParam("topic") String topic, @RequestBody List<MqttSubscribe> rules);
}
