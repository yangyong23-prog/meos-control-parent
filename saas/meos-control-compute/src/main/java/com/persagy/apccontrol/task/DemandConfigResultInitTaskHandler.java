package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.message.CacheTriggerEvent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRefreshRepository;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.context.ContextUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.task.param.DemandConfigResultInitTaskParam;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 需求配置结果初始化任务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component("demandConfigResultInitTaskHandler")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigResultInitTaskHandler extends BaseXXJob implements TecXxJobHandler {

    /**
     * 初始化任务参数非法时使用的国际化消息键。
     *
     * @since v7.260701.0
     */
    private static final String INVALID_TASK_PARAM_MESSAGE_KEY =
            "key_e65c2d1cc2a644b493db084cc9226319";

    /**
     * 日志标签。
     */
    public static final String LOG_TAG = "需求配置结果初始化";

    private final @NonNull DemandConfigResultRefreshRepository demandConfigResultRefreshRepository;

    /**
     * 需求配置结果初始化任务入口。
     *
     * <p>该任务用于首次上线或存储边界调整后重建当前稳定结果，并从执行日写入开放快照；
     * 不回填执行日之前历史，不修复旧关闭快照。任务由运维在 XXL-JOB 控制台手动触发，入口复用
     * {@link TecXxJobHandler} 的集团、项目和 XXL-JOB 分片遍历能力，但不做 cron 时间校验；
     * 空参数表示全量初始化，如需灰度或重跑，可传入 groupCodes / projectIds 做范围过滤。
     * 可通过 demandTypes 参数指定只初始化特定需求类型，例如升级时只执行新增类型。</p>
     *
     * @return 执行结果
     * @since v7.260701.0
     */
    public @NonNull ReturnT<String> execute() {
        return super.processHandleWithoutTimeCheck(this);
    }

    @Override
    public @NonNull String getHandlerName() {
        return DemandConfigResultInitTaskHandler.LOG_TAG;
    }

    @Override
    public @NonNull Future<String> processAsync(@NonNull String groupCode, @NonNull ProjectDTO projectDTO) {
        this.process(groupCode, projectDTO);
        return new AsyncResult<>(this.getHandlerName() + "执行完成:" + groupCode + ";projectId="
                + projectDTO.getProjectId());
    }

    /**
     * 执行项目需求配置结果初始化。
     *
     * @param groupCode  集团编码
     * @param projectDTO 项目
     * @since v7.260701.0
     */
    @Override
    public void process(@NonNull String groupCode, @NonNull ProjectDTO projectDTO) {
        DemandConfigResultInitTaskParam param = this.parseDemandConfigResultInitTaskParam(XxlJobHelper.getJobParam());
        this.process(groupCode, projectDTO, param);
    }

    /**
     * 按初始化参数执行项目需求配置结果初始化。
     *
     * @param groupCode  集团编码
     * @param projectDTO 项目
     * @param param      初始化参数
     * @author JiYinchuan
     * @since v7.260701.0
     */
    private void process(
            @NonNull String groupCode,
            @NonNull ProjectDTO projectDTO,
            @NonNull DemandConfigResultInitTaskParam param) {
        if (this.skipProject(param, groupCode, projectDTO)) {
            return;
        }
        ContextUtils.initGroupCodeProjectId(groupCode, projectDTO.getProjectId());
        try {
            CacheLockKey.Key key = CacheLockKey.Trigger.cache();
            new CacheTriggerEvent().executeWithLock(this, key, () -> {
                for (DemandTemplateTypeEnum templateType : this.resolveTemplateTypes(param)) {
                    this.demandConfigResultRefreshRepository.refreshByTemplateType(templateType);
                }
            });
        } finally {
            ContextUtil.unloadContext();
        }
    }

    /**
     * 解析需求配置结果初始化任务参数。
     *
     * @param jobParam XXL-JOB 任务参数
     * @return 初始化任务参数
     * @throws BusinessException 任务参数不是合法 JSON 时抛出
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultInitTaskParam parseDemandConfigResultInitTaskParam(@Nullable String jobParam) {
        if (StringUtils.isBlank(jobParam)) {
            return new DemandConfigResultInitTaskParam();
        }
        DemandConfigResultInitTaskParam param = ObjectMapperUtils.readValueFailSafe(
                jobParam, DemandConfigResultInitTaskParam.class);
        if (param == null) {
            throw new BusinessException(
                    ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                    LocaleMessageUtil.getMessage(
                            DemandConfigResultInitTaskHandler.INVALID_TASK_PARAM_MESSAGE_KEY));
        }
        return param;
    }

    /**
     * 解析初始化任务需要执行的模板类型列表。
     *
     * <p>{@code demandTypes} 为空时初始化所有已支持类型；非空时只初始化指定需求类型下的模板，
     * 用于升级场景下避免重复执行已完成类型的全量初始化。</p>
     *
     * @param param 初始化任务参数
     * @return 需要执行的模板类型列表
     * @since v7.260801.0
     */
    private @NonNull List<DemandTemplateTypeEnum> resolveTemplateTypes(
            @NonNull DemandConfigResultInitTaskParam param) {
        Set<DemandTypeEnum> demandTypes = param.getDemandTypes();
        if (CollectionUtils.isEmpty(demandTypes)) {
            return this.demandConfigResultRefreshRepository.listSupportedTemplateTypes();
        }
        return this.demandConfigResultRefreshRepository.listSupportedTemplateTypes().stream()
                .filter(templateType -> demandTypes.contains(templateType.toDemandTypeEnum()))
                .collect(Collectors.toList());
    }

    /**
     * 判断是否跳过集团。
     *
     * @param param 初始化任务参数
     * @param groupCode 集团编码
     * @return 是否跳过
     * @since v7.260701.0
     */
    private boolean skipGroup(@NonNull DemandConfigResultInitTaskParam param, @NonNull String groupCode) {
        Set<String> groupCodes = param.getGroupCodes();
        return CollectionUtils.isNotEmpty(groupCodes)
                && !groupCodes.contains(groupCode);
    }

    /**
     * 判断是否跳过项目。
     *
     * @param param 初始化任务参数
     * @param groupCode 集团编码
     * @param projectDTO 项目
     * @return 是否跳过
     * @since v7.260701.0
     */
    private boolean skipProject(
            @NonNull DemandConfigResultInitTaskParam param,
            @NonNull String groupCode,
            @NonNull ProjectDTO projectDTO) {
        Set<String> projectIds = param.getProjectIds();
        return this.skipGroup(param, groupCode)
                || CollectionUtils.isNotEmpty(projectIds)
                && !projectIds.contains(projectDTO.getProjectId());
    }

    @Override
    public @NonNull String logTag() {
        return DemandConfigResultInitTaskHandler.LOG_TAG;
    }
}
