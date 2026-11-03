package com.persagy.apccontrol.common.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.RunningLogServiceResource;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.common.opt.OptRecentKeys;
import com.persagy.apccontrol.basic.service.runninglog.NowRunningLogService;
import com.persagy.apccontrol.basic.service.runninglog.RunningLogTemplateConfig;
import com.persagy.apccontrol.basic.service.runninglog.category.MdQueryObjStrategy;
import com.persagy.apccontrol.basic.service.runninglog.category.RunningLogCategoryService;
import com.persagy.apccontrol.basic.service.runninglog.category.VentilationQueryObjStrategy;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.outside.dtpmeos.BusinessRunningLogPushDTO;
import com.persagy.apccontrol.common.outside.dtpmeos.OperDataTypeEnum;
import com.persagy.apccontrol.common.outside.dtpmeos.RunningDataTargetEnum;
import com.persagy.apccontrol.common.server.PaasDatatransferServer;
import com.persagy.apccontrol.common.service.RunningLogEdgeNowService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 边新版运行日志处理
 */
@Service
@Slf4j
public class RunningLogEdgeNowServiceImpl implements RunningLogEdgeNowService {
    
    private static final String LOG_TAG = "运行日志处理";

    /**
     * opt 分支 B1（switch.opt.autoLogDedupe）：已建档或已确认存在的 (traceId, objectId, infoCode)，10 分钟内的后续“未完成”帧直接跳过，
     * 不再重复做分类远程查询、加锁和查库。
     */
    private static final OptRecentKeys AUTO_LOG_RECENT = new OptRecentKeys(10, TimeUnit.MINUTES, 100_000);
    
    @Resource
    private RunningLogServiceResource runningLogServiceResource;
    @Resource
    private DtpRwdClientService dtpRwdClientService;
    @Resource
    private PaasDatatransferServer paasDatatransferServer;
    @Resource
    SseHandlerContext sshHandlerContext;
    @Resource
    private NowRunningLogService nowRunningLogService;
    @Resource
    private RunningLogCategoryService runningLogCategoryService;
    @Resource
    private RedissonDistributedLockerService redissonDistributedLockerService;
    /**
     * 扩展字段构建处理
     *
     * @param log
     * @param runLog
     */
    private static void buildExtendInfo(BusinessRunningLogPushDTO log, RunningLog runLog) {
        if (log.getExtendInfo() != null) {
            runLog.setEffectStartTime(log.getExtendInfo().getStartDateTime());
            runLog.setEffectEndTime(log.getExtendInfo().getEndDateTime());
            
            //如果是时间表类型 除了新增以外的其他类型都去掉  effectDateType 和 effectDateData
            if (RunningDataTargetEnum.SCHEDULE.equals(log.getDataTarget()) && !OperDataTypeEnum.ADD_OPER.equals(log.getDataType())) {
                log.getExtendInfo().setEffectDateType(null);
                log.getExtendInfo().setEffectDateData(null);
                return;
            }
            
            //把日期做一个转换
            if (StringUtils.isNotBlank(log.getExtendInfo().getEffectDateData())) {
                MidCalendarDateTagValueEnum dateTag =
                        MidCalendarDateTagValueEnum.getCompareValue(log.getExtendInfo().getEffectDateData());
                if (dateTag != null) {
                    log.getExtendInfo().setEffectDateData(dateTag.getReasonPhrase());
                }
                //如果是 、 号分割的 日期数据 添加一个括号
                if (log.getExtendInfo().getEffectDateData().contains(",")) {
                    String[] effectDateData = log.getExtendInfo().getEffectDateData().split(",");
                    String[] weekOrder = {LocaleMessageUtil.getMessage("key_59a067185cfb410abc85a23111f2d6b6"), LocaleMessageUtil.getMessage("key_8ed4545bb34b475d91b3a248bb4d0068"), LocaleMessageUtil.getMessage("key_a5d4dbc51fc04c3ba00a6ab6ec5da89e"), LocaleMessageUtil.getMessage("key_e6dc3c8a0c1249ac8f9f02eb84722ce1"), LocaleMessageUtil.getMessage("key_290791e4604543248dac06ea111f766e"), LocaleMessageUtil.getMessage("key_adf21a5868724241bb05d7c8abd05386"), LocaleMessageUtil.getMessage("key_410836c4da6c4c62b4a7ffa3d01952ed")};
                    // 使用Arrays.sort方法和自定义比较器进行排序
                    Arrays.sort(effectDateData, (o1, o2) -> {
                        int index1 = -1;
                        int index2 = -1;
                        // 查找字符串在星期顺序数组中的索引
                        for (int i = 0; i < weekOrder.length; i++) {
                            if (weekOrder[i].equals(o1)) {
                                index1 = i;
                            }
                            if (weekOrder[i].equals(o2)) {
                                index2 = i;
                            }
                        }
                        // 根据索引进行比较
                        return Integer.compare(index1, index2);
                    });
                    String msg = "（" + Arrays.toString(effectDateData).replace(",", "、") + "）";
                    log.getExtendInfo().setEffectDateData(msg.replace("[", "").replace("]", ""));
                }
            }
        }
    }
    
