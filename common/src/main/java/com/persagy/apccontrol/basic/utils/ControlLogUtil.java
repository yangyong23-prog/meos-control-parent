package com.persagy.apccontrol.basic.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.enumtype.system.LogSourceEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.LogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 控制日志
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/1/8 11:08
 */
@Slf4j
public final class ControlLogUtil {

    /**
     * 保存指令日志
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param logSource     user
     * @param logType   command_send
     * @param operateType   01
     * @param operateReason     设备维保或设备调试
     * @param contentInfo
     * 手自动切换，对象：裙楼制冷主机LJ-3冷却水出水蝶阀，设置为：手动模式-成功
     * @param userId
     */
    public static void saveInfoLog(String groupCode, String projectId, String objectId, LogSourceEnum logSource, LogTypeEnum logType,
                                   OperateTypeEnum operateType, String operateReason, String contentInfo, String userId) {
        log.info("集团：{}，项目：{}，对象：{}，日志来源：{}，日志类型：{}，操作类型：{}，操作原因：{}，操作内容：{}，操作人：{}",
                groupCode, projectId, objectId, logSource, logType, operateType, StrUtil.isBlank(operateReason) && operateType != null ? operateType.getDesc() : operateReason, contentInfo, userId);
    }

    /**
     * 保存指令日志
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param logSource     user
     * @param logType   command_send
     * @param operateType   01
     * @param operateReason     设备维保或设备调试
     * @param pointInfo
     * [{
     * 	"infoCode": "manualAutoSet",
     * 	"infoName": "手自动状态设定",
     * 	"infoValue": "0",
     * 	"expireTime": "20230920180500",
     * 	"beforeValue": null
     * }]
     * @param contentInfo
     * 手自动切换，对象：裙楼制冷主机LJ-3冷却水出水蝶阀，设置为：手动模式-成功
     * @param userId
     */
    public static void saveInfoLog(String groupCode, String projectId, String objectId, LogSourceEnum logSource, LogTypeEnum logType,
                                   OperateTypeEnum operateType, String operateReason, JSONObject pointInfo, String contentInfo, String userId) {
        log.info("集团：{}，项目：{}，对象：{}，日志来源：{}，日志类型：{}，操作类型：{}，操作原因：{}，指令详情：{}，操作内容：{}，操作人：{}",
                groupCode, projectId, objectId, logSource, logType, operateType, StrUtil.isBlank(operateReason) && operateType != null ? operateType.getDesc() : operateReason, pointInfo, contentInfo, userId);
    }

    /**
     * 保存指令日志
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param localName
     * @param logSource     user
     * @param logType   command_send
     * @param operateType   01
     * @param operateReason     设备维保或设备调试
     * @param pointInfos
     * [{
     * 	"infoCode": "manualAutoSet",
     * 	"infoName": "手自动状态设定",
     * 	"infoValue": "0",
     * 	"expireTime": "20230920180500",
     * 	"beforeValue": null
     * }]
     * @param contentInfo
     * 手自动切换，对象：裙楼制冷主机LJ-3冷却水出水蝶阀，设置为：手动模式-成功
     * @param userId
     */
    public static void saveInfoLog(String groupCode, String projectId, String objectId, String localName, LogSourceEnum logSource, LogTypeEnum logType,
                                   OperateTypeEnum operateType, String operateReason, JSONArray pointInfos, String contentInfo, String userId) {
        log.info("集团：{}，项目：{}，对象：{}，对象本地名称：{}，日志来源：{}，日志类型：{}，操作类型：{}，操作原因：{}，指令详情：{}，操作内容：{}，操作人：{}",
                groupCode, projectId, objectId, localName, logSource, logType, operateType, StrUtil.isBlank(operateReason) && operateType != null ? operateType.getDesc() : operateReason, pointInfos, contentInfo, userId);
    }

    public static void debug(String format, Object... arguments) {
        log.debug(format, arguments);
    }

    public static void info(String format, Object... arguments) {
        log.info(format, arguments);
    }

    public static void warn(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    public static void error(String format, Object... arguments) {
        log.error(format, arguments);
    }
    public static void error(String msg, Throwable t){
        log.error(msg, t);
    }

    public static void error(String format, Object arg, Throwable e) {
        log.error(format, arg, e);
    }

}
