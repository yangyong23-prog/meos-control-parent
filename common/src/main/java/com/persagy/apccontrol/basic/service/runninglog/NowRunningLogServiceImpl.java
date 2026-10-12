package com.persagy.apccontrol.basic.service.runninglog;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.runnningLog.CustomizedKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogCategoryDto;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.*;
import com.persagy.apccontrol.basic.model.param.knowledge.ObjectsExistRuleQueryParam;
import com.persagy.apccontrol.basic.model.param.log.NowPageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.log.RunningLogDownloadVO;
import com.persagy.apccontrol.basic.model.vo.log.RunningLogHomePageVO;
import com.persagy.apccontrol.basic.model.vo.log.RunningLogVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.resource.RunningLogServiceResource;
import com.persagy.apccontrol.basic.resource.param.runningLog.NowRunningLogQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.QueryLogCategoryParam;
import com.persagy.apccontrol.basic.service.export.DownFileService;
import com.persagy.apccontrol.basic.service.knowledge.BizDtpKnowledgeService;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerInfoCodeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.entity.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 新版运行日志接口实现
 */
@Service
@Slf4j
public class NowRunningLogServiceImpl implements NowRunningLogService {
    
    private static final String LOG_TAG = "运行日志处理";
    
    @Resource
    private RunningLogServiceResource runningLogServiceResource;
    @Resource
    private DtpKnowledgeClientService dtpKnowledgeClientService;
    @Resource
    private DownFileService downFileService;
    @Resource
    private BizDtpKnowledgeService bizDtpKnowledgeService;
    
    @Override
    public List<RunningLogVO> listPageBroadcastMsg(@NotNull NowPageBroadcastQueryParam param) {
        
        List<RunningLog> log = runningLogServiceResource.listPageBroadcastMsg(param);
        
        return mappingRunningLogVO(log);
    }
    
    @Override
    public R<Collection<RunningLogVO>> queryRunningLogListByPage(NowRunningLogQueryParam param) {
        IPage<RunningLog> runningLogs = runningLogServiceResource.queryRunningLogListByPage(param);
        if (CollectionUtils.isEmpty(runningLogs.getRecords())) {
            return R.successMulti(new ArrayList<>(), runningLogs.getTotal());
        }
        List<RunningLogVO> vos = mappingRunningLogVO(runningLogs.getRecords());
        return R.successMulti(vos, runningLogs.getTotal());
    }
    
