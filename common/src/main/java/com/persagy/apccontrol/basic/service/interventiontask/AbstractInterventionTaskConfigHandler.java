package com.persagy.apccontrol.basic.service.interventiontask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskTimeDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionTaskConfig;
import com.persagy.apccontrol.basic.model.enumtype.InterventionTaskConfigDataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogKeyWordEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigSaveOrUpdateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.basic.resource.intervention.InterventionTaskConfigResource;
import com.persagy.apccontrol.basic.service.runninglog.category.RunningLogAddService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 干预任务配置服务
 */
public abstract class AbstractInterventionTaskConfigHandler implements InterventionTaskConfigHandler {

    @Resource
    protected PaasDataTransferClientService paasDataTransferClientService;
    @Resource
    InterventionTaskConfigResource interventionTaskConfigService;
    @Resource
    DtpRwdClientService dtpRwdClientService;
    @Resource
    ObjectMapper objectMapper;
    @Resource
    RunningLogAddService runningLogAddService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterventionTaskConfigSaveOrUpdateVO saveOrUpdate(InterventionTaskConfigDTO dto) {

        // 保存
        List<InterventionTaskConfig> taskConfigList = interventionTaskConfigService.toEntity(dto);
        // 存在任务 ID 就把数据进行覆盖 否则直接新增
        if (dto.getTaskId() != null) {
            del(dto.getTaskId(), false);
        }
        // 批量保存
        interventionTaskConfigService.saveOrUpdateBatch(taskConfigList);

        // 保存日志
        InterventionTaskConfig cfg = taskConfigList.get(0);
        if (dto.getTaskId() != null) {
            addLogMsg(cfg, RunningLogKeyWordEnum.UPDATED);
        } else {
            addLogMsg(cfg, RunningLogKeyWordEnum.ADD);
        }

        // 同步上云
        sendCloud(PaasDataTransferParam.of(DataTypeEnum.ADD, taskConfigList));
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(Long taskId, boolean logRecord) {

        // 取出所有 id 逐个删除
        List<InterventionTaskConfig> cfgIdList = interventionTaskConfigService.lambdaQuery()
                .eq(InterventionTaskConfig::getProjectId, ContextUtil.getProjectId())
                .eq(InterventionTaskConfig::getTaskId,
                        taskId).list();

        if (CollectionUtils.isEmpty(cfgIdList)) {
            return;
        }

        // 删除配置
        interventionTaskConfigService.delByTaskId(taskId);

        List<PaasDataTransferParam<InterventionTaskConfig>> dataList = new ArrayList<>();
        for (InterventionTaskConfig cfgId : cfgIdList) {
            InterventionTaskConfig data = new InterventionTaskConfig();
            data.setId(cfgId.getId());
            dataList.addAll(PaasDataTransferParam.of(DataTypeEnum.DELETE, data));

        }
        // 同步上云
        sendCloud(dataList);

        if (logRecord) {
            // 保存日志
            addLogMsg(cfgIdList.get(0), RunningLogKeyWordEnum.DEL);
        }
    }

    /**
     * 默认直接查询数据数据返回,返回数据如果需要特殊处理的由子类实现
     * @param queryParam
     * @return
     */
    @Override
    public List<InterventionTaskConfigVO> queryList(InterventionTaskConfigQueryParam queryParam) {
        return interventionTaskConfigService.queryListByVO(queryParam.getRunningMonitorType(),
                queryParam.getRunningMonitorSecondType(), queryParam.getInstanceType(),
                queryParam.getInstanceIds(),
                queryParam.getBeginTime(),
                queryParam.getEndTime());

    }


    /**
     * 发送
     *
     * @param data 数据
     * @since 7.250403.0
     */
    <T extends BaseAssignIdDO> void sendCloud(List<PaasDataTransferParam<T>> data) {
        CompletableFuture.runAsync(() -> paasDataTransferClientService.syncDbDataToCloud(data), BusinessThreadPoolExecutor.ttlExecutor());
    }

    /**
     * 添加日志
     *
     * @param cfg
     * @param logKeyWord
     */
    abstract void addLogMsg(InterventionTaskConfig cfg, RunningLogKeyWordEnum logKeyWord);

    /**
     * 数据校验
     *
     * @param dto
     * @param t
     * @param <T>
     * @return
     */
    public <T extends InterventionTaskConfigDataDTO> InterventionTaskConfigSaveOrUpdateVO check(InterventionTaskConfigDTO dto, Class<T> t) {
        // 数据校验
        dto.getConfigData().forEach(cfg -> {
            // Json 转对象 转为对象
            T cfgDto = objectMapper.convertValue(cfg, t);
            cfgDto.check();
        });
        // 校验开始结束时间都必须大于当前时间
        dto.getTimeSlot().forEach(time -> {
            ParameterCheck.isTrue(DateUtils.now().isAfter(time.getBeginTime()), "开始时间不能小于当前时间");
            ParameterCheck.isTrue(DateUtils.now().isAfter(time.getEndTime()), "结束时间不能小于当前时间");
        });

        // 校验 时段是否有配置 空间纬度校验
        List<InterventionTaskConfig> cfgList = interventionTaskConfigService.queryListByTypeAndInstanceIds(dto.getRunningMonitorType(),
                dto.getRunningMonitorSecondType(), InterventionTaskConfigDataTypeEnum.RELATION,
                dto.getConfigObject().getInstanceType(), dto.getConfigObject().getInstanceIds());
        InterventionTaskConfigSaveOrUpdateVO vo = new InterventionTaskConfigSaveOrUpdateVO();
        Set<String> idempotentSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(cfgList)) {
            // 判断时段是否有重复
            List<InterventionTaskConfig> checkCfgList = interventionTaskConfigService.toEntity(dto);
            checkCfgList.forEach(cfgDto -> cfgList.forEach(cfg -> {
                // 如果是编辑 排除自生配置进行校验
                if (cfg.getTaskId().equals(cfgDto.getTaskId())) {
                    return;
                }
                if (cfg.getInstanceId().equals(cfgDto.getInstanceId())) {
                    // 左闭右开区间判断
                    boolean isOverlap = !(cfg.getEndTime().isBefore(cfgDto.getBeginTime())  // cfg结束 <= dto开始 → 不重叠
                            || cfg.getBeginTime().isAfter(cfgDto.getEndTime()))            // cfg开始 >= dto结束 → 不重叠
                            // 新增：检查cfgDto是否完全包含cfg区间
                            || (cfgDto.getBeginTime().isBefore(cfg.getBeginTime())
                            && cfgDto.getEndTime().isAfter(cfg.getEndTime()));
                    // 排除相邻场景
                    boolean isAdjacent = cfg.getEndTime().equals(cfgDto.getBeginTime())     // cfg结束 = dto开始
                            || cfg.getBeginTime().equals(cfgDto.getEndTime());              // cfg开始 = dto结束

                    if (isOverlap && !isAdjacent) {
                        String idempotentKey = cfgDto.getBeginTime().toString() + cfgDto.getEndTime().toString();
                        if (idempotentSet.contains(idempotentKey)) {
                            // 时段已经添加过就跳过
                            return;
                        }
                        idempotentSet.add(idempotentKey);

                        vo.getRepetitionTimeSlot().add(new InterventionTaskTimeDTO(cfgDto.getBeginTime(),
                                cfgDto.getEndTime()));
                        vo.setErrorCode("TIME_REPETITION_ERROR");
                    }
                }
            }));
        }
        if (StringUtils.isNotEmpty(vo.getErrorCode())) {
            // 有异常直接返回
            return vo;
        }
        return null;
    }
}
