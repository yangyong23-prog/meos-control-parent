package com.persagy.apccontrol.common.outside.feign;

import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.datatransfer.DataTransferKafkaOffsetInfoResponse;
import com.persagy.apccontrol.common.model.vo.PaasDataCacheVo;
import com.persagy.framework.common.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 边到云数据同步，走kafka
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/7/10 16:34
 */
@FeignClient(name = "paas-datatransfer-server", url = "${inner-url.paas.paas-datatransfer.service:}")
public interface PaasDatatransferClient {

    /**
     * 判断云端kafka是否离线
     *
     * @return 云端kafka是否离线
     * @since 5.240801.0
     */
    @GetMapping("/cloudNetHealthy")
    CustomerResponse<Boolean> cloudNetHealthy();

    /**
     * 查询Kafka消费的信息
     *
     * @param businessRemark 业务标识
     * @return Kafka消费的信息
     * @since 5.240801.0
     */
    @GetMapping("/getKafkaOffsets")
    CustomerResponse<List<DataTransferKafkaOffsetInfoResponse>> listKafkaOffsets(@RequestParam(required = false) String businessRemark);

    /**
     * http://172.19.0.143:9099/project/479/interface/api/28363
     * <p>
     * 用将边缘侧的数据同步到云端。调用此接口将发送数据到kafka，由云端进行消费。支持批量发送，每批数据最大不超过100条。超过会报错。
     * 数据中接收一个key和一个value，二者都是json格式。
     * 该方法只处理“新增”和“删除”两个逻辑。修改的逻辑会被转换为删除+插入。每次最多100条
     *
     * @param paasDataCacheVoList
     * @return
     */
    @PostMapping("/sendDataToKafka")
    @Retryable(label = "同步数据到云", backoff = @Backoff(multiplier = 1.5))
    R<Object> sendDataToKafka(@RequestBody List<PaasDataCacheVo> paasDataCacheVoList);

}
