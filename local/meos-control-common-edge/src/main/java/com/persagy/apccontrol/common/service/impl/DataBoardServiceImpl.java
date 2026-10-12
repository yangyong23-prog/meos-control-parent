package com.persagy.apccontrol.common.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.StopWatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.hot.DtpRwdCentralHotInfoCodeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.QueryFreezePumpGroupParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.QueryHotColdStationSystemParameterVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.RunningCoolerVO;
import com.persagy.apccontrol.basic.model.vo.system.QuerySystemParameterVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.common.model.vo.strategy.chiller.QueryHotStationSystemParameterVO;
import com.persagy.apccontrol.common.service.DataBoardService;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.apccontrol.common.service.StrategyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yuyao
 */
@Slf4j
@Service
public class DataBoardServiceImpl implements DataBoardService {

    private static final String LOG_TAG = "数据看板";

    @Autowired
    private ProjectCfg projectCfg;

    @Resource
    private DtpRwdConvert dtpRwdConvert;

    @Autowired
    private BizDtpRwdService bizDtpRwdService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private EquipRecordService equipRecordService;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Autowired
    private IotDataCacheService iotDataCacheService;

    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Autowired
    private BizStrategyQueryService bizStrategyQueryService;

    @Override
    public QuerySystemParameterVO querySystemParameter(String systemId) {

        QuerySystemParameterVO vo = new QuerySystemParameterVO();

        if (StringUtils.isBlank(systemId)) {
            return vo;
        }
        StopWatch stopWatch = StopWatch.create("查询系统运行参数");
        stopWatch.start("查询设备运行记录");
        //获取对象 执行结果的 MAP 映射关系
        Map<String, EquipRecord> map = getStringEquipRecordMap(Collections.singletonList(systemId));

        stopWatch.stop();

        // 冷机开启台数 根据冷机设备的runStatus点加和计算，统计runStatus =1的设备数量，按照 大、小 冷机分组统计）
        stopWatch.start("查询运行冷机数据");
        RunningCoolerVO rc = strategyService.getRunningChillerData(projectCfg.getProjectId(), systemId);
        stopWatch.stop();
        if (rc != null) {
            vo.getChillerLNum().setActualValue(rc.getRunningChillerLNum());
            vo.getChillerSNum().setActualValue(rc.getRunningChillerSNum());
        }
        vo.getChillerLNum().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILLER_L_NUM.getCode()));
        vo.getChillerSNum().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILLER_S_NUM.getCode()));
        vo.getChillerLNum().setInfoCodeName(LocaleMessageUtil.getMessage("key_3eea8a31cd3243a6900dd2fa17c7acc5"));
        vo.getChillerSNum().setInfoCodeName(LocaleMessageUtil.getMessage("key_3eea8a31cd3243a6900dd2fa17c7acc5"));

        // 冷机开关机状态
        stopWatch.start("查询冷机开关机状态");
        FeedbackInfoMsgMqttVO kgj = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode(), null);
        stopWatch.stop();
        vo.getSysSwitchSet().setActualValue(kgj == null ? null : kgj.getDataValue());
        vo.getSysSwitchSet().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode()));
        vo.getSysSwitchSet().setInfoCodeName(LocaleMessageUtil.getMessage("key_06d76e8fd7284c8a8e1be4a47911074a"));

        // 冷机出水温度 primaryChillWaterMainSupplyTemp
        stopWatch.start("查询冷机出水温度");
        FeedbackInfoMsgMqttVO tshIf = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP.getCode(), null);
        stopWatch.stop();
        vo.getPrimaryChillWaterMainSupplyTemp().setActualValue(tshIf == null ? null : tshIf.getDataValue());
        vo.getPrimaryChillWaterMainSupplyTemp().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILL_WATER_OUT_TEMP_SET.getCode()));
        vo.getPrimaryChillWaterMainSupplyTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_7931bb5c6f7b4cdca0e539844364814b"));

        // 冷冻水供回水温差 chillWaterDetaTemp
        stopWatch.start("查询冷冻水供回水温差");
        FeedbackInfoMsgMqttVO cdtIf = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.CHILL_WATER_DETA_TEMP.getCode(), null);
        stopWatch.stop();
        vo.getChillWaterDetaTemp().setActualValue(cdtIf == null ? null : cdtIf.getDataValue());
        vo.getChillWaterDetaTemp().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.getCode()));
        vo.getChillWaterDetaTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_fd764bdffee34190a5dd3839483f79b8"));

        // 冷冻水供回水压差 chillWaterDetaPress
        stopWatch.start("查询冷冻水供回水压差");
        FeedbackInfoMsgMqttVO cwdIf = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.CHILL_WATER_DETA_PRESS.getCode(), null);
        stopWatch.stop();
        vo.getChillWaterDetaPress().setActualValue(cwdIf == null ? null : cwdIf.getDataValue());
        vo.getChillWaterDetaPress().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.getCode()));
        vo.getChillWaterDetaPress().setInfoCodeName(LocaleMessageUtil.getMessage("key_4ad868c4085443f78fcf941144038179"));

        // 冷却塔出水温度 coolWaterMainReturnTemp
        stopWatch.start("查询冷却塔出水温度");
        FeedbackInfoMsgMqttVO cwmIf = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.COOL_WATER_MAIN_RETURN_TEMP.getCode(), null);
        stopWatch.stop();
        vo.getCoolWaterMainReturnTemp().setActualValue(cwmIf == null ? null : cwmIf.getDataValue());
        vo.getCoolWaterMainReturnTemp().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.COOL_WATER_RETURN_TEMP_SET.getCode()));
        vo.getCoolWaterMainReturnTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_cb620c4f3f774321965766c1fe140ec3"));

        // 冷却塔出水温度 coolWaterMainReturnTemp
        stopWatch.start(LocaleMessageUtil.getMessage("key_b75490681a314861ad1f25eaa4d68df9"));
        FeedbackInfoMsgMqttVO worstTerminalDetaPress = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode(), null);
        stopWatch.stop();
        vo.getWorstTerminalDetaPress().setActualValue(worstTerminalDetaPress == null ? null : worstTerminalDetaPress.getDataValue());
        vo.getWorstTerminalDetaPress().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode()));
        vo.getWorstTerminalDetaPress().setInfoCodeName(LocaleMessageUtil.getMessage("key_b75490681a314861ad1f25eaa4d68df9"));

        if (log.isDebugEnabled()) {
            LogExecutor.project(projectCfg.getProjectId()).debug(log, LOG_TAG, "查询系统运行参数结束",
                    Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        }
        return vo;
    }

    @Override
    public QueryHotColdStationSystemParameterVO queryHotColdStationSystemParameter(String systemId, String classCode) {
        QueryHotColdStationSystemParameterVO vo = new QueryHotColdStationSystemParameterVO();
        if (StringUtils.isBlank(systemId)) {
            return vo;
        }

        // 获取信息点配置
        Set<String> infoCodes = Sets.newHashSet(DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode(), DashBoardInfoCodeEnum.CHILL_WATER_MAIN_SUPPLY_TEMP.getCode(),
                "chillHeatWaterDetaTemp", "chillHeatWaterDetaPress");
        List<RcInfoCodeCfg> infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode, infoCodes);
        Map<String, RcInfoCodeCfg> infoCodeCfgMap = CollectionUtils.isEmpty(infoCodeConfigs) ? new HashMap<>()
                : infoCodeConfigs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v1));

        //获取对象 执行结果的 MAP 映射关系
        Map<String, EquipRecord> map = getStringEquipRecordMap(Collections.singletonList(systemId));

        // 开启台数 通过总数查询状态是开启的数量
        AtomicInteger nb = new AtomicInteger();
        List<DtpRwdObjectDTO> dtpRwdObject = bizDtpRwdService.getAcahcObj(projectCfg.getProjectId(), systemId);
        if (CollectionUtils.isNotEmpty(dtpRwdObject)) {
            dtpRwdObject.forEach(i -> {
                Boolean isYx = strategyService.queryObjectIsRunning(i.getId());
                if (isYx) {
                    nb.getAndIncrement();
                }
            });
        }
        vo.getChillerLNum().setActualValue(nb.get());
        vo.getChillerLNum().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILLER_L_NUM.getCode()));
        vo.getChillerLNum().setInfoCodeName(LocaleMessageUtil.getMessage("key_acde8da0c63d403da806a9d9574ff2a1"));

        // 开关机状态
        FeedbackInfoMsgMqttVO kgj = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode(), infoCodeCfgMap.get(DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode()));
        vo.getSysSwitchSet().setActualValue(kgj == null ? null : kgj.getDataValue());
        vo.getSysSwitchSet().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode()));
        vo.getSysSwitchSet().setInfoCodeName(LocaleMessageUtil.getMessage("key_171522a78c87426890cc2cc5d89b7a52"));

        // 主机出水温度 chillWaterMainSupplyTemp
        FeedbackInfoMsgMqttVO tshIf = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.CHILL_WATER_MAIN_SUPPLY_TEMP.getCode(), infoCodeCfgMap.get(DashBoardInfoCodeEnum.CHILL_WATER_MAIN_SUPPLY_TEMP.getCode()));
        vo.getHeatWaterSupplyTemp().setActualValue(tshIf == null ? null : tshIf.getDataValue());
        vo.getHeatWaterSupplyTemp().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.HEAT_WATER_SUPPLY_TEMP_SET.getCode()));
        vo.getHeatWaterSupplyTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_f8026dfaa102432c9e3e0ce438028bd7"));

        // 冷却塔出水温度 coolWaterMainReturnTemp
        FeedbackInfoMsgMqttVO cwmIf = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.CHILL_WATER_MAIN_SUPPLY_TEMP.getCode(), infoCodeCfgMap.get(DashBoardInfoCodeEnum.CHILL_WATER_MAIN_SUPPLY_TEMP.getCode()));
        vo.getChillWaterOutTemp().setActualValue(cwmIf == null ? null : cwmIf.getDataValue());
        vo.getChillWaterOutTemp().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILL_WATER_OUT_TEMP_SET.getCode()));
        vo.getChillWaterOutTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_3333b345f83642e99c6b10adda52fa6c"));


        // 供回水温差反馈 供水干管温度（chillWaterMainSupplyTemp）-回水干管温度（chillWaterMainReturnTemp）
        FeedbackInfoMsgMqttVO chillHeatWaterDetaTemp = iotDataCacheService.queryIotRealDataVO(systemId, "chillHeatWaterDetaTemp", infoCodeCfgMap.get("chillHeatWaterDetaTemp"));
        //BigDecimal rv = getBranchSupplyReturnTemp(systemId, DashBoardInfoCodeEnum.chillWaterMainSupplyTemp, DashBoardInfoCodeEnum.chillWaterMainReturnTemp);
        vo.getChillWaterDetaTemp().setActualValue(chillHeatWaterDetaTemp == null ? null : chillHeatWaterDetaTemp.getDataValue());
        // 供水温差设定 chillWaterDetaTempSet
        vo.getChillWaterDetaTemp().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.getCode()));
        vo.getChillWaterDetaTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce"));

        // 冷冻水供回水压差 供水干管压力（chillWaterMainSupplyPress）-回水干管压力（chillWaterMainReturnPress）
        FeedbackInfoMsgMqttVO chillHeatWaterDetaPress = iotDataCacheService.queryIotRealDataVO(systemId, "chillHeatWaterDetaPress", infoCodeCfgMap.get("chillHeatWaterDetaPress"));
        //BigDecimal mrp = getBranchSupplyReturnTemp(systemId, DashBoardInfoCodeEnum.chillWaterMainSupplyPress, DashBoardInfoCodeEnum.chillWaterMainReturnPress);
        vo.getChillWaterDetaPress().setActualValue(chillHeatWaterDetaPress == null ? null : chillHeatWaterDetaPress.getDataValue());
        // 供水压差设定 chillWaterDetaPressSet
        vo.getChillWaterDetaPress().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.getCode()));
        vo.getChillWaterDetaPress().setInfoCodeName(LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a"));

        return vo;
    }

    @Override
    public QueryHotStationSystemParameterVO queryHotStationSystemParameter(String systemId, String classCode) {
        QueryHotStationSystemParameterVO vo = new QueryHotStationSystemParameterVO();
        if (StringUtils.isBlank(systemId)) {
            return vo;
        }

        // 获取信息点配置
        Set<String> infoCodes = Sets.newHashSet(DashBoardInfoCodeEnum.EQUIP_SWITCH_SET.getCode()
                , DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaTempSet.getValue()
                , DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaPressureSet.getValue()
                , DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaTemp.getValue()
                , DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaPressure.getValue()

                , "2heatWaterMainSupplyTemp");
        List<RcInfoCodeCfg> infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode, infoCodes);
        Map<String, RcInfoCodeCfg> infoCodeCfgMap = CollectionUtils.isEmpty(infoCodeConfigs) ? new HashMap<>()
                : infoCodeConfigs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v1));

        //获取对象 执行结果的 MAP 映射关系
        Map<String, EquipRecord> map = getStringEquipRecordMap(Collections.singletonList(systemId));


        // 开关机状态
        FeedbackInfoMsgMqttVO kgj = iotDataCacheService.queryIotRealDataVO(systemId, DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode(), infoCodeCfgMap.get(DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode()));
        vo.getSysSwitchSet().setActualValue(kgj == null ? null : kgj.getDataValue());
        vo.getSysSwitchSet().setValue(getSetValue(map, systemId, DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode()));
        vo.getSysSwitchSet().setInfoCodeName(LocaleMessageUtil.getMessage("key_171522a78c87426890cc2cc5d89b7a52"));


        // 出水温度
        FeedbackInfoMsgMqttVO chillHeatWaterDetaTemp = iotDataCacheService.queryIotRealDataVO(systemId, "2heatWaterMainSupplyTemp", infoCodeCfgMap.get("2heatWaterMainSupplyTemp"));
        vo.getWaterOutTemp().setActualValue(chillHeatWaterDetaTemp == null ? null : chillHeatWaterDetaTemp.getDataValue());
        // 热水出水温度设定
        vo.getWaterOutTemp().setValue(getSetValue(map, systemId, DtpRwdCentralHotInfoCodeEnum.HOT_WATER_OUT_TEMP_SET.getValue()));
        vo.getWaterOutTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_0d7baf0b866749a286869e856b97a6a9"));

        // 供回水压差
        FeedbackInfoMsgMqttVO chillHeatWaterDetaPress = iotDataCacheService.queryIotRealDataVO(systemId, "heatSecondarySideWaterDetaPressure", infoCodeCfgMap.get("heatSecondarySideWaterDetaPressure"));
        vo.getWaterDetaPress().setActualValue(chillHeatWaterDetaPress == null ? null : chillHeatWaterDetaPress.getDataValue());
        // 热源二次侧供回水压差设定
        vo.getWaterDetaPress().setValue(getSetValue(map, systemId, DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaPressureSet.getValue()));
        vo.getWaterDetaPress().setInfoCodeName(LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a"));


        //供回水温差
        FeedbackInfoMsgMqttVO heatSecondarySideWaterDetaTemp = iotDataCacheService.queryIotRealDataVO(systemId, "heatSecondarySideWaterDetaTemp", infoCodeCfgMap.get("heatSecondarySideWaterDetaTemp"));
        vo.getChillWaterDetaTemp().setActualValue(heatSecondarySideWaterDetaTemp == null ? null : heatSecondarySideWaterDetaTemp.getDataValue());
        // 供回水温差设定
        vo.getChillWaterDetaTemp().setValue(getSetValue(map, systemId, DtpRwdCentralHotInfoCodeEnum.heatSecondarySideWaterDetaTempSet.getValue()));
        vo.getChillWaterDetaTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce"));


        return vo;
    }

    // 获取设定值
    private String getSetValue(Map<String, EquipRecord> map, String objId, String infoCode) {
        if (map.isEmpty()) {
            return null;
        }

        if (map.get(objId + infoCode) == null) {
            return null;
        }

        return map.get(objId + infoCode).getChangeContent().getInfoValue();
    }

    @Override
    public List<ColdSiteSystemContrastVO> queryManualLastpumbParameter(String systemId) {
        List<ColdSiteSystemContrastVO> list = new ArrayList<>();
        if (StringUtils.isBlank(systemId)) {
            return list;
        }

        StopWatch stopWatch = StopWatch.create("泵组运行信息查询");

        // 查询系统下的泵组信息
        stopWatch.start("查询系统下的泵组信息");
        StrategyFreezeGroupObjectQueryParam dd = new StrategyFreezeGroupObjectQueryParam();
        dd.setProjectId(projectCfg.getProjectId());
        List<DtpRwdFreezePumpDTO> dtpRwdFreezePumpDTO = bizStrategyQueryService.listLastFreezePumpGroups(dd);
        stopWatch.stop();

        if (CollectionUtils.isEmpty(dtpRwdFreezePumpDTO)) {
            return list;
        }
        Map<String, List<DtpRwdFreezePumpDTO>> collect = dtpRwdFreezePumpDTO.stream().collect(Collectors.groupingBy(DtpRwdFreezePumpDTO::getRelationAcccId));
        List<DtpRwdFreezePumpDTO> lastFreezePumpGroups = collect.get(systemId);
        if (CollectionUtils.isEmpty(lastFreezePumpGroups)) {
            return list;
        }

        List<String> objId = new ArrayList<>();
        lastFreezePumpGroups.forEach(i -> objId.add(i.getId()));

        //获取对象 执行结果的 MAP 映射关系
        stopWatch.start("获取对象 执行结果的 MAP 映射关系");
        Map<String, EquipRecord> map = getStringEquipRecordMap(objId);
        stopWatch.stop();


        stopWatch.start("遍历整合数据");


        lastFreezePumpGroups.forEach(i -> {
            // 查询对象信息
            DtpRwdObjectDTO obj = dtpRwdClientService.getObject(true, true, projectCfg.getProjectId(),
                    new DtpRwdObjectQueryParam().setId(Lists.newArrayList(i.getId())));

            //批量获取 redis 数据
            List<String> infoCodes = new ArrayList<>();
            infoCodes.add(DashBoardInfoCodeEnum.ENABLE_CTRL.getCode());
            infoCodes.add(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP.getCode());
            infoCodes.add(DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode());
            infoCodes.add(DashBoardInfoCodeEnum.BRANCH_DETA_PRESS.getCode());

            List<FeedbackInfoMsgMqttVO> rList = iotDataCacheService.queryIotRealDataVO(i.getId(), infoCodes);
            Map<String, FeedbackInfoMsgMqttVO> infoCodeMap = rList.stream().collect(Collectors.toMap(FeedbackInfoMsgMqttVO::getInfoCode, Function.identity()));

            // 开关机状态
            ColdSiteSystemContrastVO vo1 = new ColdSiteSystemContrastVO();
            vo1.setLocalName(obj == null ? null : obj.getLocalName());
            vo1.setObjectId(i.getId());
            FeedbackInfoMsgMqttVO wc1 = infoCodeMap.get(DashBoardInfoCodeEnum.ENABLE_CTRL.getCode());
            vo1.setActualValue(wc1 == null ? null : wc1.getDataValue());
            String cwm1 = getSetValue(map, i.getId(), DashBoardInfoCodeEnum.ENABLE_CTRL.getCode());
            vo1.setValue(cwm1);
            vo1.setInfoCode(DashBoardInfoCodeEnum.ENABLE_CTRL.getCode());
            vo1.setInfoCodeName(LocaleMessageUtil.getMessage("key_88321fc596fb4bc4adf842c0d1089132"));
            list.add(vo1);

            ColdSiteSystemContrastVO vo = new ColdSiteSystemContrastVO();
            vo.setLocalName(obj == null ? null : obj.getLocalName());
            vo.setObjectId(i.getId());

            FeedbackInfoMsgMqttVO wc2 = infoCodeMap.get(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP.getCode());
            vo.setActualValue(wc2 == null ? null : wc2.getDataValue());
            vo.setValue(getSetValue(map, i.getId(), DashBoardInfoCodeEnum.BRANCH_DETA_TEMP_SET.getCode()));
            vo.setInfoCodeName(LocaleMessageUtil.getMessage("key_6a5f754410f44f078a167ae27a0d5be6"));
            vo.setInfoCode(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP_SET.getCode());
            list.add(vo);

            ColdSiteSystemContrastVO vo2 = BeanUtil.toBean(vo, ColdSiteSystemContrastVO.class);

            FeedbackInfoMsgMqttVO wc3 = infoCodeMap.get(DashBoardInfoCodeEnum.BRANCH_DETA_PRESS.getCode());
            vo2.setActualValue(wc3 == null ? null : wc3.getDataValue());
            vo2.setValue(getSetValue(map, i.getId(), DashBoardInfoCodeEnum.BRANCH_DETA_PRESS_ET.getCode()));
            vo2.setInfoCodeName(LocaleMessageUtil.getMessage("key_0f548e33e93d42b09933c96ade91756b"));
            vo2.setInfoCode(DashBoardInfoCodeEnum.BRANCH_DETA_PRESS_ET.getCode());
            list.add(vo2);

            ColdSiteSystemContrastVO vo3 = BeanUtil.toBean(vo, ColdSiteSystemContrastVO.class);
            FeedbackInfoMsgMqttVO worstTerminalDetaPress = infoCodeMap.get(DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode());
            vo3.setActualValue(worstTerminalDetaPress == null ? null : worstTerminalDetaPress.getDataValue());
            vo3.setValue(getSetValue(map, i.getId(), DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode()));
            vo3.setInfoCodeName(LocaleMessageUtil.getMessage("key_b75490681a314861ad1f25eaa4d68df9"));
            vo3.setInfoCode(DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode());
            list.add(vo3);

        });


        // 通过设备名称进行排序
        list.sort((o1, o2) -> ObjectUtils.compare(o1.getLocalName(), o2.getLocalName()));

        stopWatch.stop();

        if (log.isDebugEnabled()) {
            LogExecutor.instance().info(log, LOG_TAG, "泵组运行信息查询结束",
                    Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        }

        return list;
    }

    /**
     * 获取对象 执行结果的 MAP 映射关系
     *
     * @param objId
     * @return
     */
    private Map<String, EquipRecord> getStringEquipRecordMap(List<String> objId) {
        List<EquipRecord> equipRecordList = equipRecordService.getEquipRecordByInfoCodeDesc(projectCfg.getGroupCode(), projectCfg.getProjectId(), objId, null);
        Map<String, EquipRecord> map = new HashMap<>();
        equipRecordList.forEach(i -> map.put(i.getObjId() + i.getInfoCode(), i));
        return map;
    }

    @Override
    public List<DtpRwdObjectVO> queryObjByFreezePumpGroupId(QueryFreezePumpGroupParam param) {
        // 查询
        DtpRwdObjectQueryParam query = new DtpRwdObjectQueryParam();
        // 查询冷站系统关联的冷冻水泵控制组
        query.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(param.getFreezePumpGroupId()));
        // 查出泵
        List<DtpRwdObjectDTO> lstPump = dtpRwdClientService.listObject(true, false, param.getProjectId(), query);

        return dtpRwdConvert.toObjectVo(lstPump);
    }


}
