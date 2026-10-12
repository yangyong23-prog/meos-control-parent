package com.persagy.apccontrol.common.service.command.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.map.MapUtil;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingDataDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitSettingDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingResultService;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingResultQueryDaoParam;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.InterventionConfigQueryInfrastructure;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.param.InterventionConfigQueryParam;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.model.context.DemandCommandTaskCtx;
import com.persagy.apccontrol.common.service.command.batch.CommandBatchExecuteService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 照明需求指令计算任务
 * 1.获取 demand_config_lighting_result 表的指令(该表是云端自动同步下来)
 * 2.数据做一次过滤 和 equip_record 表比对 是否需要执行的指令已经执行过了 筛选出未执行的指令
 * 3.获取空间数据 判断空间下的回路是否 全开 全关 不是就是部分开始
 * 4.创建当前执行的指令保存到指令表 统一下发
 * <p>
 * 触发计算的场景:
 * 1.场景切换 初始第一个场景会直接执行(场景切换触发调用)
 * 2.配置变更 MQ 消息通知 进行一次计算
 * 3.补偿任务 补偿已经执行的和待执行的 需求是否存在变化 如果变化了就触发计算(1 分钟一次)
 */
@Service
@Slf4j
public class DemandCommandTask implements DemandCommandTaskEngine {

    private static final String LOG_TAG = "照明需求指令计算任务";

    @Resource
    private DemandConfigLightingResultService demandConfigLightingResultService;
    @Resource
    private EquipRecordService equipRecordService;
    @Resource
    private DemandLightingQueryService demandLightingQueryService;
    @Resource
    private DemandLightingStrategy demandLightingStrategy;
    @Resource
    private ProjectCfg projectCfg;
    @Resource
    private CommandBatchExecuteService commandBatchExecuteService;

    @Resource
    private InterventionConfigQueryInfrastructure<DemandConfigLightingDataDTO> interventionConfigQueryInfrastructure;

    @Resource
    SseHandlerContext handlerContext;




    @Override
    public void execute(String dateString) {
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());
        ContextUtil.setProjectId(projectCfg.getProjectId());

        //获取当前时间的照明需求场景
        Map<DemandTemplateTypeEnum, LightingSceneDTO> map = getTodayAllLightingSceneDTO();

        LogExecutor.instance().info(log, LOG_TAG, "获取场景需求");

        if (map.isEmpty()) {
            return;
        }

