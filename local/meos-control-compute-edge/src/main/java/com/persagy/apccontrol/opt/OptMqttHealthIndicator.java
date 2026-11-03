package com.persagy.apccontrol.opt;

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
 */
@Component("optMqtt")
@ConditionalOnProperty(name = "switch.opt.mqttHealth", havingValue = "true")
public class OptMqttHealthIndicator extends AbstractHealthIndicator {

    @Value("${opt.mqtt.health.max-silence-seconds:600}")
    private long maxSilenceSeconds;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        long now = System.currentTimeMillis();
        long last = OptMqttStatus.lastReceivedAt();
        long reference = last == 0 ? OptMqttStatus.startedAt() : last;
        long silenceSeconds = (now - reference) / 1000;
        builder.withDetail("received", OptMqttStatus.received())
                .withDetail("silenceSeconds", silenceSeconds);
        if (silenceSeconds > maxSilenceSeconds) {
            builder.down();
        } else {
            builder.up();
        }
    }
}
