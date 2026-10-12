package com.persagy.apccontrol.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.common.service.ColdHeatStationService;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 冷热站
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/1/29 14:42
 */
@Slf4j
@Service
public class ColdHeatStationServiceImpl implements ColdHeatStationService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Autowired
    private EquipRecordService equipRecordService;

    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Override
    public JSONObject getSystemRunningModel(String groupCode, String projectId, String systemId) {
        JSONObject result = new JSONObject();

        if (StringUtils.isBlank(systemId)) {
            return result;
        }

        // 1.验证系统是否存在
        DtpRwdObjectDTO object = dtpRwdClientService.getObject(false, false, projectId, new DtpRwdObjectQueryParam().setId(systemId));
        if (Objects.isNull(object)) {
            return result;
        }
        String classCode = object.getClassCode();

        String runningModel;
        if (DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.value().equals(classCode)) {
            // 获取失效时间
            EquipRecord equipRecords = this.equipRecordService.getEquipRecordByInfoCodeDesc(groupCode, projectId, null, systemId, InfoCodeEnum.RUN_MODEL_SET.getCode(), -1);
            result.put("expireTime", equipRecords == null ? null : equipRecords.getChangeContent().getExecTime());

            // 获取运行模式
            runningModel = this.redisCacheUtil.get(RedisKey.buildSystemRunnningModel(projectId, systemId));
            if (StrUtil.isBlank(runningModel)) {
                MidCalendarAcSeasonTypeEnum season = this.abstractCommonServer.getProjectSeason(groupCode, projectId, LocalDate.now());
                runningModel = MidCalendarAcSeasonTypeEnum.SEASON_COOLING.equals(season) ? "1"
                        : MidCalendarAcSeasonTypeEnum.SEASON_HEATING.equals(season) ? "2" : null;
            }
        } else {
            MidCalendarAcSeasonTypeEnum season = this.abstractCommonServer.getProjectSeason(groupCode, projectId, LocalDate.now());
            runningModel = MidCalendarAcSeasonTypeEnum.SEASON_COOLING.equals(season) ? "1"
                    : MidCalendarAcSeasonTypeEnum.SEASON_HEATING.equals(season) ? "2" : null;
        }

        result.put(InfoCodeEnum.RUN_MODEL_SET.getCode(), runningModel);
        return result;
    }

    @Override
    public MidCalendarAcSeasonTypeEnum getAcciSystemRunningModel(String groupCode, String projectId, String systemId) {
        // 获取运行模式
        String runningModel = this.redisCacheUtil.get(RedisKey.buildSystemRunnningModel(projectId, systemId));
        if (StrUtil.isBlank(runningModel)) {
            return this.abstractCommonServer.getProjectSeason(groupCode, projectId, LocalDate.now());
        }
        return "1".equals(runningModel) ? MidCalendarAcSeasonTypeEnum.SEASON_COOLING : "2".equals(runningModel) ? MidCalendarAcSeasonTypeEnum.SEASON_HEATING : null;
    }
}
