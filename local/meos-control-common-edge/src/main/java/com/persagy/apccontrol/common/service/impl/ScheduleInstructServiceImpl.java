package com.persagy.apccontrol.common.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDayDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyPlanDTO;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFixedQueryParam;
import com.persagy.apccontrol.basic.opt.OptCallMemo;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.DateUtils;
import com.persagy.apccontrol.common.model.param.strategy.chiller.QuerySeasonOrder;
import com.persagy.apccontrol.common.outside.rs.RcSaasClientService;
import com.persagy.apccontrol.common.service.ScheduleInstructService;
import com.persagy.apccontrol.common.util.SpecialInfoCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @BelongsProject: meos-control-parent
 * @BelongsPackage: com.persagy.apccontrol.basic.service.impl
 * @Author: laixingqi
 * @CreateTime: 2023-12-28  18:27
 * @Version: v4.5.0
 */
@Service
@Slf4j
public class ScheduleInstructServiceImpl implements ScheduleInstructService {

    @Autowired
    private BizStrategyQueryService bizStrategyQueryService;

    @Autowired
    private EquipRecordService equipRecordService;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Resource
    private ProjectCfg projectCfg;

    @Autowired
    private RcSaasClientService rcSaasClientService;

    @Override
    public void updateScheduleInstruct(String groupCode, String projectId, String systemId, String classCode,
                                       String objId) {

        //清除当日时间表重新创建
        String expireTime = LocalDate.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
        equipRecordService.deleteEquipRecordByScheduleExpireTime(groupCode, projectId, objId, expireTime);

        //获取今日时间表
        Map<String, List<Map<LocalDateTime, String>>> mapMatic = getStrategys();

        //根据objecId+信息点取出时间表计划
        List<Map<LocalDateTime, String>> listMatic = mapMatic.get(objId + "-" + projectId);

        if (CollectionUtil.isEmpty(listMatic)) {
            log.info("没有获取到时间表信息，{}，{}", objId, systemId);
            return;
        }

        distributionSchedule(groupCode, projectId, systemId, classCode, objId, listMatic, LocalDateTime.now());
    }