    @Override
    public void saveByBdtpMsg(BusinessRunningLogPushDTO logDto) {
        
        if (logDto == null) {
            return;
        }
        
        //数据前置过滤
        if (preFiltration(logDto)) {
            log.info("[{}] 不记录信息点,跳过日志记录:{}", LOG_TAG, logDto);
            return;
        }
        
        //数据转换
        RunningLog runLog = new RunningLog();
        runLog.setProjectId(ContextUtil.getProjectId());
        runLog.setType(logDto.getDataSource());
        if (logDto.getDataTarget() != null) {
            runLog.setSourceType(RunningLogSourceTypeEnum.resolve(logDto.getDataTarget().getValue()));
        } else {
            //未指定 默认都是 控制类
            runLog.setSourceType(RunningLogSourceTypeEnum.COMMAND);
        }
        if ((RunningDataTargetEnum.SCHEDULE.equals(logDto.getDataTarget()) && logDto.getDataType() != null) ||
                RunningDataTargetEnum.INTERVENTION.equals(logDto.getDataTarget())) {
            //时间表配置 /解除干预 直接成功
            runLog.setState(RunningLogSourceStateEnum.SUCCEED);
        } else {
            runLog.setState(RunningLogSourceStateEnum.UNKNOWN);
        }
        runLog.setDataTime(logDto.getDataTime());
        runLog.setCreateTime(LocalDateTime.now());
        runLog.setClassCode(logDto.getClassCode());
        buildExtendInfo(logDto, runLog);
        runLog.setTraceId(logDto.getTraceData());
        runLog.setInfoCode(logDto.getInfoCode());
        runLog.setInfoValue(logDto.getInfoValue());
        runLog.setCreateUser(logDto.getUserName());
        //设置操作内容
        setContent(logDto, runLog);
        
        String keyWord = buildKeyWord(runLog);
        //关键词没有匹配上不记录日志 跳过
        if (StringUtils.isBlank(keyWord)) {
            log.info("[{}] 未匹配到日志关键词模版,不记录日志:{}", LOG_TAG, logDto);
            return;
        }
        //匹配模版
        boolean check =
                RunningLogTemplateConfig.bdtpMeosContentDesc.keySet().stream().anyMatch(key -> RunningLogTemplateConfig.spelMappingByBoolean(runLog, key));
  
        //未匹配上模版不处理
        if (!check) {
            log.info("[{}] 未匹配到日志模版模版,不记录日志:{}", LOG_TAG, logDto);
            return;
        }
        
        //设置对象名称
        setObjName(logDto, runLog);
        //匹配日志分类
        setCategory(runLog);
        runLog.setKeyWord(keyWord);
        
        save(runLog);
        
    }
    
    /**
     * 数据前置过滤
     *
     * @param logDto
     * @return
     */
    private boolean preFiltration(BusinessRunningLogPushDTO logDto) {
        //按照信息点值过滤
        if (RunningLogTemplateConfig.bdtpMeosFiltrationInfoCodeVal.contains(logDto.getInfoValue())) {
            return true;
        }
        return false;
    }
    
    /**
     * 设置日志分类 来源控制服务的日志分类
     * 1.日志分类 存分类编码,如果是对象 就存对象 ID
     * 2.把分类枚举放在扩展字段里面 方法后续消费
     * @param runLog
     */
    private void setCategory(RunningLog runLog) {
        runningLogCategoryService.buildLogCategory(runLog);
    }
    
