package com.persagy.apccontrol.common.service.strategy.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.StopWatch;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.QueryFreezePumpGroupParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.QueryHotColdStationSystemParameterVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.RunningCoolerVO;
import com.persagy.apccontrol.basic.model.vo.system.QuerySystemParameterVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.common.service.strategy.DataBoardService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author LiuYang
 * @since v5.241101.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DataBoardServiceImpl implements DataBoardService {

    private static final String LOG_TAG = "数据看板";

    private final DtpRwdConvert dtpRwdConvert;
    private final BizDtpRwdService bizDtpRwdService;
    private final DtpRwdClientService dtpRwdClientService;
    private final BizStrategyQueryService bizStrategyQueryService;
    private final IotConverter iotConverter;

    private static final String PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP_CODE = DashBoardInfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP.getCode();
    private static final String CHILL_WATER_OUT_TEMP_SET_CODE = DashBoardInfoCodeEnum.CHILL_WATER_OUT_TEMP_SET.getCode();
    private static final String CHILL_WATER_DETA_TEMP_CODE = DashBoardInfoCodeEnum.CHILL_WATER_DETA_TEMP.getCode();
    private static final String CHILL_WATER_DETA_TEMP_SET_CODE = DashBoardInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.getCode();
    private static final String CHILL_WATER_DETA_PRESS_SET_CODE = DashBoardInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.getCode();
    private static final String CHILL_WATER_DETA_PRESS_CODE = DashBoardInfoCodeEnum.CHILL_WATER_DETA_PRESS.getCode();
    private static final String COOL_WATER_MAIN_RETURN_TEMP_CODE = DashBoardInfoCodeEnum.COOL_WATER_MAIN_RETURN_TEMP.getCode();
    private static final String COOL_WATER_RETURN_TEMP_SET_CODE = DashBoardInfoCodeEnum.COOL_WATER_RETURN_TEMP_SET.getCode();
    private static final String WORST_TERMINAL_DETA_PRESS = DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode();
    private static final String WORST_TERMINAL_DETA_PRESS_SET = DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode();

    private static final List<String> COLD_STATION_INFO_CODE_LIST = Arrays.asList(
            PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP_CODE, CHILL_WATER_OUT_TEMP_SET_CODE,
            CHILL_WATER_DETA_TEMP_CODE, CHILL_WATER_DETA_TEMP_SET_CODE,
            CHILL_WATER_DETA_PRESS_SET_CODE, CHILL_WATER_DETA_PRESS_CODE,
            COOL_WATER_MAIN_RETURN_TEMP_CODE, COOL_WATER_RETURN_TEMP_SET_CODE,
            WORST_TERMINAL_DETA_PRESS, WORST_TERMINAL_DETA_PRESS_SET
    );

    @Override
    public QuerySystemParameterVO querySystemParameter(String systemId) {

        QuerySystemParameterVO parameter = new QuerySystemParameterVO();

        if (StringUtils.isBlank(systemId)) {
            return parameter;
        }
        StopWatch stopWatch = StopWatch.create("查询系统运行参数");


        Map<String, Object> infoCodeValueMap = queryInfoCodeValueMap(
                Collections.singletonList(systemId), COLD_STATION_INFO_CODE_LIST);

        // 冷机开启台数 根据冷机设备的runStatus点加和计算，统计runStatus =1的设备数量，按照 大、小 冷机分组统计）
        stopWatch.start("查询运行冷机数据");
        RunningCoolerVO rc = getRunningChillerData(ContextUtil.getProjectId(), systemId);
        stopWatch.stop();
        if (rc != null) {
            parameter.getChillerLNum().setActualValue(rc.getRunningChillerLNum());
            parameter.getChillerSNum().setActualValue(rc.getRunningChillerSNum());
        }
        parameter.getChillerLNum().setInfoCodeName(LocaleMessageUtil.getMessage("key_3eea8a31cd3243a6900dd2fa17c7acc5"));
        parameter.getChillerSNum().setInfoCodeName(LocaleMessageUtil.getMessage("key_3eea8a31cd3243a6900dd2fa17c7acc5"));

        // 冷机出水温度 primaryChillWaterMainSupplyTemp
        parameter.getPrimaryChillWaterMainSupplyTemp().setActualValue(
                infoCodeValueMap.getOrDefault(PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP_CODE, null));
        parameter.getPrimaryChillWaterMainSupplyTemp().setValue(
                infoCodeValueMap.getOrDefault(CHILL_WATER_OUT_TEMP_SET_CODE, null));
        parameter.getPrimaryChillWaterMainSupplyTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_7931bb5c6f7b4cdca0e539844364814b"));

        // 冷冻水供回水温差 chillWaterDetaTemp
        parameter.getChillWaterDetaTemp().setActualValue(
                infoCodeValueMap.getOrDefault(CHILL_WATER_DETA_TEMP_CODE, null));
        parameter.getChillWaterDetaTemp().setValue(
                infoCodeValueMap.getOrDefault(CHILL_WATER_DETA_TEMP_SET_CODE, null));
        parameter.getChillWaterDetaTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_fd764bdffee34190a5dd3839483f79b8"));

        // 冷冻水供回水压差 chillWaterDetaPress
        parameter.getChillWaterDetaPress().setActualValue(
                infoCodeValueMap.getOrDefault(CHILL_WATER_DETA_PRESS_CODE, null));
        parameter.getChillWaterDetaPress().setValue(
                infoCodeValueMap.getOrDefault(CHILL_WATER_DETA_PRESS_SET_CODE, null));
        parameter.getChillWaterDetaPress().setInfoCodeName(LocaleMessageUtil.getMessage("key_4ad868c4085443f78fcf941144038179"));

        // 冷却塔出水温度 coolWaterMainReturnTemp
        parameter.getCoolWaterMainReturnTemp().setActualValue(
                infoCodeValueMap.getOrDefault(COOL_WATER_MAIN_RETURN_TEMP_CODE, null));
        parameter.getCoolWaterMainReturnTemp().setValue(
                infoCodeValueMap.getOrDefault(COOL_WATER_RETURN_TEMP_SET_CODE, null));
        parameter.getCoolWaterMainReturnTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_cb620c4f3f774321965766c1fe140ec3"));

        // 最不利末端压差 worstTerminalDetaPress
        parameter.getWorstTerminalDetaPress().setActualValue(
                infoCodeValueMap.getOrDefault(WORST_TERMINAL_DETA_PRESS, null));
        parameter.getWorstTerminalDetaPress().setValue(
                infoCodeValueMap.getOrDefault(WORST_TERMINAL_DETA_PRESS_SET, null));
        parameter.getWorstTerminalDetaPress().setInfoCodeName(LocaleMessageUtil.getMessage("key_b75490681a314861ad1f25eaa4d68df9"));

        if (log.isDebugEnabled()) {
            LogExecutor.project(ContextUtil.getProjectId()).debug(log, LOG_TAG, "查询系统运行参数结束",
                    Collections.singletonList(Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS))));
        }
        return parameter;
    }

    private static final String RUN_STATUS = InfoCodeEnum.RUN_STATUS.value();
    private static final String ONLINE_STATUS = InfoCodeEnum.ONLINE_STATUS.value();
    private static final List<String> RUN_ONLINE_STATUS = Arrays.asList(RUN_STATUS, ONLINE_STATUS);

    public RunningCoolerVO getRunningChillerData(String projectId, String systemId) {
        RunningCoolerVO runningCoolerVO = new RunningCoolerVO();
        // 查询当前运行的冷机台数
        AcccccGroupDTO acccacGroup = bizDtpRwdService.queryAcccccGroupBySystemId(ContextUtil.getGroupCode(), projectId, systemId);
        List<String> small = acccacGroup.getSmall();
        List<String> large = acccacGroup.getLarge();
        HashSet<String> objects = new HashSet<>();
        boolean smallNotEmpty = CollectionUtils.isNotEmpty(small);
        boolean largeNotEmpty = CollectionUtils.isNotEmpty(large);
        if (smallNotEmpty) {
            objects.addAll(small);
        }
        if (largeNotEmpty) {
            objects.addAll(large);
        }

        if (CollectionUtils.isEmpty(objects)) {
            return runningCoolerVO;
        }

        // 按对象id，再按信息点，最后的value是数值方便取值
        Map<String, Map<String, String>> objectInfoCodeValueMap = queryRunOfflineObjectMap(objects);

        if (smallNotEmpty) {
            // 计算小型冷机运行台数
            runningCoolerVO.setRunningChillerSNum(small.stream()
                    .mapToInt(s -> isRunning(objectInfoCodeValueMap, s) ? 1 : 0)
                    .sum());
        }
        if (largeNotEmpty) {
            // 计算大型冷机运行台数
            runningCoolerVO.setRunningChillerLNum(large.stream()
                    .mapToInt(l -> isRunning(objectInfoCodeValueMap, l) ? 1 : 0)
                    .sum());
        }
        return runningCoolerVO;
    }

    /**
     * 判断冷机是否运行的方法
     */
    private boolean isRunning(Map<String, Map<String, String>> objectInfoCodeValueMap, String id) {
        Map<String, String> infoCodeValue = objectInfoCodeValueMap.getOrDefault(id, new HashMap<>());
        return "1".equals(infoCodeValue.getOrDefault(RUN_STATUS, "0"))
                && "1".equals(infoCodeValue.getOrDefault(ONLINE_STATUS, "0"));
    }


    /**
     * 查询对象信息指定时间点的运行在离线状态
     * 底层重写冷源、热源、冷热源系统替换为前端请求头中传递过来的时间
     *
     * @param objects 对象信息
     * @return 响应map<对象id, < 信息点, 数值>>
     */
    private Map<String, Map<String, String>> queryRunOfflineObjectMap(Collection<String> objects) {
        // latest 双条件门控下查询最新数据，未命中回退 15min 查询
        List<InfoCodeValueVO> infoCodeValues = this.queryObjectIdLatestPersistentDataList(objects, RUN_ONLINE_STATUS);

        // 按对象id，再按信息点，最后的value是数值方便取值
        return infoCodeValues.stream()
                .collect(Collectors.groupingBy(
                                InfoCodeValueVO::getObjectId,
                                Collectors.toMap(
                                        InfoCodeValueVO::getInfoCode,
                                        item -> String.valueOf(item.getInfoCodeValue())
                                )
                        )
                );
    }

    private static final String SYS_SWITCH_SET_CODE = DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode();
    private static final String CHILL_WATER_MAIN_SUPPLY_TEMP_CODE = DashBoardInfoCodeEnum.CHILL_WATER_MAIN_SUPPLY_TEMP.getCode();
    private static final String HEAT_WATER_SUPPLY_TEMP_SET_CODE = DashBoardInfoCodeEnum.HEAT_WATER_SUPPLY_TEMP_SET.getCode();
    private static final String CHILL_HEAT_WATER_DETA_TEMP_CODE = DashBoardInfoCodeEnum.CHILL_HEAT_WATER_DETA_TEMP.getCode();
    private static final String CHILL_HEAT_WATER_DETA_PRESS_CODE = DashBoardInfoCodeEnum.CHILL_HEAT_WATER_DETA_PRESS.getCode();
    private static final List<String> HOT_COLD_STATION_INFO_CODE_LIST = Arrays.asList(
            SYS_SWITCH_SET_CODE, CHILL_WATER_MAIN_SUPPLY_TEMP_CODE,
            HEAT_WATER_SUPPLY_TEMP_SET_CODE, CHILL_WATER_OUT_TEMP_SET_CODE,
            CHILL_HEAT_WATER_DETA_TEMP_CODE, CHILL_HEAT_WATER_DETA_PRESS_CODE,
            CHILL_WATER_DETA_PRESS_SET_CODE, CHILL_WATER_DETA_TEMP_SET_CODE
    );

    @Override
    public QueryHotColdStationSystemParameterVO queryHotColdStationSystemParameter(
            String systemId, String classCode) {
        QueryHotColdStationSystemParameterVO vo = new QueryHotColdStationSystemParameterVO();
        if (StringUtils.isBlank(systemId)) {
            return vo;
        }

        // 开启台数 通过总数查询状态是开启的数量
        AtomicInteger nb = new AtomicInteger();
        List<DtpRwdObjectDTO> dtpRwdObject = bizDtpRwdService.getAcahcObj(ContextUtil.getProjectId(), systemId);

        Set<String> objectIds = dtpRwdObject.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        Map<String, Map<String, String>> objectInfoCodeValueMap = queryRunOfflineObjectMap(objectIds);

        if (CollectionUtils.isNotEmpty(dtpRwdObject)) {
            dtpRwdObject.forEach(i -> {
                Map<String, String> infoCodeValue = objectInfoCodeValueMap.getOrDefault(i.getId(), new HashMap<>());
                boolean isRunning = "1".equals(infoCodeValue.getOrDefault(RUN_STATUS, "0"))
                        && "1".equals(infoCodeValue.getOrDefault(ONLINE_STATUS, "0"));
                if (isRunning) {
                    nb.getAndIncrement();
                }
            });
        }


        Map<String, Object> infoCodeValueMap = queryInfoCodeValueMap(
                Collections.singletonList(systemId), HOT_COLD_STATION_INFO_CODE_LIST);

        vo.getChillerLNum().setActualValue(nb.get());
        vo.getChillerLNum().setInfoCodeName(LocaleMessageUtil.getMessage("key_acde8da0c63d403da806a9d9574ff2a1"));

        // 开关机状态
        vo.getSysSwitchSet().setActualValue(infoCodeValueMap.getOrDefault(SYS_SWITCH_SET_CODE, null));
        vo.getSysSwitchSet().setInfoCodeName(LocaleMessageUtil.getMessage("key_171522a78c87426890cc2cc5d89b7a52"));

        // 主机出水温度 chillWaterMainSupplyTemp
        vo.getHeatWaterSupplyTemp().setActualValue(infoCodeValueMap.getOrDefault(CHILL_WATER_MAIN_SUPPLY_TEMP_CODE, null));
        vo.getHeatWaterSupplyTemp().setValue(infoCodeValueMap.getOrDefault(HEAT_WATER_SUPPLY_TEMP_SET_CODE, null));
        vo.getHeatWaterSupplyTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_f8026dfaa102432c9e3e0ce438028bd7"));

        // 冷却塔出水温度 coolWaterMainReturnTemp
        vo.getChillWaterOutTemp().setActualValue(infoCodeValueMap.getOrDefault(CHILL_WATER_MAIN_SUPPLY_TEMP_CODE, null));
        vo.getChillWaterOutTemp().setValue(infoCodeValueMap.getOrDefault(CHILL_WATER_OUT_TEMP_SET_CODE, null));
        vo.getChillWaterOutTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_3333b345f83642e99c6b10adda52fa6c"));


        // 供回水温差反馈
        vo.getChillWaterDetaTemp().setActualValue(infoCodeValueMap.getOrDefault(CHILL_HEAT_WATER_DETA_TEMP_CODE, null));
        vo.getChillWaterDetaTemp().setValue(infoCodeValueMap.getOrDefault(CHILL_WATER_DETA_TEMP_SET_CODE, null));
        vo.getChillWaterDetaTemp().setInfoCodeName(LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce"));

        // 冷冻水供回水压差
        vo.getChillWaterDetaPress().setActualValue(infoCodeValueMap.getOrDefault(CHILL_HEAT_WATER_DETA_PRESS_CODE, null));
        vo.getChillWaterDetaPress().setValue(infoCodeValueMap.getOrDefault(CHILL_WATER_DETA_PRESS_SET_CODE, null));
        vo.getChillWaterDetaPress().setInfoCodeName(LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a"));

        return vo;
    }

    /**
     * 查询指定时间点的信息点分精度数据
     *
     * @param objects   对象id
     * @param infoCodes 信息点
     * @return <信息点,iot数值>
     */
    private Map<String, Object> queryInfoCodeValueMap(Collection<String> objects, Collection<String> infoCodes) {
        // latest 双条件门控下查询最新数据，未命中回退 15min 查询
        List<InfoCodeValueVO> infoCodeValues = this.queryObjectIdLatestPersistentDataList(objects, infoCodes);

        return infoCodeValues.stream().filter(x -> Objects.nonNull(x.getInfoCodeValue()))
                .collect(Collectors.toMap(InfoCodeValueVO::getInfoCode, InfoCodeValueVO::getInfoCodeValue));
    }

    /**
     * 查询对象信息点的最新持久化数据（分精度改造 latest 双条件门控）
     * <p>双条件（请求头 removeDefaultIpType=true 且壳配置持久化数据查询逻辑=latest）命中时查询"距离当前最新"的数据，
     * 否则由底层回退为 15min + 前端替换参数查询，与改造前行为一致；
     * moment 查询按 对象+信息点 返回最新一条，此处直接转换消费</p>
     *
     * @param objectIds 对象ID集合
     * @param infoCodes 信息点编码集合
     * @return 最新持久化数据集合
     * @since v7.260901.0
     */
    private List<InfoCodeValueVO> queryObjectIdLatestPersistentDataList(
            Collection<String> objectIds, Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Lists.newArrayList();
        }
        List<EnergyDataResponse> responses = this.dtpRwdClientService.queryIpDataOnMoment(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                new ArrayList<>(objectIds), new ArrayList<>(infoCodes));
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        return responses.stream()
                .map(this.iotConverter::toInfoCodeValueVo)
                .collect(Collectors.toList());
    }

    // 末级泵组信息点
    private static final List<String> MANUAL_LASTPUMB_INFO_CODE_LIST = Arrays.asList(
            DashBoardInfoCodeEnum.ENABLE_CTRL.getCode(),
            DashBoardInfoCodeEnum.BRANCH_DETA_TEMP.getCode(),
            DashBoardInfoCodeEnum.BRANCH_DETA_PRESS.getCode(),
            DashBoardInfoCodeEnum.BRANCH_DETA_TEMP_SET.getCode(),
            DashBoardInfoCodeEnum.BRANCH_DETA_PRESS_ET.getCode(),
            DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode(),
            DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode()
    );

    @Override
    public List<ColdSiteSystemContrastVO> queryManualLastpumbParameter(String systemId) {
        List<ColdSiteSystemContrastVO> list = new ArrayList<>();
        if (StringUtils.isBlank(systemId)) {
            return list;
        }

        StopWatch stopWatch = StopWatch.create("泵组运行信息查询");

        // 查询系统下的泵组信息
        stopWatch.start("查询系统下的泵组信息");
        StrategyFreezeGroupObjectQueryParam objectQueryParam = new StrategyFreezeGroupObjectQueryParam();
        objectQueryParam.setProjectId(ContextUtil.getProjectId());
        List<DtpRwdFreezePumpDTO> dtpRwdFreezePumpDTO = bizStrategyQueryService.listLastFreezePumpGroups(objectQueryParam);
        stopWatch.stop();

        if (CollectionUtils.isEmpty(dtpRwdFreezePumpDTO)) {
            return list;
        }
        Map<String, List<DtpRwdFreezePumpDTO>> collect = dtpRwdFreezePumpDTO.stream().collect(Collectors.groupingBy(DtpRwdFreezePumpDTO::getRelationAcccId));
        List<DtpRwdFreezePumpDTO> lastFreezePumpGroups = collect.get(systemId);
        if (CollectionUtils.isEmpty(lastFreezePumpGroups)) {
            return list;
        }

        List<String> objIds = new ArrayList<>();
        lastFreezePumpGroups.forEach(i -> objIds.add(i.getId()));

        // 查询末级泵组最新持久化数据（latest 双条件门控，未命中回退 15min 查询）
        List<InfoCodeValueVO> infoCodeValues = this.queryObjectIdLatestPersistentDataList(objIds, MANUAL_LASTPUMB_INFO_CODE_LIST);

        Map<String, List<InfoCodeValueVO>> objectInfoCodeValueMap =
                infoCodeValues.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));

        // 查询对象信息
        List<DtpRwdObjectDTO> objs = dtpRwdClientService.listObject(true, true, ContextUtil.getProjectId(),
                new DtpRwdObjectQueryParam().setId(objIds));
        Map<String, DtpRwdObjectDTO> objectsMap = objs.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity()));


        stopWatch.start("遍历整合数据");


        lastFreezePumpGroups.forEach(i -> {

            DtpRwdObjectDTO dtpRwdObjectInfo = objectsMap.getOrDefault(i.getId(), null);

            List<InfoCodeValueVO> rList = objectInfoCodeValueMap.getOrDefault(i.getId(), new ArrayList<>());
            Map<String, InfoCodeValueVO> infoCodeMap = rList.stream().collect(Collectors.toMap(InfoCodeValueVO::getInfoCode, Function.identity()));

            // 开关机状态
            ColdSiteSystemContrastVO enableCtrlContrast = new ColdSiteSystemContrastVO();
            enableCtrlContrast.setLocalName(dtpRwdObjectInfo == null ? null : dtpRwdObjectInfo.getLocalName());
            enableCtrlContrast.setObjectId(i.getId());
            InfoCodeValueVO wc1 = infoCodeMap.get(DashBoardInfoCodeEnum.ENABLE_CTRL.getCode());
            enableCtrlContrast.setActualValue(wc1 == null ? null : wc1.getInfoCodeValue());
            enableCtrlContrast.setInfoCode(DashBoardInfoCodeEnum.ENABLE_CTRL.getCode());
            enableCtrlContrast.setInfoCodeName(LocaleMessageUtil.getMessage("key_88321fc596fb4bc4adf842c0d1089132"));
            list.add(enableCtrlContrast);

            ColdSiteSystemContrastVO branchDetaTempContrast = new ColdSiteSystemContrastVO();
            branchDetaTempContrast.setLocalName(dtpRwdObjectInfo == null ? null : dtpRwdObjectInfo.getLocalName());
            branchDetaTempContrast.setObjectId(i.getId());

            InfoCodeValueVO branchDetaTemp = infoCodeMap.get(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP.getCode());
            branchDetaTempContrast.setActualValue(branchDetaTemp == null ? null : branchDetaTemp.getInfoCodeValue());
            InfoCodeValueVO branchDetaTempSet = infoCodeMap.get(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP_SET.getCode());
            branchDetaTempContrast.setValue(branchDetaTempSet == null ? null : branchDetaTempSet.getInfoCodeValue());
            branchDetaTempContrast.setInfoCodeName(LocaleMessageUtil.getMessage("key_6a5f754410f44f078a167ae27a0d5be6"));
            branchDetaTempContrast.setInfoCode(DashBoardInfoCodeEnum.BRANCH_DETA_TEMP_SET.getCode());
            list.add(branchDetaTempContrast);

            ColdSiteSystemContrastVO branchDetaPressContrast = BeanUtil.toBean(branchDetaTempContrast, ColdSiteSystemContrastVO.class);

            InfoCodeValueVO branchDetaPress = infoCodeMap.get(DashBoardInfoCodeEnum.BRANCH_DETA_PRESS.getCode());
            branchDetaPressContrast.setActualValue(branchDetaPress == null ? null : branchDetaPress.getInfoCodeValue());
            InfoCodeValueVO branchDetaPressSet = infoCodeMap.get(DashBoardInfoCodeEnum.BRANCH_DETA_PRESS_ET.getCode());
            branchDetaPressContrast.setValue(branchDetaPressSet == null ? null : branchDetaPressSet.getInfoCodeValue());
            branchDetaPressContrast.setInfoCodeName(LocaleMessageUtil.getMessage("key_0f548e33e93d42b09933c96ade91756b"));
            branchDetaPressContrast.setInfoCode(DashBoardInfoCodeEnum.BRANCH_DETA_PRESS_ET.getCode());
            list.add(branchDetaPressContrast);

            ColdSiteSystemContrastVO worstTerminalDetaPressVO = BeanUtil.toBean(branchDetaTempContrast, ColdSiteSystemContrastVO.class);
            InfoCodeValueVO worstTerminalDetaPress = infoCodeMap.get(DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode());
            worstTerminalDetaPressVO.setActualValue(worstTerminalDetaPress == null ? null : worstTerminalDetaPress.getInfoCodeValue());
            InfoCodeValueVO worstTerminalDetaPressSet = infoCodeMap.get(DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode());
            worstTerminalDetaPressVO.setValue(worstTerminalDetaPressSet == null ? null : worstTerminalDetaPressSet.getInfoCodeValue());
            worstTerminalDetaPressVO.setInfoCodeName(LocaleMessageUtil.getMessage("key_b75490681a314861ad1f25eaa4d68df9"));
            worstTerminalDetaPressVO.setInfoCode(DashBoardInfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode());
            list.add(worstTerminalDetaPressVO);

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