    /**
     * @Description: 时间表下发
     * @Author: laixingqi
     * @Date: 2024/1/10 19:55
     * @param: [groupCode, projectId, systemId, classCode, objId, infoCodeSyList, listMatic]
     * @return: void
     **/
    @Override
    //@Transactional(rollbackFor = Exception.class)
    public void distributionSchedule(String groupCode, String projectId, String systemId, String classCode,
                                     String objId, List<Map<LocalDateTime, String>> listMatic, LocalDateTime executionTime) {
        List<EquipRecord> addList = new ArrayList<>();

        List<AutoMaticInfoCodeEnum> infoCodeSyList = SpecialInfoCodeUtil.getAutoMaticInfoCodeEnumByClassCode(classCode);
        if (CollectionUtil.isEmpty(infoCodeSyList)) {
            return;
        }

        ControlLogUtil.info("手自动指令判断-进入时间表处理逻辑:{}:{}:{}", systemId, objId, infoCodeSyList);
        DtpRwdObjectDTO objectDTO = dtpRwdClientService.getObject(true, true,
                projectId, new DtpRwdObjectQueryParam().setId(objId));
        // opt B10（switch.opt.scheduleSeasonMemo）：同一天的季节只查一次
        OptCallMemo<LocalDate, TreeMap<LocalDateTime, String>> seasonMemo =
                OptCallMemo.create("scheduleSeason", OptSwitch.SCHEDULE_SEASON_MEMO);

        for (Map<LocalDateTime, String> localDateTimeStringMap : listMatic) {
            //当天时间
            ControlLogUtil.info("手自动指令判断-对象ID-{},当前时间:{}", objId, executionTime);
            for (Map.Entry<LocalDateTime, String> localDateTimeStringEntry :
                    localDateTimeStringMap.entrySet()) {

                //下发时间
                LocalDateTime timeMatic = localDateTimeStringEntry.getKey();
                ControlLogUtil.info("手自动指令判断-对象ID-{},指令下发时间:{}", objId, timeMatic);
                //如果下发时间在当天时间之前，不操作
                if (timeMatic.isBefore(executionTime)) {
                    continue;
                }

                String value = localDateTimeStringEntry.getValue();
                JSONObject jsonValue = JSONObject.parseObject(value);

                ControlLogUtil.info("手自动指令判断-对象ID-{},指令下发时间:{}:{}:{}", objId, timeMatic, value, jsonValue);

                for (AutoMaticInfoCodeEnum code : infoCodeSyList) {
                    //指令特殊处理 时间表存的和实际下发不一致

                    String val = jsonValue.getString(code.getCode());
                    val = SpecialInfoCodeUtil.getSysInfoCodeByVal(val);
                    Integer sustainTime = jsonValue.getInteger("sustainTime");

                    if (StringUtils.isBlank(val)) {
                        continue;
                    }

                    String infoCode = convertInfoCode(projectId, objectDTO, timeMatic, code, seasonMemo);
                    if (StringUtils.isBlank(infoCode)) {
                        continue;
                    }
                    // 热源供回水温压差判断
                    String hotWaterDetaSetType = jsonValue.getString("hotWaterDetaSetType");
                    if (StringUtils.isNotEmpty(hotWaterDetaSetType) &&
                            (AutoMaticInfoCodeEnum.HEAT_SECONDARY_SIDE_WATER_DETA_TEMP_SET.equals(code)
                                    || AutoMaticInfoCodeEnum.HEAT_SECONDARY_SIDE_WATER_DETA_PRESSURE_SET.equals(code))
                            && !infoCode.equals(hotWaterDetaSetType)) {
                        continue;
                    }
                    ControlLogUtil.info("手自动指令判断-对象ID-{},时间表开关机信息点和值转换val:{}:{}", objId, val, infoCode);

                    //下发指令
                    EquipRecord equipRecord = new EquipRecord();
                    equipRecord.init();
                    equipRecord.setGroupCode(groupCode);
                    equipRecord.setProjectId(projectId);
                    equipRecord.setObjId(objId);
                    equipRecord.setSystemId(systemId);
                    equipRecord.setSystemCode(classCode);

                    ChangeContent changeContent = new ChangeContent(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN).format(timeMatic),
                            infoCode, val, SourceFromEnum.SCHEDULE);
                    if (sustainTime != null) {
                        changeContent.setTimeDuration(timeMatic.plusMinutes(sustainTime));
                    }
                    equipRecord.setChangeContent(changeContent);

                    addList.add(equipRecord);
                }
                log.info("时间表指令添加成功，{}，{}", timeMatic, objId);
            }
        }
        ControlLogUtil.info("手自动指令判断-时间表下发指令All，{}，{}:{}", addList, classCode, projectId);
        try {
            equipRecordService.saveOrUpdateRecordBySchedule(addList, classCode, projectId, SourceFromEnum.SCHEDULE, DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN), null);
        } catch (Exception e) {
            ControlLogUtil.error("时间表指令下发入库失败，{}", objId, e);
            equipRecordService.saveOrUpdateRecordBySchedule(addList, classCode, projectId, SourceFromEnum.SCHEDULE, DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN), null);
        }
        ControlLogUtil.info("时间表指令下发成功，{}，{}", LocalDateTime.now(), objId);
    }

    /**
     * 信息点转换
     *
     * @param projectId
     * @param objectDTO
     * @param timeMatic
     * @param code
     * @param seasonMemo 季节查询去重（开关关闭时每次都查）
     * @return
     */
    private String convertInfoCode(String projectId, DtpRwdObjectDTO objectDTO, LocalDateTime timeMatic, AutoMaticInfoCodeEnum code,
                                   OptCallMemo<LocalDate, TreeMap<LocalDateTime, String>> seasonMemo) {
        //时间表开关机信息点和值转换
        String infoCode = SpecialInfoCodeUtil.getSysInfoCodeByClassCode(objectDTO.getClassCode(), code.getPracticalCode());
        //冷热站 出水温度信息点特殊处理
        if (AutoMaticInfoCodeEnum.COLD_HOT_CHILL_WATER_OUT_TEMP_SET.value().equals(infoCode) ||
                AutoMaticInfoCodeEnum.COLD_HOT_CHILL_WATER_MAIN_SUPPLY_TEMP_SET.value().equals(infoCode)
        ) {
            //判断当前执行时间 的模式
            QuerySeasonOrder param = new QuerySeasonOrder();
            param.setProjectId(projectId);

            LocalDate st = LocalDate.of(timeMatic.getYear(), timeMatic.getMonthValue(), timeMatic.getDayOfMonth());
            LocalDate et = st.plusDays(1);
            param.setBeginDateTime(st);
            param.setEndDateTime(et);

            TreeMap<LocalDateTime, String> stList = seasonMemo.get(st, d -> rcSaasClientService.querySeasonByTime(param));
            if (CollectionUtil.isNotEmpty(stList)) {
                //取第一个值
                Map.Entry<LocalDateTime, String> firstEntry = stList.firstEntry();
                String type = firstEntry.getValue();
                if (MidCalendarAcSeasonTypeEnum.SEASON_COOLING.getReasonPhrase().equals(type)) {
                    infoCode = AutoMaticInfoCodeEnum.COLD_HOT_CHILL_WATER_OUT_TEMP_SET.value();
                } else if (MidCalendarAcSeasonTypeEnum.SEASON_HEATING.getReasonPhrase().equals(type)) {
                    infoCode = AutoMaticInfoCodeEnum.COLD_HOT_CHILL_WATER_MAIN_SUPPLY_TEMP_SET.value();
                } else {
                    //过度季 不下发
                    infoCode = null;
                }
            }
        }
        return infoCode;
    }

    /**
     * @Description: 1根据objecId+信息点取出时间表计划
     * 2如果有，下发时间指令
     * 3 如果没有，存objectId+信息点为自动
     * @Author: laixingqi
     * @Date: 2023/12/23 10:53
     * @param: []
     * @return: java.util.Map<java.lang.String, java.util.List < java.util.Map < java.time.LocalDateTime, java.lang.String>>>
     **/
    @Override
    public Map<String, List<Map<LocalDateTime, String>>> getStrategys() {
        //查询时间表
        StrategyFixedQueryParam param = new StrategyFixedQueryParam();

        LocalDate beginDate = LocalDate.now();
//        LocalDate beginDate = LocalDate.parse("2023-09-21");
        LocalDate endDate = beginDate.plusDays(1);

        param.setBeginDate(beginDate);
        param.setEndDate(endDate);

        List<StrategyDTO> strategys = bizStrategyQueryService.listFixedStrategy(param);

        Map<String, List<Map<LocalDateTime, String>>> mapMatic = new HashMap<>();
        if (CollectionUtil.isNotEmpty(strategys)) {
            for (StrategyDTO strategy : strategys) {
                String objectId = strategy.getObjectId();

                //取出数据： objectId-信息点: list<>时间表计划
                List<StrategyDayDTO> dayPlans = strategy.getDayPlans();
                if (CollectionUtil.isEmpty(dayPlans)) {
                    continue;
                }
                List<Map<LocalDateTime, String>> list = new ArrayList<>();
                for (StrategyDayDTO dayPlan : dayPlans) {

                    List<StrategyPlanDTO> plans = dayPlan.getPlans();
                    if (CollectionUtil.isEmpty(plans)) {
                        continue;
                    }
                    ControlLogUtil.info("手自动指令判断-对象ID-{},时间表的plans，{}", objectId, plans);
                    for (StrategyPlanDTO plan : plans) {
                        Map<LocalDateTime, String> mapPlan = new HashMap<>();

                        String result = plan.getResult();
                        JSONObject jsonObject = JSONObject.parseObject(result);

                        String timeLocal = jsonObject.getString("strategyTime");

                        LocalDateTime localDateTime = DateUtils.getLocalDateTime(timeLocal);

                        mapPlan.put(localDateTime, result);
                        list.add(mapPlan);
                    }

                }
                mapMatic.put(objectId + "-" + strategy.getProjectId(), list);
            }
        }
        return mapMatic;
    }

}