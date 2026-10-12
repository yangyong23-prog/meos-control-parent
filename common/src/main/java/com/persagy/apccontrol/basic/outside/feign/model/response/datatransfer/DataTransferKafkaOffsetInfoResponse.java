package com.persagy.apccontrol.basic.outside.feign.model.response.datatransfer;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 数据同步服务-Kafka消费位置响应
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DataTransferKafkaOffsetInfoResponse extends BaseResponse {

    /**
     * Topic名称
     */
    private String topicName;

    /**
     * 未处理数量
     */
    private String unHandleCount;

}
