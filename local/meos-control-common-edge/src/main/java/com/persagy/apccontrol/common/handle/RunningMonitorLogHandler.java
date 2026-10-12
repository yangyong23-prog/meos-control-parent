package com.persagy.apccontrol.common.handle;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.PointInfoDTO;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.ManualAutoSetEnum;
import com.persagy.apccontrol.basic.model.enumtype.OperateParamterReason;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTemplateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogInsertParam;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.model.dto.RunningModeDTO;
import com.persagy.apccontrol.common.service.RunningLogEdgeService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 系统运行监测过程中，所需要记录的日志
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/21 10:50
 */
@Slf4j
@Component
public class RunningMonitorLogHandler {

    @Autowired
    private RunningLogEdgeService runningLogEdgeService;

    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;


    /**
     * 控制指令下发日志记录
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param pointInfo
     * @param rcInfoCodeCfg
     */
    @Async
    public void saveColdStationParamSetRunningLog(String groupCode, String projectId, String objectId, PointInfoDTO pointInfo, RcInfoCodeCfg rcInfoCodeCfg) {
        if (rcInfoCodeCfg == null || StrUtil.isBlank(pointInfo.getValue())) {
            return;
        }
        try {
            ContextUtil.setGroupCode(groupCode);
            ContextUtil.setTenant(groupCode);

            RwdObjectBusinessFeildDTO businessFeildDTO = this.localRedisCacheHandler.getObjectBusinessFeildAndSystemInfo(groupCode, projectId, objectId, null);
            if (businessFeildDTO == null) {
                return;
            }

            //是否是枚举值，枚举值对应的是设备开关设定
            RunningLogCategoryEnum manualResultLogCategory = RunningLogCategoryEnum.getManualResultLogCategory(pointInfo.getInfoCode(), businessFeildDTO);

            // 手自动, 默认人工
            RunningLogTypeEnum runningLogTypeEnum = RunningLogTypeEnum.MANUAL;
            if (null != pointInfo.getExtendSetting() && ManualAutoSetEnum.AUTO.equals(pointInfo.getExtendSetting().getManualAutoSet())) {
                runningLogTypeEnum = RunningLogTypeEnum.AUTO;
            }

            boolean enums = CollectionUtil.isNotEmpty(rcInfoCodeCfg.getDataSource());
            String infoValueName = enums ? rcInfoCodeCfg.getDataValueName(pointInfo.getValue()) :pointInfo.getValue();
            String infoValueOldName = enums ? rcInfoCodeCfg.getDataValueName(pointInfo.getBeforeValue()) : pointInfo.getBeforeValue();

            RunningLogInsertParam insertParam = new RunningLogInsertParam(projectId, businessFeildDTO.getSystemId(), objectId, businessFeildDTO.getLocalName(),
                    businessFeildDTO.getClassCode(), RunningLogObjectCategpryEnum.getCategoryByObjType(businessFeildDTO.getObjType()), manualResultLogCategory, runningLogTypeEnum, ContextUtil.getUserName());
            if (InfoCodeEnum.MANUAL_AUTO_SET.getCode().equals(pointInfo.getInfoCode())) {
                insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.EQUIPMENT_MODE_SWITCH, businessFeildDTO.getLocalName(), infoValueName);
            } else if (enums) {
                if (InfoCodeEnum.EQUIP_ON_SET.getCode().equals(pointInfo.getInfoCode())) {
                    infoValueName = "0".equals(pointInfo.getValue()) ? LocaleMessageUtil.getMessage("key_b1620633958840638440dd1b3e10024e") : infoValueName;
                }
                insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.SCHEDULE_COMMAND_RESULT,  infoValueName, businessFeildDTO.getLocalName());
            } else {
                infoValueName = enums || StrUtil.isBlank(infoValueName) ? infoValueName : infoValueName + rcInfoCodeCfg.getUnit();
                infoValueOldName = enums || StrUtil.isBlank(infoValueOldName) ? infoValueOldName : infoValueOldName + rcInfoCodeCfg.getUnit();

                insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.GENERAL, businessFeildDTO.getLocalName(), rcInfoCodeCfg.getName(), infoValueOldName, infoValueName);
                insertParam.setDetail(Lists.newArrayList(RunningLogInsertParam.buildDto(RunningLogTemplateEnum.CONTEN_DETAIL.getTemplate(),
                        StrUtil.isBlank(infoValueOldName) ? LocaleMessageUtil.getMessage("key_231cf459d261416c8a55a92ba726d65b") : infoValueOldName, infoValueName)));
            }

            if (pointInfo.getContent() != null) {
                String reason = OperateParamterReason.getOperateParamterReason().get(pointInfo.getContent());
                reason = reason == null ? pointInfo.getContent() : reason;
                insertParam.setReason(reason);
            }
            runningLogEdgeService.saveLog(groupCode, insertParam);
        } catch (Exception e) {
            log.error("日志记录失败！error:{}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Async
    public void saveManualAutoSetRunningLog(String groupCode, String projectId, String objectId, String model, RunningLogTypeEnum logType, String expireTime, String operateReason, String userName) {
        RwdObjectBusinessFeildDTO businessFeildDTO = this.localRedisCacheHandler.getObjectBusinessFeildAndSystemInfo(groupCode, projectId, objectId, null);
        if (businessFeildDTO == null) {
            log.warn("对象不存在，或属于未知模块: {}", objectId);
            return;
        }

        String manualAutoSet = InfoCodeEnum.MANUAL_AUTO_SET.getCode();
        String modeName = "0".equals(model) ? LocaleMessageUtil.getMessage("key_a2341d9fc6b04ba2a3f5781eb747b14c") : LocaleMessageUtil.getMessage("key_e31ea221b6e74c5898d5ed6208968a54");

        RunningLogCategoryEnum categrory =  RunningLogCategoryEnum.getManualResultLogCategory(manualAutoSet, businessFeildDTO);
        RunningLogInsertParam insertParam = new RunningLogInsertParam(projectId, businessFeildDTO.getSystemId(), objectId, businessFeildDTO.getLocalName(),
                businessFeildDTO.getClassCode(), RunningLogObjectCategpryEnum.getCategoryByObjType(businessFeildDTO.getObjType()), categrory, logType, userName,
                RunningLogObjectCategpryEnum.SYSTEM.getCode().equals(businessFeildDTO.getObjType()) ? RunningLogTemplateEnum.SWITCH_RUN_MODE : RunningLogTemplateEnum.EQUIPMENT_MODE_SWITCH,
                RunningLogObjectCategpryEnum.SYSTEM.getCode().equals(businessFeildDTO.getObjType()) ? "系统下设备" : businessFeildDTO.getLocalName(), modeName);
        if (StringUtils.isNotEmpty(expireTime)) {
            insertParam.setEffectStartTime(LocalDateTime.now());
            insertParam.setEffectEndTime(LocalDateTime.parse(expireTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }
        insertParam.setReason(operateReason);
        runningLogEdgeService.saveLog(groupCode, insertParam);
    }

    /**
     * 记录批量操作日志
     *
     * @param groupCode
     * @param dto
     * @param objectList
     * @param runningLogTypeEnum
     * @author yuecaipu
     * @date 2024/11/7 15:27
     * @since v5.241101.0
     */
    public void saveBatchManualAutoSetRunningLog(String groupCode, RunningModeDTO dto, List<JSONObject> objectList, RunningLogTypeEnum runningLogTypeEnum) {
        if (objectList.size() == 1) {
            saveManualAutoSetRunningLog(groupCode, dto.getProjectId(), objectList.get(0).getString("id"), dto.getModel(), runningLogTypeEnum, dto.getExpireTime(), dto.getOperateReason(), ContextUtil.getUserName());
        } else {
            String manualAutoSet = InfoCodeEnum.MANUAL_AUTO_SET.getCode();
            String modeName = "0".equals(dto.getModel()) ? LocaleMessageUtil.getMessage("key_00a4a048e0f24c229d1ae21ed5e07835") : LocaleMessageUtil.getMessage("key_084a9ca0975743f9953110e38644b008");
            RwdObjectBusinessFeildDTO businessFeildDTO = this.localRedisCacheHandler.getObjectBusinessFeildAndSystemInfo(groupCode, dto.getProjectId(), objectList.get(0).getString("id"), null);
            RunningLogCategoryEnum categrory = RunningLogCategoryEnum.getManualResultLogCategory(manualAutoSet, businessFeildDTO);
            String systemId = businessFeildDTO.getSystemId();
            RunningLogInsertParam insertParam = new RunningLogInsertParam(dto.getProjectId(), systemId, systemId, businessFeildDTO.getSystemType().getReasonPhrase(),
                    businessFeildDTO.getClassCode(), RunningLogObjectCategpryEnum.getCategoryByObjType(businessFeildDTO.getObjType()), categrory, runningLogTypeEnum, ContextUtil.getUserName());
            insertParam.buildRunningLogTemplate(RunningLogTemplateEnum.EQUIPMENT_MODE_SWITCH_BATCH, objectList.size() + "", DtpRwdClassCodeEnum.analyze(businessFeildDTO.getClassCode()).getReasonPhrase(), modeName);
            if (StringUtils.isNotEmpty(dto.getExpireTime())) {
                insertParam.setEffectStartTime(LocalDateTime.now());
                insertParam.setEffectEndTime(LocalDateTime.parse(dto.getExpireTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            }
            insertParam.setReason(dto.getOperateReason());
            runningLogEdgeService.saveLog(groupCode, insertParam);
        }
    }
}