    /**
     * 设置操作内容
     *
     * @param log
     * @param runLog
     */
    private void setContent(BusinessRunningLogPushDTO log, RunningLog runLog) {
        JSONObject content = new JSONObject();
        if (log.getExtendInfo() != null) {
            content = (JSONObject) JSONObject.toJSON(log.getExtendInfo());
        }
        //把为消费字段追加到扩展字段里面
        content.put(BusinessRunningLogPushDTO.Fields.modelType, log.getModelType());
        content.put(BusinessRunningLogPushDTO.Fields.modelClass, log.getModelClass());
        content.put(BusinessRunningLogPushDTO.Fields.userId, log.getUserId());
        if (log.getDataType() != null) {
            content.put(BusinessRunningLogPushDTO.Fields.dataType, log.getDataType().getReasonPhrase());
        }
        content.put(BusinessRunningLogPushDTO.Fields.beforeValue, log.getBeforeValue());
        content.put(BusinessRunningLogPushDTO.Fields.result, log.getResult());
        content.put(BusinessRunningLogPushDTO.Fields.reasonSource, log.getReasonDetail());
        content.put(BusinessRunningLogPushDTO.Fields.reasonDetail, log.getReasonSource());
        content.put(BusinessRunningLogPushDTO.Fields.remark, log.getRemark());
        content.put(BusinessRunningLogPushDTO.Fields.infoName, log.getInfoName());
        
        runLog.setContent(content);
    }
    
