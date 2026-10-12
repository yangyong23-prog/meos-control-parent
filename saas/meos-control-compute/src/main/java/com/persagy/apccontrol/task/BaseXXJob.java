package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.utils.dateUtil.CronTimeValidator;
import com.persagy.apccontrol.common.outside.MidPermissionService;
import com.persagy.apccontrol.common.outside.dto.GroupDTO;
import com.persagy.cloud.timezone.TimeZoneManager;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author yuyao
 * @date 2022/9/7 17:40
 * 按照项目分片
 */
@Slf4j
public class BaseXXJob implements LogComponent {

    @Resource
    private MidPermissionService midPermissionService;
    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private RcBasicsConfigService rcBasicsConfigService;

    public @NonNull ReturnT<String> processHandle(@NonNull TecXxJobHandler handler) {
        String param = XxlJobHelper.getJobParam();
        
        if (StringUtils.isEmpty(param)) {
            XxlJobHelper.log("[{}] ------------------------- 缺少任务表达式参数 -------------------------", this.logTag());
            return ReturnT.FAIL;
        }
        return this.processHandle(handler, param, true, false);
    }

    /**
     * 执行不校验 cron 表达式的项目分片任务。
     *
     * <p>用于任务参数承载业务过滤条件的 XXL-JOB。该类任务仍复用集团、项目和分片遍历能力，
     * 但不会把任务参数当成 cron 表达式做多时区触发时间校验；单项目执行失败不会阻断同集团后续项目，
     * 任务结束时会统一标记失败，便于运维检查日志后重跑。</p>
     *
     * @param handler 任务处理器
     * @return 执行结果
     * @author JiYinchuan
     * @since v7.260701.0
     */
    protected @NonNull ReturnT<String> processHandleWithoutTimeCheck(@NonNull TecXxJobHandler handler) {
        return this.processHandleWithoutTimeCheck(handler, XxlJobHelper.getJobParam());
    }

    /**
     * 使用指定参数执行不校验 cron 表达式的项目分片任务。
     *
     * <p>非 XXL-JOB 线程可以通过该入口复用集团、项目遍历和失败汇总逻辑，不需要手动构造
     * {@link com.xxl.job.core.context.XxlJobContext}。</p>
     *
     * @param handler 任务处理器
     * @param param   任务参数
     * @return 执行结果
     * @author JiYinchuan
     * @since v7.260701.0
     */
    protected @NonNull ReturnT<String> processHandleWithoutTimeCheck(
            @NonNull TecXxJobHandler handler, @Nullable String param) {
        return this.processHandle(handler, param, false, true);
    }