        map.forEach((k, v) -> {
            LogExecutor.instance().info(log, LOG_TAG, "开始执行计算",
                    Arrays.asList(Pair.of("需求类模版型", k),
                            Pair.of("场景信息", v)));

            DemandCommandTaskCtx ctx = new DemandCommandTaskCtx(v);
            execute(ctx);
        });

    }

    /**
     * 获取当天的所有场景
     * time 过滤时间后的场景
     */
    public Map<DemandTemplateTypeEnum, LightingSceneDTO> getTodayAllLightingSceneDTO() {

        Map<DemandTemplateTypeEnum, LightingSceneDTO> map = new HashMap<>();
        DemandTemplateTypeEnum.getLightingDemandTemplateType().forEach(t -> {
            LightingSceneDTO r = demandLightingStrategy.getSceneByNow(projectCfg.getProjectId(), t);
            if (r == null) {
                return;
            }
            map.put(t, r);
        });
        return map;
    }


    @Override
    public void execute(DemandCommandTaskCtx ctx) {


        if (ctx.check()) {
            LogExecutor.instance().error(log, LOG_TAG, "参数缺失跳过计算", Collections.singletonList(Pair.of("ctx", ctx)));
            return;
        }

        LogExecutor.instance().info(log, LOG_TAG, "开始计算", Collections.singletonList(Pair.of("ctx", ctx)));

        //删除历史指令
        delEquip(ctx);

        switch (ctx.getTemplateType()) {
            //公共照明 停车场
            case PUBLIC_AREA_LIGHTING:
                spaceCompute(ctx);
                break;
            case PARKING_LOT_LIGHTING:
                spaceCompute(ctx);
                break;
            default:
                //楼体/景观/标识照明 按照回路直接下发
                spaceComputeByCircuit(ctx);
        }

        saveEquipRecords(ctx);

        //当前时间的指令直接下发
        if (!ctx.isFutureTime()) {
            // 直接进行指令下发
            commandBatchExecuteService.commandExecuteByEquipRecord(ctx.getEquipRecordLis());

            // 照明需求变化sse推送
            try {
                handlerContext.getStrategy(SseEmitterEventEnum.LIGHT_DEMAND_CHANGE)
                        .doOperation(projectCfg.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
                handlerContext.getStrategy(SseEmitterEventEnum.LIGHT_LOOP)
                        .doOperation(projectCfg.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
            } catch (Exception e) {
                LogExecutor.instance().error(log, LOG_TAG, "照明需求变化 sse推送失败", e);
            }
        }


    }

    /**
     * 删除历史指令
     *
     * @param ctx
     */
    private void delEquip(DemandCommandTaskCtx ctx) {

        String time = ctx.getTime().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));

        //删除未来待执行的指令
        equipRecordService.deleteLightDemandByExpireTime(projectCfg.getGroupCode(), projectCfg.getProjectId(),
                time, ctx.getTemplateType().getValue(), ctx.getAi());
    }

    /**
     * 指令保存
     *
     * @param ctx
     */
    private void saveEquipRecords(DemandCommandTaskCtx ctx) {

        //构建回路的指令
        ctx.buildCircuitEquipRecordLis();

        if (CollectionUtils.isEmpty(ctx.getEquipRecordLis())) {
            return;
        }

        //只判断当前时间段指令 干预指令不过滤
        if (ctx.getAi() && !ctx.isFutureTime()) {
            List<EquipRecord> equipRecordList = equipRecordService.getEquipRecordByInfoCodeDesc(projectCfg.getGroupCode(), projectCfg.getProjectId(),
                    ctx.getEquipRecordIds(), AutoMaticInfoCodeEnum.getControlDemandSwitchState("4"));

            Map<String, EquipRecord> map = equipRecordList.stream().collect(Collectors.toMap(EquipRecord::getObjInfoIndexes, Function.identity()));
            //判断对象信息点值是否一致一致不重复添加
            ctx.getEquipRecordLis().removeIf(er -> er.checkInfoCodeAndVal(map.get(er.getObjInfoIndexes())));
        }

        if (CollectionUtils.isEmpty(ctx.getEquipRecordLis())) {
            return;
        }

        // 指令保存
        equipRecordService.saveBatch(ctx.getEquipRecordLis());

        // 查询出保存的数据 主要是获取到 ID(数据库自增) 通过对象 id 和批次号查询
        List<EquipRecord> rList = equipRecordService.queryByBatchNumber(ctx.getEquipRecordIds(), ctx.getBatchNumber());

        ctx.setEquipRecordLis(rList);
    }


    @Override
    public void executeIntervene(RunningMonitorSecondTypeEnum type) {

        log.info("干预计算触发-{}-{}", LOG_TAG, type);
        if (type == null) {
            return;
        }
        DemandTemplateTypeEnum demandTemplateType = type.toDemandTemplateTypeEnum();
        InterventionConfigQueryParam p = new InterventionConfigQueryParam();
        p.setRunningMonitorSecondType(type);

        p.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        //夜景照明按照项目获取配置
        if (demandTemplateType.isNightLighting()) {
            p.setFilterInstanceIds(Collections.singleton(ContextUtil.getProjectId()));
        } else {
            p.setInstanceType(DemandInstanceTypeEnum.SPACE);
            List<DtpRwdSpaceDTO> spList = demandLightingStrategy.listSpacesByCircuit(ContextUtil.getProjectId(), demandTemplateType, null, null);
            List<String> spId = new ArrayList<>();
            spList.forEach(sp -> spId.add(sp.getId()));
            p.setFilterInstanceIds(spId);
        }
        p.setDateTimeContainRange(new DateTimeRangeValueObject(DateUtils.now(), DateUtils.now().plusDays(3)));
        //获取干预配置
        List<InterventionConfigModel<DemandConfigLightingDataDTO>> r = interventionConfigQueryInfrastructure.of(p);
        log.info("干预计算-获取到的干预配置-{}-{}", LOG_TAG, r);

        if (CollectionUtils.isEmpty(r)) {
            return;
        }

        //按照开始结束时段对数据进行分组
        Map<String, List<InterventionConfigModel<DemandConfigLightingDataDTO>>> map = new HashMap<>();
        r.forEach(i -> {
            String key = i.getDateTimeRange().getBeginDateTime().toString() + i.getDateTimeRange().getEndDateTime().toString();
            if (map.containsKey(key)) {
                map.get(key).add(i);
            } else {
                List<InterventionConfigModel<DemandConfigLightingDataDTO>> l = new ArrayList<>();
                l.add(i);
                map.put(key, l);
            }
        });

        map.forEach((key, icm) -> {
            if (CollectionUtil.isEmpty(icm)) {
                return;
            }
            //获取时间模版数据
            InterventionConfigModel<DemandConfigLightingDataDTO> mb = icm.get(0);

            //装配数据
            Map<String, List<DemandLightingFuncTypeSettingDTO>> objCfg = getStringListMap(type, icm, demandTemplateType, mb);
            log.info("干预计算-获取到空间的干预配置-{}", objCfg);
            if (MapUtil.isEmpty(objCfg)) {
                return;
            }

            //取出对象配置分组
            Map<String, List<DemandLightingFuncTypeSettingDTO>> allCfg = new HashMap<>();
            icm.forEach(i -> {
                String id = i.getInstance().getInstanceId();
                List<DemandLightingFuncTypeSettingDTO> cfg = objCfg.get(id);
                if (CollectionUtil.isEmpty(cfg)) {
                    log.warn("干预计算-获取到配置为空-{}", id);
                    return;
                }
                allCfg.put(id, cfg);
            });
            //创建干预计算上下文
            DemandCommandTaskCtx ctx = new DemandCommandTaskCtx(type, mb.getDateTimeRange().getBeginDateTime(), mb.getDateTimeRange().getEndDateTime(), allCfg);

            //执行干预计算
            execute(ctx);
        });

    }

    /**
     * 装配数据
     *
     * @param type
     * @param icm
     * @param demandTemplateType
     * @param mb
     * @return
     */
    private Map<String, List<DemandLightingFuncTypeSettingDTO>> getStringListMap(RunningMonitorSecondTypeEnum type, List<InterventionConfigModel<DemandConfigLightingDataDTO>> icm, DemandTemplateTypeEnum demandTemplateType, InterventionConfigModel<DemandConfigLightingDataDTO> mb) {
        //装配数据
        Map<String, List<DemandLightingFuncTypeSettingDTO>> objCfg = new HashMap<>();
        if (demandTemplateType.isNightLighting()) {
            List<DemandLightingFuncTypeSettingDTO> cfgList = new ArrayList<>();
            icm.forEach(i -> {
                if (i.getConfig() == null || i.getConfig().getFinalConfig() == null) {
                    return;
                }
                cfgList.addAll(i.getConfig().getFinalConfig().getGlobalSettings());
            });

            objCfg.put(mb.getInstance().getInstanceId(), cfgList);
        } else {
            Map<String, DemandConfigLightingDataDTO> id2config = new HashMap<>();
            icm.forEach(i -> {
                if (i.getConfig() == null || i.getConfig().getFinalConfig() == null) {
                    return;
                }
                id2config.put(i.getInstance().getInstanceId(), i.getConfig().getFinalConfig());

            });
            objCfg = demandLightingStrategy.spaceSettingsBuilding(projectCfg.getProjectId(), type.toDemandTemplateTypeEnum(), id2config);
        }
        return objCfg;
    }


    /**
     * 空间状态计算
     * 1.获取空间树结构 查看空间的开启状态
     * 2.对全开全关的空间 按照空间下的回路功能类型下发指令
     * 3.部分开启的空间 空间下发 99 不控制状态,下发开启的回路指令
     *
     * @param ctx       执行上下文
     */
    public void spaceCompute(DemandCommandTaskCtx ctx) {

        StopWatch stopWatch = new StopWatch(LOG_TAG + "耗时统计-空间状态计算");
        LogExecutor.instance().info(log, LOG_TAG, "开始计算场景指令", Collections.singletonList(Pair.of("ctx", ctx)));

        //判断是否无需求
        if (ctx.getAi() && ctx.getIsNoScene()) {
            stopWatch.start(LOG_TAG + "耗时统计-无需求计算");
            noDemand(ctx);
            stopWatch.stop();
            log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
            return;
        }

        stopWatch.start(LOG_TAG + "耗时统计-获取项目下的总配置");
        Map<String, List<DemandLightingFuncTypeSettingDTO>> allCfg = null;
        //如果是干预需求 获取干预配置
        if (!ctx.getAi()) {
            allCfg = ctx.getCfList();
        } else {
            //获取项目下的总配置
            allCfg = demandLightingQueryService.listAllSpaceSceneObjectSettings(projectCfg.getProjectId(), ctx.getDemandTemplateId(), ctx.getSceneId());
        }

        log.info("获取项目下的总配置-{}", allCfg);

        //添加开启的回路
        ctx.addOpenCircuitIds(allCfg);
        stopWatch.stop();

        if (MapUtil.isEmpty(allCfg)) {
            LogExecutor.instance().info(log, LOG_TAG, "未获取空间配置跳过计算", Collections.singletonList(Pair.of("ctx", ctx)));
            return;
        }

        stopWatch.start(LOG_TAG + "耗时统计-指令计算");


        allCfg.forEach((sp, cfgList) -> {
            cfgList.forEach(cfg -> {
                //判断空间状态 全开全关还是部分开启 不分开启获取回路状态进行下发
                if (cfg.getSettingMode() != null) {
                    DemandLightingTypeSettingModeEnum st = cfg.getSettingMode();
                    //基础校验 获取执行信息点
                    AutoMaticInfoCodeEnum infCode = getAutoMaticInfoCodeEnum(cfg.getFuncType());
                    if (infCode == null) return;
                    switch (st) {
                        case ALL_OPEN:
                        case ALL_CLOSE:
                        case NOT_CONFIGURED:
                            //全开/全关/未配置
                            addEquipSwitch(ctx, st, sp, infCode);
                            break;
                        case PARTIAL_OPEN:
                            //部分开启
                            getEquipRecord(sp, infCode.getPracticalCode(), DemandCommandTaskCtx.OTHER, ctx);
                            //添加空间下回路的状态
                            if (CollectionUtils.isEmpty(cfg.getCircuitSettings())) {
                                LogExecutor.instance().info(log, LOG_TAG, "空间下回路数据为空", Collections.singletonList(Pair.of("cfg", cfg)));
                                return;
                            }
                            cfg.getCircuitSettings().forEach(cs -> addDemandLightingCircuitEquipSwitch(ctx, cs.getSettingMode(), cs.getCircuitId()));
                            break;
                        case FEEDBACK_ADJUSTMENT:
                            //反馈调节
                            feedbackAdjustmentCompute(ctx,cfg,sp,infCode);
                        default:
                            //不操作
                            break;
                    }

                }

            });
        });
        stopWatch.stop();
        log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));

    }

    /**
     * 反馈调节计算开关
     * @param ctx
     * @param cfg
     * @param sp
     * @param infCode
     */
   private void feedbackAdjustmentCompute(DemandCommandTaskCtx ctx,DemandLightingFuncTypeSettingDTO cfg
           ,String sp,AutoMaticInfoCodeEnum infCode){
       /**   空间照明孪生需求的写入逻辑：
        * （1）计算服务该空间的所有照明回路的开关设定需求。
        （2）按照明回路的功能类型分组判断对应功能类型下的照明回路的开关设定需求是否统一。
        a. 功能类型下的照明回路全部为开启，对应上述空间孪生控制需求写入  1=开启
        b.  功能类型下的照明回路全部为关闭或者未配置，对应上述空间孪生控制需求写入  2=关闭
        c. 功能类型下的照明回路全部为反馈调节，对应上述空间孪生控制需求写入  3=反馈（自动调节）
        d. 功能类型下的照明回路开关设定需求不统一，对应上述空间孪生控制需求写入  99=其他
        */

       //获取项目下的总配置
       DemandConfigLightingResultQueryDaoParam param = new DemandConfigLightingResultQueryDaoParam();
       param.setRelDemandLightingSceneId(ctx.getSceneId());
       param.setRelDemandTemplateId(ctx.getDemandTemplateId());
       param.setSt(StateTypeEnum.VALID);
       List<DemandLightingCircuitSettingDTO> circuitSettings = cfg.getCircuitSettings();
       Set<String>  circuitIds = new HashSet<>();
       for (DemandLightingCircuitSettingDTO circuitSetting : circuitSettings) {
           circuitIds.add(circuitSetting.getCircuitId());
       }
       param.setCircuitIds(circuitIds);
       List<DemandConfigLightingResult> dcList = demandConfigLightingResultService.list(projectCfg.getProjectId(), param);
       if (CollectionUtils.isEmpty(dcList)) {
           return;
       }
       // 回路级反馈条件由 LightingFeedbackAdjustTask 下发，此处仅计算空间级开关需求
       Integer allCount = dcList.size();
       Integer feedbackAdjustmentCount = 0;

       for (DemandConfigLightingResult demandConfigLightingResult : dcList) {
           DemandLightingCircuitSettingModeEnum circuitSettingMode = demandConfigLightingResult.getConfigData().getCircuitSettingMode();
           switch (circuitSettingMode) {
              case  FEEDBACK_ADJUSTMENT:
                  //回路的
                  addDemandLightingCircuitEquipSwitch(ctx, DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT,
                          demandConfigLightingResult.getCircuitId());
                  feedbackAdjustmentCount++;
                      break;
           }
       }
       //功能类型下的照明回路全部为反馈调节，对应上述空间孪生控制需求写入  3=反馈（自动调节）
       if(allCount.equals(feedbackAdjustmentCount))
           getEquipRecord(sp, infCode.getPracticalCode(), DemandCommandTaskCtx.FEEDBACK, ctx);
       else {
           // 功能类型下的照明回路开关设定需求不统一，对应上述空间孪生控制需求写入  99=其他
           getEquipRecord(sp, infCode.getPracticalCode(), DemandCommandTaskCtx.OTHER, ctx);
       }
   }
    /**
     * 无需求下发空间级别关
     * @param ctx
     * @return
     */
    private void noDemand(DemandCommandTaskCtx ctx) {

        switch (ctx.getTemplateType()) {
            //公共照明 停车场
            case PUBLIC_AREA_LIGHTING:
                noDemandBySpace(ctx);
                break;
            case PARKING_LOT_LIGHTING:
                noDemandBySpace(ctx);
                break;
            default:
                noDemandByCircuit(ctx);
        }

    }

    /**
     * 无需求下发回路级别关
     *
     * @param ctx
     */
    private void noDemandByCircuit(DemandCommandTaskCtx ctx) {
        List<DtpRwdCircuitDTO> rwdObjLst = demandLightingStrategy.listCircuits(projectCfg.getProjectId(), ctx.getTemplateType(), null);
        LogExecutor.instance().warn(log, LOG_TAG, "无需求下发回路级别关", Collections.singletonList(Pair.of("rwdObjLst", rwdObjLst)));
        rwdObjLst.forEach(i -> getEquipRecord(i.getId(), AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode(), DemandCommandTaskCtx.CLOSE, ctx));

    }

    /**
     * 无需求下发空间级别关
     *
     * @param ctx
     */
    private void noDemandBySpace(DemandCommandTaskCtx ctx) {

        List<DtpRwdSpaceDTO> rwdObjLst = demandLightingStrategy.listSpacesByCircuit(projectCfg.getProjectId(), ctx.getTemplateType(), null, null);

        if (CollectionUtils.isEmpty(rwdObjLst)) {
            LogExecutor.instance().warn(log, LOG_TAG, "无空间信息跳过", Collections.singletonList(Pair.of("ctx", ctx)));
            return;
        }

        List<DtpRwdCircuitDTO> hlList = demandLightingStrategy.listCircuits(projectCfg.getProjectId(), ctx.getTemplateType(), null);
        //获取回路 ID 和回路对象 MAP
        Map<String, DtpRwdCircuitDTO> hlMap = hlList.stream().collect(Collectors.toMap(DtpRwdCircuitDTO::getId, i -> i));
        //取出空间回路对象集合
        Map<String, List<DtpRwdCircuitDTO>> spIdByCircuit = new HashMap<>();
        rwdObjLst.forEach(i -> {
            if (CollectionUtils.isEmpty(i.getRelObjIds())) {
                return;
            }
            i.getRelObjIds().forEach(r -> {
                if (spIdByCircuit.containsKey(i.getId())) {
                    spIdByCircuit.get(i.getId()).add(hlMap.get(r));
                } else {
                    List<DtpRwdCircuitDTO> ls = new ArrayList<>();
                    ls.add(hlMap.get(r));
                    spIdByCircuit.put(i.getId(), ls);
                }
            });
        });


        rwdObjLst.forEach(i -> {
            LogExecutor.instance().warn(log, LOG_TAG, "无需求下发空间级别关", Collections.singletonList(Pair.of("i", i)));
            Set<String> funcType = new HashSet<>();

            List<DtpRwdCircuitDTO> cList = spIdByCircuit.get(i.getId());
            if (CollectionUtils.isEmpty(cList)) {
                LogExecutor.instance().info(log, LOG_TAG, "空间下无回路,跳过空间需求下发", Collections.singletonList(Pair.of("空间信息", i)));
                return;
            }

            cList.forEach(h -> funcType.add(h.getFuncType()));

            funcType.forEach(h -> {
                AutoMaticInfoCodeEnum infCode = AutoMaticInfoCodeEnum.getByCode(h);
                if (infCode == null) {
                    LogExecutor.instance().info(log, LOG_TAG, "照明功能类型未匹配上,跳过空间需求下发", Collections.singletonList(Pair.of("hlList", hlList)));
                    return;
                }
                addEquipSwitch(ctx, DemandLightingTypeSettingModeEnum.NOT_CONFIGURED, i.getId(), infCode);
            });

        });
    }

    /**
     * 基础校验
     *
     * @param t 照明类型
     * @return
     */
    private AutoMaticInfoCodeEnum getAutoMaticInfoCodeEnum(DtpRwdCircuitFuncTypeEnum t) {

        //当前只下发这几类 指令 进行一次判断
        if (AutoMaticInfoCodeEnum.controlDemandSwitchState(t.getValue())) {
            LogExecutor.instance().info(log, LOG_TAG, "获取数据为空跳过计算,非当前配置信息点", Collections.singletonList(Pair.of("sceneObjectSettings", t)));
            return null;
        }
        //取到信息点
        AutoMaticInfoCodeEnum infCode = AutoMaticInfoCodeEnum.getByCode(t.getValue());
        if (infCode == null) {
            LogExecutor.instance().info(log, LOG_TAG, "获取数据为空跳过计算,信息点为空", Collections.singletonList(Pair.of("infCode", t)));
            return null;
        }
        return infCode;
    }

    /**
     * 添加开关机状态
     *
     * @param ctx
     * @param st
     * @param spId
     * @param infCode
     */
    private void addEquipSwitch(DemandCommandTaskCtx ctx,
                                DemandLightingTypeSettingModeEnum st, String spId, AutoMaticInfoCodeEnum infCode) {

        if (infCode == null) {
            return;
        }

        switch (st) {
            case ALL_OPEN:
                getEquipRecord(spId, infCode.getPracticalCode(), DemandCommandTaskCtx.OPEN, ctx);
                break;
            case ALL_CLOSE:
            case NOT_CONFIGURED:
                getEquipRecord(spId, infCode.getPracticalCode(), DemandCommandTaskCtx.CLOSE, ctx);
            default:
        }
    }

    /**
     * //楼体/景观/标识照明 按照回路直接下发
     *
     * @param ctx
     */
    public void spaceComputeByCircuit(DemandCommandTaskCtx ctx) {

        StopWatch stopWatch = new StopWatch(LOG_TAG + "耗时统计-空间状态楼体/景观/标识照明计算");
        stopWatch.start(LOG_TAG + "耗时统计-总体计算");
        //判断是否无需求
        if (ctx.getAi() && ctx.getIsNoScene()) {
            noDemand(ctx);
            return;
        }

        if (!ctx.getAi()) {
            List<DemandLightingCircuitSettingDTO> circuitSettings = new ArrayList<>();
            //获取干预配置
            for (Map.Entry<String, List<DemandLightingFuncTypeSettingDTO>> entry : ctx.getCfList().entrySet()) {
                List<DemandLightingFuncTypeSettingDTO> v = entry.getValue();
                if (CollectionUtils.isEmpty(v)) {
                    continue;
                }
                v.forEach(i -> circuitSettings.addAll(i.getCircuitSettings()));
            }
            //添加指令
            circuitSettings.forEach(i -> addDemandLightingCircuitEquipSwitch(ctx, i.getSettingMode(), i.getCircuitId()));
        } else {
            //获取项目下的总配置
            DemandConfigLightingResultQueryDaoParam param = new DemandConfigLightingResultQueryDaoParam();
            param.setRelDemandLightingSceneId(ctx.getSceneId());
            param.setRelDemandTemplateId(ctx.getDemandTemplateId());
            param.setSt(StateTypeEnum.VALID);
            List<DemandConfigLightingResult> dcList = demandConfigLightingResultService.list(projectCfg.getProjectId(), param);
            if (CollectionUtils.isEmpty(dcList)) {
                return;
            }
            //获取当前场景 ID 下所有回路进行下发
            dcList.forEach(i -> addDemandLightingCircuitEquipSwitch(ctx, i.getConfigData().getCircuitSettingMode(), i.getCircuitId()));
        }
        stopWatch.stop();
        log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }

    /**
     * 照明回路开关状态
     *
     * @param ctx
     * @param st
     * @param objId
     */
    private void addDemandLightingCircuitEquipSwitch(DemandCommandTaskCtx ctx,
                                                     DemandLightingCircuitSettingModeEnum st, String objId) {

        if (Objects.requireNonNull(st) == DemandLightingCircuitSettingModeEnum.OPEN) {
            getEquipRecord(objId, AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode(), DemandCommandTaskCtx.OPEN, ctx);
        } else if(Objects.requireNonNull(st) == DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT){
            getEquipRecord(objId, AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode(), DemandCommandTaskCtx.FEEDBACK, ctx);
        }else {
            getEquipRecord(objId, AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode(), DemandCommandTaskCtx.CLOSE, ctx);
        }
    }

    /**
     * 获取指令
     *
     * @param i
     * @param infoCode
     * @param infoValue
     * @param ctx
     */
    private void getEquipRecord(String i, String infoCode, String infoValue, DemandCommandTaskCtx ctx) {

        LocalDateTime time = ctx.getTime();
        if (!ctx.isFutureTime()) {
            time = ctx.getNowTime();
        }

        EquipRecord er = new EquipRecord(projectCfg.getGroupCode(), projectCfg.getProjectId(), DemandTypeEnum.LIGHTING.getValue(), ctx.getTemplateSceneId(), i,
                new ChangeContent(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN).format(time),
                        infoCode, infoValue));
        er.getChangeContent().setAi(ctx.getAi());
        er.getChangeContent().setSourceFrom(SourceFromEnum.AI_DEMAND);
        er.getChangeContent().setBatchNumber(ChangeContent.buildBatchNumber(ctx.getTime()));
        er.setExecStatus(-1);
        er.init();

        //判断是否是干预需求 设置干预类型
        if (!ctx.getAi()) {
            er.getChangeContent().setSourceFrom(SourceFromEnum.Lighting_ARTIFICIAL);
            er.getChangeContent().setTimeDuration(ctx.getTimeDuration());
        }

        //回路的指令单独存储
        if (AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode().equals(infoCode)) {
            //判断是否功能类型下已经存在了开启的回路 已经存在就不在处理
            if (ctx.getOpenFuncTypeCircuitIds().contains(i)) {
                return;
            }
            //其它场景正常添加回路
            ctx.addCircuitEquipRecordMap(er);
        } else {
            ctx.getEquipRecordLis().add(er);
        }
    }

    /**
     * 获取类型的信息点
     *
     * @param circuitFuncType
     * @return
     */
    public String getInfoCode(String circuitFuncType) {
        AutoMaticInfoCodeEnum type = AutoMaticInfoCodeEnum.getControlDemand(circuitFuncType);
        if (type != null) {
            return type.getPracticalCode();
        }
        return null;
    }


}
