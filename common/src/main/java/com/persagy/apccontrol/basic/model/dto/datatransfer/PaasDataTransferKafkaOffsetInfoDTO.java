package com.persagy.apccontrol.basic.model.dto.datatransfer;

import lombok.Data;

/**
 * 数据同步服务-Kafka消费信息DTO
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Data
public class PaasDataTransferKafkaOffsetInfoDTO {

    /**
     * Topic名称
     */
    private String topicName;

    /**
     * 未处理数量
     */
    private Integer unHandleCount;

}