    private @NonNull ReturnT<String> processHandle(
            @NonNull TecXxJobHandler handler,
            @Nullable String param,
            boolean checkTriggerTime,
            boolean failOnProjectFailure) {
        
        XxlJobHelper.log("[{}] ------------------------- 任务执行开始 -------------------------", this.logTag());
        StopWatch stopWatch = StopWatch.createStarted();
        // 分片参数 获取集团信息
        List<GroupDTO> groupDTO = this.midPermissionService.queryValidGroupList();
        if (CollectionUtils.isEmpty(groupDTO)) {
            XxlJobHelper.log("[{}] ------------------------- 任务执行结束,集团列表为空 [{}ms] -------------------------",
                    this.logTag(), stopWatch.getTime());
            return ReturnT.SUCCESS;
        }
        AtomicBoolean isAllFailed = new AtomicBoolean(true);
        AtomicBoolean hasProjectFailed = new AtomicBoolean(false);
        groupDTO.forEach(group -> {
            this.logInstance().log(this.traversalProgressLogLevel(),
                    "[定时任务开始执行]:当前集团", Pair.of("groupCode", group.getCode()));
            try {
                ContextUtil.setGroupCode(group.getCode());
                ContextUtil.setTenant(group.getCode());
                //校验集团库是否存在
                if (!this.checkDataBase()) {
                    this.logInstance().log(this.traversalProgressLogLevel(),
                            "[定时任务开始执行]:集团不存在跳过执行",
                            Pair.of("groupCode", group.getCode()));
                    return;
                }
                StopWatch groupSw = StopWatch.createStarted();
                this.handleGroup(handler, param, checkTriggerTime, failOnProjectFailure, hasProjectFailed);
                groupSw.stop();
                this.logInstance().log(this.traversalProgressLogLevel(), "当前集团执行成功",
                        Arrays.asList(Pair.of("groupCode", group.getCode()),
                                Pair.of("耗时ms", groupSw.getTime())));
                isAllFailed.set(false);
            } catch (Exception e) {
                this.logInstance().error("[定时任务处理失败]:当前集团", e, Pair.of("groupCode", group.getCode()));
            } finally {
                ContextUtil.unloadContext();
            }
        });
        stopWatch.stop();
        XxlJobHelper.log("[{}] ------------------------- 任务执行结束 [{}ms] -------------------------",
                this.logTag(), stopWatch.getTime());
        if (isAllFailed.get() || failOnProjectFailure && hasProjectFailed.get()) {
            XxlJobHelper.handleFail();
            if (failOnProjectFailure && hasProjectFailed.get()) {
                return ReturnT.FAIL;
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 获取集团下的项目 进行分发
     *
     * @param handler handler
     * @author yuyao
     * @date 2022/8/15 11:00
     * @Return void
     */
    private void handleGroup(
            @NonNull TecXxJobHandler handler,
            @Nullable String param,
            boolean checkTriggerTime,
            boolean failOnProjectFailure,
            @NonNull AtomicBoolean hasProjectFailed) {
        String groupCode = ContextUtil.getGroupCode();
        List<ProjectDTO> projectList = this.midPermissionService.queryIsInUseProjectDTOList(groupCode);
        if (CollectionUtils.isEmpty(projectList)) {
            this.logInstance().log(this.traversalProgressLogLevel(),
                    "该集团无项目信息-跳过", Pair.of("groupCode", groupCode));
            return;
        }

        try {
            int total = XxlJobHelper.getShardTotal();
            int index = XxlJobHelper.getShardIndex();
            this.handleProject(handler, total, index, groupCode, projectList, param, checkTriggerTime,
                    failOnProjectFailure, hasProjectFailed);
        } catch (Exception e) {
            this.logInstance().error("[定时任务处理失败]:当前集团项目", e, Pair.of("groupCode", groupCode));
            throw e;
        }

    }

    /**
     * 按项目进行 分片
     *
     * @param handler     handler
     * @param total       执行器数量
     * @param index       当前分片
     * @param groupCode   groupCode
     * @param projectList projectList
     * @author yuyao
     * @date 2022/8/15 11:01
     * @Return void
     */
    private void handleProject(
            @NonNull TecXxJobHandler handler,
            int total,
            int index,
            @NonNull String groupCode,
            @NonNull List<ProjectDTO> projectList,
            @Nullable String param,
            boolean checkTriggerTime,
            boolean failOnProjectFailure,
            @NonNull AtomicBoolean hasProjectFailed) {
        int size = projectList.size();
        List<Future<String>> objects = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ProjectDTO projectDTO = projectList.get(i);
            //时间校验
            if (checkTriggerTime && !this.timeCheck(groupCode, projectDTO, param)) {
                this.logInstance().log(this.traversalProgressLogLevel(),
                        "项目时间未到-跳过", Pair.of("projectId", projectDTO.getProjectId()));
                continue;
            }
            
            if (i % total == index || total == -1) {
                //当前计算
                this.logInstance().log(this.traversalProgressLogLevel(), "开始执行任务",
                        Arrays.asList(Pair.of("groupCode", groupCode),
                                Pair.of("projectId", projectDTO.getProjectId())));
                if (failOnProjectFailure) {
                    try {
                        Future<String> stringFuture = handler.processAsync(groupCode, projectDTO);
                        objects.add(stringFuture);
                    } catch (Exception e) {
                        hasProjectFailed.set(true);
                        this.logInstance().error("[定时任务处理失败]:当前项目", e, Arrays.asList(
                                Pair.of("groupCode", groupCode),
                                Pair.of("projectId", projectDTO.getProjectId())));
                    }
                } else {
                    Future<String> stringFuture = handler.processAsync(groupCode, projectDTO);
                    objects.add(stringFuture);
                }
            } else {
                this.logInstance().log(this.traversalProgressLogLevel(),
                        "忽略", Pair.of("projectId", projectDTO.getProjectId()));
            }

        }
        for (Future<String> object : objects) {
            try {
                object.get();
            } catch (Exception e) {
                hasProjectFailed.set(true);
                this.logInstance().error("object.get() 失败", e);
            }
        }
    }
    
    /**
     * 多时区处理 时间校验
     * 校验是否到了表达式执行的时间 如果没到就跳过不执行
     *
     * @param cron
     * @param groupCode
     * @param projectDTO
     * @return
     */
    private boolean timeCheck(String groupCode, ProjectDTO projectDTO, String cron) {
        //param 是时间表达式
        ZoneId zoneId = TimeZoneManager.getProjectZoneIdDefaultConfigTimeZone(projectDTO.getProjectId());
        ZonedDateTime testTime = ZonedDateTime.now(zoneId);
        boolean timeCheck = false;
        //项目多时区判断,判断是否在执行时间 再才执行
        try {
            timeCheck = CronTimeValidator.isTriggerTime(cron, testTime);
        } catch (ParseException e) {
            logInstance().warn("时区处理错误", Arrays.asList(Pair.of("groupCode", groupCode), Pair.of("projectId",
                    projectDTO.getProjectId())));
        }
        return timeCheck;
    }
    
    
    /**
     * 校验如果数据库不存在就直接跳过
     *
     * @author yuyao
     * @date 2022/7/19 15:17
     * @Return boolean
     */
    private boolean checkDataBase() {

        if (redisCacheUtil.get(RedisKey.NON_GROUP_CODE + ContextUtil.getGroupCode()) != null) {
            logInstance().warn("集团不存在，跳过执行", Pair.of("groupCode", ContextUtil.getGroupCode()));
            return false;
        }

        try {
            rcBasicsConfigService.lambdaQuery().last(CommonConst.LIMIT_ONE).one();
            return true;
        } catch (Exception e) {
            logInstance().error("集团库不存在，跳过执行", e, Pair.of("groupCode", ContextUtil.getGroupCode()));
            //缓存1天
            redisCacheUtil.setEx(RedisKey.NON_GROUP_CODE + ContextUtil.getGroupCode(), ContextUtil.getGroupCode(), 86400L);
            return false;
        }

    }

    /**
     * 集团和项目遍历进度日志级别。
     *
     * <p>默认保留历史 INFO 行为；拥有项目终态日志的批量初始化任务可以覆盖为 DEBUG，
     * 避免分片忽略和业务过滤前的项目开始日志灌入 XXL-JOB。</p>
     *
     * @return 遍历进度日志级别
     * @since v7.260701.0
     */
    protected @NonNull Level traversalProgressLogLevel() {
        return Level.INFO;
    }


    @Override
    public String logTag() {
        return "xxl-job任务基础类";
    }
}


