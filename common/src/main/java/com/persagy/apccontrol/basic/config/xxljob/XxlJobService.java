package com.persagy.apccontrol.basic.config.xxljob;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.redis.lock.annotationsupport.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: xxl-job service类
 * @Author: MAF
 * @Date: 2022/7/18
 */
@Component
@Slf4j
public class XxlJobService {

    @Resource
    XxlJobUtil xxlJobUtil;


    /**
     * 添加任务
     *
     * @param vo vo
     * @author yuyao
     * @date 2022/9/11 15:32
     * @Return java.lang.Integer
     */
    public Integer addJob(XxlJobVo vo) {
        return this.addJob(vo, "CRON", true);
    }

    /**
     * 添加无调度任务。
     *
     * <p>无调度任务只在 XXL-JOB 控制台手动触发，适用于上线初始化、一次性修复等不需要 cron 自动调度的场景。</p>
     *
     * @param vo 任务配置
     * @return 新增任务 ID；如果任务已存在则返回 {@code null}
     * @author JiYinchuan
     * @since v7.260701.0
     */
    public @Nullable Integer addNoneScheduleJob(@NonNull XxlJobVo vo) {
        return this.addJob(vo, "NONE", false, true);
    }

    /**
     * 添加任务。
     *
     * @param vo            任务配置
     * @param scheduleType  调度类型
     * @param startAfterAdd 新增后是否立即启动
     * @return 新增任务 ID；如果任务已存在则返回 {@code null}
     * @author JiYinchuan
     * @since v7.260701.0
     */
    private @Nullable Integer addJob(@NonNull XxlJobVo vo, @NonNull String scheduleType, boolean startAfterAdd) {
        return this.addJob(vo, scheduleType, startAfterAdd, false);
    }