    @Override
    public String downloadLog(@NonNull NowRunningLogQueryParam param) {
        param.setPageNumber(1);
        param.setPageSize(100000);
        
        IPage<RunningLog> runningLogs = runningLogServiceResource.queryRunningLogListByPage(param);
        List<RunningLog> dataList = runningLogs.getRecords();
        if (CollectionUtils.isEmpty(dataList)) {
            return "";
        }
        CustomizedKnowledgeInfoCodeDTO customizedKnowledgeInfoCode = getCustomizedKnowledgeInfoCode(dataList);
        
        List<RunningLogDownloadVO> voList = new ArrayList<>();
        dataList.forEach(runningLog -> {
            
            //设置信息点名称
            setKnowledgeInfoCode(runningLog, customizedKnowledgeInfoCode);
            
            RunningLogDownloadVO vo = new RunningLogDownloadVO();
            vo.setLogCategory(getRunningLogCategory(runningLog));
            vo.setLogType(runningLog.getType().getDesc());
            vo.setCreateUser(runningLog.getCreateUser());
            vo.setObjectName(runningLog.getObjectName());
            vo.setContentDesc(mappingContentDesc(runningLog));
            if (runningLog.getEffectStartTime() != null && runningLog.getEffectEndTime() != null) {
                vo.setEffectTime(runningLog.getEffectStartTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd " +
                        "HH:mm:ss"))
                        + "-" + runningLog.getEffectEndTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd " +
                        "HH:mm:ss")));
            }
            if (runningLog.getContent() != null) {
                vo.setReason(runningLog.getContent().getString("reasonSource"));
            }
            
            vo.setLogTime(runningLog.getDataTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")));
            voList.add(vo);
        });
        
        String fileName = LocaleMessageUtil.getMessage("key_2f264796a74a460292841a17f0f7105c");
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(null, fileName), RunningLogDownloadVO.class,
                voList);
        return downFileService.handlerDownLoadByWorkbook(workbook, fileName);
    }
    
    @Override
    public List<RunningLogCategoryDto> getSecondLevelRunningLogCategory(@NonNull QueryLogCategoryParam param) {
        
        NowRunningLogCategory runningLogCategory = NowRunningLogCategory.getEnumByCode(param.getFirstLevelCategory());
        //没匹配上新版日志 走老逻辑
        if (runningLogCategory == null) {
            List<RunningLogCategoryDto> list = RunningLogCategoryEnum.getCategoryList();
            List<RunningLogCategoryDto> firstLeveList =
                    list.stream().filter(category -> StringUtils.isEmpty(category.getParentCode())).collect(Collectors.toList());
            for (RunningLogCategoryDto category : firstLeveList) {
                category.setChildCategoryList(list.stream().filter(x -> StringUtils.isNotEmpty(x.getParentCode()) && x.getParentCode().equals(category.getCode())).collect(Collectors.toList()));
                for (RunningLogCategoryDto secondCategory : category.getChildCategoryList()) {
                    secondCategory.setChildCategoryList(list.stream().filter(x -> StringUtils.isNotEmpty(x.getParentCode()) && x.getParentCode().equals(secondCategory.getCode())).collect(Collectors.toList()));
                }
                if (StringUtils.equals(category.getCode(), param.getFirstLevelCategory())) {
                    //取出符合的 子集
                    return category.getChildCategoryList();
                }
            }
            return null;
        }
        
        //新版日志----
        List<RunningLogCategoryDto> list = new ArrayList<>();
        
        List<RunningLog> r = runningLogServiceResource.getSecondLevelRunningLogCategory(param);
        
        if (CollectionUtils.isEmpty(r)) {
            return list;
        }
        r.forEach(x -> {
            RunningLogCategoryDto dto = new RunningLogCategoryDto();
            //判断一级分组是否是规则类型,规则类型单独映射名称
            if (StringUtils.isNotEmpty(runningLogCategory.getNameRule())) {
                dto.setCode(RunningLogTemplateConfig.spelMapping(x, runningLogCategory.getCodeRule()));
                dto.setName(RunningLogTemplateConfig.spelMapping(x, runningLogCategory.getNameRule()));
            } else {
                NowRunningLogCategory fl = NowRunningLogCategory.getEnumByCode(x.getSecondLevelCategory());
                if (fl == null) {
                    return;
                }
                dto.setCode(fl.getCode());
                dto.setName(fl.getName());
                dto.setOrder(fl.getOrder());
            }
            
            list.add(dto);
        });
        //排个序
        list.sort(Comparator.comparingInt(RunningLogCategoryDto::getOrder));
        
        return list;
    }
    
    public TodayLogSummaryVo todayLogSummary() {
        TodayLogSummaryVo todayLogSummaryVo = new TodayLogSummaryVo(0L, 0L, null);
        Map<RunningLogTypeEnum, Long> countWithType = runningLogServiceResource.countWithTypeByToday();
        if (!countWithType.isEmpty()) {
            // 日志统计汇总  总数只记录手动和自动的
            AtomicReference<Long> total = new AtomicReference<>(0L);
            countWithType.forEach((k, v) -> {
                if (k.equals(RunningLogTypeEnum.AUTO) || k.equals(RunningLogTypeEnum.MANUAL)) {
                    total.updateAndGet(v1 -> v1 + v);
                }
            });
            todayLogSummaryVo.setAutoCount(countWithType.get(RunningLogTypeEnum.AUTO));
            todayLogSummaryVo.setTotal(total.get());
            
            // 查询项目今日最新三条自动日志
            NowRunningLogQueryParam queryParam = new NowRunningLogQueryParam();
            queryParam.setDataStartTime(DateUtils.now().withHour(0).withMinute(0).withSecond(0));
            queryParam.setType(RunningLogTypeEnum.AUTO);
            queryParam.setPageNumber(1);
            queryParam.setPageSize(3);
            R<Collection<RunningLogVO>> collectionR = queryRunningLogListByPage(queryParam);
            todayLogSummaryVo.setLatestContent(getRunningLogHomePageVO(collectionR.getData()));
        }
        
        return todayLogSummaryVo;
    }
    
    /**
     * 获取日志分类 编码
     * 返回结果的时候 转换为名称
     * 前端筛选数据通过编码筛选数据
     *
     * @param vo
     * @return
     */
    public String getRunningLogCategory(RunningLog vo) {
        
        NowRunningLogCategory firstLevelCategory = vo.getFirstLevelCategory();
        //判断是否有取数规则 有规则用规则匹配
        if (StringUtils.isNotBlank(firstLevelCategory.getNameRule())) {
            return RunningLogTemplateConfig.spelMapping(vo, firstLevelCategory.getNameRule());
        }
        //非系统类型直接去二级分类名称
        return NowRunningLogCategory.getEnumByCode(firstLevelCategory.getCode()).getName();
    }
    
    //RunningLog 转换为 RunningLogVO对象
    @Override
    public RunningLogVO mappingRunningLogVO(RunningLog log,
                                            CustomizedKnowledgeInfoCodeDTO customizedKnowledgeInfoCode) {
        if (log == null) {
            return null;
        }
        if (customizedKnowledgeInfoCode == null) {
            customizedKnowledgeInfoCode = getCustomizedKnowledgeInfoCode(Collections.singletonList(log));
        }
        
        //设置信息点名称
        setKnowledgeInfoCode(log, customizedKnowledgeInfoCode);
        
        RunningLogVO vo = new RunningLogVO();
        vo.setType(log.getType());
        vo.setObjectId(log.getObjectId());
        vo.setObjectName(log.getObjectName());
        vo.setInfoCode(log.getInfoCode());
        vo.setTraceId(log.getTraceId());
        vo.setClassCode(log.getClassCode());
        vo.setState(log.getState());
        vo.setKeyWord(log.getKeyWord());
        vo.setCreateUser(log.getCreateUser());
        vo.setDataTime(log.getDataTime());
        vo.setCreateTime(log.getCreateTime());
        vo.setEffectStartTime(log.getEffectStartTime());
        vo.setEffectEndTime(log.getEffectEndTime());
        vo.setContent(log.getContent());
        vo.setSystemId(log.getSystemId());
        vo.setSystemName(log.getSystemName());
        vo.setSystemClassCode(log.getSystemClassCode());
        vo.setSecondLevelCategoryName(getRunningLogCategory(log));
        
        vo.setSecondLevelCategory(log.getSecondLevelCategory());
        vo.setFirstLevelCategory(log.getFirstLevelCategory());
        
        if (log.getFirstLevelCategory() != null) {
            vo.setFirstLevelCategoryName(log.getFirstLevelCategory().getName());
            //二级分类名称需要判断一级菜单是否是规则生成
            if (log.getFirstLevelCategory().getNameRule() != null) {
                vo.setSecondLevelCategoryName(RunningLogTemplateConfig.spelMapping(log,
                        log.getFirstLevelCategory().getNameRule()));
            } else {
                vo.setSecondLevelCategoryName(log.getFirstLevelCategory().getName());
            }
        }
        String contentDesc = mappingContentDesc(log);
        if (contentDesc == null) {
            return null;
        }
        vo.setContentDesc(contentDesc);
        
        return vo;
    }
    
    private void setKnowledgeInfoCode(RunningLog log, CustomizedKnowledgeInfoCodeDTO customizedKnowledgeInfoCode) {
        //查询出信息点的定义 追加到 RunningLog中
        if (StringUtils.isNotEmpty(log.getClassCode()) && StringUtils.isNotEmpty(log.getInfoCode())) {
            DtpKnowledgeInfoCodeDTO knowledgeInfoCode =
                    customizedKnowledgeInfoCode.getObjKnowledgeInfoCode(log.getClassCode(), log.getInfoCode());
            if (knowledgeInfoCode == null) {
                knowledgeInfoCode = new DtpKnowledgeInfoCodeDTO();
                //尝试去扩展信息里面的 name
                knowledgeInfoCode.setName(log.getContent().getString("infoName"));
            }
            
            //追加自定义信息点配置 有信息点名字才进行自定义单位处理
            RunningLogInfoCodeDataSourceEnum dataSourceConfig = RunningLogInfoCodeDataSourceEnum.getByInfoCode(log.getInfoCode());
            if (dataSourceConfig != null) {
                // 如果存在配置，直接设置数据源
                knowledgeInfoCode.setDataType("ENUM");
                knowledgeInfoCode.setDataSources(dataSourceConfig.getDataSources());
            }
            
            log.setKnowledgeInfoCode(knowledgeInfoCode);
        }
        
        //自定义信息点特殊处理
        if (ChillerInfoCodeEnum.getAllChillerInfoCode().contains(log.getInfoCode())) {
            DtpKnowledgeInfoCodeDTO smallKnowledgeInfoCode = new DtpKnowledgeInfoCodeDTO();
            smallKnowledgeInfoCode.setCode(log.getInfoCode());
            smallKnowledgeInfoCode.setName(log.getContent().getString("infoName"));
            smallKnowledgeInfoCode.setUnit(ChillerInfoCodeEnum.analyze(log.getInfoCode()).getChillerSize().getName());
            //如果是大机信息点判断是否有小机信息点
            if (ChillerInfoCodeEnum.getLargeToSmallInfoCodeMap().containsKey(ChillerInfoCodeEnum.analyze(log.getInfoCode()))) {
                ChillerInfoCodeEnum smallInfoCode =
                        ChillerInfoCodeEnum.getLargeToSmallInfoCodeMap().get(ChillerInfoCodeEnum.analyze(log.getInfoCode()));
                ObjectsExistRule smallExistRule =
                        customizedKnowledgeInfoCode.getSysExistRule(smallInfoCode.getInfoCode()
                        , log.getSystemId());
                if (smallExistRule == null) {
                    smallExistRule = customizedKnowledgeInfoCode.getSysExistRule(smallInfoCode.getInfoCode()
                            , log.getObjectId());
                }
                //判断是否有小机信息点 没有 单位就是台
                if (smallExistRule == null) {
                    //新增：如果串联冷机组如果是组的形式，日志的单位需要单独处理“组”
                    ChillerInfoCodeEnum chillerInfoCodeEnum = ChillerInfoCodeEnum.analyze(log.getInfoCode());
                    if(ChillerInfoCodeEnum.ACSCG_LARGE_NUM.equals(chillerInfoCodeEnum) || ChillerInfoCodeEnum.ACSCG_SMALL_NUM.equals(chillerInfoCodeEnum) || ChillerInfoCodeEnum.ACSCG_M1_NUM.equals(chillerInfoCodeEnum)){
                        smallKnowledgeInfoCode.setUnit(LocaleMessageUtil.getMessage("key_series_chiller_setting_group"));
                    }else {
                        smallKnowledgeInfoCode.setUnit(LocaleMessageUtil.getMessage("key_56152edc51c64d808d0710eb543c04e6"));
                    }
                }
            }
            log.setKnowledgeInfoCode(smallKnowledgeInfoCode);
        }
        
    }
    
    //批量转换
    @Override
    public List<RunningLogVO> mappingRunningLogVO(List<RunningLog> log) {
        List<RunningLogVO> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(log)) {
            return list;
        }
        
        CustomizedKnowledgeInfoCodeDTO customizedKnowledgeInfoCode = getCustomizedKnowledgeInfoCode(log);
        
        log.forEach(item -> {
            RunningLogVO vo = mappingRunningLogVO(item, customizedKnowledgeInfoCode);
            if (vo == null) {
                return;
            }
            list.add(vo);
        });
        return list;
    }
    
    /**
     * 批量查询信息点定义
     *
     * @param log
     */
    private CustomizedKnowledgeInfoCodeDTO getCustomizedKnowledgeInfoCode(List<RunningLog> log) {
        //对象 class_code and info_code
        Map<String, Set<String>> objClassCodeAndInfoCodeMap = new HashMap<>();
        //系统 class_code and info_code
        Map<String, Set<String>> systemClassCodeAndInfoCodeMap = new HashMap<>();
        log.forEach(item -> {
            objClassCodeAndInfoCodeMap.computeIfAbsent(item.getClassCode(), e -> Sets.newHashSet()).add(item.getInfoCode());
            
            //如果存在大机信息点 查询小机信息点
            ChillerInfoCodeEnum smallInfoCode =
                    ChillerInfoCodeEnum.getLargeToSmallInfoCodeMap().get(ChillerInfoCodeEnum.analyze(item.getInfoCode()));
            if (smallInfoCode != null) {
                systemClassCodeAndInfoCodeMap.computeIfAbsent(item.getSystemClassCode(), e -> Sets.newHashSet()).add(smallInfoCode.getInfoCode());
                systemClassCodeAndInfoCodeMap.computeIfAbsent(item.getClassCode(), e -> Sets.newHashSet()).add(smallInfoCode.getInfoCode());
            }
        });
        
        //批量查询 信息点定义
        //批量查询系统的绑点规则
        ObjectsExistRuleQueryParam param = new ObjectsExistRuleQueryParam();
        List<ClassCode> criteria = new ArrayList<>();
        systemClassCodeAndInfoCodeMap.forEach((key, value) -> {
            ClassCode classCode = new ClassCode(key);
            classCode.setClassCode(key);
            classCode.setInfoCodes(value);
            criteria.add(classCode);
        });
        param.setCriteria(criteria);
        param.setTerminal(TerminalEnum.EDGE);
        
        List<ObjectsExistRule> objectsExistRules = bizDtpKnowledgeService.queryObjectsExistRule(param);
        
        //批量获取对象的信息点定义
        Map<String, List<DtpKnowledgeInfoCodeDTO>> knowledgeInfoCodeMap =
                dtpKnowledgeClientService.mapClassCodeAndInfoCodeDefines(false, false,
                        objClassCodeAndInfoCodeMap.keySet());
        
        return new CustomizedKnowledgeInfoCodeDTO(objectsExistRules, knowledgeInfoCodeMap);
    }
    
   
    
    /**
     * 解析操作内容
     *
     * @param runLog
     */
    public String mappingContentDesc(RunningLog runLog) {
        if (RunningLogSourceTypeEnum.isTwin(runLog.getSourceType())) {
            //映射 twin 服务的模版映射
            return mappingContentDesc(runLog, RunningLogTemplateConfig.twinContentDesc);
        }
        if (RunningLogSourceTypeEnum.isDtpMeos(runLog.getSourceType())) {
            //映射 dtp-meos 服务的模版映射
            return mappingContentDesc(runLog, RunningLogTemplateConfig.bdtpMeosContentDesc);
        }
        if (RunningLogSourceTypeEnum.isMeos(runLog.getSourceType())) {
            //映射 meos 服务的模版映射
            return mappingContentDesc(runLog, RunningLogTemplateConfig.meosContentDesc);
        }
        return null;
    }
    
    private String mappingContentDesc(RunningLog runLog, Map<String, String> contentDesc) {
        
        if (contentDesc == null) {
            return null;
        }
        
        //先解析出属于什么模版
        for (Map.Entry<String, String> entry : contentDesc.entrySet()) {
            //通过模版匹配操作内容
            try {
                if (RunningLogTemplateConfig.spelMappingByBoolean(runLog, entry.getKey())) {
                    return RunningLogTemplateConfig.spelMapping(runLog, entry.getValue());
                }
            } catch (Exception e) {
                log.error("[{}] 服务的模版映射,日志描述匹配失败:{}", LOG_TAG, runLog, e);
            }
        }
        return null;
    }
    
    private List<RunningLogHomePageVO> getRunningLogHomePageVO(Collection<RunningLogVO> runningLogs) {
        List<RunningLogHomePageVO> vos = new ArrayList<>();
        for (RunningLogVO runningLog : runningLogs) {
            RunningLogHomePageVO logDto = new RunningLogHomePageVO();
            logDto.setFirstLevelCategoryName(runningLog.getFirstLevelCategoryName());
            if (runningLog.getContent() != null) {
                logDto.setContentDesc(runningLog.getContentDesc());
            }
            logDto.setCreateUser(runningLog.getCreateUser());
            logDto.setCreateTime(runningLog.getDataTime());
            vos.add(logDto);
        }
        return vos;
    }
    
    
}
