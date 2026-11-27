package com.persagy.apccontrol.opt;

import com.persagy.apccontrol.basic.opt.OptSwitch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * B1：MQTT 健康检查（{@code switch.opt.mqttHealth=true} 时才注册，需重启）。
 *
 * <p>Twin 周期推送约 45 秒一次，超过 {@code opt.mqtt.health.max-silence-seconds}（默认 600 秒）
 * 没有收到任何 MQTT 消息即判定 DOWN。启动后的同样时长内视为宽限期。
 * 收包时间由 MqttReceiveServiceImpl 在 {@code switch.opt.mqttIsolation=true} 时记录。
 *
 * <p>孪生数据源切到 meos-hub（{@code switch.opt.twinSource=hub}）后，dtp-twin 的 twin/*_external 主题不再是数据来源；
 * 此时若没配置 meos-hub 出口主题 {@code mqtt.topic.meos-hub-twin.name}，就没有任何应当持续来数据的主题，
 * 静默是预期状态，判 UP 并在明细里说明，避免整体健康检查长期误报 DOWN。
 */
@Component("optMqtt")
@ConditionalOnProperty(name = "switch.opt.mqttHealth", havingValue = "true")
public class OptMqttHealthIndicator extends AbstractHealthIndicator {

    @Value("${opt.mqtt.health.max-silence-seconds:600}")
    private long maxSilenceSeconds;

    /** meos-hub 盒内业务 MQTT 出口主题，与 MqttReceiveServiceImpl 同一配置项 */
    @Value("${mqtt.topic.meos-hub-twin.name:}")
    private String hubTwinTopic;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        long now = System.currentTimeMillis();
        long last = OptMqttStatus.lastReceivedAt();
        long reference = last == 0 ? OptMqttStatus.startedAt() : last;
        long silenceSeconds = (now - reference) / 1000;
        builder.withDetail("received", OptMqttStatus.received())
                .withDetail("silenceSeconds", silenceSeconds);
        String twinSource = OptSwitch.value(OptSwitch.TWIN_SOURCE, "twin");
        if ("hub".equalsIgnoreCase(twinSource) && StringUtils.isBlank(hubTwinTopic)) {
            builder.withDetail("skipped", "twinSource=hub 且未配置 mqtt.topic.meos-hub-twin.name，无需持续收包的主题")
                    .up();
            return;
        }
        if (silenceSeconds > maxSilenceSeconds) {
            builder.down();
        } else {
            builder.up();
        }
    }
}
