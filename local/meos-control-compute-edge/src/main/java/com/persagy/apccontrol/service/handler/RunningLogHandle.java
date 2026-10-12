package com.persagy.apccontrol.service.handler;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTemplateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogInsertParam;
import com.persagy.apccontrol.common.service.RunningLogEdgeNowService;
import com.persagy.apccontrol.common.service.RunningLogEdgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@Slf4j
public class RunningLogHandle {

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private RunningLogEdgeService runningLogEdgeService;

    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;
    @Resource
    private RunningLogEdgeNowService runningLogEdgeNowService;

    @Autowired
    private MidPermissionClientService midPermissionClientService;

    @Async
    public void handleLog(FeedbackInfoMsgMqttVO source){
        if (source.getClassCode() == null || source.getInfoCode() == null || StrUtil.isBlank(source.getDataValue()) || "~".equals(source.getDataValue())) {
            return;
        }
        this.iotCtrlRealDataHandler(source);
    }

    /**
     * 控制IOT数据处理
     *
     * @param source
     */
    private void iotCtrlRealDataHandler(FeedbackInfoMsgMqttVO source) {
        if (!EnumIotDataType.CTRL.getCode().equals(source.getType())) {
            return;
        }
        //更新新版日志的状态
        runningLogEdgeNowService.updateByTwinMsg(source);
        
        RcInfoCodeCfg infoCodeConfig = this.localRedisCacheHandler.getInfoCodeConfig(source.getClassCode(), source.getInfoCode());
        if (infoCodeConfig == null) {
            return;
        }
        // 获取系统信息，非真正的系统，按模块编码赋值
        RwdObjectBusinessFeildDTO businessFeild = localRedisCacheHandler.getObjectBusinessFeildAndSystemInfo(projectCfg.getGroupCode(), projectCfg.getProjectId(), source.getObjectId(), null);
        if (businessFeild == null) {
            return;
        }

        this.equipSwitchSetHandler(source, infoCodeConfig, businessFeild);
    }
    
    
    /**
     * 设备开关指令数据处理
     *
     * @param source
     */
    private void equipSwitchSetHandler(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeConfig, RwdObjectBusinessFeildDTO businessFeild) {
        if (infoCodeConfig == null || infoCodeConfig.getDataSource() == null || infoCodeConfig.getDataSource().size() == 0) {
            return;
        }

        // 验证 result 级别
        String resultName = this.getResultName(source.getResult());
        if (StrUtil.isBlank(resultName) || source.getDataStatus() == null || source.getDataStatus() != 2) {
            log.warn("非完成状态的控制指令，不记录日志：{}", source);
            return;
        }

        String valueName = infoCodeConfig.getDataValueName(source.getDataValue());
        if (StrUtil.isBlank(valueName)) {
            log.warn("非常规的枚举类型控制指令，不记录日志：{}", source);
            return;
        }

        log.warn("完成状态的控制指令，记录日志：{}", source);

        RunningLogInsertParam insertParam = new RunningLogInsertParam(source.getProjectId(), businessFeild.getSystemId(), source.getObjectId(), source.getLocalName(), source.getClassCode(),
                RunningLogObjectCategpryEnum.getCategoryByObjType(source.getObjType()), RunningLogCategoryEnum.getCommandResultLogCategory(businessFeild), getLogType(source.getCmdLevel()),  CommonConst.SYSTEM_NAME,
                RunningLogTemplateEnum.OPEN_COMMAND_RESULT, valueName, source.getLocalName(), resultName);
        String user = source.getUser();
        if (RunningLogTypeEnum.MANUAL.equals(insertParam.getType()) && StrUtil.isNotBlank(user)) {
            insertParam.setCreateUser(user);
            Map<String, String> userNameByUserIds =
                    this.midPermissionClientService.getUserNameByUserIds(projectCfg.getGroupCode(),
                            Sets.newHashSet(user));
            if (userNameByUserIds.containsKey(user)) {
                insertParam.setCreateUser(userNameByUserIds.get(user));
            }
        }

        runningLogEdgeService.saveLog(projectCfg.getGroupCode(), insertParam);
    }

    private String getResultName(Integer result) {
        switch (result) {
            case 1:
                return LocaleMessageUtil.getMessage("key_423f54233e7e415f92ce5d27942881a0");
            case 2:
                return LocaleMessageUtil.getMessage("key_f2704e092ff44465b0b37f56e2fa03f6");
            case 3:
                return LocaleMessageUtil.getMessage("key_14a1210127964b1a89c57ad87a488f43");
            case 4:
                return LocaleMessageUtil.getMessage("key_ff45adcc17174ad4a0c03ab969dc8fb0");
            case 5:
                return LocaleMessageUtil.getMessage("key_5bf0b8410a1f4947ab5c4d9c580e3db4");
            default:
                return null;
        }
    }

    private RunningLogTypeEnum getLogType(String cmdLevel) {
        if ("8".equals(cmdLevel)) {
            return RunningLogTypeEnum.MANUAL;
        }else if ("12".equals(cmdLevel)) {
            return RunningLogTypeEnum.AUTO;
        }
        return null;
    }

}
