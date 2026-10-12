package com.persagy.apccontrol.basic.service.duty.strategy;

import cn.hutool.core.date.DatePattern;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractControlConsoleStrategyServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.HotColdCardDataVO;
import com.persagy.apccontrol.basic.model.vo.duty.HotColdChildCardDataVO;
import com.persagy.apccontrol.basic.model.vo.duty.RunningStatisticsVO;
import com.persagy.apccontrol.basic.model.vo.outside.SystemGraphVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningTimeVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.SystemGraphExternalService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS;
import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;
import static com.persagy.apccontrol.basic.model.base.InfoCodeEnum.ONLINE_STATUS;
import static com.persagy.apccontrol.basic.model.base.InfoCodeEnum.RUN_STATUS;


/**
 * 冷源运行值班首页策略实现类
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/11/6 15:27
 * @since v5.241002.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ColdSourceHomePageCardDataHandler extends AbstractHomePageCardDataHandler<HotColdCardDataVO> implements LogComponent {

    private final DtpRwdClientService dtpRwdClientService;
    private final BizDtpRwdService bizDtpRwdService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final SystemGraphExternalService systemGraphExternalService;
    private final AbstractControlConsoleStrategyServer abstractControlConsoleStrategyServer;
    protected final NacosSwitchConfig nacosSwitchConfig;

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.COLD_SOURCE;
    }

    private static final ArrayList<String> WITH_COLUMNS = Lists.newArrayList(
            DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.localId, DtpRwdObjectDTO.Fields.localName);

    private static final Collection<DtpRwdClassCodeEnum> CLASS_CODE_LIST = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACCCCC, DtpRwdClassCodeEnum.ACCCSC, DtpRwdClassCodeEnum.ACCCPC, DtpRwdClassCodeEnum.ACCCAC,
            DtpRwdClassCodeEnum.ACCCOT, DtpRwdClassCodeEnum.ACCCCP, DtpRwdClassCodeEnum.ACCCOP, DtpRwdClassCodeEnum.ACCCGP,
            DtpRwdClassCodeEnum.ACCCVD, DtpRwdClassCodeEnum.ACCCCD, DtpRwdClassCodeEnum.ACCCTU, DtpRwdClassCodeEnum.ACCCST,
            DtpRwdClassCodeEnum.ACCCWT, DtpRwdClassCodeEnum.ACCCOC, DtpRwdClassCodeEnum.ACGEST, DtpRwdClassCodeEnum.ACGEET,
            DtpRwdClassCodeEnum.ACGECF, DtpRwdClassCodeEnum.ACGEWS, DtpRwdClassCodeEnum.ACGEDB, DtpRwdClassCodeEnum.ACGECL,
            DtpRwdClassCodeEnum.ACGEFP, DtpRwdClassCodeEnum.ACGEPT, DtpRwdClassCodeEnum.ACGEHE, DtpRwdClassCodeEnum.ACVVNO,
            DtpRwdClassCodeEnum.ACVVEO, DtpRwdClassCodeEnum.ACVVNJ, DtpRwdClassCodeEnum.ACVVEJ, DtpRwdClassCodeEnum.ACVVSB,
            DtpRwdClassCodeEnum.ACVVDB, DtpRwdClassCodeEnum.ACVVWC, DtpRwdClassCodeEnum.ACVVRV, DtpRwdClassCodeEnum.ACVVSV,
            DtpRwdClassCodeEnum.ACVVEV, DtpRwdClassCodeEnum.ACVVTW, DtpRwdClassCodeEnum.WESSTP, DtpRwdClassCodeEnum.WESSFL,
            DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.WESSPM, DtpRwdClassCodeEnum.ACCCOF
    );
    public static final String SYS_COP_CODE = DashBoardInfoCodeEnum.SYS_COP.getCode();
    public static final String CHILLERS_COP_CODE = DashBoardInfoCodeEnum.CHILLERS_COP.getCode();
    public static final String PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP_CODE = DashBoardInfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP.getCode();
    public static final String PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP_CODE = DashBoardInfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP.getCode();

    /**
     * 信息卡的信息点
     */
    private static final List<String> CARD_INFORMATION_POINT = Arrays.asList(
            // SYS_COP_CODE,
            // CHILLERS_COP_CODE,
            PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP_CODE,
            PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP_CODE
    );

    @Override
    @SuppressWarnings({"DuplicatedCode"})
    public HotColdCardDataVO execute(HomePageParam param) {
        LocalDateTime fiveMinutePrecisionTime = DateUtils.getTheLastPrecisionMinutes(5);
        LocalDateTime tenMinutesAgo = fiveMinutePrecisionTime.minusMinutes(5);
        HashMap<String, Object> extra = new HashMap<>();
        extra.put(CommonConst.IPDATA_START_TIME, tenMinutesAgo.format(DateUtils.DTF_SDF_SECOND));
        extra.put(CommonConst.IPDATA_END_TIME, fiveMinutePrecisionTime.format(DateUtils.DTF_SDF_SECOND));
        extra.put(CommonConst.IPTYPE, DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES.value());
        extra.put(CommonConst.RUNNING_MONITOR_TYPE, RunningMonitorTypeEnum.COLD_SOURCE);
        ContextUtil.getContext().setExtra(extra);
        HotColdCardDataVO vo = new HotColdCardDataVO();
        vo.setRunningMonitorType(getRunningMonitorType());
        String projectId = ContextUtil.getProjectId();
        List<HotColdChildCardDataVO> childCardList = new ArrayList<>();
        String systemClassCode = DtpRwdClassCodeEnum.COLD_SYSTEM.value();
        DtpRwdObjectQueryParam criteria = new DtpRwdObjectQueryParam()
                .addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setClassCode(systemClassCode)
                .setWithColumns(WITH_COLUMNS);
        List<DtpRwdObjectDTO> rwdSystems = dtpRwdClientService.listObject(false, false, projectId, criteria);

        if (CollectionUtils.isEmpty(rwdSystems)) {
            return vo;
        }

        Set<String> systemIds = rwdSystems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        List<DtpRwdObjectDTO> list = dtpRwdClientService.listObject(
                false, false, projectId, getQueryParam(systemIds));

        // 根据所属的系统id进行分组 转成for循环
        Map<String, Set<String>> relObjectIdMap = list.stream()
                .filter(obj -> Objects.nonNull(obj.getRelObjIds()))
                .flatMap(obj -> obj.getRelObjIds().stream()
                        .map(relId -> Pair.of(relId, obj.getId())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));

        HashMap<String, CardTagVO> systemCardTagMap = new HashMap<>();
        Set<String> forceCodes = Collections.singleton(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());
        relObjectIdMap.forEach((systemId, relEquipmentIds) -> {
            Map<String, ObjectIdAndForceCodeQuery> queryMap = ObjectIdAndForceCodeQuery.buildDutyHomePageQuery(
                    RunningMonitorTypeEnum.COLD_SOURCE.getValue(), relEquipmentIds, forceCodes);
            DutyHomePageGroupDTO dutyHomePageGroup = queryHomePageCardGroupIotData(queryMap);
            CardTagVO tag = buildCardTagVO(dutyHomePageGroup, relEquipmentIds);
            systemCardTagMap.put(systemId, tag);
        });

        // 查询对象信息点数值map
        Map<String, Map<String, Object>> systemIdInfoCodeValueMap = objectInfoCodeValueStatistics(systemIds);

        Map<String, RunStatusEnum> objectIdAndRunStatus = new HashMap<>();
        List<EquipRecord> equipRecords = new ArrayList<>();
        try {
            EquipRecordQueryDaoParam equipRecordQueryDaoParam = new EquipRecordQueryDaoParam()
                    .setProjectId(projectId)
                    .setGroupCode(ContextUtil.getGroupCode())
                    .setSystemCode(systemClassCode).setObjIds(systemIds)
                    .setChangeContent(new ChangeContent().setInfoCode(
                            AutoMaticInfoCodeEnum.RUN_STATUS.getPracticalCode())).setExecStatus(1);
            objectIdAndRunStatus = abstractControlConsoleStrategyServer.mapRecentRunningStatus(equipRecordQueryDaoParam);
            // 查询冷站运行记录  只显示已经执行成功和未执行的指令
            equipRecordQueryDaoParam.setBeginExecuteTime(DateUtils.getPastTime(1))
                    .setEndExecuteTime(DateUtils.getLastTime(2)).setExecStatus(null)
                    .setExecStatuses(Arrays.asList(-1, 1));
            equipRecords = abstractControlConsoleStrategyServer.listEquipRecord(equipRecordQueryDaoParam);
        } catch (Exception e) {
            logProjectInstance().error("查询运行记录异常", e);
        }
        Map<String, RunningStatisticsVO> runningStatisticsMap = numberOfLargeAndSmallCoolers(systemIds);

        // 获取可以跳转的系统图
        HashMap<String, SystemGraphVO> drawingsThatCanBeJumped = getDrawingsThatCanBeJumped(systemClassCode, systemIds);

        Map<String, RunStatusEnum> finalObjectIdAndRunStatus = objectIdAndRunStatus;
        List<EquipRecord> finalEquipRecords = equipRecords;
        rwdSystems.forEach(rwdSystem -> {
            // 按步骤拆分方法
            String systemId = rwdSystem.getId();
            Map<String, Object> infoCodeMap = systemIdInfoCodeValueMap.getOrDefault(systemId, new HashMap<>());
            HotColdChildCardDataVO hotColdChildCardDataVO = HotColdChildCardDataVO.of(rwdSystem, drawingsThatCanBeJumped, infoCodeMap);
            hotColdChildCardDataVO.setCardTag(systemCardTagMap.get(systemId));
            hotColdChildCardDataVO.setRunStatus(finalObjectIdAndRunStatus.getOrDefault(systemId, null));
            hotColdChildCardDataVO.setRunningStatistics(runningStatisticsMap.getOrDefault(systemId, new RunningStatisticsVO()));
            fillRunningTime(hotColdChildCardDataVO, finalEquipRecords);
            setDemandTemplateType(hotColdChildCardDataVO);
            childCardList.add(hotColdChildCardDataVO);
        });

        vo.setHotColdchildCardVOList(childCardList);
        return vo;
    }

    /**
     * 获取可供跳转的系统图以及图名
     *
     * @param systemClassCode 系统对象类
     * @param systemIds       系统id
     * @return <系统id,图名>
     */
    public HashMap<String, SystemGraphVO> getDrawingsThatCanBeJumped(String systemClassCode, Collection<String> systemIds) {
        List<SystemGraphVO> systemGraphVOS = systemGraphExternalService.querySystemGraphList(Collections.singletonList(systemClassCode));
        HashMap<String, SystemGraphVO> drawingsThatCanBeJumped = new HashMap<>();

        if (CollectionUtils.isNotEmpty(systemGraphVOS)) {
            for (SystemGraphVO systemGraphVO : systemGraphVOS) {
                String systemObjectId = systemGraphVO.getSystemObjectId();
                if (systemIds.contains(systemObjectId)
                        && !drawingsThatCanBeJumped.containsKey(systemObjectId)) {
                    drawingsThatCanBeJumped.put(systemObjectId, systemGraphVO);
                }
            }
        }
        return drawingsThatCanBeJumped;
    }

    /**
     * 获取rwd查询参数
     *
     * @param systemIds 系统id
     * @return 查询参数
     */
    private DtpRwdObjectQueryParam getQueryParam(Set<String> systemIds) {
        return new DtpRwdObjectQueryParam()
                .setWithColumns(WITH_COLUMNS)
                .addWithColumn(DtpRwdObjectDTO.Fields.relObjIds)
                .setClassCodeEnum(CLASS_CODE_LIST)
                .addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_IN_SYSTEM)
                        .setObjFrom(systemIds));
    }


    /**
     * 查询对象信息点数值
     *
     * @param objIds    对象id
     * @return <对象id,<信息点,数值>>>
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> objectInfoCodeValueStatistics(Collection<String> objIds) {
        List<InfoCodeValueVO> infoCodeValueVOList =
                abstractIotDataCacheServer.listInfoCodeValue(ContextUtil.getProjectId(),
                        DtpRwdClassCodeEnum.COLD_SYSTEM, objIds, CARD_INFORMATION_POINT);
        //sysCOP 和 chillersCOP 单独查询日纬度数据
        Set<String> sysCopCodes = new HashSet<>();
        sysCopCodes.add(SYS_COP_CODE);
        sysCopCodes.add(CHILLERS_COP_CODE);
        List<EnergyDataResponse> dataVal = dtpRwdClientService.queryIpDataOnMoment(new HashSet<>(objIds),
                sysCopCodes, DtpRwdPersistenceTimeTypeEnum.analyze(nacosSwitchConfig.getBdtpTimeTypeEfficiency()),
                LocalDateTime.of(DateUtils.now().toLocalDate(),
                        LocalTime.MIDNIGHT), null);
        
        if (CollectionUtils.isNotEmpty(dataVal)) {
            dataVal.forEach(data -> {
                InfoCodeValueVO vo = new InfoCodeValueVO();
                vo.setObjectId(data.getObjectId());
                vo.setInfoCode(data.getInfoCode());
                vo.setInfoCodeValue(data.getIpValue());
                infoCodeValueVOList.add(vo);
            });
        }
        
        return infoCodeValueVOList.stream().collect(Collectors.groupingBy(
                InfoCodeValueVO::getObjectId,
                Collectors.toMap(
                        InfoCodeValueVO::getInfoCode, InfoCodeValueVO::getInfoCodeValue
                )
        ));
    }

    /**
     * 查询空调需求配置的营业状态
     *
     * @param hotColdChildCardDataVO 响应数据
     */
    public void setDemandTemplateType(HotColdChildCardDataVO hotColdChildCardDataVO) {
        DemandTemplateTypeEnum airconditioning = DemandTemplateTypeEnum.AIRCONDITIONING;
        //填充小卡片的日期标签
        Map<String, DemandTemplateVO> nameMap = getDemandTemplateType(Collections.singletonList(airconditioning));
        String airCondition = airconditioning.value();
        if (nameMap.containsKey(airCondition)) {
            DemandTemplateVO demandTemplateVO = nameMap.get(airCondition);
            hotColdChildCardDataVO.setTemplateName(demandTemplateVO.getTemplateName());
            hotColdChildCardDataVO.setTemplateTag(demandTemplateVO.getTemplateTag());
        }
    }

    /**
     * 大小冷机运行台数
     *
     * @param systemIds 系统id集合
     * @return <系统id,运行统计>
     */
    public Map<String, RunningStatisticsVO> numberOfLargeAndSmallCoolers(Set<String> systemIds) {
        ArrayList<String> objectIds = new ArrayList<>();
        Map<String, AcccccGroupDTO> acccccGroupMap = new HashMap<>();
        systemIds.forEach(systemId -> {
            // 查询当前运行的冷机台数
            AcccccGroupDTO acccacGroup = bizDtpRwdService.queryAcccccGroupBySystemId(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), systemId);
            List<String> small = acccacGroup.getSmall();
            if (CollectionUtils.isNotEmpty(small)) {
                objectIds.addAll(small);
            }
            List<String> large = acccacGroup.getLarge();
            if (CollectionUtils.isNotEmpty(large)) {
                objectIds.addAll(large);
            }
            acccccGroupMap.put(systemId, acccacGroup);
        });

        Set<String> finalObjectIds = objectIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        // 获取所有的对象的信息点的数值
        Map<String, Map<String, String>> objectInfoCodeValueMap = queryRunOfflineObjectMap(finalObjectIds);
        Map<String, RunningStatisticsVO> runningStatisticsMap = new HashMap<>();
        acccccGroupMap.forEach((systemId, acccccGroup) -> {
            // 大小冷机统计运行数量, 运行且在线
            RunningStatisticsVO value = new RunningStatisticsVO();
            List<String> small = acccccGroup.getSmall();
            if (CollectionUtils.isNotEmpty(small)) {
                for (String s : small) {
                    Map<String, String> infoCodeValue = objectInfoCodeValueMap.getOrDefault(s, new HashMap<>());
                    Long chillerSNum = value.getChillerSNum();
                    if (Objects.isNull(chillerSNum)) {
                        chillerSNum = 0L;
                    }
                    boolean isRunning = "1".equals(infoCodeValue.getOrDefault(RUN_STATUS.getCode(), "0"))
                            && "1".equals(infoCodeValue.getOrDefault(ONLINE_STATUS.getCode(), "0"));
                    if (isRunning) {
                        chillerSNum += 1;
                    }
                    value.setChillerSNum(chillerSNum);
                }
            }
            List<String> large = acccccGroup.getLarge();
            if (CollectionUtils.isNotEmpty(large)) {
                for (String l : large) {
                    Map<String, String> infoCodeValue = objectInfoCodeValueMap.getOrDefault(l, new HashMap<>());
                    boolean isRunning = "1".equals(infoCodeValue.getOrDefault(RUN_STATUS.getCode(), "0"))
                            && "1".equals(infoCodeValue.getOrDefault(ONLINE_STATUS.getCode(), "0"));
                    Long chillerLNum = value.getChillerLNum();
                    if (Objects.isNull(chillerLNum)) {
                        chillerLNum = 0L;
                    }
                    if (isRunning) {
                        chillerLNum += 1;
                    }
                    value.setChillerLNum(chillerLNum);
                }
            }
            runningStatisticsMap.put(systemId, value);
        });
        return runningStatisticsMap;
    }

    private static final List<String> RUN_OFFLINE = Arrays.asList(RUN_STATUS.getCode(), ONLINE_STATUS.getCode());

    /**
     * 查询对象信息指定时间点的运行在离线状态
     *
     * @param objects 对象信息
     * @return 响应map<对象id, < 信息点, 数值>>
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    private Map<String, Map<String, String>> queryRunOfflineObjectMap(Collection<String> objects) {
        List<InfoCodeValueVO> infoCodeValues =
                abstractIotDataCacheServer.listInfoCodeValueInOneHour(objects, RUN_OFFLINE);

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


    /**
     * 填充运行时间
     *
     * @param target       目标源
     * @param equipRecords 设备执行记录
     * @since v5.241101.0
     */
    @SuppressWarnings({"DuplicatedCode"})
    public void fillRunningTime(@NonNull HotColdChildCardDataVO target, @NonNull List<EquipRecord> equipRecords) {
        if (CollectionUtils.isEmpty(equipRecords)) {
            return;
        }
        DateTimeFormatter dateTimeFormatter = DatePattern.PURE_DATETIME_FORMATTER;
        equipRecords = equipRecords.stream()
                .filter(item -> item.getSystemId().equals(target.getSystemId()))
                .filter(item -> Objects.nonNull(item.getChangeContent()))
                .sorted(Comparator.comparing(equipRecord -> LocalDateTime.parse(
                        equipRecord.getChangeContent().getExecTime(), dateTimeFormatter)))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(equipRecords)) {
            return;
        }

        LocalDateTime openDateTime = null;
        LocalDateTime closeDateTime = null;
        List<RunningTimeVO> openCloseTimes = new ArrayList<>();

        LocalDate today = DateUtils.now().toLocalDate();
        for (EquipRecord equipRecord : equipRecords) {
            ChangeContent changeContent = equipRecord.getChangeContent();
            String execTime = changeContent.getExecTime();
            String infoValue = changeContent.getInfoValue();
            // 如果是 人工模式 未来的ai指令不显示

            LocalDateTime executeTime = LocalDateTime.parse(execTime, dateTimeFormatter);
            LocalDate executeDate = executeTime.toLocalDate();
            boolean isOpen = StringUtils.equals(infoValue, "1");
            if (today.isEqual(executeDate)) {
                if (isOpen) {
                    // 开启时间取当天第一条
                    if (Objects.isNull(openDateTime)) {
                        openDateTime = executeTime;
                    }
                } else {
                    // 关闭时间取当天最后一条
                    closeDateTime = executeTime;
                }
            }
            openCloseTimes.add(new RunningTimeVO().setDateTime(executeTime)
                    .setRunStatus(isOpen ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE));
        }
        // 当关闭时间在开启时间之前, 关闭时间设置为空
        if (Objects.nonNull(openDateTime) && Objects.nonNull(closeDateTime)
                && !closeDateTime.isAfter(openDateTime)) {
            closeDateTime = null;
        }
        target.setOpenDateTime(openDateTime);
        target.setCloseDateTime(closeDateTime);
        target.setOpenCloseTimes(openCloseTimes);
    }


    @Override
    public String logTag() {
        return "冷源运行首页策略";
    }
}
