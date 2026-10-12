package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.persagy.apccontrol.basic.abstracts.AbstractRunningMonitorServer;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.ColdHeatGroupObjectInfo;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.InfoCodeConfigExtendInfo;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.common.service.ColdHeatStationService;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.apccontrol.common.service.RunningMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/12 17:43
 */
@Slf4j
@Service
public class RunningMonitorServerEdge extends AbstractRunningMonitorServer {

    @Autowired
    private EquipRecordService equipRecordService;

    @Autowired
    private IotDataCacheService iotDataCacheService;

    @Lazy
    @Autowired
    private RunningMonitorService runningMonitorService;

    @Lazy
    @Autowired
    private ColdHeatStationService coldHeatStationService;

    @Override
    public String getRunModelSet(String groupCode, String projectId, String systemId) {
        JSONObject runningModel = this.coldHeatStationService.getSystemRunningModel(groupCode, projectId, systemId);
        return runningModel.getString(InfoCodeEnum.RUN_MODEL_SET.getCode());
    }

    @Override
    public void loadRealInfoCodeGroupTree(RcInfoCodeCfg infoCodeCfg, JSONObject objectInfo, DtpPointInfo dtpPointInfo, boolean queryIot) {
        String objectId = objectInfo.getString("id");
        String groupCode = objectInfo.getString("groupCode");
        String projectId = objectInfo.getString("projectId");
        String classCode = infoCodeCfg.getClassCode();
        boolean heatGroup = "ACAHC".equals(classCode) || "ACCCAH".equals(classCode);

        InfoCodeConfigExtendInfo extendInfo = infoCodeCfg.getExtendInfo();
        if (extendInfo != null) {
            if (CollectionUtil.isNotEmpty(extendInfo.getForceInfoCodeInfos())) {
                infoCodeCfg.setForceInfo(this.runningMonitorService.getAvailableForceInfo(groupCode, projectId, objectId, classCode, infoCodeCfg.getExtendInfo() == null ? null : infoCodeCfg.getExtendInfo().getForceInfoCodeInfos()));
                infoCodeCfg.setForceModel(this.iotDataCacheService.getStringCacheData(RedisKey.buildObjectInfoCodeForcedKey(objectId, infoCodeCfg.getInfoCode())));
            }
        }
        // 判断是否需要进行扩展信息点的状态赋值
        if (extendInfo != null && extendInfo.getTriggerFormulaInfo() != null && "rcRunStatus".equals(extendInfo.getTriggerFormulaInfo().getTriggerInfoCode())) {
            FeedbackInfoMsgMqttVO rcRunStatus = this.iotDataCacheService.queryIotRealDataRedisVO(objectId, InfoCodeEnum.RC_RUN_STATUS.getCode());
            objectInfo.put(InfoCodeEnum.RC_RUN_STATUS.getCode(), rcRunStatus == null ? null : rcRunStatus.getDataValue());
        } else {
            objectInfo.put(InfoCodeEnum.RC_RUN_STATUS.getCode(), this.iotDataCacheService.queryIotRealData(objectId, "runStatus", null));
        }

        //干预的信息
        LambdaQueryWrapper<EquipRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(EquipRecord::getProjectId, projectId)
                .eq(EquipRecord::getExecStatus, -1)
                .eq(EquipRecord::getInfoCode, infoCodeCfg.getInfoCode())
                .in(EquipRecord::getObjId, objectId)
        ;
        List<EquipRecord> equipRecordList = equipRecordService.list(qw);
        if (CollectionUtils.isNotEmpty(equipRecordList)) {
            ChangeContent changeContent = equipRecordList.get(0).getChangeContent();
            infoCodeCfg.setDataTime(changeContent.getExecTime());
            infoCodeCfg.setChangeContent(JSONObject.parseObject(JSON.toJSONString(changeContent)));
        }

        // 赋值，空气源热泵及其泵组的单独处理
        if (heatGroup && InfoCodeEnum.RUN_STATUS.getCode().equals(infoCodeCfg.getInfoCode())) {
            dtpPointInfo = this.getGroupObjectInfoCodeValue(groupCode, projectId, objectId, classCode, infoCodeCfg.getInfoCode());
        } else if (queryIot && dtpPointInfo == null) {
            this.iotDataCacheService.setDataValueAndTime(this.iotDataCacheService.queryIotRealDataVO(objectId, infoCodeCfg.getInfoCode(), infoCodeCfg), infoCodeCfg, objectInfo);
        }

        if (dtpPointInfo != null) {
            infoCodeCfg.setDataValue(dtpPointInfo.getDataValue());
            infoCodeCfg.setDataTime(dtpPointInfo.getDataTime());
            if (objectInfo != null) {
                objectInfo.put(infoCodeCfg.getInfoCode(), dtpPointInfo.getDataValue());
            }
        }
    }