    /**
     * 添加任务。
     *
     * @param vo               任务配置
     * @param scheduleType     调度类型
     * @param startAfterAdd    新增后是否立即启动
     * @param stopBeforeUpdate 更新前是否先停止
     * @return 新增任务 ID；如果任务已存在则返回 {@code null}
     * @author JiYinchuan
     * @since v7.260701.0
     */
    private @Nullable Integer addJob(
            @NonNull XxlJobVo vo, @NonNull String scheduleType,
            boolean startAfterAdd, boolean stopBeforeUpdate) {
        LinkedMultiValueMap<String, Object> map = this.buildJobParam(vo, scheduleType);
        // 查询任务是否 存在跳过
        JSONObject rw = this.xxlJobUtil.getJobInfoByJobExecutorHandler(vo.getJobHandle());
        if (rw != null) {
            log.info("任务已建立，{} 任务更新！", vo.getJobDesc());
            Integer jobId = rw.getInteger("id");
            if (stopBeforeUpdate && Integer.valueOf(1).equals(rw.getInteger("triggerStatus"))) {
                this.stopJob(jobId);
            }
            map.add("id", jobId);
            this.xxlJobUtil.updateJob(map);
            return null;
        }
        JSONObject result = this.xxlJobUtil.addJob(map);
        Integer xxlJobId = result.getInteger("content");
        if (null == xxlJobId) {
            log.error("自动创建任务失败：{}", vo.getJobDesc());
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), "自动创建任务失败");
        }
        log.warn("自动创建定时任务，{}--成功！", vo.getJobDesc());
        if (startAfterAdd) {
            // 添加后直接启动
            this.startJob(xxlJobId);
        }

        return xxlJobId;
    }

    /**
     * 构建 XXL-JOB 创建/更新参数。
     *
     * @param vo           任务配置
     * @param scheduleType 调度类型
     * @return XXL-JOB 表单参数
     * @author JiYinchuan
     * @since v7.260701.0
     */
    private @NonNull LinkedMultiValueMap<String, Object> buildJobParam(@NonNull XxlJobVo vo,
                                                                       @NonNull String scheduleType) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        String groupId = this.xxlJobUtil.getJobGroupIdByAppName();
        if (StringUtils.isBlank(groupId)) {
            // 添加执行器
            this.xxlJobUtil.addGroup();
            // 再次获取groupId
            groupId = this.xxlJobUtil.getJobGroupIdByAppName();
            if (StringUtils.isBlank(groupId)) {
                throw new BusinessException("获取执行器失败", "获取执行器失败");
            }
        }

        map.add("jobGroup", groupId);
        map.add("jobDesc", vo.getJobDesc());
        map.add("author", "tec_sys");
        map.add("scheduleType", scheduleType);
        map.add("glueType", "BEAN");
        // 路由策略： 一致性hash
        map.add("executorRouteStrategy", "CONSISTENT_HASH");
        // 调度过期策略 什么都不做
        map.add("misfireStrategy", "DO_NOTHING");
        map.add("executorBlockStrategy", vo.getExecutorBlockStrategy());
        map.add("scheduleConf", "NONE".equals(scheduleType) ? "" : vo.getScheduleConf());
        map.add("executorHandler", vo.getJobHandle());
        map.add("executorParam", vo.getExecutorParam());
        // map.add("alarmEmail", "yuyao@persagy.com");
        map.add("childJobId", null);
        map.add("executorTimeout", "0");
        map.add("executorFailRetryCount", vo.getExecutorFailRetryCount());
        map.add("glueRemark", "");
        map.add("glueSource", "");
        return map;
    }

    /**
     * 停止任务
     *
     * @param id
     */
    public void stopJob(Integer id) {
        xxlJobUtil.stopJob(id);
    }

    /**
     * 启动任务
     *
     * @param id
     */
    public void startJob(Integer id) {
        xxlJobUtil.startJob(id);
    }

    /**
     * 触发作业
     *
     * @param jobDesc 任务描述
     */
    @RedisLock(lockName = CommonConst.LOCK_PREFIX + "triggerJob", param = "#jobDesc")
    public void triggerJob(String jobDesc) {
        log.info(">>>>>>> 执行xxlJob任务：{}", jobDesc);
        JSONObject jobInfoByJobDesc = xxlJobUtil.getJobInfoByJobDesc(jobDesc);
        if (jobInfoByJobDesc == null) {
            return;
        }
        Integer jobId = jobInfoByJobDesc.getInteger("id");
        String executorParam = jobInfoByJobDesc.getString("executorParam");
        xxlJobUtil.triggerJob(jobId, executorParam);
    }

    /**
     * 移除任务
     *
     * @param id
     */
    public void removeJob(Integer id) {
        if (id == null) {
            return;
        }
        xxlJobUtil.removeJob(id);
    }


    /**
     * 获取所有任务列表
     */
    public List<Integer> getTaskIdsByAppName() {
        return xxlJobUtil.getTaskIdsByAppName();
    }

    /**
     * 获取所有任务列表
     */
    public Map<String, Integer> getTaskByAppName() {
        return xxlJobUtil.getTaskByAppName();
    }


    /**
     * 移除不存在的任务
     *
     * @param list 配置任务列表
     */
    public void removeJob(List<XxlJobVo> list) {
        String groupId = xxlJobUtil.getJobGroupIdByAppName();
        if (StringUtils.isBlank(groupId)) {
            // 添加执行器
            xxlJobUtil.addGroup();
            // 再次获取groupId
            groupId = xxlJobUtil.getJobGroupIdByAppName();
            if (StringUtils.isBlank(groupId)) {
                throw new BusinessException("获取执行器失败", "获取执行器失败");
            }
        }
        Map<String, Integer> ids = getTaskByAppName();
        List<String> retainTask = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list) && !ids.isEmpty()) {
            list.forEach(item -> {
                if (ids.containsKey(item.getJobHandle())) {
                    retainTask.add(item.getJobHandle());
                }
            });
        }
        ids.forEach((k, v) -> {
            if (!retainTask.contains(k)) {
                removeJob(v);
            }
        });
    }

}
