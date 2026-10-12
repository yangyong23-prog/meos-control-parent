package com.persagy.apccontrol.common.service.command;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.common.service.DtpRwdService;
import com.persagy.apccontrol.common.service.ScheduleInstructService;
import com.persagy.apccontrol.common.util.SpecialInfoCodeUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 初始化需求手自动状态
 */
@Service
@Slf4j
public class InitManualAutoMaticServiceImpl implements InitManualAutoMaticService {


    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Autowired
    private DtpRwdService dtpRwdService;


    @Resource
    private ProjectCfg projectCfg;

    @Autowired
    private ManualAutoMaticSetService manualAutoMaticSetService;

    @Autowired
    private ScheduleInstructService scheduleInstructService;

    @Autowired
    private DemandLightingStrategy demandLightingStrategy;


    @Override
    public void setManualAutoMatic(DemandTypeEnum type) {

        switch (type) {
            case AIRCONDITIONING:
                setColdSourceManualAutoMatic();
                break;
            default:
        }

    }

    @Override
    public void setDemandLightingManualAutoMatic() {

        //清楚历史设置
        manualAutoMaticSetService.resetArtificialType(DemandTypeEnum.LIGHTING);

        //重置状态
        List<DemandTemplateTypeEnum> typeList = DemandTypeEnum.LIGHTING.toDemandTemplateTypes();

        typeList.forEach(i -> {
            List<DtpRwdCircuitDTO> c = demandLightingStrategy.listCircuits(projectCfg.getProjectId(), i, null);
            c.forEach(dr -> {
                setAutomatic(dr.getId(), AutoMaticInfoCodeEnum.CONTROL_DEMAND.getPracticalCode());
            });
        });

    }


    /**
     * @Description: 1 根据系统、末级泵组去redis库里面，删除自动模式
     * <p>
     * 2 根据系统、末级泵组查询时间表的数据，判断是否有设置时间表
     * <p>
     * 3 如果有时间表配置（前提为当前时间之后，并且是当天的时间），下发指令（存放在指令库）
     * <p>
     * 4 如果没有时间表配置，把对象设置为自动，存放redis
     **/
    @Override
    public void setColdSourceManualAutoMatic() {

        ContextUtil.setGroupCode(projectCfg.getGroupCode());

        //删除redis手自动 只删除开关机状态
        manualAutoMaticSetService.resetArtificialType(DemandTypeEnum.AIRCONDITIONING);

        //获取今日时间表
        Map<String, List<Map<LocalDateTime, String>>> mapMatic = scheduleInstructService.getStrategys();

        ControlLogUtil.info("手自动指令判断-获取今日时间表:{}", mapMatic);
        //获取冷机系统
        Set<String> cl = dtpRwdService.queryCentralCoolingSystemIds();

        //获取末级泵组的对象
        Set<String> fp = dtpRwdService.getFreezePump(cl);
        //泵组 -> 系统
        Map<String, String> gx = dtpRwdService.getSystemIdByFreezePumpId();
        cl.addAll(fp);

        //获取冷热源系统
        Set<String> ch = dtpRwdService.queryHotColdStationSystemIds();
        cl.addAll(ch);

        ControlLogUtil.info("手自动指令判断-泵组 -> 系统:{}", cl);
        if (CollectionUtil.isEmpty(cl)) {
            return;
        }

        for (String sysId : cl) {

            DtpRwdObjectDTO objectDTO = dtpRwdClientService.getObject(true, true,
                    projectCfg.getProjectId(), new DtpRwdObjectQueryParam().setId(sysId));

            if (StringUtils.equals(objectDTO.getClassCode(), DtpRwdClassCodeEnum.COLD_SYSTEM.getValue()) ||
                    StringUtils.equals(objectDTO.getClassCode(), DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue())
            ) {
                //设置手自动
                setManualAutoMatic(mapMatic, projectCfg.getProjectId(), sysId, objectDTO, null);
            } else {
                //末级泵组设置手自动
                setManualAutoMatic(mapMatic, projectCfg.getProjectId(), sysId, objectDTO, gx);
            }
        }
    }

    /**
     * @Description: 判断是否有时间表计划，设置手自动
     * @Author: laixingqi
     * @Date: 2023/12/23 10:53
     * @param: [mapMatic, infoCodeSyList, objId]
     * @return: void
     **/
    private void setManualAutoMatic(Map<String, List<Map<LocalDateTime, String>>> mapMatic, String projectId, String sysId,
                                    DtpRwdObjectDTO obj,
                                    Map<String, String> gx) {
        //根据objecId+信息点取出时间表计划
        List<Map<LocalDateTime, String>> listMatic = mapMatic.get(obj.getId() + "-" + projectId);

        if (gx != null && gx.get(sysId) != null) {
            sysId = gx.get(sysId);
        }

        if (CollectionUtil.isEmpty(listMatic)) {
            setAutomatic(obj.getId(), SpecialInfoCodeUtil.getRunStatusSysByClassCode(obj.getClassCode()));
        } else {
            ControlLogUtil.info("手自动指令判断-泵组 -> 系统:{}:{}:{}", projectId, sysId, obj.getId());

            //执行时间设置为0点 0点也会可能有指令
            LocalDate currentDate = LocalDate.now();
            LocalDateTime executionTime = LocalDateTime.of(currentDate, LocalTime.MIN);

            scheduleInstructService.distributionSchedule(ContextUtil.getGroupCode(), projectId, sysId,
                    obj.getClassCode(),
                    obj.getId(),
                    listMatic, executionTime);
        }
    }

    private void setAutomatic(String objId, String infoCode) {
        //存自动到redis
        String key = RedisKey.getAutomatic(objId, infoCode);
        redisCacheUtil.set(key, 1);
    }

}
