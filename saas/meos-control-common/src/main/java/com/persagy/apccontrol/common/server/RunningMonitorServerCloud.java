package com.persagy.apccontrol.common.server;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.abstracts.AbstractRunningMonitorServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.ColdHeatGroupObjectInfo;
import com.persagy.apccontrol.basic.model.entity.InfoCodeConfigExtendInfo;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDateTag;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/12 18:36
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorServerCloud extends AbstractRunningMonitorServer {

    private final MidCalendarClientService midCalendarClientService;

    @Override
    public String getRunModelSet(String groupCode, String projectId, String systemId) {
        MidCalendarDateTag serviceSeason = this.midCalendarClientService.getSeason(groupCode, projectId, DateUtils.now().toLocalDate());
        MidCalendarAcSeasonTypeEnum seasonTypeEnum = serviceSeason == null ? null : MidCalendarAcSeasonTypeEnum.analyze(serviceSeason.getDictCode());
        return MidCalendarAcSeasonTypeEnum.SEASON_COOLING.equals(seasonTypeEnum) ? "1"
                : MidCalendarAcSeasonTypeEnum.SEASON_HEATING.equals(seasonTypeEnum) ? "2" : null;
    }

    @Override
    public void loadRealInfoCodeGroupTree(RcInfoCodeCfg infoCodeCfg, JSONObject objectInfo, DtpPointInfo dtpPointInfo, boolean queryIot) {
        String objectId = objectInfo.getString("id");
        String groupCode = objectInfo.getString("groupCode");
        String projectId = objectInfo.getString("projectId");
        String classCode = objectInfo.getString("classCode");
        boolean heatGroup = "ACAHC".equals(classCode) || "ACCCAH".equals(classCode);

        // 判断是否需要进行扩展信息点的状态赋值
        InfoCodeConfigExtendInfo extendInfo = infoCodeCfg.getExtendInfo();
        if (extendInfo != null && extendInfo.getTriggerFormulaInfo() != null && "rcRunStatus".equals(extendInfo.getTriggerFormulaInfo().getTriggerInfoCode())) {
            objectInfo.put("rcRunStatus", this.abstractIotDataCacheServer.queryIotRealData(groupCode, projectId, objectId, classCode, "runStatus"));
        }

        // 空气源热泵的和泵组的单独处理
        if (heatGroup && InfoCodeEnum.RUN_STATUS.getCode().equals(infoCodeCfg.getInfoCode())) {
            dtpPointInfo = this.getGroupObjectInfoCodeValue(groupCode, projectId, objectId, classCode, infoCodeCfg.getInfoCode());
        } else if (queryIot && dtpPointInfo == null) {
            Object pointInfo = this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, objectId, infoCodeCfg);
            dtpPointInfo = pointInfo == null ? null : pointInfo instanceof DtpPointInfo ? (DtpPointInfo) pointInfo : null;
        }

        if (dtpPointInfo != null) {
            infoCodeCfg.setDataValue(dtpPointInfo.getIpValue());
            infoCodeCfg.setDataTime(dtpPointInfo.getIpTime());
            if (objectInfo != null) {
                objectInfo.put(dtpPointInfo.getInfoCode(), dtpPointInfo.getIpValue());
            }
        }
    }

    @Override
    public DtpPointInfo getGroupObjectInfoCodeValue(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        DtpPointInfo resultInfo = null;
        boolean heatGroup = "ACAHC".equals(classCode) || "ACCCAH".equals(classCode);

        if (heatGroup && (InfoCodeEnum.RUN_STATUS.getCode().equals(infoCode) || InfoCodeEnum.RC_RUN_STATUS.getCode().equals(infoCode))) {
            resultInfo = this.getGroupObjectMessage(groupCode, projectId, objectId, classCode, InfoCodeEnum.RUN_STATUS.getCode());
        } else {
            Object pointInfo = null;
            if (InfoCodeEnum.RC_RUN_STATUS.getCode().equals(infoCode)) {
                pointInfo = this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, objectId, classCode, InfoCodeEnum.RUN_STATUS.getCode());
            } else {
                pointInfo = this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, objectId, classCode, infoCode);
            }
            resultInfo = pointInfo == null ? null : pointInfo instanceof DtpPointInfo ? (DtpPointInfo) pointInfo : null;
        }
        return resultInfo;
    }

    /**
     * 获取此对象对应信息点的IOT数据
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param infoCode
     * @return
     */
    private DtpPointInfo getGroupObjectMessage(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        // 如果是泵组且是运行状态的判断，运行状态=allEquipRule ？ 任取一个泵的 ：自己
        // 如果是泵组且是故障状态的判断，故障状态=allEquipRule ？ 任取一个泵的 ：自己
        // 如果是泵且是运行状态的判断，运行状态=allEquipRule ？ 自己 ：存在泵组的 ？泵组的 : 自己
        // 如果是泵且是故障状态的判断，故障状态=allEquipRule ？ 自己 ：存在泵组的 ？泵组的 : 自己

        // 1.检查规则的交付情况
        boolean group = "ACAHC".equals(classCode);
        ColdHeatGroupObjectInfo groupObjectInfo = this.checkGroupEquipmentRule(groupCode, projectId, objectId, infoCode, group);

        // 2.返回对象信息点的IOT数据
        Object pointInfo = group
                ? groupObjectInfo.isAllEquipRule()
                ? this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, groupObjectInfo.getEquipObjectIds().stream().findFirst().get(), "ACCCAH", infoCode)
                : this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, groupObjectInfo.getGroupObjectId(), "ACAHC", infoCode)
                : groupObjectInfo.isAllEquipRule()
                ? this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, objectId, classCode, infoCode) : groupObjectInfo.isGroupRule()
                ? this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, groupObjectInfo.getGroupObjectId(), "ACAHC", infoCode)
                : this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, objectId, classCode, infoCode);
        return pointInfo == null ? null : pointInfo instanceof DtpPointInfo ? (DtpPointInfo) pointInfo : null;
    }

}
