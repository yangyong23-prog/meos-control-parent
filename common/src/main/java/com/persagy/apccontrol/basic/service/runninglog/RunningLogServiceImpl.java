package com.persagy.apccontrol.basic.service.runninglog;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.mapper.RcRunningLogMapper;
import com.persagy.apccontrol.basic.model.dto.runnningLog.*;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.RcRunningLogService;
import com.persagy.apccontrol.basic.resource.RunningLogServiceResource;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogEquipmentListQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogPageQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogQueryParam;
import com.persagy.apccontrol.basic.service.export.DownFileService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RunningLogServiceImpl implements RunningLogService {

    @Autowired
    private RcRunningLogService rcRunningLogService;
    @Autowired
    private RcRunningLogMapper rcRunningLogMapper;
    @Autowired
    private DownFileService downFileService;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Resource
    private RunningLogServiceResource runningLogServiceResource;

    private static final String ENTER_STR = "\n";

    @Override
    public List<RunningLogCategoryDto> getLogCategoryList() {
        List<RunningLogCategoryDto> list = RunningLogCategoryEnum.getCategoryList();
        
        return list.stream().filter(category -> StringUtils.isEmpty(category.getParentCode())).collect(Collectors.toList());
    }
    

    @Override
    public List<JSONObject> listSecondLevelCategory(Set<String> firstLevelCategoryCodes) {
        List<JSONObject> resultList = new ArrayList<>();
        if (CollectionUtil.isEmpty(firstLevelCategoryCodes)) {
            return resultList;
        }
        for (String firstLevelCategoryCode : firstLevelCategoryCodes) {
            RunningLogCategoryEnum firstLevelCategory = RunningLogCategoryEnum.getEnumByCode(firstLevelCategoryCode);
            if (firstLevelCategory == null) {
                continue;
            }

            // 获取二级
            List<RunningLogCategoryEnum> runningLogCategoryEnums = RunningLogCategoryEnum.getEnumByParentCode(firstLevelCategoryCode);
            for (RunningLogCategoryEnum runningLogCategoryEnum : runningLogCategoryEnums) {
                JSONObject resultObject = new JSONObject();
                resultObject.put("secondLevelCategory", runningLogCategoryEnum.getCode());
                resultObject.put("secondLevelCategoryName", runningLogCategoryEnum.getName());
                resultList.add(resultObject);
            }
        }
        return resultList;
    }

    @Override
    public List<RunningLogDto> listRunningLogByDate(@NotNull RunningLogQueryParam param) {
        List<RcRunningLog> rcRunningLogs = this.rcRunningLogService.queryRunningLogList(param);
        if (CollectionUtils.isEmpty(rcRunningLogs)) {
            return Lists.newArrayList();
        }
        return RunningLogService.getRunningLogDtos(rcRunningLogs);
    }


    @Override
    public R<Collection<RunningLogDto>> queryLogList(@NonNull RunningLogPageQueryParam param) {
        IPage<RcRunningLog> runningLogDtos = rcRunningLogService.queryRunningLogList(param);
        if (CollectionUtils.isEmpty(runningLogDtos.getRecords())) {
            return R.successMulti(new ArrayList<>(), runningLogDtos.getTotal());
        }
        List<RunningLogDto> logDtos = RunningLogService.getRunningLogDtos(runningLogDtos.getRecords());
        return R.successMulti(logDtos, runningLogDtos.getTotal());
    }

    @Override
    public List<RunningLogResDto> queryLogListByParam(RunningLogPageQueryParam param) {
        IPage<RcRunningLog> runningLogDtos = rcRunningLogService.queryRunningLogList(param);
        if (CollectionUtils.isEmpty(runningLogDtos.getRecords())) {
            return new ArrayList<>();
        }
        List<RunningLogResDto> resDtos = new ArrayList<>();
        for (RcRunningLog logDto : runningLogDtos.getRecords()) {
            RunningLogResDto dto = new RunningLogResDto();
            BeanUtils.copyProperties(logDto, dto, "id");
            dto.setId(IdUtil.getSnowflakeNextId());
            dto.setOriId(logDto.getId());
            dto.setContent(logDto.getContent().getContentDesc());
            if (CollectionUtils.isNotEmpty(logDto.getDetail())) {
                List<RunningLogContentDto> details = JSONObject.parseArray(JSON.toJSONString(logDto.getDetail()), RunningLogContentDto.class);
                dto.setDetail(details.stream().filter(x -> x != null).map(x -> x.getContentDesc()).collect(Collectors.toList()));
            }
            resDtos.add(dto);
        }
        return resDtos;
    }

    @Override
    public List<RunningLogEquipmentDto> equipmentList(@NonNull RunningLogEquipmentListQueryParam param) {
        param.setProjectId(ContextUtil.getProjectId());
        
        //新版日志适配
        if (param.getFirstLevelCategory() != null) {
            return adaptiveNowEquipmentList(param);
        }
        return rcRunningLogMapper.equipmentList(param);
    }
    
    /**
     * 适配新版运行日志查询设备
     *
     * @param param
     * @return
     */
    public List<RunningLogEquipmentDto> adaptiveNowEquipmentList(@NonNull RunningLogEquipmentListQueryParam param) {
        //新版日志适配
        List<RunningLog> runningLogList = runningLogServiceResource
                .lambdaQuery()
                .select(RunningLog::getObjectId, RunningLog::getObjectName)
                .eq(RunningLog::getProjectId, ContextUtil.getProjectId())
                .eq(param.getFirstLevelCategory() != null, RunningLog::getFirstLevelCategory,
                        param.getFirstLevelCategory())
                .eq(StringUtils.isNotEmpty(param.getSecondLevelCategory()), RunningLog::getSecondLevelCategory,
                        param.getSecondLevelCategory())
                .eq(StringUtils.isNotBlank(param.getSystemId()), RunningLog::getSystemId, param.getSystemId())
                .ge(RunningLog::getDataTime, param.getDataStartTime())
                .le(RunningLog::getDataTime, param.getDataEndTime())
                .eq(param.getType() != null, RunningLog::getType, param.getType())
                .like(StringUtils.isNotBlank(param.getWord()), RunningLog::getObjectName, param.getWord())
                .groupBy(RunningLog::getObjectId)
                .list();
        
        if (CollectionUtils.isEmpty(runningLogList)) {
            return null;
        }
        //查询出对象的 类型
        Set<String> objIds = runningLogList.stream().map(RunningLog::getObjectId).collect(Collectors.toSet());
        List<DtpRwdObjectDTO> objects =
                dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setId(objIds).addWithColumn(
                        DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.objType));
        //objects 转换成map
        Map<String, DtpRwdObjectDTO> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(objects)) {
            map = objects.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, x -> x));
        }
        
        List<RunningLogEquipmentDto> vo = new ArrayList<>();
        //适配成之前的数据格式
        Map<String, DtpRwdObjectDTO> finalMap = map;
        runningLogList.forEach(runningLog -> {
            RunningLogEquipmentDto dto = new RunningLogEquipmentDto();
            dto.setObjectId(runningLog.getObjectId());
            dto.setObjectName(runningLog.getObjectName());
            DtpRwdObjectDTO obj = finalMap.get(runningLog.getObjectId());
            if (obj != null) {
                dto.setObjectCategory(obj.getObjType());
            }
            vo.add(dto);
        });
        return vo;
    }
    

    @Override
    public String export(@NonNull RunningLogPageQueryParam param) {
        param.setPageNumber(1);
        param.setPageSize(10000);
        param.setProjectId(ContextUtil.getProjectId());
        List<RcRunningLog> dataList = rcRunningLogService.queryRunningLogList(param).getRecords();
        if (CollectionUtils.isEmpty(dataList)) {
            return "";
        }
        if (CollectionUtils.isEmpty(dataList)) {
            dataList = new ArrayList<>();
        }else {
            dataList.stream().forEach(runningLog -> {
                runningLog.setLogType(RunningLogTypeEnum.resolve(runningLog.getType()).getReasonPhrase());
                runningLog.setLogCategory(runningLog.getThirdLevelCategory().getReasonPhrase());
                StringBuilder sb = new StringBuilder();
                sb.append(runningLog.getContent().getContentDesc());
                if (CollectionUtils.isNotEmpty(runningLog.getDetail())) {
                    sb.append(":" + ENTER_STR );
                    List<RunningLogContentDto> details = JSONObject.parseArray(JSON.toJSONString(runningLog.getDetail()), RunningLogContentDto.class);
                    for (RunningLogContentDto contentDto : details) {
                        sb.append(details.size() == 1 ? "" : (details.indexOf(contentDto) + 1) + ".");
                        sb.append(contentDto.getContentDesc());
                        sb.append(details.indexOf(contentDto) == details.size()-1 ? "" : ENTER_STR);
                    }
                }
                runningLog.setContentDetail(sb.toString());
                if (runningLog.getEffectStartTime() != null && runningLog.getEffectEndTime() != null) {
                    runningLog.setEffectTime(runningLog.getEffectStartTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
                    +"-"+runningLog.getEffectEndTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
                }
                runningLog.setLogTime(runningLog.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
            });
        }
        String fileName = LocaleMessageUtil.getMessage("key_2f264796a74a460292841a17f0f7105c");
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(null, fileName), RcRunningLog.class, dataList);
        String fileKey = downFileService.handlerDownLoadByWorkbook(workbook, fileName);
        return fileKey;
    }

}