    @Override
    public DtpPointInfo getGroupObjectInfoCodeValue(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        FeedbackInfoMsgMqttVO infoMsgMqttVO = null;
        boolean heatGroup = "ACAHC".equals(classCode) || "ACCCAH".equals(classCode);
        if (heatGroup && (InfoCodeEnum.RUN_STATUS.getCode().equals(infoCode) || InfoCodeEnum.RC_RUN_STATUS.getCode().equals(infoCode))) {
            infoMsgMqttVO = this.getGroupObjectMessage(groupCode, projectId, objectId, classCode, InfoCodeEnum.RUN_STATUS.getCode());
        } else {
            if (InfoCodeEnum.RC_RUN_STATUS.getCode().equals(infoCode)) {
                infoMsgMqttVO = this.iotDataCacheService.queryIotRealDataRedisVO(objectId, infoCode);
                if (infoMsgMqttVO == null || StrUtil.isBlank(infoMsgMqttVO.getDataValue())) {
                    infoMsgMqttVO = this.iotDataCacheService.queryIotRealDataVO(objectId, InfoCodeEnum.RUN_STATUS.getCode(), null);
                }
            } else {
                infoMsgMqttVO = this.iotDataCacheService.queryIotRealDataVO(objectId, infoCode, null);
            }
        }
        return infoMsgMqttVO == null ? null : new DtpPointInfo(infoMsgMqttVO.getObjectId(),
                infoMsgMqttVO.getInfoCode(), infoMsgMqttVO.getDataValue(), infoMsgMqttVO.getDataTime(),
                infoMsgMqttVO.getDataIndate());
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
    public FeedbackInfoMsgMqttVO getGroupObjectMessage(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        // 如果是泵组且是运行状态的判断，运行状态=allEquipRule ？ 任取一个泵的 ：自己
        // 如果是泵组且是故障状态的判断，故障状态=allEquipRule ？ 任取一个泵的 ：自己
        // 如果是泵且是运行状态的判断，运行状态=allEquipRule ？ 自己 ：存在泵组的 ？泵组的 : 自己
        // 如果是泵且是故障状态的判断，故障状态=allEquipRule ？ 自己 ：存在泵组的 ？泵组的 : 自己

        // 1.检查规则的交付情况
        boolean group = "ACAHC".equals(classCode);
        ColdHeatGroupObjectInfo groupObjectInfo = this.checkGroupEquipmentRule(groupCode, projectId, objectId, infoCode, group);

        // 2.返回对象信息点的IOT数据
        FeedbackInfoMsgMqttVO iotRealData = group
                ? groupObjectInfo.isAllEquipRule()
                ? this.iotDataCacheService.queryIotRealDataVO(groupObjectInfo.getEquipObjectIds().stream().findFirst().get(), infoCode, null)
                : this.iotDataCacheService.queryIotRealDataVO(groupObjectInfo.getGroupObjectId(), infoCode, null)
                : groupObjectInfo.isAllEquipRule()
                ? this.iotDataCacheService.queryIotRealDataVO(objectId, infoCode, null) : groupObjectInfo.isGroupRule()
                ? this.iotDataCacheService.queryIotRealDataVO(groupObjectInfo.getGroupObjectId(), infoCode, null)
                : this.iotDataCacheService.queryIotRealDataVO(objectId, infoCode, null);
        return iotRealData;
    }

}