    /**
     * 设置对象名称
     *
     * @param log
     * @param runLog
     */
    private void setObjName(BusinessRunningLogPushDTO log, RunningLog runLog) {
        //需要查询名称的 ID
        Map<String, DtpRwdObjectDTO> map = new HashMap<>();
        List<String> objNameIds = new ArrayList<>(Collections.singletonList(log.getObjectId()));
        objNameIds.add(log.getModelType());
        List<DtpRwdObjectDTO> objects = dtpRwdClientService.listObject(
                new DtpRwdObjectQueryParam().setId(objNameIds).addWithColumn(DtpRwdObjectDTO.Fields.localName));
        if (CollectionUtils.isNotEmpty(objects)) {
            //转成 MAP
            map = objects.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, x -> x));
        }
        //设置系统名称
        runLog.setSystemId(log.getModelType());
        runLog.setSystemClassCode(log.getModelClass());
        
        if (map.containsKey(log.getModelType())) {
            runLog.setSystemName(map.get(log.getModelType()).getLocalName());
        }
        //设置对象名称
        if (StringUtils.isNotBlank(log.getObjectId())) {
            runLog.setObjectId(log.getObjectId());
            if (map.containsKey(log.getObjectId())) {
                runLog.setObjectName(map.get(log.getObjectId()).getLocalName());
            }
        } else {
            //如果对象 ID 为空,就用系统 id 和名称作为对象名称和 ID,都为空就不处理
            runLog.setObjectId(runLog.getSystemId());
            runLog.setObjectName(runLog.getSystemName());
            runLog.setClassCode(runLog.getSystemClassCode());
        }
    }
    
    /**
     * 构建关键词
     *
     * @param runLog
     * @return
     */
    public String buildKeyWord(RunningLog runLog) {
        return RunningLogTemplateConfig.spelMapping(runLog, RunningLogTemplateConfig.keyWord);
    }
    
    @Override
    public void updateByTwinMsg(FeedbackInfoMsgMqttVO vo) {
        //只处理设定消息
        if (vo.getType() != 1) {
            return;
        }
        //孪生信息日志记录
        if (twinMsgLog(vo)) {
            return;
        }
        //更新日志状态
        if (StringUtils.isBlank(vo.getTraceData()) || "null".equalsIgnoreCase(vo.getTraceData())) {
            log.info("[{}] twin 数据格式错误不进行记录:{}", LOG_TAG, vo);
            return;
        }
        //其它信息点 获取日志进行状态更新
        RunningLogSourceStateEnum st = RunningLogSourceStateEnum.resolve(vo.getResult());
        //状态错误不更新
        if (st == null || RunningLogSourceStateEnum.UNKNOWN.equals(st)) {
            log.info("[{}] twin 状态错误不更新:{}", LOG_TAG, vo);
            return;
        }
        
        String traceId = vo.getTraceData();
        String objectId = vo.getObjectId();
        String infoCode = vo.getInfoCode();
        // 终态回写与建档共用同一把 per-(traceId,objectId,infoCode) 锁：入口 handleLog 带 @Async，建档帧(result=0)
        // 与终态帧(result≠0)乱序并发，终态若早于建档 INSERT 提交执行会查不到行而丢终态(表现为"自动日志全是 0")。
        // 进同一把锁串行化"双查 + 写"，并在无行时按终态补建，保证任意到达顺序都收敛为单行、终态不丢、状态不回退。
        
        // 锁外预判：仅当"当前无任何行"且本帧具备补建资格(自动 + 白名单 + 终态)时，才预构建候选并执行重的
        // setCategory 远程归类查询，避免常见的"建档已先行"场景白白触发远程查询。
        RunningLog createCandidate = null;
        boolean terminalState = !RunningLogSourceStateEnum.UNFINISHED.equals(st);
        if (terminalState && isAutoTwinLog(vo) && isTwinWhitelisted(vo)
                && !runningLogServiceResource.existsByTraceId(traceId, objectId, infoCode)) {
            createCandidate = buildTwinRunningLog(vo, st);
            if (createCandidate != null) {
                setCategory(createCandidate);
                //没有匹配上系统分类 不能补建
                if (createCandidate.getFirstLevelCategory() == null) {
                    createCandidate = null;
                }
            }
        }
        final RunningLog candidate = createCandidate;
        
        String lockKey = RedisKey.buildRunningLogTwinLockKey(traceId, objectId, infoCode);
        redissonDistributedLockerService.executeWithLock(lockKey, 5, () -> {
            //1) 命中非终态行(-1/0) → 条件更新为终态(WHERE state IN(-1,0)，DB 层单调守卫防回退)
            Long id = runningLogServiceResource.getIdByUnknown(traceId, objectId, infoCode);
            if (id != null) {
                if (runningLogServiceResource.updateStateById(id, st)) {
                    RunningLog runLog = runningLogServiceResource.getById(id);
                    //同步到云
                    paasDatatransferServer.sendLogDataToKafkaUpdate(runLog);
                    //数据重新播报
                    ssePush(runLog);
                } else {
                    log.info("[{}] twin 状态已终态,条件更新未命中,跳过:{}", LOG_TAG, vo);
                }
                return;
            }
            //2) 未命中但已存在(全状态) → 已终态，跳过冻结(首个终态冻结，防覆盖)
            if (runningLogServiceResource.existsByTraceId(traceId, objectId, infoCode)) {
                log.info("[{}] twin 指令已终态,跳过重复回写:{}", LOG_TAG, vo);
                return;
            }
            //3) 从未建档 → 按终态补建；不具备资格(非自动/非白名单/无分类)则维持原丢弃语义
            if (candidate == null) {
                log.info("[{}] twin 终态无对应建档且不可补建,跳过:{}", LOG_TAG, vo);
                return;
            }
            //补建(终态)：save 内部含 kafka 同步与 sse 播报
            save(candidate);
        });
    }

    
    /**
     * 孪生信息日志记录(建档)：仅处理操作人不存在(user=none)的自动日志，只认 result==0(未完成)的白名单帧。
     *
     * @param vo 孪生反馈报文
     * @return true-已作为自动建档帧处理(含跳过)，调用方应短路；false-非建档帧，交由终态回写处理
     */
    private boolean twinMsgLog(FeedbackInfoMsgMqttVO vo) {
        //只记录操作人不存在的自动数据，且 traceData 合法(非空、非 dtp-meos 控制)
        if (!isAutoTwinLog(vo)) {
            return false;
        }
        //建档闸门：只认 result==0(未完成) 且命中白名单的固定信息点/类码
        if (!RunningLogSourceStateEnum.UNFINISHED.value().equals(vo.getResult()) || !isTwinWhitelisted(vo)) {
            return false;
        }
        final String dedupeKey = OptSwitch.on(OptSwitch.AUTO_LOG_DEDUPE)
                ? OptRecentKeys.key(vo.getTraceData(), vo.getObjectId(), vo.getInfoCode()) : null;
        if (dedupeKey != null && AUTO_LOG_RECENT.contains(dedupeKey)) {
            OptMetrics.inc("runningLog.autoDedupeHit");
            return true;
        }
        RunningLog runLog = buildTwinRunningLog(vo, RunningLogSourceStateEnum.UNFINISHED);
        if (runLog == null) {
            //没有时间 不记录(仍属建档帧，短路后续终态处理)
            return true;
        }
        //设置分类信息
        setCategory(runLog);
        //没有匹配上系统分类 不进行记录
        if (runLog.getFirstLevelCategory() == null) {
            log.info("[{}] twin 没有匹配上系统分类 不进行记录:{}", LOG_TAG, vo);
            return true;
        }
        // 同一控制指令(traceId)设备会陆续推送多帧报文(开始执行/执行中，result 均为 0 未完成)。建档闸门只认
        // result==0，若不去重会对同一指令重复插入多行，而后续终态更新是 LIMIT 1 只能改其中一行，形成"一条超时+
        // 一条未完成"脏数据。这里按 traceId 加锁串行化"查重 + 建档"，已建档则跳过(@Async 并发下同样安全)。
        String lockKey = RedisKey.buildRunningLogTwinLockKey(runLog.getTraceId(), runLog.getObjectId(),
                runLog.getInfoCode());
        redissonDistributedLockerService.executeWithLock(lockKey, 5, () -> {
            if (runningLogServiceResource.existsByTraceId(runLog.getTraceId(), runLog.getObjectId(),
                    runLog.getInfoCode())) {
                log.info("[{}] twin 指令已建档,跳过重复记录:{}", LOG_TAG, vo);
                return;
            }
            save(runLog);
        });
        if (dedupeKey != null) {
            AUTO_LOG_RECENT.mark(dedupeKey);
        }
        return true;
    }
    
    /**
     * 是否为孪生自动日志帧：操作人不存在(user=none) 且 traceData 合法(非空、非 dtp-meos 控制)。
     */
    private boolean isAutoTwinLog(FeedbackInfoMsgMqttVO vo) {
        return "none".equals(vo.getUser())
                && StringUtils.isNotEmpty(vo.getTraceData())
                && !vo.getTraceData().contains("dtp-meos");
    }
    
    /**
     * 是否命中孪生自动日志白名单：固定信息点(twinClassCodeInfoCodes) 或 空调末端/通风类码白名单。
     */
    private boolean isTwinWhitelisted(FeedbackInfoMsgMqttVO vo) {
        List<String> infoCodeList = RunningLogTemplateConfig.twinClassCodeInfoCodes
                .getOrDefault(vo.getClassCode(), Collections.emptyList());
        return infoCodeList.contains(vo.getInfoCode())
                || MdQueryObjStrategy.classList.contains(vo.getClassCode())
                || VentilationQueryObjStrategy.classList.contains(vo.getClassCode());
    }
    
    /**
     * 由孪生反馈报文构建运行日志(不含分类，分类由调用方 setCategory 填充)。
     *
     * @param vo    孪生反馈报文
     * @param state 建档时为 UNFINISHED(0)，终态补建时为 resolve(result)
     * @return 构建好的运行日志；若无有效时间则返回 null(不记录)
     */
    private RunningLog buildTwinRunningLog(FeedbackInfoMsgMqttVO vo, RunningLogSourceStateEnum state) {
        LocalDateTime time = vo.toStartTime();
        if (time == null) {
            log.info("[{}] twin 没有时间 不进行记录:{}", LOG_TAG, vo);
            return null;
        }
        RunningLog runLog = new RunningLog();
        runLog.setProjectId(ContextUtil.getProjectId());
        runLog.setType(RunningLogTypeEnum.AUTO);
        runLog.setObjectId(vo.getObjectId());
        runLog.setObjectName(vo.getLocalName());
        runLog.setState(state);
        runLog.setKeyWord(LocaleMessageUtil.getMessage("key_bf6301133a294213b036069bdaaf275e"));
        runLog.setDataTime(time);
        runLog.setCreateTime(LocalDateTime.now());
        runLog.setContent((JSONObject) JSONObject.toJSON(vo));
        runLog.setClassCode(vo.getClassCode());
        runLog.setTraceId(vo.getTraceData());
        runLog.setInfoCode(vo.getInfoCode());
        runLog.setInfoValue(vo.getDataValue());
        runLog.setSourceType(RunningLogSourceTypeEnum.TWIN_COMMAND);
        return runLog;
    }
    
    /**
     * 保存数据,向云端推送和进行日志播报
     *
     * @param runLog
     */
    @Override
    public void save(RunningLog runLog) {
        //只接入固定系统
        if (!NowRunningLogCategory.newLoggingSystem(runLog.getFirstLevelCategory())) {
            log.warn("[{}] 非当前需要接入系统 不进行记录:{}", LOG_TAG, runLog);
            return;
        }
        //创建人国际化支持
        if (StringUtils.isEmpty(runLog.getCreateUser())) {
            runLog.setCreateUser(LocaleMessageUtil.getMessage("key_system"));
        }
        
        //数据保存
        runningLogServiceResource.save(runLog);
        //同步到云
        paasDatatransferServer.sendLogDataToKafka(runLog);
        
        //进行日志播报
        ssePush(runLog);
        
    }
    
    /**
     * sse数据推送
     *
     * @param runLog
     */
    private void ssePush(RunningLog runLog) {
        sshHandlerContext.getStrategy(SseEmitterEventEnum.PAGE_BROADCAST)
                         .doOperation(ContextUtil.getProjectId(), nowRunningLogService.mappingRunningLogVO(runLog,
                                 null));
    }
}
