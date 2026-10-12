package com.persagy.apccontrol.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.common.model.param.ai.AiDemandConfigQueryParam;
import com.persagy.apccontrol.common.model.vo.ai.AiDemandConfigVO;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;

/**
 * 运行智能体服务
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
public interface AiService extends LogComponent {

    /**
     * 查询需求配置
     *
     * @param param 查询参数
     * @return 需求配置
     * @since 7.250702.0
     */
    AiDemandConfigVO getDemandConfig(@NonNull @Valid AiDemandConfigQueryParam param);

    /**
     * 计算状态回调
     *
     * <pre>{@code {
     *     "calcStatus": XXX, // 计算状态， 0开始计算，1计算成功，2计算失败；
     *     "systemId": "Sy74803828241747160901841784834",
     *     "timeLabel": "yyyyMMddHHmmss", // 预测时序数据的首个15min的时间值
     *     "startTime": "20250715095015", // 计算开始时间
     *     "endTime": "20250715095311", // 计算结束时间
     *     "cmpCount": 20, // 计算次数
     *     "cmpResult": "success", // 计算结果，success表示成功
     *     "cmpNextStartTime": "20250715100515", // 下次计算开始时间
     *     "stgyCodeList": [ // 三套策略的编码
     *         {
     *             "code": "12314124", // 策略编码
     *             "isBest": true // 是否最优策略
     *         }
     *     ]
     * }}</pre>
     *
     * @param param 回调参数
     * @return 是否成功
     * @since 7.250702.0
     */
    boolean notifyCalcStatus(@NonNull @Valid JsonNode param);

}