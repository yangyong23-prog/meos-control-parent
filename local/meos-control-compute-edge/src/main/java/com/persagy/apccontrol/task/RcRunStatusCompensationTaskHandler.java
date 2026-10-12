package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisCacheExpUtil;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * {@code rcRunStatus}补偿任务。
 *
 * <p>设备进入开启中或关闭中后，如果后续稳定态消息未正常推送，
 * {@code rcRunStatus}可能长期停留在过渡态。定时任务通过补偿标记找出这些设备，
 * 再结合{@code runStatus}判断过渡是否已经完成。
 *
 * <p>补偿标记必须记录过渡方向，不能只根据{@code runStatus=0/1}直接覆盖：
 * {@code 2}表示开启中，只有{@code runStatus=1}时才补偿为开启；
 * {@code 3}表示关闭中，只有{@code runStatus=0}时才补偿为关闭。
 *
 * @author JiYinchuan
 * @since 7.260402.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class RcRunStatusCompensationTaskHandler implements LogComponent {

    private static final String STOPPED = "0";
    private static final String RUNNING = "1";
    private static final String OPENING = "2";
    private static final String CLOSING = "3";

    private static final int RESULT_SKIPPED = 0;
    private static final int RESULT_UPDATED = 1;
    private static final int RESULT_REMOVED = 2;
    private static final int COUNT_RESULT_SIZE = 3;
    private static final int COUNT_UPDATED_INDEX = 0;
    private static final int COUNT_SKIPPED_INDEX = 1;
    private static final int COUNT_REMOVED_INDEX = 2;

    private final ProjectCfg projectCfg;
    private final RedisCacheUtil redisCacheUtil;
    private final RedisCacheExpUtil redisCacheExpUtil;
    private final IotDataCacheService iotDataCacheService;
    private final SseHandlerContext sseHandlerContext;

    /**
     * 执行{@code rcRunStatus}补偿任务。
     *
     * <p>按项目扫描所有设备类型的补偿Hash，分别统计补偿成功、暂不满足条件、清理无效标记的数量。
     */
    @XxlJob("rcRunStatusCompensationHandler")
    public void execute() {
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());
        ContextUtil.setProjectId(projectCfg.getProjectId());

        int successCount = 0;
        int skipCount = 0;
        int removedCount = 0;
        int trackingKeyCount = 0;

        String hashPatternKey = RedisKey.buildRcRunStatusTrackingHashKey(projectCfg.getProjectId(), "*");
        Set<String> hashTrackingKeys = redisCacheExpUtil.scanKeys(hashPatternKey);
        if (hashTrackingKeys != null && !hashTrackingKeys.isEmpty()) {
            trackingKeyCount += hashTrackingKeys.size();
            for (String trackingKey : hashTrackingKeys) {
                int[] count = this.processHashTrackingKey(trackingKey);
                successCount += count[COUNT_UPDATED_INDEX];
                skipCount += count[COUNT_SKIPPED_INDEX];
                removedCount += count[COUNT_REMOVED_INDEX];
            }
        }

        if (trackingKeyCount == 0) {
            logProjectInstance().info("无待补偿的重计算运行状态设备");
            return;
        }

        logProjectInstance().info("重计算运行状态补偿检查完成",
                Arrays.asList(Pair.of("成功", successCount), Pair.of("跳过", skipCount),
                        Pair.of("清理", removedCount), Pair.of("跟踪标记数", trackingKeyCount)));
    }

    /**
     * 处理单个补偿Hash。
     *
     * <p>Hash字段为设备ID，Hash值为进入补偿时的过渡状态，{@code classCode}从key尾部解析。
     *
     * @param trackingKey 补偿跟踪Hash key
     * @return 处理结果计数，数组下标依次表示补偿成功数、跳过数、清理标记数
     */
    private int[] processHashTrackingKey(String trackingKey) {
        int[] count = new int[COUNT_RESULT_SIZE];
        String classCode = this.parseClassCode(trackingKey);
        if (StringUtils.isBlank(classCode)) {
            logProjectInstance().error("重计算运行状态补偿跳过，补偿Hash无法解析设备类型",
                    Pair.of("补偿Hash", trackingKey));
            return count;
        }

        Map<String, String> trackingMap = this.queryHashTrackingMap(trackingKey);
        if (MapUtils.isEmpty(trackingMap)) {
            return count;
        }

        for (Map.Entry<String, String> entry : trackingMap.entrySet()) {
            String objectId = entry.getKey();
            String transitionStatus = entry.getValue();
            int result = this.processDevice(objectId, classCode, transitionStatus, trackingKey);
            this.accumulate(count, result);
        }
        return count;
    }

    /**
     * 处理单个设备的补偿判断。
     *
     * <p>先校验补偿标记和当前{@code rcRunStatus}是否仍处于过渡态，
     * 再根据过渡方向判断{@code runStatus}是否已到达目标稳定态。
     *
     * @param objectId         设备ID
     * @param classCode        设备类型编码
     * @param transitionStatus 补偿标记中记录的过渡状态
     * @param trackingKey      补偿跟踪Hash key
     * @return 处理结果，见{@code RESULT_*}常量
     */
    private int processDevice(String objectId, String classCode, String transitionStatus, String trackingKey) {
        transitionStatus = this.normalizeStatus(transitionStatus);
        String latestTransitionStatus = this.normalizeStatus(this.queryHashTrackingStatus(trackingKey, objectId));
        if (!StringUtils.equals(transitionStatus, latestTransitionStatus)) {
            logProjectInstance().info("重计算运行状态补偿跳过，补偿标记已变化",
                    Arrays.asList(Pair.of("设备", objectId), Pair.of("设备类型", classCode),
                            Pair.of("快照过渡状态", transitionStatus), Pair.of("当前过渡状态", latestTransitionStatus)));
            return RESULT_SKIPPED;
        }

        if (this.isTransitionStatus(transitionStatus)) {
            FeedbackInfoMsgMqttVO currentRcRunStatusMsg = this.queryCurrentRcRunStatusMsg(objectId);
            if (currentRcRunStatusMsg == null || StringUtils.isBlank(currentRcRunStatusMsg.getDataValue())) {
                logProjectInstance().error("重计算运行状态补偿跳过，未查询到当前重计算运行状态",
                        Arrays.asList(Pair.of("设备", objectId), Pair.of("设备类型", classCode),
                                Pair.of("过渡状态", transitionStatus)));
                return RESULT_SKIPPED;
            }

            String currentRcRunStatus = this.normalizeStatus(currentRcRunStatusMsg.getDataValue());
            if (!this.isTransitionStatus(currentRcRunStatus)) {
                // 实时缓存已经不是过渡态，说明补偿已无必要，仅清理遗留标记。
                if (this.removeHashTrackingIfMatched(trackingKey, objectId, transitionStatus)) {
                    logProjectInstance().info("清理已结束的重计算运行状态补偿标记",
                            Arrays.asList(Pair.of("设备", objectId), Pair.of("当前重计算运行状态", currentRcRunStatus),
                                    Pair.of("过渡状态", transitionStatus)));
                    return RESULT_REMOVED;
                }
                return RESULT_SKIPPED;
            }

            if (!transitionStatus.equals(currentRcRunStatus)) {
                // 设备在补偿周期内可能从开启中切到关闭中，按当前过渡方向重新等待对应稳定态。
                transitionStatus = currentRcRunStatus;
                this.putHashTracking(trackingKey, objectId, transitionStatus);
            }
        } else {
            if (this.removeHashTrackingIfMatched(trackingKey, objectId, transitionStatus)) {
                logProjectInstance().info("清理无效的重计算运行状态补偿标记",
                        Arrays.asList(Pair.of("设备", objectId), Pair.of("过渡状态", transitionStatus)));
                return RESULT_REMOVED;
            }
            return RESULT_SKIPPED;
        }

        String runStatus = this.normalizeStatus(iotDataCacheService.queryIotRealData(
                objectId, InfoCodeEnum.RUN_STATUS.getCode(), null));
        if (StringUtils.isBlank(runStatus)) {
            logProjectInstance().info("重计算运行状态补偿跳过，运行状态为空",
                    Arrays.asList(Pair.of("设备", objectId), Pair.of("过渡状态", transitionStatus)));
            return RESULT_SKIPPED;
        }

        String targetRcRunStatus = this.resolveTargetRcRunStatus(transitionStatus, runStatus);
        if (targetRcRunStatus == null) {
            logProjectInstance().info("重计算运行状态补偿跳过，过渡未完成",
                    Arrays.asList(Pair.of("设备", objectId), Pair.of("过渡状态", transitionStatus),
                            Pair.of("运行状态", runStatus)));
            return RESULT_SKIPPED;
        }

        if (this.updateRcRunStatus(objectId, classCode, transitionStatus, runStatus, targetRcRunStatus)) {
            if (this.removeHashTrackingIfMatched(trackingKey, objectId, transitionStatus)) {
                return RESULT_UPDATED;
            }
            logProjectInstance().info("重计算运行状态补偿已写入，补偿标记已变化，跳过清理",
                    Arrays.asList(Pair.of("设备", objectId), Pair.of("设备类型", classCode),
                            Pair.of("过渡状态", transitionStatus)));
        }
        return RESULT_SKIPPED;
    }

    /**
     * 根据过渡方向和运行状态计算目标{@code rcRunStatus}。
     *
     * <p>开启中只接受运行状态=运行，关闭中只接受运行状态=停止；方向不匹配时继续等待。
     *
     * @param transitionStatus 过渡状态
     * @param runStatus        运行状态
     * @return 目标{@code rcRunStatus}；过渡方向与运行状态不匹配时返回{@code null}
     */
    private String resolveTargetRcRunStatus(String transitionStatus, String runStatus) {
        if (OPENING.equals(transitionStatus) && RUNNING.equals(runStatus)) {
            return RUNNING;
        }
        if (CLOSING.equals(transitionStatus) && STOPPED.equals(runStatus)) {
            return STOPPED;
        }
        return null;
    }

    /**
     * 写入并推送补偿后的{@code rcRunStatus}。
     *
     * <p>先写入IOT实时缓存，再重新查询缓存中的完整消息发送SSE。写入方法会补充数据时间和过期时间，
     * 因此不能使用当前方法自行构造的不完整消息作为推送内容。
     *
     * @param objectId          设备ID
     * @param classCode         设备类型编码
     * @param transitionStatus  过渡状态
     * @param runStatus         运行状态
     * @param targetRcRunStatus 目标{@code rcRunStatus}
     * @return {@code true}表示写入和SSE推送成功，{@code false}表示写入、查询或SSE推送失败
     */
    private boolean updateRcRunStatus(String objectId, String classCode, String transitionStatus, String runStatus,
                                       String targetRcRunStatus) {
        try {
            FeedbackInfoMsgMqttVO msgMqttVO = this.buildRcRunStatusMsg(objectId, classCode, targetRcRunStatus);
            iotDataCacheService.insert(msgMqttVO, null);
            FeedbackInfoMsgMqttVO completeMsgMqttVO = this.queryCurrentRcRunStatusMsg(objectId);
            if (completeMsgMqttVO == null) {
                logProjectInstance().error("[重计算运行状态-补偿] 写入后未查询到完整实时数据，跳过SSE推送并保留补偿标记",
                        Arrays.asList(Pair.of("设备", objectId), Pair.of("设备类型", classCode),
                                Pair.of("过渡状态", transitionStatus), Pair.of("运行状态", runStatus),
                                Pair.of("目标重计算运行状态", targetRcRunStatus)));
                return false;
            }

            String completeRcRunStatus = this.normalizeStatus(completeMsgMqttVO.getDataValue());
            if (!targetRcRunStatus.equals(completeRcRunStatus)) {
                logProjectInstance().error("[重计算运行状态-补偿] 写入后查询结果与补偿目标不一致，跳过SSE推送并保留补偿标记",
                        Arrays.asList(Pair.of("设备", objectId), Pair.of("设备类型", classCode),
                                Pair.of("过渡状态", transitionStatus), Pair.of("运行状态", runStatus),
                                Pair.of("目标重计算运行状态", targetRcRunStatus),
                                Pair.of("当前重计算运行状态", completeRcRunStatus)));
                return false;
            }

            sseHandlerContext.getStrategy(SseEmitterEventEnum.IOT_DATA_CHANGE).doOperation(
                    projectCfg.getProjectId(), completeMsgMqttVO);

            logProjectInstance().info("[重计算运行状态-补偿] SSE推送成功",
                    Arrays.asList(Pair.of("设备", objectId), Pair.of("设备类型", classCode),
                            Pair.of("过渡状态", transitionStatus), Pair.of("运行状态", runStatus),
                            Pair.of("重计算运行状态", targetRcRunStatus)));
            return true;
        } catch (Exception e) {
            logProjectInstance().error("[重计算运行状态-补偿] 写入或SSE推送失败", e,
                    Arrays.asList(Pair.of("设备", objectId), Pair.of("设备类型", classCode),
                            Pair.of("过渡状态", transitionStatus), Pair.of("运行状态", runStatus),
                            Pair.of("目标重计算运行状态", targetRcRunStatus)));
            return false;
        }
    }

    /**
     * 构造用于写入缓存的{@code rcRunStatus}补偿消息。
     *
     * @param objectId          设备ID
     * @param classCode         设备类型编码
     * @param targetRcRunStatus 目标{@code rcRunStatus}
     * @return 缓存写入消息
     */
    private FeedbackInfoMsgMqttVO buildRcRunStatusMsg(String objectId, String classCode, String targetRcRunStatus) {
        FeedbackInfoMsgMqttVO msgMqttVO = new FeedbackInfoMsgMqttVO();
        msgMqttVO.setGroupCode(projectCfg.getGroupCode());
        msgMqttVO.setProjectId(projectCfg.getProjectId());
        msgMqttVO.setObjectId(objectId);
        msgMqttVO.setClassCode(classCode);
        msgMqttVO.setInfoCode(InfoCodeEnum.RC_RUN_STATUS.getCode());
        msgMqttVO.setDataValue(targetRcRunStatus);
        msgMqttVO.setDataStatus(1);
        msgMqttVO.setInvalid(0);
        msgMqttVO.setType(EnumIotDataType.FEEDBACK.getCode());
        msgMqttVO.setTraceData("COMPENSATION");
        return msgMqttVO;
    }

    /**
     * 查询当前缓存中的{@code rcRunStatus}消息。
     *
     * <p>调用{@link IotDataCacheService#insert(FeedbackInfoMsgMqttVO, RcInfoCodeCfg)}
     * 后会补充数据时间和过期时间，推送前需要重新查询缓存中的完整消息。
     *
     * @param objectId 设备ID
     * @return 当前缓存中的{@code rcRunStatus}消息；未查询到时返回{@code null}
     * @since v7.260402.0
     */
    private FeedbackInfoMsgMqttVO queryCurrentRcRunStatusMsg(String objectId) {
        return iotDataCacheService.queryIotRealDataRedisVO(objectId, InfoCodeEnum.RC_RUN_STATUS.getCode());
    }

    /**
     * 判断是否为需要补偿跟踪的过渡状态。
     *
     * @param value 状态值
     * @return {@code true}表示开启中或关闭中，{@code false}表示其他状态
     */
    private boolean isTransitionStatus(String value) {
        return OPENING.equals(value) || CLOSING.equals(value);
    }

    /**
     * 统一状态值格式。
     *
     * <p>部分缓存值可能以小数字符串保存，例如{@code 1.0}、{@code 2.0}；
     * 补偿逻辑统一按整数状态码比较。
     *
     * @param value 原始状态值
     * @return 归一化后的状态值
     */
    private String normalizeStatus(String value) {
        String trimmedValue = StringUtils.trim(value);
        if (StringUtils.isBlank(trimmedValue)) {
            return trimmedValue;
        }
        switch (trimmedValue) {
            case "0.0":
                return STOPPED;
            case "1.0":
                return RUNNING;
            case "2.0":
                return OPENING;
            case "3.0":
                return CLOSING;
            default:
                return trimmedValue;
        }
    }

    /**
     * 查询补偿跟踪Hash中的所有设备标记。
     *
     * @param trackingKey 补偿跟踪Hash key
     * @return 设备补偿标记，key为设备ID，value为过渡状态
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> queryHashTrackingMap(String trackingKey) {
        return (Map<String, String>) redisCacheUtil.getRedisTemplate().opsForHash().entries(trackingKey);
    }

    /**
     * 查询单个设备当前的补偿跟踪状态。
     *
     * @param trackingKey 补偿跟踪Hash key
     * @param objectId    设备ID
     * @return 当前补偿跟踪状态；未查询到时返回{@code null}
     * @since v7.260402.0
     */
    @SuppressWarnings("unchecked")
    private String queryHashTrackingStatus(String trackingKey, String objectId) {
        Object value = redisCacheUtil.getRedisTemplate().opsForHash().get(trackingKey, objectId);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 写入单个设备的补偿跟踪标记。
     *
     * @param trackingKey      补偿跟踪Hash key
     * @param objectId         设备ID
     * @param transitionStatus 过渡状态
     */
    @SuppressWarnings("unchecked")
    private void putHashTracking(String trackingKey, String objectId, String transitionStatus) {
        redisCacheUtil.getRedisTemplate().opsForHash().put(trackingKey, objectId, transitionStatus);
    }

    /**
     * 按期望状态删除单个设备的补偿跟踪标记。
     *
     * <p>定时任务处理的是Hash快照，删除前需要再次确认标记未被实时链路更新，避免误删新的过渡方向。
     *
     * @param trackingKey              补偿跟踪Hash key
     * @param objectId                 设备ID
     * @param expectedTransitionStatus 期望删除的过渡状态
     * @return {@code true}表示已删除，{@code false}表示当前标记已变化
     * @since v7.260402.0
     */
    @SuppressWarnings("unchecked")
    private boolean removeHashTrackingIfMatched(String trackingKey, String objectId, String expectedTransitionStatus) {
        String currentTransitionStatus = this.normalizeStatus(this.queryHashTrackingStatus(trackingKey, objectId));
        if (!StringUtils.equals(expectedTransitionStatus, currentTransitionStatus)) {
            return false;
        }
        redisCacheUtil.getRedisTemplate().opsForHash().delete(trackingKey, objectId);
        return true;
    }

    /**
     * 从补偿Hash key中解析设备类型。
     *
     * @param trackingKey 补偿跟踪Hash key
     * @return 设备类型编码；解析失败时返回{@code null}
     */
    private String parseClassCode(String trackingKey) {
        String keyPrefix = RedisKey.APC_RC_RUNSTATUS_TRACKING_HASH_KEY + ":" + projectCfg.getProjectId() + ":";
        if (trackingKey != null && trackingKey.startsWith(keyPrefix)) {
            return trackingKey.substring(keyPrefix.length());
        }
        int index = trackingKey == null ? -1 : trackingKey.lastIndexOf(":");
        return index < 0 ? null : trackingKey.substring(index + 1);
    }

    /**
     * 汇总单设备处理结果。
     *
     * @param count  处理结果计数，数组下标依次表示补偿成功数、跳过数、清理标记数
     * @param result 单设备处理结果，见{@code RESULT_*}常量
     */
    private void accumulate(int[] count, int result) {
        if (RESULT_UPDATED == result) {
            count[COUNT_UPDATED_INDEX]++;
        } else if (RESULT_REMOVED == result) {
            count[COUNT_REMOVED_INDEX]++;
        } else {
            count[COUNT_SKIPPED_INDEX]++;
        }
    }

    @Override
    public String logTag() {
        return "XXL-Job定时任务-重计算运行状态补偿";
    }
}
