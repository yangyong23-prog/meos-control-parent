package com.persagy.apccontrol.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.HistoryStorageResult;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.outside.enums.StorageTagEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.SyncHistoryStorageParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.HistoryStorageResultService;
import com.persagy.apccontrol.common.outside.feign.AlgorithmFeignClient;
import com.persagy.apccontrol.common.outside.model.param.HistoryStorageParam;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.common.outside.model.response.HistoryStorageResponse;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.TimeTypeEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class HistoryStorageTaskHandler implements LogComponent {

    private final AlgorithmFeignClient algorithmFeignClient;
    private final DtpRwdClientService dtpRwdClientService;
    private final ProjectCfg projectCfg;
    private final HistoryStorageResultService historyStorageResultService;
    private final PaasDataTransferClientService paasDataTransferClientService;


    /**
     * 执行
     *
     * @param syncDate 同步日期
     */
    @XxlJob("historyStorageHandler")
    public void execute(LocalDate syncDate) {
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());
        ContextUtil.setProjectId(projectCfg.getProjectId());
        DtpRwdObjectQueryParam objectQueryParam = new DtpRwdObjectQueryParam();
        // 蓄冷/蓄热标签实例范围覆盖六类空调系统（旧版 ACCC/ACCI/ACCH + 新版 ACAC/ACAH/ACAI），新版实例无蓄冷规则时算法自然不返回、不参与并集
        objectQueryParam.setClassCode(DtpRwdClassCodeEnum.listAirConditionSystemClassCodes());
        objectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(objectQueryParam);
        if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
            logProjectInstance().info("未查询到系统对象信息");
            return;
        }
        Set<String> systemIds = dtpRwdObjectDTOS.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        HistoryStorageParam param = new HistoryStorageParam();
        param.setGroupCode(projectCfg.getGroupCode());
        param.setProjectId(projectCfg.getProjectId());
        param.setSystemId(systemIds);
        if (syncDate == null) {
            syncDate = LocalDate.now().plusDays(-1);
        } else {
            syncDate = syncDate.plusDays(-1);
        }
        //默认获取昨天的数据
        param.setStartTime(syncDate.atStartOfDay());
        param.setEndTime(syncDate.atStartOfDay().plusDays(1));
        // 获取标签
        CustomerResponse<List<HistoryStorageResponse>> historyStorages = algorithmFeignClient.getHistoryStorage(param);
        List<HistoryStorageResponse> historyStorageResponses = CustomerResponse.handleResult(historyStorages);
        if (CollectionUtils.isEmpty(historyStorageResponses)) {
            logProjectInstance().info("未查询到蓄冷蓄热的历史数据", Pair.of("param", param));
            return;
        }
        Map<LocalDateTime, List<HistoryStorageResponse>> timeListMap = historyStorageResponses.stream().collect(Collectors.groupingBy(HistoryStorageResponse::getTimestamp));
        List<SyncHistoryStorageParam.RecordContent> storageTagList = new LinkedList<>();
        LocalDate finalSyncDate = syncDate;
        DateTimeRangeExecutor.generateTime(TimeTypeEnum.MINUTE, 15, LocalTime.MIN, LocalTime.MAX, time -> {
            //0:无，1:蓄冷，2:蓄热
            SyncHistoryStorageParam.RecordContent recordContent = new SyncHistoryStorageParam.RecordContent();
            recordContent.setTime(time);
            storageTagList.add(recordContent);
            List<HistoryStorageResponse> responses = timeListMap.get(time.atDate(finalSyncDate));
            if (CollectionUtils.isEmpty(responses)) {
                recordContent.setStorageTag(StorageTagEnum.NONE);
                return;
            }
            //获取不蓄的数量，如果不续的数量和该时间段的总数量一致，则该时间段为不蓄
            long not = responses.stream().map(HistoryStorageResponse::getFlag).filter("0"::equals).count();
            if (not == responses.size()) {
                recordContent.setStorageTag(StorageTagEnum.NONE);
            } else {
                //获取蓄冷数量，如果有蓄冷不为0，则该是时间段为蓄冷，否则为蓄热
                long coldNum = responses.stream().map(HistoryStorageResponse::getStorageTag).filter("0"::equals).count();
                if (coldNum > 0) {
                    recordContent.setStorageTag(StorageTagEnum.COLD);
                } else {
                    recordContent.setStorageTag(StorageTagEnum.HOT);
                }
            }
        });
        DataTypeEnum dataTypeEnum = DataTypeEnum.ADD;
        LambdaQueryWrapper<HistoryStorageResult> uw = new LambdaQueryWrapper<>();
        uw.eq(HistoryStorageResult::getRecordDate, syncDate);
        List<HistoryStorageResult> list = historyStorageResultService.list(uw);
        if (CollectionUtils.isNotEmpty(list)) {
            dataTypeEnum = DataTypeEnum.EDIT;
        }
        HistoryStorageResult data = this.saveOrUpdateData(syncDate, storageTagList);
        if (data != null) {
            paasDataTransferClientService.syncDbDataToCloud(PaasDataTransferParam.of(dataTypeEnum, data));
        }
    }

    private HistoryStorageResult saveOrUpdateData(LocalDate now, List<SyncHistoryStorageParam.RecordContent> storageTagList) {
        HistoryStorageResult result = new HistoryStorageResult();
        result.setRecordDate(now);
        result.setProjectId(projectCfg.getProjectId());
        result.setContent(storageTagList);
        result.clearUpdateParameter();
        LambdaQueryWrapper<HistoryStorageResult> uw = new LambdaQueryWrapper<>();
        uw.eq(HistoryStorageResult::getRecordDate, now);
        //默认保存180天数据
        LocalDate date = LocalDate.now().plusDays(-180);
        LambdaQueryWrapper<HistoryStorageResult> delQw = new LambdaQueryWrapper<>();
        //删除180天前的数据
        delQw.lt(HistoryStorageResult::getRecordDate, date);
        historyStorageResultService.remove(delQw);
        boolean saveBool = historyStorageResultService.saveOrUpdate(result, uw);
        if (saveBool) {
            return historyStorageResultService.getOne(uw);
        }
        return null;
    }


    @Override
    public String logTag() {
        return "xxl-job定时任务-获取蓄冷蓄热标签";
    }
}
