package com.persagy.apccontrol.common.task;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoSendDTO;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 手自动切换，0#objectId-失败，1#objectId-成功
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2022/12/12 16:01
 */
@Slf4j
public class ObjectCommandSendTask implements Callable<ObjectCommandSendResult> {

    private String groupCode;
    private String projectId;
    private String objectId;
    private String infoCode;
    private String infoValue;
    private DtpTwinClientService dtpTwinClientService;
    private boolean autoSend;
    private boolean throwException;

    public ObjectCommandSendTask(String groupCode, String projectId, String objectId, String infoCode, String infoValue, DtpTwinClientService dtpTwinClientService, boolean autoSend, boolean throwException) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.objectId = objectId;
        this.infoCode = infoCode;
        this.infoValue = infoValue;
        this.autoSend = autoSend;
        this.dtpTwinClientService = dtpTwinClientService;
        this.throwException = throwException;
    }

    @Override
    public ObjectCommandSendResult call() throws Exception {
        try {
            log.info("指令下发开始入参：groupCode: {}, projectId: {}, autoSend：{}, objectId: {}, infoCode: {}, infoValue: {}", groupCode, projectId, autoSend, objectId, infoCode, infoValue);
            if (autoSend) {
                this.dtpTwinClientService.autoSend(new DtpTwinAutoSendDTO(objectId, infoCode, infoValue));
            } else {
                this.dtpTwinClientService.manualSend(DtpTwinManualSend.builder().objectId(objectId).infoCode(infoCode).value(infoValue).build());
            }
        } catch (Exception e) {
            if (throwException) {
                throw e;
            }
            log.error("指令下发失败，对象ID：{}", objectId, e);
            return new ObjectCommandSendResult(false, objectId, infoCode, e.getMessage());
        }
        return new ObjectCommandSendResult(true, objectId, infoCode, null);
    }

    public static List<ObjectCommandSendResult> joinAll(List<Future<ObjectCommandSendResult>> futureList) {
        List<ObjectCommandSendResult> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(futureList)) {
            return result;
        }
        try {
            for (Future<ObjectCommandSendResult> future : futureList) {
                ObjectCommandSendResult commandSendResult = future == null ? null : future.get();
                if (commandSendResult != null) {
                    result.add(commandSendResult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
