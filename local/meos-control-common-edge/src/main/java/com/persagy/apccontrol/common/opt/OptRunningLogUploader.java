package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.common.model.vo.PaasDataCacheVo;
import com.persagy.apccontrol.common.outside.feign.PaasDatatransferClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.nio.file.Paths;
import java.util.List;

/**
 * B1：运行日志上云改为有界本地队列 + 独立线程合批发送（开关 {@code switch.opt.runningLogQueue}）。
 *
 * <p>开关关闭时 {@link #enabled()} 为 false，调用方仍走原来的同步 sendDataToKafka；
 * 打开后调用方只把已组装好的 Kafka 消息入队，不再在 MQTT 消费链路里同步发 HTTP。
 *
 * <p>配置（启动时读取）：
 * {@code opt.runninglog.queue-capacity}（默认 10000）、
 * {@code opt.runninglog.spool-file}（默认 ./opt-spool/running-log.jsonl）、
 * {@code opt.runninglog.spool-max-bytes}（默认 50MB）。
 * 指标：runningLog.sent / fail / drop / spoolDrop / replayed。
 */
@Component
public class OptRunningLogUploader {

    private final OptBatchUploader<PaasDataCacheVo> uploader;

    public OptRunningLogUploader(PaasDatatransferClient paasDatatransferClient,
                                 ObjectMapper objectMapper,
                                 @Value("${opt.runninglog.queue-capacity:10000}") int capacity,
                                 @Value("${opt.runninglog.spool-file:./opt-spool/running-log.jsonl}") String spoolFile,
                                 @Value("${opt.runninglog.spool-max-bytes:52428800}") long spoolMaxBytes) {
        this.uploader = new OptBatchUploader<>("runningLog", PaasDataCacheVo.class,
                batch -> {
                    for (List<PaasDataCacheVo> part : Lists.partition(batch, PaasDataCacheVo.MAX_LIMIT)) {
                        paasDatatransferClient.sendDataToKafka(part);
                    }
                },
                capacity, PaasDataCacheVo.MAX_LIMIT, Paths.get(spoolFile), spoolMaxBytes, objectMapper.copy())
                .start();
    }

    public boolean enabled() {
        return OptSwitch.on(OptSwitch.RUNNING_LOG_QUEUE);
    }

    /**
     * 入队（不阻塞）。同一条日志的多条消息（如先删后增）按传入顺序入队、按顺序发送。
     */
    public boolean offer(List<PaasDataCacheVo> messages) {
        return uploader.offer(messages);
    }

    @PreDestroy
    public void stop() {
        uploader.stop();
    }
}
